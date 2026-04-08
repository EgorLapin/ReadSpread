package data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId"]), Index(value = ["pageNumber"])]
)
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val bookId: Long,

    // Номер страницы
    val pageNumber: Int,

    // Процент прогресса на момент создания закладки
    val progressPercent: Float = 0f,

    // Позиция в документе
    val position: String,

    // ID главы
    val chapterId: String? = null,

    // Название главы
    val chapterTitle: String? = null,

    // Превью текста (первые 100 символов)
    val textPreview: String? = null,

    // Заметка пользователя
    val note: String? = null,

    // Временные метки
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)