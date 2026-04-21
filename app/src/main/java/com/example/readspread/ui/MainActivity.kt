package com.example.readspread.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.readspread.navigation.AppNavGraph
import com.example.readspread.ui.theme.ReadSpreadTheme
import dagger.hilt.android.AndroidEntryPoint
import data.local.domain.repository.BookRepository
import data.local.entity.Book
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var bookRepository: BookRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Вставка тестовой книги, если база пуста
        lifecycleScope.launch {
            if (bookRepository.getBooksCount() == 0) {
                bookRepository.insertBook(
                    Book(
                        title = "Тестовая книга",
                        author = "Автор",
                        filePath = "/dev/null",
                        format = "TXT",
                        totalPages = 100,
                        description = "Это тестовая книга для проверки."
                    )
                )
            }
        }

        setContent {
            ReadSpreadTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}