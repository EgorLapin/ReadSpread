package com.example.readspread.ui.reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readspread.ui.reader.ReaderViewModel.UiState
import dagger.hilt.android.AndroidEntryPoint
import data.local.entity.Book
import data.local.entity.Bookmark
import kotlin.math.min

@AndroidEntryPoint
class BookActivity : ComponentActivity() {
    private val viewModel: ReaderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val bookId = intent.getLongExtra("BOOK_ID", 0L)
        viewModel.setBookId(bookId)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()

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
                        bookmarks = bookmarks,
                        onPageChanged = { page, totalPages ->
                            viewModel.updatePageAndProgress(page, totalPages)
                        },
                        onFontSizeChanged = { newSize ->
                            viewModel.updateFontSize(newSize)
                        },
                        onTotalPagesCalculated = { totalPages ->
                            viewModel.updateTotalPages(totalPages)
                        },
                        onAddBookmark = { offset, pageNumber, textPreview ->
                            viewModel.addBookmark(offset, pageNumber, textPreview)
                        },
                        onDeleteBookmark = { bookmarkId ->
                            viewModel.deleteBookmark(bookmarkId)
                        }
                    )
                }
            }
        }
    }
}

enum class ReaderMode { Pages, Scroll }

@Composable
fun BookContent(
    book: Book,
    fullContent: String,
    bookmarks: List<Bookmark>,
    onPageChanged: (page: Int, totalPages: Int) -> Unit,
    onFontSizeChanged: (fontSize: Int) -> Unit,
    onTotalPagesCalculated: (totalPages: Int) -> Unit,
    onAddBookmark: (offset: Int, pageNumber: Int, textPreview: String) -> Unit,
    onDeleteBookmark: (bookmarkId: Long) -> Unit
) {
    data class TextChunk(val start: Int, val text: String)

    val lightBlue = Color(0xffEAAAFF)
    var fontSize by rememberSaveable { mutableIntStateOf(book.fontSize.takeIf { it > 0 } ?: 20) }
    var readerMode by rememberSaveable { mutableStateOf(ReaderMode.Pages) }
    var selectedFont by remember { mutableStateOf(FontFamily.Default) }
    var fontSizeMenuExpanded by remember { mutableStateOf(false) }
    var bookmarksMenuExpanded by remember { mutableStateOf(false) }

    // Flag to track if initial restoration has been done
    var isRestored by rememberSaveable { mutableStateOf(false) }

    val fontSizeOptions = listOf(12, 14, 16, 18, 20, 22, 24, 28, 32)

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val listState = rememberLazyListState()
    val itemLayouts = remember { mutableStateMapOf<Int, TextLayoutResult>() }

    val horizontalPadding = 16.dp
    val verticalPadding = 8.dp
    val verticalPaddingPx = with(density) { verticalPadding.toPx() }

    val textStyle = TextStyle(
        fontSize = fontSize.sp,
        fontFamily = selectedFont,
        lineHeight = (fontSize * 1.4).sp
    )

    val annotatedContent = remember(fullContent) { AnnotatedString(fullContent) }

    val chunks = remember(fullContent) {
        val maxChunk = 4000
        val result = mutableListOf<TextChunk>()
        var start = 0
        while (start < fullContent.length) {
            var end = (start + maxChunk).coerceAtMost(fullContent.length)
            if (end < fullContent.length) {
                val newline = fullContent.lastIndexOf('\n', end)
                if (newline > start + maxChunk / 2) end = newline + 1
            }
            result += TextChunk(start, fullContent.substring(start, end))
            start = end
        }
        result.ifEmpty { listOf(TextChunk(0, "")) }
    }

    fun chunkIndexForOffset(offset: Int): Int {
        val safe = offset.coerceIn(0, fullContent.length.coerceAtLeast(1) - 1)
        return chunks.indexOfLast { it.start <= safe }.coerceAtLeast(0)
    }

    var contentSize by remember { mutableStateOf(IntSize.Zero) }
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    var lastReadingOffset by rememberSaveable { mutableIntStateOf(0) }
    var pendingScrollOffset by remember { mutableStateOf<Int?>(null) }

    // Pages are computed for both reading modes
    val pages = remember(annotatedContent, textStyle, contentSize) {
        if (contentSize.width <= 0 || contentSize.height <= 0) {
            emptyList()
        } else {
            val availableWidthPx = with(density) { contentSize.width - horizontalPadding.toPx() * 2 }
            val availableHeightPx = with(density) { contentSize.height - verticalPadding.toPx() * 2 }

            val fullLayout = textMeasurer.measure(
                text = annotatedContent,
                style = textStyle,
                maxLines = Int.MAX_VALUE,
                constraints = Constraints(maxWidth = availableWidthPx.toInt().coerceAtLeast(1))
            )

            data class Line(val start: Int, val end: Int, val bottom: Float)
            val lines = (0 until fullLayout.lineCount).map { i ->
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
                val pageStartChar = lines[lineIndex].start
                var lastFits = lineIndex
                while (lastFits < lines.size) {
                    val heightFromTop = lines[lastFits].bottom - pageTopY
                    if (heightFromTop <= availableHeightPx) lastFits++ else break
                }
                val lastFittingIndex = lastFits - 1
                if (lastFittingIndex < lineIndex) {
                    val forcedLine = lines[lineIndex]
                    pageList += annotatedContent.subSequence(pageStartChar, forcedLine.end)
                    pageTopY = forcedLine.bottom
                    lineIndex++
                } else {
                    val lastLine = lines[lastFittingIndex]
                    pageList += annotatedContent.subSequence(pageStartChar, lastLine.end)
                    pageTopY = lastLine.bottom
                    lineIndex = lastFittingIndex + 1
                }
            }
            pageList.ifEmpty { listOf(AnnotatedString("")) }
        }
    }

    val pageOffsets = remember(pages) {
        var offset = 0
        pages.map {
            val start = offset
            offset += it.text.length
            start
        }
    }

    val totalPages = pages.size.coerceAtLeast(1)

    // Save total pages to database when they change
    LaunchedEffect(totalPages) {
        if (totalPages > 0) {
            onTotalPagesCalculated(totalPages)
        }
    }

    fun pageIndexForOffset(offset: Int): Int {
        if (pageOffsets.isEmpty()) return 0
        val safe = offset.coerceIn(0, fullContent.length.coerceAtLeast(1) - 1)
        return pageOffsets.indexOfLast { it <= safe }
            .coerceAtLeast(0)
            .coerceAtMost(totalPages - 1)
    }

    // Restore reading position only once when pages become available
    LaunchedEffect(pages) {
        if (!isRestored && pages.isNotEmpty()) {
            // Restore reading position
            val savedPage = book.currentPage.coerceIn(1, totalPages)
            val savedOffset = pageOffsets.getOrElse(savedPage - 1) { 0 }
            if (savedOffset > 0) {
                lastReadingOffset = savedOffset
            }
            isRestored = true
        }
    }

    // Update current page from offset whenever pages or offset changes
    LaunchedEffect(pages, lastReadingOffset) {
        if (pages.isNotEmpty()) {
            currentPage = pageIndexForOffset(lastReadingOffset)
        }
    }

    // Save progress whenever the logical page changes
    LaunchedEffect(currentPage, totalPages) {
        onPageChanged(currentPage + 1, totalPages)
    }

    // Scroll mode: update offset from LazyList
    LaunchedEffect(readerMode, chunks) {
        if (readerMode == ReaderMode.Scroll) {
            snapshotFlow {
                listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
            }.collect { (index, scrollPx) ->
                val chunk = chunks.getOrNull(index) ?: return@collect
                val layout = itemLayouts[index]
                val localOffset = if (layout != null && chunk.text.isNotEmpty()) {
                    val y = (scrollPx - verticalPaddingPx).coerceAtLeast(0f)
                    val offset = layout.getOffsetForPosition(Offset(0f, y))
                    val line = layout.getLineForOffset(offset)
                    layout.getLineStart(line)
                } else 0
                lastReadingOffset = (chunk.start + localOffset)
                    .coerceIn(0, fullContent.length)
            }
        }
    }

    // Scroll to offset when mode switches or fontSize changes
    LaunchedEffect(readerMode, pendingScrollOffset, fontSize, contentSize) {
        val targetOffset = pendingScrollOffset ?: return@LaunchedEffect
        if (readerMode != ReaderMode.Scroll) return@LaunchedEffect
        val index = chunkIndexForOffset(targetOffset)
        val chunk = chunks[index]
        val localOffset = (targetOffset - chunk.start).coerceIn(0, chunk.text.length)
        listState.scrollToItem(index, 0)
        repeat(10) { if (itemLayouts[index] == null) withFrameNanos { } }
        val layout = itemLayouts[index]
        if (layout != null && chunk.text.isNotEmpty()) {
            val safeOffset = localOffset.coerceIn(0, chunk.text.length - 1)
            val line = layout.getLineForOffset(safeOffset)
            val y = layout.getLineTop(line) + verticalPaddingPx
            listState.scrollToItem(index, y.toInt())
        }
        pendingScrollOffset = null
    }

    // --- UI helpers ---
    val pageText = pages.getOrElse(currentPage) { AnnotatedString("") }
    val currentPageStartOffset = pageOffsets.getOrElse(currentPage) { 0 }

    // Bookmark handling
    val bookmarkOffsets = remember(bookmarks) {
        bookmarks.mapNotNull { it.position.toIntOrNull() }.toSet()
    }
    val currentBookmarkOffset = if (readerMode == ReaderMode.Pages)
        currentPageStartOffset else lastReadingOffset
    val isCurrentBookmarked = currentBookmarkOffset in bookmarkOffsets
    val existingBookmark = bookmarks.find { it.position.toIntOrNull() == currentBookmarkOffset }

    val controlsTextSize = (fontSize * 0.75f).coerceIn(12f, 20f).sp
    val indicatorTextSize = (fontSize * 0.70f).coerceIn(11f, 18f).sp

    var dragOffset by remember { mutableFloatStateOf(0f) }
    val swipeThresholdPx = with(density) { 100.dp.toPx() }

    var showControls by rememberSaveable { mutableStateOf(false) }
    var counterBarHeightPx by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { showControls = !showControls }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val readerModifier = if (readerMode == ReaderMode.Pages) {
                Modifier.pointerInput(currentPage, totalPages) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragOffset = 0f },
                        onDragEnd = {
                            when {
                                dragOffset > swipeThresholdPx && currentPage > 0 -> {
                                    lastReadingOffset = pageOffsets.getOrElse(currentPage - 1) { lastReadingOffset }
                                }
                                dragOffset < -swipeThresholdPx && currentPage < totalPages - 1 -> {
                                    lastReadingOffset = pageOffsets.getOrElse(currentPage + 1) { lastReadingOffset }
                                }
                            }
                            dragOffset = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            dragOffset += dragAmount
                            change.consume()
                        }
                    )
                }
            } else Modifier

            Box(
                modifier = Modifier
                    .weight(1f)
                    .statusBarsPadding()
                    .onSizeChanged { contentSize = it }
                    .then(readerModifier)
            ) {
                if (readerMode == ReaderMode.Pages) {
                    val visualOffset = with(density) { dragOffset.toDp() }
                    Text(
                        text = pageText,
                        style = textStyle,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                            .offset(x = visualOffset)
                    )
                } else {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(
                            items = chunks,
                            key = { _, chunk -> chunk.start }
                        ) { index, chunk ->
                            Text(
                                text = chunk.text,
                                style = textStyle,
                                onTextLayout = { itemLayouts[index] = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                            )
                        }
                    }
                }
            }

            // Always visible counter bar
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .background(lightBlue)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .onGloballyPositioned { coordinates ->
                        counterBarHeightPx = coordinates.size.height
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (readerMode == ReaderMode.Pages) {
                        "${currentPage + 1} / $totalPages"
                    } else {
                        val percent = if (fullContent.isEmpty()) 0 else lastReadingOffset * 100 / fullContent.length
                        "$percent% · $lastReadingOffset / ${fullContent.length}"
                    },
                    fontSize = indicatorTextSize,
                    color = Color.White
                )
            }
        }

        // Controls overlay
        if (showControls) {
            // Top title bar
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .background(lightBlue)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = book.title, fontSize = 24.sp, color = Color.White)
            }

            val offsetY = with(density) { counterBarHeightPx.toDp() }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = -offsetY)
                    .background(lightBlue)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous page button (Pages mode) or spacer
                if (readerMode == ReaderMode.Pages) {
                    IconButton(
                        onClick = {
                            if (currentPage > 0) {
                                lastReadingOffset = pageOffsets.getOrElse(currentPage - 1) { lastReadingOffset }
                            }
                        },
                        enabled = currentPage > 0,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Previous Page",
                            tint = if (currentPage > 0) Color.White else Color.LightGray
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Font size
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$fontSize sp", color = Color.White, fontSize = controlsTextSize,
                            modifier = Modifier.clickable { fontSizeMenuExpanded = true }
                        )
                        Icon(
                            Icons.Default.ArrowDropDown, "Font size", tint = Color.White,
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
                                        if (readerMode == ReaderMode.Scroll)
                                            pendingScrollOffset = lastReadingOffset
                                        fontSizeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Mode switch
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Листание", color = Color.White, fontSize = controlsTextSize)
                        Switch(
                            checked = readerMode == ReaderMode.Scroll,
                            onCheckedChange = { scroll ->
                                if (readerMode == ReaderMode.Pages)
                                    lastReadingOffset = currentPageStartOffset
                                readerMode = if (scroll) ReaderMode.Scroll else ReaderMode.Pages
                                if (scroll) pendingScrollOffset = lastReadingOffset
                            },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text("Скролл", color = Color.White, fontSize = controlsTextSize)
                    }

                    // Bookmarks
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (isCurrentBookmarked) {
                                    existingBookmark?.let { onDeleteBookmark(it.id) }
                                } else {
                                    val previewStart = currentBookmarkOffset
                                    val previewEnd = min(previewStart + 40, fullContent.length)
                                    val snippet = fullContent.substring(previewStart, previewEnd)
                                        .replace('\n', ' ').trim()
                                    onAddBookmark(currentBookmarkOffset, currentPage + 1, snippet)
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                if (isCurrentBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkAdd,
                                "Bookmark",
                                tint = Color.White
                            )
                        }
                        Text(
                            "Закладки", color = Color.White, fontSize = controlsTextSize,
                            modifier = Modifier.padding(start = 4.dp).clickable { bookmarksMenuExpanded = true }
                        )
                        Icon(
                            Icons.Default.ArrowDropDown, "Open bookmarks", tint = Color.White,
                            modifier = Modifier.clickable { bookmarksMenuExpanded = true }
                        )
                        DropdownMenu(
                            expanded = bookmarksMenuExpanded,
                            onDismissRequest = { bookmarksMenuExpanded = false }
                        ) {
                            if (bookmarks.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Нет закладок") },
                                    onClick = { bookmarksMenuExpanded = false },
                                    enabled = false
                                )
                            } else {
                                bookmarks.sortedBy { it.position.toIntOrNull() ?: 0 }.forEach { bm ->
                                    val offset = bm.position.toIntOrNull() ?: 0
                                    val start = offset.coerceIn(0, fullContent.length.coerceAtLeast(1) - 1)
                                    val end = min(start + 40, fullContent.length)
                                    val snippet = fullContent.substring(start, end).replace('\n', ' ').trim()
                                    val label = if (readerMode == ReaderMode.Pages)
                                        "Страница ${bm.pageNumber}"
                                    else {
                                        val percent = if (fullContent.isEmpty()) 0 else offset * 100 / fullContent.length
                                        "$percent%"
                                    }
                                    DropdownMenuItem(
                                        text = { Text("$label — $snippet", fontSize = controlsTextSize) },
                                        onClick = {
                                            lastReadingOffset = offset
                                            if (readerMode == ReaderMode.Pages) currentPage = pageIndexForOffset(offset)
                                            else pendingScrollOffset = offset
                                            bookmarksMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Next page button (Pages mode) or spacer
                if (readerMode == ReaderMode.Pages) {
                    IconButton(
                        onClick = {
                            if (currentPage < totalPages - 1) {
                                lastReadingOffset = pageOffsets.getOrElse(currentPage + 1) { lastReadingOffset }
                            }
                        },
                        enabled = currentPage < totalPages - 1,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            "Next Page",
                            tint = if (currentPage < totalPages - 1) Color.White else Color.LightGray
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}