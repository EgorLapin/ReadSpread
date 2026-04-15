package com.example.readspread.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.readspread.ui.library.LibraryScreen
import com.example.readspread.ui.reader.ReaderScreen

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
                    navController.navigate(AppDestinations.readerRoute(book.id))
                }
            )
        }

        // Экран чтения
        composable(
            route = AppDestinations.READER,
            arguments = listOf(
                androidx.navigation.navArgument("bookId") {
                    type = androidx.navigation.NavType.IntType
                }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getInt("bookId") ?: 0
            ReaderScreen(
                bookId = bookId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}