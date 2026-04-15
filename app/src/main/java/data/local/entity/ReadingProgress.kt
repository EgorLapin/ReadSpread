package data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "reading_progress",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId"])]
)
data class ReadingProgress(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val bookId: Long,

    // Текущая страница
    val currentPage: Int = 0,

    // Процент прочтения
    val progressPercent: Float = 0f,

    // Позиция в документе (для EPUB - CFI, для PDF - номер страницы)
    val position: String = "",

    // ID текущей главы
    val currentChapter: String? = null,

    // Название текущей главы
    val chapterTitle: String? = null,

    // Временные метки
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)