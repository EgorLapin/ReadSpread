package com.example.readspread.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.readspread.ui.details.BookDetailsScreen
import com.example.readspread.ui.library.LibraryScreen
import androidx.compose.foundation.clickable
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
            LibraryScreen(
                onBookClick = { book ->
                    // Переход на экран деталей книги
                    navController.navigate(AppDestinations.bookDetailsRoute(book.id))
                }
            )
        }

        // Экран деталей книги
        composable(
            route = AppDestinations.BOOK_DETAILS,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
            Log.d("NAV_GRAPH", "Открываем BookDetailsScreen с bookId = $bookId")
            BookDetailsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Экран чтения (BookActivity) запускается из BookDetailsScreen по кнопке «Начать читать»
    }
}