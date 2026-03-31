package com.example.readspread.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.readspread.domain.model.Book
import com.example.readspread.ui.components.BookCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.unit.dp
// Временные данные для теста
private val sampleBooks = listOf(
    Book(1, "Война и мир", "Л.Н. Толстой", totalPages = 1225, currentPage = 450),
    Book(2, "Преступление и наказание", "Ф.М. Достоевский", totalPages = 672, currentPage = 100),
    Book(3, "1984", "Дж. Оруэлл", totalPages = 328, currentPage = 328, isFavorite = true),
    Book(4, "Мастер и Маргарита", "М.А. Булгаков", totalPages = 480, currentPage = 0),
    Book(5, "Гарри Поттер и философский камень", "Дж.К. Роулинг", totalPages = 432, currentPage = 200)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    books: List<Book> = sampleBooks,
    onBookClick: (Book) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Моя библиотека") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (books.isEmpty()) {
                // Экран пустой библиотеки
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📚",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = "Библиотека пуста",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = "Добавьте книги для начала чтения",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Список книг
                LazyColumn {
                    items(books, key = { it.id }) { book ->
                        BookCard(
                            book = book,
                            onClick = { onBookClick(book) }
                        )
                    }
                }
            }
        }
    }
}