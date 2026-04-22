package com.example.readspread.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import data.local.domain.repository.BookRepository
import data.local.entity.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: Long = checkNotNull(savedStateHandle["bookId"])

    private val _uiState = MutableStateFlow(BookDetailsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadBook()
    }

    private fun loadBook() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val book = bookRepository.getBookByIdSync(bookId)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    book = book,
                    error = if (book == null) "Книга не найдена" else null
                )
            }
        }
    }

    fun toggleFavorite() {
        val currentBook = _uiState.value.book ?: return
        val newFavorite = !currentBook.isFavorite
        viewModelScope.launch {
            bookRepository.updateFavorite(currentBook.id, newFavorite)
            _uiState.update { state ->
                state.copy(
                    book = state.book?.copy(isFavorite = newFavorite)
                )
            }
        }
    }

    fun deleteBook(onSuccess: () -> Unit) {
        val book = _uiState.value.book ?: return
        viewModelScope.launch {
            bookRepository.deleteBook(book)
            onSuccess()
        }
    }

    data class BookDetailsUiState(
        val isLoading: Boolean = false,
        val book: Book? = null,
        val error: String? = null
    )
}