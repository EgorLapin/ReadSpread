package data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "highlights",
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
data class Highlight(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val bookId: Long,

    // Выделенный текст
    val selectedText: String,

    // Начальная позиция выделения
    val startPosition: String,

    // Конечная позиция выделения
    val endPosition: String,

    // Номер страницы
    val pageNumber: Int = 0,

    // ID главы
    val chapterId: String? = null,

    // Цвет выделения (YELLOW, GREEN, BLUE, PINK, ORANGE)
    val color: String = HighlightColor.YELLOW,

    // Заметка к выделению
    val note: String? = null,

    // Временные метки
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

// Цвета выделения
object HighlightColor {
    const val YELLOW = "YELLOW"
    const val GREEN = "GREEN"
    const val BLUE = "BLUE"
    const val PINK = "PINK"
    const val ORANGE = "ORANGE"
}