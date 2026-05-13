package com.example.readspread.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import data.local.domain.repository.BookRepository
import data.local.entity.Book
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class BookFilter(val label: String) {
    ALL("Все книги"),
    FAVORITES("Избранное"),
    READING("Читаю сейчас")
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookRepository: BookRepository
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
                    if (query.isBlank()) {
                        bookRepository.getAllBooks()
                    } else {
                        bookRepository.searchBooks(query)
                    }
                }
                BookFilter.FAVORITES -> {
                    // Для избранного поиск по названию/автору тоже можно реализовать
                    // Пока упрощаем: если запрос пустой — все избранные, иначе ищем по избранным?
                    // В репозитории есть searchBooks, он ищет по всем книгам, но нет метода searchFavoriteBooks.
                    // Мы можем отфильтровать на уровне UI или добавить метод в репозиторий.
                    // Для простоты: вернём все избранные, игнорируя запрос, или добавим в репозиторий метод.
                    // Здесь используем getFavoriteBooks и потом фильтруем локально по запросу.
                    // Чтобы не усложнять, сделаем фильтрацию локально.
                    bookRepository.getFavoriteBooks()
                        .flatMapLatest { favList ->
                            val filtered = if (query.isBlank()) favList
                            else favList.filter { book ->
                                book.title.contains(query, ignoreCase = true) ||
                                        book.author.contains(query, ignoreCase = true)
                            }
                            kotlinx.coroutines.flow.flowOf(filtered)
                        }
                }
                BookFilter.READING -> {
                    // Аналогично "Читаю сейчас"
                    bookRepository.getReadingBooks()
                        .flatMapLatest { readingList ->
                            val filtered = if (query.isBlank()) readingList
                            else readingList.filter { book ->
                                book.title.contains(query, ignoreCase = true) ||
                                        book.author.contains(query, ignoreCase = true)
                            }
                            kotlinx.coroutines.flow.flowOf(filtered)
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
}