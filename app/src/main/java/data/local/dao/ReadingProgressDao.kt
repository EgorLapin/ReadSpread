package data.local.dao


import androidx.room.*
import data.local.entity.ReadingProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDao {

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId ORDER BY updatedAt DESC LIMIT 1")
    fun getLatestProgressForBook(bookId: Long): Flow<ReadingProgress?>

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestProgressForBookSync(bookId: Long): ReadingProgress?

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId ORDER BY updatedAt DESC")
    fun getAllProgressForBook(bookId: Long): Flow<List<ReadingProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ReadingProgress): Long

    @Update
    suspend fun updateProgress(progress: ReadingProgress)

    @Delete
    suspend fun deleteProgress(progress: ReadingProgress)

    @Query("DELETE FROM reading_progress WHERE bookId = :bookId")
    suspend fun deleteProgressForBook(bookId: Long)

    @Query("UPDATE reading_progress SET fontSize = :fontSize WHERE bookId = :bookId")
    suspend fun updateFontSize(bookId: Long, fontSize: Int)

    @Query("UPDATE reading_progress SET currentPage = :currentPage , progressPercent = :progress WHERE bookId = :bookId")
    suspend fun updatePageAndProgress(bookId: Long, currentPage: Int, progress: Float)
    }