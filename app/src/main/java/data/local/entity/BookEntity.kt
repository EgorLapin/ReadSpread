package data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "books",
    indices = [Index(value = ["title", "author"])]
)
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val author: String,
    val isbn: String? = null,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val description: String? = null,

    // Путь к файлу книги
    val filePath: String,

    // Формат книги
    val format: String,

    // Путь к обложке
    val coverPath: String? = null,

    // Размер файла в байтах
    val fileSize: Long = 0,

    // Общее количество страниц
    val totalPages: Int = 0,

    // Текущая страница
    val currentPage: Int = 1,

    // Прогресс чтения (0-100)
    val progress: Float = 0f,

    // Статус: NOT_STARTED, READING, COMPLETED
    val status: String = "NOT_STARTED",

    // Избранное
    val isFavorite: Boolean = false,

    // Хэш файла для дедупликации
    val fileHash: String? = null,

    // Временные метки
    val addedAt: Long = System.currentTimeMillis(),
    val lastReadAt: Long? = null,
    val completedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

// Статусы книги
object BookStatus {
    const val NOT_STARTED = "NOT_STARTED"
    const val READING = "READING"
    const val COMPLETED = "COMPLETED"
}

// Форматы книг
object BookFormat {
    const val EPUB = "EPUB"
    const val PDF = "PDF"
    const val FB2 = "FB2"
    const val MOBI = "MOBI"
    const val TXT = "TXT"
}