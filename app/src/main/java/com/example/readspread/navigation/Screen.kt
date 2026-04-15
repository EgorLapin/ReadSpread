package com.example.readspread.navigation

import com.example.readspread.domain.model.Book

//sealed class Screen(val route: String) {
//    object Library : Screen("library")
//    object Reader : Screen("reader/{bookId}") {
//        fun createRoute(bookId: Int) = "reader/$bookId"
//    }
//}

// ✅ Более современный подход (с Navigation 2.7+):
object AppDestinations {
    const val LIBRARY = "library"
    const val READER = "reader/{bookId}"

    fun readerRoute(bookId: Int) = "reader/$bookId"
}