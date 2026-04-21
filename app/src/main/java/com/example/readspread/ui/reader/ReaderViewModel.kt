package com.example.readspread.ui.reader

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import data.local.domain.repository.BookRepository
import data.local.entity.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val book: Book, val content: String) : UiState()   // ← Добавлено поле content
        data class Error(val message: String) : UiState()
    }

    private val bookIdFlow = MutableStateFlow(0L)

    val uiState: StateFlow<UiState> = bookIdFlow
        .flatMapLatest { id ->
            Log.d("READER_VM", "Запрашиваем книгу с ID = $id")
            repository.getBookById(id)
        }
        .map { book ->
            if (book != null) {
                Log.d("READER_VM", "Книга найдена: ${book.title}")
                // ↓↓↓ ВРЕМЕННАЯ ЗАГЛУШКА ТЕКСТА ↓↓↓
                val content = "Это тестовый текст книги. Здесь будет отображаться содержимое файла."
                UiState.Success(book, content)   // ← Передаём content в Success
            } else {
                Log.d("READER_VM", "Книга НЕ найдена (ID = ${bookIdFlow.value})")
                UiState.Error("Книга с ID = ${bookIdFlow.value} не найдена в базе")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun setBookId(id: Long) {
        Log.d("READER_VM", "setBookId вызван: $id")
        bookIdFlow.value = id
    }
}