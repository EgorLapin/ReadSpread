package com.example.readspread.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readspread.ui.components.BookCard
import data.local.entity.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBookClick: (Book) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val books by viewModel.books.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    var filterMenuExpanded by remember { mutableStateOf(false) }

    // Локальное состояние поля ввода — TextFieldValue для поддержки композиции
    var textFieldValue by remember { mutableStateOf(TextFieldValue(searchQuery)) }

    // Однократная начальная синхронизация при старте экрана (если ViewModel вернул непустой запрос)
    LaunchedEffect(Unit) {
        if (searchQuery.isNotEmpty()) {
            textFieldValue = TextFieldValue(
                text = searchQuery,
                selection = androidx.compose.ui.text.TextRange(searchQuery.length)
            )
        }
    }

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
            // Поисковая строка
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    // Сразу отправляем текст в ViewModel без задержки
                    viewModel.updateSearchQuery(newValue.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Поиск по названию или автору") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (textFieldValue.text.isNotEmpty()) {
                        IconButton(onClick = {
                            textFieldValue = TextFieldValue("")
                            viewModel.updateSearchQuery("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить поиск")
                        }
                    }
                },
                singleLine = true
            )

            // Фильтр
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Фильтр: ${selectedFilter.label}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                Box {
                    TextButton(onClick = { filterMenuExpanded = true }) {
                        Text("Сменить")
                    }
                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false }
                    ) {
                        BookFilter.values().forEach { filter ->
                            DropdownMenuItem(
                                text = { Text(filter.label) },
                                onClick = {
                                    viewModel.updateFilter(filter)
                                    filterMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Список книг
            if (books.isEmpty()) {
                EmptyLibrary()
            } else {
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

@Composable
private fun EmptyLibrary() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📚", style = MaterialTheme.typography.displayLarge)
        Text(
            text = "Книги не найдены",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "Попробуйте изменить запрос или фильтр",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}