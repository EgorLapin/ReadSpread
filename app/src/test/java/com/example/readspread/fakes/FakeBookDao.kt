package com.example.readspread.fakes

import data.local.dao.BookDao
import data.local.entity.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeBookDao : BookDao {
    private val books = MutableStateFlow<List<Book>>(emptyList())

    override fun getAllBooks(): Flow<List<Book>> = books

    override fun getBookById(bookId: Long): Flow<Book?> = books.map { list ->
        list.find { it.id == bookId }
    }

    override suspend fun getBookByIdSync(bookId: Long): Book? =
        books.value.find { it.id == bookId }

    override fun getBooksByStatus(status: String): Flow<List<Book>> = books.map { list ->
        list.filter { it.status == status }
    }

    override fun getFavoriteBooks(): Flow<List<Book>> = books.map { list ->
        list.filter { it.isFavorite }
    }

    override fun searchBooks(query: String): Flow<List<Book>> = books.map { list ->
        list.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.author.contains(query, ignoreCase = true) ||
                    (it.customTitle?.contains(query, ignoreCase = true) == true)
        }
    }

    override suspend fun getBookByHash(hash: String): Book? = null

    override suspend fun insertBook(book: Book): Long {
        val newId = (books.value.maxOfOrNull { it.id } ?: 0) + 1
        val newBook = book.copy(id = newId)
        books.value = books.value + newBook
        return newId
    }

    override suspend fun insertBooks(booksList: List<Book>) {
        books.value = books.value + booksList
    }

    override suspend fun updateBook(book: Book) {
        books.value = books.value.map { if (it.id == book.id) book else it }
    }

    override suspend fun deleteBook(book: Book) {
        books.value = books.value.filter { it.id != book.id }
    }

    override suspend fun deleteBookById(bookId: Long) {
        books.value = books.value.filter { it.id != bookId }
    }

    override suspend fun updateProgress(
        bookId: Long,
        progress: Float,
        status: String,
        lastReadAt: Long,
        updatedAt: Long
    ) {
        val book = books.value.find { it.id == bookId } ?: return
        val updated = book.copy(
            progress = progress,
            status = status,
            lastReadAt = lastReadAt,
            updatedAt = updatedAt
        )
        updateBook(updated)
    }

    override suspend fun updatePageAndProgress(
        bookId: Long,
        page: Int,
        progress: Float,
        status: String,
        lastReadAt: Long,
        updatedAt: Long
    ) {
        val book = books.value.find { it.id == bookId } ?: return
        val updated = book.copy(
            currentPage = page,
            progress = progress,
            status = status,
            lastReadAt = lastReadAt,
            updatedAt = updatedAt
        )
        updateBook(updated)
    }

    override suspend fun updateFavorite(bookId: Long, isFavorite: Boolean, updatedAt: Long) {
        val book = books.value.find { it.id == bookId } ?: return
        val updated = book.copy(isFavorite = isFavorite, updatedAt = updatedAt)
        updateBook(updated)
    }

    override suspend fun getBooksCount(): Int = books.value.size

    override suspend fun getBooksByStatusCount(status: String): Int =
        books.value.count { it.status == status }

    override suspend fun getTotalFilesSize(): Long = 0
}