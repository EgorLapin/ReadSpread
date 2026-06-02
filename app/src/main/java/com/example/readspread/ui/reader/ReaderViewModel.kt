package com.example.readspread.ui.reader

import android.app.Application
import android.util.Log
import android.util.Xml
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import data.local.domain.repository.BookRepository
import data.local.domain.repository.BookmarkRepository
import data.local.domain.repository.ReadingProgressRepository
import data.local.entity.Book
import data.local.entity.BookFormat
import data.local.entity.Bookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.StringReader
import java.util.zip.ZipFile
import javax.inject.Inject
import kotlin.text.RegexOption

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repository: BookRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val readingProgressRepository: ReadingProgressRepository,
    private val application: Application
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val book: Book, val content: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val bookIdFlow = MutableStateFlow(0L)

    val uiState: StateFlow<UiState> = bookIdFlow
        .flatMapLatest { id -> repository.getBookById(id) }
        .map { book ->
            if (book != null) {
                val content = loadBookContent(book)
                UiState.Success(book, content)
            } else {
                UiState.Error("Книга не найдена")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    val bookmarks: StateFlow<List<Bookmark>> = bookIdFlow
        .flatMapLatest { id -> bookmarkRepository.getBookmarksForBook(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setBookId(id: Long) {
        bookIdFlow.value = id
    }

    fun updatePageAndProgress(page: Int, totalPages: Int) {
        viewModelScope.launch {
            val bookId = bookIdFlow.value
            if (bookId > 0) {
                // Update the ReadingProgress table
                readingProgressRepository.updatePageAndProgress(bookId, page, totalPages)
                // Also update the Book entity so currentPage is available on next launch
                repository.updatePageAndProgress(bookId, page, totalPages)
            }
        }
    }

    fun updateFontSize(fontSize: Int) {
        viewModelScope.launch {
            val bookId = bookIdFlow.value
            if (bookId > 0) {
                readingProgressRepository.updateFontSize(bookId, fontSize)
                // Also update the Book entity so fontSize is available on next launch
                repository.updateFontSize(bookId, fontSize)
            }
        }
    }

    fun addBookmark(offset: Int, pageNumber: Int, textPreview: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val bookId = bookIdFlow.value
            val bookmark = Bookmark(
                bookId = bookId,
                pageNumber = pageNumber,
                position = offset.toString(),
                textPreview = textPreview
            )
            bookmarkRepository.addBookmark(bookmark)
        }
    }

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            bookmarkRepository.deleteBookmarkById(bookmarkId)
        }
    }
    fun updateTotalPages(totalPages: Int) {
        viewModelScope.launch {
            val bookId = bookIdFlow.value
            if (bookId > 0) {
                repository.updateTotalPages(bookId, totalPages)
            }
        }
    }
    // --- Content loading methods (unchanged) ---
    private suspend fun loadBookContent(book: Book): String = withContext(Dispatchers.IO) {
        if (book.filePath.startsWith("test_")) {
            return@withContext getTestBookContent(book.title)
        }
        try {
            val file = File(book.filePath)
            if (!file.exists()) return@withContext "Error: File not found at ${book.filePath}"
            when (book.format.uppercase()) {
                BookFormat.EPUB -> readEpubText(file)
                BookFormat.TXT -> file.readText()
                else -> "Unsupported format: ${book.format}"
            }
        } catch (e: Exception) {
            Log.e("READER_VM", "Error loading book content", e)
            "Error loading content: ${e.localizedMessage}"
        }
    }

    private fun getTestBookContent(title: String): String {
        return when (title) {
            "Война и мир" -> "Глава 1\n\nВ 1805 году, в самый разгар наполеоновских войн, в Петербурге..."
            "Преступление и наказание" -> "В начале июля, в чрезвычайно жаркое время, под вечер..."
            "1984" -> "Был холодный ясный апрельский день, и часы пробили тринадцать..."
            "Мастер и Маргарита" -> "Однажды весною, в час небывало жаркого заката, в Москве..."
            "Гарри Поттер и философский камень" -> "Мистер и миссис Дурсль, проживавшие в доме номер четыре..."
            else -> "Текст книги временно недоступен для предпросмотра."
        }
    }

    private fun readEpubText(file: File): String {
        return try {
            ZipFile(file).use { zip ->
                val rootfilePath = getRootfilePath(zip)
                    ?: return "Invalid EPUB: container.xml missing or unreadable"
                val opfEntry = zip.getEntry(rootfilePath)
                    ?: return "OPF entry not found: $rootfilePath"
                val opfContent = String(zip.getInputStream(opfEntry).readBytes())
                val opfDir = File(rootfilePath).parent?.let { "$it/" } ?: ""

                val manifestItems = parseManifest(opfContent)
                val spineIds = parseSpineOrder(opfContent)

                val contentPaths = spineIds.mapNotNull { id ->
                    manifestItems[id]?.let { href -> resolveHref(href, opfDir) }
                }

                val textBuilder = StringBuilder()
                for (path in contentPaths) {
                    val entry = zip.getEntry(path)
                    if (entry != null) {
                        val html = String(zip.getInputStream(entry).readBytes(), Charsets.UTF_8)
                        val plainText = htmlToPlainText(html)
                        if (plainText.isNotBlank()) {
                            textBuilder.append(plainText.trim())
                            textBuilder.append("\n\n")
                        }
                    }
                }
                textBuilder.toString().ifEmpty {
                    "No readable text found in EPUB."
                }
            }
        } catch (e: Exception) {
            Log.e("READER_VM", "EPUB parsing error", e)
            "Error reading EPUB: ${e.message}"
        }
    }

    private fun htmlToPlainText(html: String): String {
        return html
            .replace(Regex("</?p[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<br[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</?div[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</?h[1-6][^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</?li[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<head[^>]*>.*?</head>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace(Regex("[ \t]+"), " ")
            .replace(Regex("\n{2,}"), "\n")
            .trim()
    }

    // --- XML parsing helpers (unchanged) ---
    private fun getRootfilePath(zip: ZipFile): String? {
        val entry = zip.getEntry("META-INF/container.xml") ?: return null
        val xml = String(zip.getInputStream(entry).readBytes())
        return parseXmlForAttribute(xml, "rootfile", "full-path")
    }

    private fun parseManifest(opfXml: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        var inManifest = false
        val parser = Xml.newPullParser()
        parser.setInput(StringReader(opfXml))
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "manifest") inManifest = true
                    if (inManifest && parser.name == "item") {
                        val id = parser.getAttributeValue(null, "id")
                        val href = parser.getAttributeValue(null, "href")
                        if (id != null && href != null) map[id] = href
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "manifest") inManifest = false
                }
            }
            eventType = parser.next()
        }
        return map
    }

    private fun parseSpineOrder(opfXml: String): List<String> {
        val ids = mutableListOf<String>()
        var inSpine = false
        val parser = Xml.newPullParser()
        parser.setInput(StringReader(opfXml))
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "spine") inSpine = true
                    if (inSpine && parser.name == "itemref") {
                        val idref = parser.getAttributeValue(null, "idref")
                        if (idref != null) ids.add(idref)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "spine") inSpine = false
                }
            }
            eventType = parser.next()
        }
        return ids
    }

    private fun resolveHref(href: String, opfDir: String): String {
        val cleanHref = href.substringBefore("#")
        return when {
            cleanHref.startsWith("/") -> cleanHref.removePrefix("/")
            else -> opfDir + cleanHref
        }
    }

    private fun parseXmlForAttribute(xml: String, tag: String, attribute: String): String? {
        val parser = Xml.newPullParser()
        parser.setInput(StringReader(xml))
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == tag) {
                return parser.getAttributeValue(null, attribute)
            }
            eventType = parser.next()
        }
        return null
    }


}