package com.example.readspread.data.repository

import com.example.readspread.fakes.FakeBookDao
import data.local.domain.repository.BookRepository
import data.local.entity.Book
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class BookRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dao: FakeBookDao
    private lateinit var repository: BookRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dao = FakeBookDao()
        repository = BookRepository(dao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAllBooks should return all books`() = runTest {
        val book1 = Book(id = 1, title = "Book 1", author = "A", filePath = "test1.txt", format = "TXT")
        val book2 = Book(id = 2, title = "Book 2", author = "B", filePath = "test2.txt", format = "TXT")
        dao.insertBook(book1)
        dao.insertBook(book2)

        val result = repository.getAllBooks().first()
        assertEquals(2, result.size)
        assertEquals("Book 1", result[0].title)
    }

    @Test
    fun `searchBooks should filter by title`() = runTest {
        val book = Book(id = 1, title = "Kotlin", author = "JetBrains", filePath = "test.epub", format = "EPUB")
        dao.insertBook(book)

        val result = repository.searchBooks("kot").first()
        assertEquals(1, result.size)
        assertEquals("Kotlin", result[0].title)
    }

    @Test
    fun `updateFavorite should toggle isFavorite`() = runTest {
        val book = Book(id = 1, title = "Fav", author = "Me", filePath = "f.txt", format = "TXT")
        dao.insertBook(book)

        repository.updateFavorite(1, true)
        val updated = dao.getBookByIdSync(1)
        assertTrue(updated?.isFavorite ?: false)
    }
}