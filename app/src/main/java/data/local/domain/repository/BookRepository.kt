package data.local.domain.repository

import data.local.dao.BookDao
import data.local.entity.Book
import data.local.entity.BookStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookRepository(
    private val bookDao: BookDao
) {

    // Получение всех книг
    fun getAllBooks(): Flow<List<Book>> = bookDao.getAllBooks()

    // Получение книги по ID
    fun getBookById(bookId: Long): Flow<Book?> = bookDao.getBookById(bookId)

    suspend fun getBookByIdSync(bookId: Long): Book? = withContext(Dispatchers.IO) {
        bookDao.getBookByIdSync(bookId)
    }

    // Получение книг по статусу
    fun getBooksByStatus(status: String): Flow<List<Book>> = bookDao.getBooksByStatus(status)

    fun getReadingBooks(): Flow<List<Book>> = bookDao.getBooksByStatus(BookStatus.READING)
    fun getCompletedBooks(): Flow<List<Book>> = bookDao.getBooksByStatus(BookStatus.COMPLETED)
    fun getNotStartedBooks(): Flow<List<Book>> = bookDao.getBooksByStatus(BookStatus.NOT_STARTED)

    // Избранные книги
    fun getFavoriteBooks(): Flow<List<Book>> = bookDao.getFavoriteBooks()

    // Поиск книг
    fun searchBooks(query: String): Flow<List<Book>> = bookDao.searchBooks(query)

    // Несинхронизированные книги
    suspend fun getUnsyncedBooks(): List<Book> = withContext(Dispatchers.IO) {
        bookDao.getUnsyncedBooks()
    }

    // Проверка дубликатов по хэшу
    suspend fun getBookByHash(hash: String): Book? = withContext(Dispatchers.IO) {
        bookDao.getBookByHash(hash)
    }

    // Добавление книги
    suspend fun insertBook(book: Book): Long = withContext(Dispatchers.IO) {
        bookDao.insertBook(book)
    }

    suspend fun insertBooks(books: List<Book>) = withContext(Dispatchers.IO) {
        bookDao.insertBooks(books)
    }

    // Обновление книги
    suspend fun updateBook(book: Book) = withContext(Dispatchers.IO) {
        bookDao.updateBook(book)
    }

    // Удаление книги
    suspend fun deleteBook(book: Book) = withContext(Dispatchers.IO) {
        bookDao.deleteBook(book)
    }

    suspend fun deleteBookById(bookId: Long) = withContext(Dispatchers.IO) {
        bookDao.deleteBookById(bookId)
    }

    // Обновление прогресса
    suspend fun updateProgress(
        bookId: Long,
        progress: Float,
        lastReadAt: Long = System.currentTimeMillis()
    ) = withContext(Dispatchers.IO) {
        val status = when {
            progress >= 100f -> BookStatus.COMPLETED
            progress > 0f -> BookStatus.READING
            else -> BookStatus.NOT_STARTED
        }
        bookDao.updateProgress(bookId, progress, status, lastReadAt, System.currentTimeMillis())
    }

    // Обновление страницы и прогресса
    suspend fun updatePageAndProgress(
        bookId: Long,
        page: Int,
        totalPages: Int
    ) = withContext(Dispatchers.IO) {
        val progress = if (totalPages > 0) (page.toFloat() / totalPages.toFloat() * 100) else 0f
        val status = when {
            progress >= 100f -> BookStatus.COMPLETED
            progress > 0f -> BookStatus.READING
            else -> BookStatus.NOT_STARTED
        }
        bookDao.updatePageAndProgress(bookId, page, progress, status, System.currentTimeMillis(), System.currentTimeMillis())
    }

    // Обновление избранного
    suspend fun updateFavorite(bookId: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        bookDao.updateFavorite(bookId, isFavorite, System.currentTimeMillis())
    }

    // Обновление статуса синхронизации
    suspend fun updateSyncStatus(bookId: Long, isSynced: Boolean) = withContext(Dispatchers.IO) {
        bookDao.updateSyncStatus(bookId, isSynced)
    }

    // Статистика
    suspend fun getBooksCount(): Int = withContext(Dispatchers.IO) {
        bookDao.getBooksCount()
    }

    suspend fun getBooksByStatusCount(status: String): Int = withContext(Dispatchers.IO) {
        bookDao.getBooksByStatusCount(status)
    }

    suspend fun getTotalFilesSize(): Long = withContext(Dispatchers.IO) {
        bookDao.getTotalFilesSize() ?: 0L
    }
}