package com.example.readspread.ui.details

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.readspread.ui.reader.BookActivity
import com.example.readspread.ui.components.BookCoverPlaceholder
import data.local.entity.Book
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import android.content.Context
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    onBackClick: () -> Unit,
    viewModel: BookDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали книги") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                uiState.book != null -> {
                    BookDetailsContent(
                        book = uiState.book!!,
                        onStartReading = {
                            val intent = Intent(context, BookActivity::class.java).apply {
                                putExtra("BOOK_ID", uiState.book!!.id)
                            }
                            context.startActivity(intent)
                        },
                        onToggleFavorite = viewModel::toggleFavorite,
                        onDelete = { viewModel.deleteBook { onBackClick() } },
                        onCoverChange = { newPath -> viewModel.updateCoverPath(newPath) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookDetailsContent(
    book: Book,
    onStartReading: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onCoverChange: (String) -> Unit
) {
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = saveCoverToInternalStorage(context, it, book.id)
            savedPath?.let { path -> onCoverChange(path) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { imagePickerLauncher.launch("image/*") }
        ) {
            val imageUrl = book.coverPath
            var isLoadingError by remember { mutableStateOf(false) }

            if (!imageUrl.isNullOrEmpty() && !isLoadingError) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Обложка книги",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onError = { isLoadingError = true }
                )
            } else {
                BookCoverPlaceholder(
                    title = book.title,
                    author = book.author,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = book.title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            text = book.author,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (!book.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Описание", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(book.description, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))
        }

        if (!book.publishedDate.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow("Год издания", book.publishedDate)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onStartReading, modifier = Modifier.fillMaxWidth()) {
            Text("Начать читать")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onToggleFavorite, modifier = Modifier.weight(1f)) {
                Icon(
                    if (book.isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (book.isFavorite) "В избранном" else "В избранное")
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Удалить")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

fun saveCoverToInternalStorage(context: Context, sourceUri: Uri, bookId: Long): String? {
    return try {
        val coverDir = java.io.File(context.filesDir, "covers")
        if (!coverDir.exists()) coverDir.mkdirs()
        val coverFile = java.io.File(coverDir, "cover_${bookId}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            java.io.FileOutputStream(coverFile).use { output ->
                input.copyTo(output)
            }
        }
        coverFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}