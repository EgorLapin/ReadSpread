package data.local.dao

import androidx.room.*
import data.local.entity.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books ORDER BY lastReadAt DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookById(bookId: Long): Flow<Book?>

    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookByIdSync(bookId: Long): Book?

    @Query("SELECT * FROM books WHERE status = :status ORDER BY lastReadAt DESC")
    fun getBooksByStatus(status: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR customTitle LIKE '%' || :query || '%'")
    fun searchBooks(query: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE fileHash = :hash")
    suspend fun getBookByHash(hash: String): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<Book>)

    @Query("DELETE FROM books WHERE filePath LIKE :prefix || '%'")
    suspend fun deleteBooksByFilePathPrefix(prefix: String)
    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: Long)

    @Query("UPDATE books SET progress = :progress, status = :status, lastReadAt = :lastReadAt, updatedAt = :updatedAt WHERE id = :bookId")
    suspend fun updateProgress(bookId: Long, progress: Float, status: String, lastReadAt: Long, updatedAt: Long)

    @Query("UPDATE books SET currentPage = :page, progress = :progress, status = :status, lastReadAt = :lastReadAt, updatedAt = :updatedAt WHERE id = :bookId")
    suspend fun updatePageAndProgress(bookId: Long, page: Int, progress: Float, status: String, lastReadAt: Long, updatedAt: Long)

    @Query("UPDATE books SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :bookId")
    suspend fun updateFavorite(bookId: Long, isFavorite: Boolean, updatedAt: Long)

    @Query("UPDATE books SET fontSize = :fontSize, updatedAt = :updatedAt WHERE id = :bookId")
    suspend fun updateFontSize(bookId: Long, fontSize: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBooksCount(): Int

    @Query("SELECT COUNT(*) FROM books WHERE status = :status")
    suspend fun getBooksByStatusCount(status: String): Int

    @Query("SELECT SUM(fileSize) FROM books")
    suspend fun getTotalFilesSize(): Long?

    @Query("UPDATE books SET totalPages = :totalPages, updatedAt = :updatedAt WHERE id = :bookId")
    suspend fun updateTotalPages(bookId: Long, totalPages: Int, updatedAt: Long)
}