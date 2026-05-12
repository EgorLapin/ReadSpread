package com.example.readspread.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import android.util.Log
import com.example.readspread.ui.library.LibraryScreen
//import com.example.readspread.ui.reader.ReaderScreen   // ← должен быть этот импорт
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.readspread.ui.reader.BookActivity

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.LIBRARY,
        modifier = modifier
    ) {
        // Экран библиотеки
        composable(route = AppDestinations.LIBRARY) {
            val context = LocalContext.current
            LibraryScreen(
                onBookClick = { book ->
                    val intent = Intent(context, BookActivity::class.java).apply {
                        putExtra("BOOK_ID", book.id)
                    }
                    context.startActivity(intent)
                }
            )
        }

        // Экран чтения
//        composable(
//            route = AppDestinations.READER,
//            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
//        ) { backStackEntry ->
//            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
//            ReaderScreen(bookId = bookId, onBackClick = { navController.popBackStack() })
//        }
    }
}