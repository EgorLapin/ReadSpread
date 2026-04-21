//package com.example.readspread.domain.model
//
//data class old_Book(
//    val id: Int,
//    val title: String,
//    val author: String,
//    val coverImage: String = "", // URL или путь к обложке
//    val totalPages: Int,
//    val currentPage: Int = 0,
//    val isFavorite: Boolean = false
//) {
//    // Вычисляемое свойство: прогресс чтения в процентах
//    val progressPercent: Int
//        get() = if (totalPages > 0) (currentPage * 100) / totalPages else 0
//}