package com.example.readspread.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readspread.ui.reader.ReaderViewModel
import com.example.readspread.ui.reader.ReaderViewModel.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookActivity : ComponentActivity() {

    private val viewModel: ReaderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val bookId = intent.getLongExtra("BOOK_ID", 0L)
        viewModel.setBookId(bookId)

        setContent {
            BookReaderScreen(viewModel)
        }
    }
}

@Composable
fun BookReaderScreen(viewModel: ReaderViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${state.message}",
                    color = Color.Red,
                    fontSize = 16.sp
                )
            }
        }

        is UiState.Success -> {
            BookContent(
                book = state.book,
                fullContent = state.content,
                onPageChanged = { newPage ->
                    // TODO: Call repository to update current page in database
                    // repository.updateBookCurrentPage(state.book.id, newPage)
                },
                onFontSizeChanged = { newSize ->
                    // TODO: Save font size preference for this book or globally
                    // repository.updateBookFontSize(state.book.id, newSize)
                }
            )
        }
    }
}

@Composable
fun BookContent(
    book: data.local.entity.Book,
    fullContent: String,
    onPageChanged: (Int) -> Unit,
    onFontSizeChanged: (Int) -> Unit
) {
    // Light blue color definition
    val lightBlue = Color(0xFFADD8E6)  // Light blue
    val lightBlueTransparent = lightBlue.copy(alpha = 0.9f)

    // Split content into pages (simple paragraph-based pagination)
    val pages = remember(fullContent) {
        if (fullContent.isBlank()) {
            listOf("No content available")
        } else {
            fullContent.split("\n\n").filter { it.isNotBlank() }
        }
    }

    var pageNumber by remember { mutableIntStateOf(book.currentPage - 1) }
    var fontSize by remember { mutableIntStateOf(20) }
    var selectedFont by remember { mutableStateOf(FontFamily.Default) }
    var fontSizeMenuExpanded by remember { mutableStateOf(false) }

    // Font size options
    val fontSizeOptions = listOf(12, 14, 16, 18, 20, 22, 24, 28, 32)

    val currentPageContent = if (pages.isNotEmpty() && pageNumber in pages.indices) {
        pages[pageNumber]
    } else {
        "No content"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Book title with light blue background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(lightBlue)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = book.title,
                    fontSize = 24.sp,
                    color = Color.White
                )
            }

            // Main content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = currentPageContent,
                    fontSize = fontSize.sp,
                    style = TextStyle(fontFamily = selectedFont),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Bottom area with light blue background containing font dropdown and page counter
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(lightBlue)
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            // Font size dropdown (centered)
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$fontSize sp",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { fontSizeMenuExpanded = true }
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Font Size",
                    tint = Color.White,
                    modifier = Modifier.clickable { fontSizeMenuExpanded = true }
                )

                DropdownMenu(
                    expanded = fontSizeMenuExpanded,
                    onDismissRequest = { fontSizeMenuExpanded = false }
                ) {
                    fontSizeOptions.forEach { size ->
                        DropdownMenuItem(
                            text = { Text("$size sp") },
                            onClick = {
                                fontSize = size
                                onFontSizeChanged(size)
                                fontSizeMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Page counter (centered below font dropdown or integrated)
            if (pages.isNotEmpty()) {
                Text(
                    text = "${pageNumber + 1}/${pages.size}",
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(top = 40.dp)  // Position below font dropdown
                )
            }
        }

        // Previous page button (bottom-left corner)
        IconButton(
            onClick = {
                if (pageNumber > 0) {
                    pageNumber--
                    onPageChanged(pageNumber + 1)
                }
            },
            enabled = pageNumber > 0,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
                .size(56.dp)
                .background(lightBlueTransparent, shape = androidx.compose.foundation.shape.CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Previous Page",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // Next page button (bottom-right corner)
        IconButton(
            onClick = {
                if (pageNumber < pages.size - 1) {
                    pageNumber++
                    onPageChanged(pageNumber + 1)
                }
            },
            enabled = pageNumber < pages.size - 1,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .size(56.dp)
                .background(lightBlueTransparent, shape = androidx.compose.foundation.shape.CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Next Page",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}