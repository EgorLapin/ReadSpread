package com.example.readspread.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import data.local.domain.repository.BookRepository
import data.local.entity.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: Long = checkNotNull(savedStateHandle["bookId"])

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadBook()
    }

    private fun loadBook() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val book = bookRepository.getBookByIdSync(bookId)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                book = book,
                error = if (book == null) "Книга не найдена" else null
            )
        }
    }

    fun updateCoverPath(newPath: String) {
        viewModelScope.launch {
            _uiState.value.book?.let { book ->
                val updatedBook = book.copy(coverPath = newPath)
                bookRepository.updateBook(updatedBook)
                _uiState.value = _uiState.value.copy(book = updatedBook)
            }
        }
    }

    fun toggleFavorite() {
        val book = _uiState.value.book ?: return
        val newFavorite = !book.isFavorite
        viewModelScope.launch {
            bookRepository.updateFavorite(book.id, newFavorite)
            _uiState.value = _uiState.value.copy(
                book = book.copy(isFavorite = newFavorite)
            )
        }
    }

    fun deleteBook(onDeleted: () -> Unit) {
        val book = _uiState.value.book ?: return
        viewModelScope.launch {
            bookRepository.deleteBook(book)
            onDeleted()
        }
    }

    data class UiState(
        val isLoading: Boolean = false,
        val book: Book? = null,
        val error: String? = null
    )
}