package com.example.readspread.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readspread.data.local.ThemeDataStore
import com.example.readspread.data.local.ThemeMode
import com.example.readspread.ui.components.BookCard
import data.local.entity.Book
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBookClick: (Book) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
    themeDataStore: ThemeDataStore
) {
    val books by viewModel.books.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val currentTheme by themeDataStore.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

    var filterMenuExpanded by remember { mutableStateOf(false) }

    var textFieldValue by remember { mutableStateOf(TextFieldValue(searchQuery)) }

    LaunchedEffect(Unit) {
        if (searchQuery.isNotEmpty()) {
            textFieldValue = TextFieldValue(
                text = searchQuery,
                selection = androidx.compose.ui.text.TextRange(searchQuery.length)
            )
        }
    }

    val importBookLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBook(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Моя библиотека") },
                actions = {
                    ThemeSwitchButton(
                        themeDataStore = themeDataStore,
                        currentTheme = currentTheme
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                importBookLauncher.launch(arrayOf("*/*"))
            }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить книгу")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
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
fun ThemeSwitchButton(
    themeDataStore: ThemeDataStore,
    currentTheme: ThemeMode
) {
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val selectedMode = if (currentTheme == ThemeMode.SYSTEM) ThemeMode.LIGHT else currentTheme

    IconButton(onClick = { showDialog = true }) {
        Icon(Icons.Default.DarkMode, contentDescription = "Выбор темы")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Тема оформления") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedMode == ThemeMode.LIGHT,
                            onClick = {
                                scope.launch { themeDataStore.setThemeMode(ThemeMode.LIGHT) }
                                showDialog = false
                            }
                        )
                        Text("Светлая", modifier = Modifier.clickable {
                            scope.launch { themeDataStore.setThemeMode(ThemeMode.LIGHT) }
                            showDialog = false
                        })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedMode == ThemeMode.DARK,
                            onClick = {
                                scope.launch { themeDataStore.setThemeMode(ThemeMode.DARK) }
                                showDialog = false
                            }
                        )
                        Text("Тёмная", modifier = Modifier.clickable {
                            scope.launch { themeDataStore.setThemeMode(ThemeMode.DARK) }
                            showDialog = false
                        })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedMode == ThemeMode.SEPIA,
                            onClick = {
                                scope.launch { themeDataStore.setThemeMode(ThemeMode.SEPIA) }
                                showDialog = false
                            }
                        )
                        Text("Сепия", modifier = Modifier.clickable {
                            scope.launch { themeDataStore.setThemeMode(ThemeMode.SEPIA) }
                            showDialog = false
                        })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Отмена")
                }
            }
        )
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