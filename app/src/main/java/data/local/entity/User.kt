package data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey
    val id: String,

    val email: String,
    val username: String,

    // Токены для авторизации
    val accessToken: String? = null,
    val refreshToken: String? = null,

    // Аватар
    val avatarUrl: String? = null,

    // Использование хранилища
    val storageUsed: Long = 0, // в байтах
    val storageLimit: Long = 5 * 1024 * 1024 * 1024, // 5 GB по умолчанию

    // Временные метки
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)