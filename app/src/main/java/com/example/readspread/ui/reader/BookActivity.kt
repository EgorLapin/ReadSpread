package com.example.readspread.ui
import androidx.compose.ui.platform.LocalDensity

import androidx.compose.ui.layout.onSizeChanged
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.message}", color = Color.Red, fontSize = 16.sp)
            }
        }
        is UiState.Success -> {
            BookContent(
                book = state.book,
                fullContent = state.content,
                onPageChanged = { newPage, totalPages ->
                    // TODO: repository.updateBookPageAndProgress(...)
                },
                onFontSizeChanged = { newSize ->
                    // TODO: repository.updateFontSize(...)
                }
            )
        }
    }
}

@Composable
fun BookContent(
    book: data.local.entity.Book,
    fullContent: String,
    onPageChanged: (page: Int, totalPages: Int) -> Unit,
    onFontSizeChanged: (fontSize: Int) -> Unit
) {
    val lightBlue = Color(0xFFADD8E6)
    var fontSize by remember { mutableIntStateOf(20) }
    var selectedFont by remember { mutableStateOf(FontFamily.Default) }
    var fontSizeMenuExpanded by remember { mutableStateOf(false) }
    val fontSizeOptions = listOf(12, 14, 16, 18, 20, 22, 24, 28, 32)

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val textStyle = TextStyle(
        fontSize = fontSize.sp,
        fontFamily = selectedFont,
        lineHeight = (fontSize * 1.4).sp
    )

    val annotatedContent = remember(fullContent) {
        AnnotatedString(fullContent)
    }

    var contentSize by remember { mutableStateOf(IntSize.Zero) }
    var currentPage by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(1) }

    // Character offset of the first character on the **currently visible page**
    // This is updated every time the user flips a page, so it always reflects
    // where the user was last reading.
    var lastReadingOffset by remember { mutableIntStateOf(0) }

    val horizontalPadding = 16.dp
    val verticalPadding = 8.dp

    /* ---- Reliable pagination ---- */
    val pages = remember(annotatedContent, textStyle, contentSize) {
        if (contentSize.width <= 0 || contentSize.height <= 0) {
            listOf(AnnotatedString(""))
        } else {
            val availableWidthPx = with(density) { contentSize.width - horizontalPadding.toPx() * 2 }
            val availableHeightPx = with(density) { contentSize.height - verticalPadding.toPx() * 2 }

            val fullLayout = textMeasurer.measure(
                text = annotatedContent,
                style = textStyle,
                maxLines = Int.MAX_VALUE,
                constraints = Constraints(maxWidth = availableWidthPx.toInt())
            )

            val totalLines = fullLayout.lineCount
            if (totalLines == 0) return@remember listOf(AnnotatedString(""))

            data class Line(val start: Int, val end: Int, val bottom: Float)
            val lines = (0 until totalLines).map { i ->
                Line(
                    start = fullLayout.getLineStart(i),
                    end = fullLayout.getLineEnd(i),
                    bottom = fullLayout.getLineBottom(i)
                )
            }

            val pageList = mutableListOf<AnnotatedString>()
            var pageTopY = 0f
            var lineIndex = 0

            while (lineIndex < lines.size) {
                val firstLineOfPage = lines[lineIndex]
                val pageStartChar = firstLineOfPage.start
                var lastFits = lineIndex

                while (lastFits < lines.size) {
                    val l = lines[lastFits]
                    val heightFromTop = l.bottom - pageTopY
                    if (heightFromTop <= availableHeightPx) {
                        lastFits++
                    } else {
                        break
                    }
                }

                val lastFittingIndex = lastFits - 1
                if (lastFittingIndex < lineIndex) {
                    // Force at least one line (shouldn't happen)
                    val forcedLine = lines[lineIndex]
                    pageList.add(annotatedContent.subSequence(pageStartChar, forcedLine.end))
                    pageTopY = forcedLine.bottom
                    lineIndex++
                } else {
                    val lastLine = lines[lastFittingIndex]
                    pageList.add(annotatedContent.subSequence(pageStartChar, lastLine.end))
                    pageTopY = lastLine.bottom
                    lineIndex = lastFittingIndex + 1
                }
            }

            pageList.ifEmpty { listOf(AnnotatedString("")) }
        }
    }

    // Pre‑calculate the character offset at which each page starts.
    val pageOffsets = remember(pages) {
        val offsets = mutableListOf<Int>()
        var offset = 0
        for (page in pages) {
            offsets.add(offset)
            offset += page.text.length
        }
        offsets
    }

    totalPages = pages.size.coerceAtLeast(1)

    // ---- Preserve reading position when pages change (font size, orientation, etc.) ----
    LaunchedEffect(pages) {
        if (pages.isNotEmpty() && pageOffsets.isNotEmpty()) {
            val targetPage = pageOffsets
                .indexOfLast { it <= lastReadingOffset }
                .coerceIn(0, totalPages - 1)
            currentPage = targetPage
        }
    }

    // Notify parent about the new page (only for saving progress)
    LaunchedEffect(currentPage, totalPages) {
        onPageChanged(currentPage + 1, totalPages)
    }

    val pageText = pages.getOrElse(currentPage) { AnnotatedString("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Title bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(lightBlue)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = book.title, fontSize = 24.sp, color = Color.White)
            }

            // Content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onSizeChanged { contentSize = it }
            ) {
                if (contentSize.width > 0 && contentSize.height > 0) {
                    Text(
                        text = pageText,
                        style = textStyle,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                    )
                }
            }

            // Bottom bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(lightBlue)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentPage > 0) {
                            currentPage--
                            lastReadingOffset = pageOffsets.getOrElse(currentPage) { 0 }
                        }
                    },
                    enabled = currentPage > 0,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Previous Page",
                        tint = if (currentPage > 0) Color.White else Color.LightGray
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$fontSize sp",
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.clickable { fontSizeMenuExpanded = true }
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Font size",
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
                    Text(
                        text = "${currentPage + 1} / $totalPages",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        if (currentPage < totalPages - 1) {
                            currentPage++
                            lastReadingOffset = pageOffsets.getOrElse(currentPage) { 0 }
                        }
                    },
                    enabled = currentPage < totalPages - 1,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next Page",
                        tint = if (currentPage < totalPages - 1) Color.White else Color.LightGray
                    )
                }
            }
        }
    }
}