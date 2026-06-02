package com.example.readspread.ui.reader

import android.app.Application
import android.util.Log
import android.util.Xml
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import data.local.domain.repository.BookRepository
import data.local.entity.Book
import data.local.entity.BookFormat
import data.local.entity.BookStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    private val application: Application
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val book: Book, val content: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val bookIdFlow = MutableStateFlow(0L)

    val uiState: StateFlow<UiState> = bookIdFlow
        .flatMapLatest { id ->
            repository.getBookById(id)
        }
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

    fun setBookId(id: Long) {
        bookIdFlow.value = id
    }

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
        // … (identical to your current version, no change needed)
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
                        val plainText = htmlToPlainText(html)   // <-- improved method
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

    // ---------- IMPROVED HTML TO PLAIN TEXT ----------
    private fun htmlToPlainText(html: String): String {
        return html
            // Replace block elements with a single newline
            .replace(Regex("</?p[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<br[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</?div[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</?h[1-6][^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</?li[^>]*>", RegexOption.IGNORE_CASE), "\n")
            // Remove head, scripts, styles
            .replace(Regex("<head[^>]*>.*?</head>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
            // Strip all remaining tags (including <img>, <figure>, etc.)
            .replace(Regex("<[^>]+>"), " ")
            // Decode entities
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            // Collapse whitespace – keep only single newlines, no blank lines
            .replace(Regex("[ \t]+"), " ")
            .replace(Regex("\n{2,}"), "\n")
            .trim()
    }

    // --- Everything else unchanged (getRootfilePath, parseManifest, etc.) ---
    private fun getRootfilePath(zip: ZipFile): String? { /* ... same ... */
        val entry = zip.getEntry("META-INF/container.xml") ?: return null
        val xml = String(zip.getInputStream(entry).readBytes())
        return parseXmlForAttribute(xml, "rootfile", "full-path")
    }

    private fun parseManifest(opfXml: String): Map<String, String> { /* ... same ... */
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

    private fun parseSpineOrder(opfXml: String): List<String> { /* ... same ... */
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

    private fun parseXmlForAttribute(xml: String, tag: String, attribute: String): String? { /* ... same ... */
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

    // ---------- STUB ----------
    private fun createStubBook(id: Long): Book {
        val testEpubPath = File(application.getExternalFilesDir(null), "test.epub").absolutePath
        return Book(
            id = id.takeIf { it > 0 } ?: 1L,
            title = "Test EPUB Book",
            author = "Unknown",
            filePath = testEpubPath,
            format = BookFormat.EPUB,
            totalPages = 0,
            currentPage = 1,
            progress = 0f,
            status = "NOT_STARTED",
            addedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}