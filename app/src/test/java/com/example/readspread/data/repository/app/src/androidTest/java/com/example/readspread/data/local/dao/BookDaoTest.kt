package com.example.readspread.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import data.local.AppDatabase
import data.local.entity.Book
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: BookDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = database.bookDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun insertAndGetAll() = runTest {
        val book = Book(
            title = "Test Book",
            author = "Tester",
            filePath = "/test/book.txt",
            format = "TXT"
        )
        dao.insertBook(book)
        val books = dao.getAllBooks().first()
        assertEquals(1, books.size)
        assertEquals("Test Book", books[0].title)
    }
}