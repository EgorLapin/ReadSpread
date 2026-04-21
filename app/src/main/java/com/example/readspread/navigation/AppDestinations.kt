package com.example.readspread.navigation

object AppDestinations {
    const val LIBRARY = "library"
    const val READER = "reader/{bookId}"   // важно: с плейсхолдером {bookId}

    // Вспомогательная функция для удобной навигации
    fun readerRoute(bookId: Long): String = "reader/$bookId"
}
