//package com.example.readspread.ui.reader
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.example.readspread.ui.reader.ReaderViewModel.UiState
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ChevronLeft
//import androidx.compose.material.icons.filled.Error
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ReaderScreen(
//    bookId: Int,
//    onBackClick: () -> Unit,           // ← здесь была пропущена запятая
//    viewModel: ReaderViewModel = hiltViewModel()
//) {
//    // Передаём ID книги в ViewModel
//    LaunchedEffect(bookId) {
//        viewModel.setBookId(bookId.toLong())
//    }
//
//    val uiState by viewModel.uiState.collectAsState()
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        "Чтение",
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Medium
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(
//                            imageVector = Icons.Filled.ChevronLeft,
//                            contentDescription = "Назад"
//                        )
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            when (val state = uiState) {
//                is UiState.Loading -> {
//                    CircularProgressIndicator(
//                        modifier = Modifier.align(Alignment.Center)
//                    )
//                }
//
//                is UiState.Success -> {
//                    val book = state.book
//                    Column(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .verticalScroll(rememberScrollState())
//                            .padding(16.dp)
//                    ) {
//                        Text(
//                            text = book.title,
//                            fontSize = 24.sp,
//                            fontWeight = FontWeight.Bold,
//                            modifier = Modifier.padding(bottom = 8.dp)
//                        )
//
//                        Text(
//                            text = book.author,
//                            fontSize = 16.sp,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant,
//                            modifier = Modifier.padding(bottom = 24.dp)
//                        )
//
//                        if (!book.description.isNullOrBlank()) {
//                            Text(
//                                text = "Описание:",
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                modifier = Modifier.padding(bottom = 8.dp)
//                            )
//                            Text(
//                                text = book.description,
//                                fontSize = 14.sp,
//                                lineHeight = 20.sp,
//                                modifier = Modifier.padding(bottom = 24.dp)
//                            )
//                        }
//
//                        Card(
//                            modifier = Modifier.fillMaxWidth(),
//                            colors = CardDefaults.cardColors(
//                                containerColor = MaterialTheme.colorScheme.surfaceVariant
//                            )
//                        ) {
//                            Column(modifier = Modifier.padding(16.dp)) {
//                                InfoRow("Формат", book.format)
//                                Divider(modifier = Modifier.padding(vertical = 8.dp))
//                                InfoRow("Страниц", book.totalPages.toString())
//                                Divider(modifier = Modifier.padding(vertical = 8.dp))
//                                InfoRow("Прогресс", "${book.progress.toInt()}%")
//
//                                if (book.currentPage > 1) {
//                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
//                                    InfoRow("Текущая страница", book.currentPage.toString())
//                                }
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(24.dp))
//
//                        Button(
//                            onClick = { /* можно добавить логику */ },
//                            modifier = Modifier.fillMaxWidth(),
//                            enabled = book.progress < 100f
//                        ) {
//                            Text("Отметить как прочитанное")
//                        }
//                    }
//                }
//
//                is UiState.Error -> {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(16.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Center
//                    ) {
//                        Icon(
//                            imageVector = Icons.Filled.Error,
//                            contentDescription = "Ошибка",
//                            tint = MaterialTheme.colorScheme.error,
//                            modifier = Modifier.size(64.dp)
//                        )
//                        Spacer(modifier = Modifier.height(16.dp))
//                        Text(
//                            text = state.message,
//                            color = MaterialTheme.colorScheme.error,
//                            fontSize = 16.sp
//                        )
//                        Spacer(modifier = Modifier.height(16.dp))
//                        Button(onClick = onBackClick) {
//                            Text("Вернуться назад")
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun InfoRow(label: String, value: String) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Text(
//            text = label,
//            fontSize = 14.sp,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//        Text(
//            text = value,
//            fontSize = 14.sp,
//            fontWeight = FontWeight.Medium
//        )
//    }
//}