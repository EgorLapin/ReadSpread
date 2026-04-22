package com.example.readspread.navigation

object AppDestinations {
    const val LIBRARY = "library"
    const val BOOK_DETAILS = "book_details/{bookId}"
    // READER уже не нужен, т.к. используем Activity
    fun bookDetailsRoute(bookId: Long) = "book_details/$bookId"
}
