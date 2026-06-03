package com.example.readspread.ui.library

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import data.local.domain.repository.BookRepository
import data.local.entity.Book
import data.local.entity.BookStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

enum class BookFilter(val label: String) {
    ALL("Все книги"),
    FAVORITES("Избранное"),
    READING("Читаю сейчас")
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val application: Application   // <-- добавлен Application
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow(BookFilter.ALL)
    val selectedFilter: StateFlow<BookFilter> = _selectedFilter

    @OptIn(ExperimentalCoroutinesApi::class)
    val books: StateFlow<List<Book>> = combine(
        _searchQuery, _selectedFilter
    ) { query, filter -> query to filter }
        .flatMapLatest { (query, filter) ->
            when (filter) {
                BookFilter.ALL -> {
                    if (query.isBlank()) bookRepository.getAllBooks()
                    else bookRepository.searchBooks(query)
                }
                BookFilter.FAVORITES -> {
                    bookRepository.getFavoriteBooks()
                        .flatMapLatest { favList ->
                            kotlinx.coroutines.flow.flowOf(
                                if (query.isBlank()) favList
                                else favList.filter { book ->
                                    book.title.contains(query, ignoreCase = true) ||
                                            book.author.contains(query, ignoreCase = true) ||
                                            (book.customTitle?.contains(query, ignoreCase = true) == true)
                                }
                            )
                        }
                }
                BookFilter.READING -> {
                    bookRepository.getReadingBooks()
                        .flatMapLatest { readingList ->
                            kotlinx.coroutines.flow.flowOf(
                                if (query.isBlank()) readingList
                                else readingList.filter { book ->
                                    book.title.contains(query, ignoreCase = true) ||
                                            book.author.contains(query, ignoreCase = true) ||
                                            (book.customTitle?.contains(query, ignoreCase = true) == true)
                                }
                            )
                        }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: BookFilter) {
        _selectedFilter.value = filter
    }

    /**
     * Импорт книги из URI, полученного через SAF.
     */
    fun importBook(uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val contentResolver = application.contentResolver
                val fileName = getFileName(contentResolver, uri) ?: "unknown_book"
                val extension = fileName.substringAfterLast('.', "").lowercase()

                // Копируем файл во внутреннее хранилище
                val inputStream = contentResolver.openInputStream(uri) ?: return@withContext
                val booksDir = File(application.filesDir, "books")
                if (!booksDir.exists()) booksDir.mkdirs()
                val destFile = File(booksDir, fileName)
                FileOutputStream(destFile).use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()

                val book = Book(
                    title = fileName.substringBeforeLast('.'),
                    author = "Неизвестный автор",
                    filePath = destFile.absolutePath,
                    format = extension.uppercase(),
                    totalPages = 0,   // будет вычислено при открытии
                    currentPage = 1,
                    progress = 0f,
                    status = BookStatus.NOT_STARTED,
                    addedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                bookRepository.insertBook(book)
                bookRepository.deleteTestBooks()
            }
        }
    }

    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) return it.getString(nameIndex)
            }
        }
        return uri.lastPathSegment
    }
}