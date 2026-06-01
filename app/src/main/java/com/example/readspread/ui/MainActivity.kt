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
                // Книга 1: Война и мир (начало)
                bookRepository.insertBook(
                    Book(
                        title = "Война и мир",
                        author = "Лев Толстой",
                        filePath = "test_war_and_peace.txt",
                        format = "TXT",
                        totalPages = 1225,
                        currentPage = 1,
                        description = "Роман-эпопея о русском обществе в эпоху наполеоновских войн.",
                        publishedDate = "1869",
                        coverImageUrl = null // заглушка автоматически
                    )
                )

                // Книга 2: Преступление и наказание (начало)
                bookRepository.insertBook(
                    Book(
                        title = "Преступление и наказание",
                        author = "Фёдор Достоевский",
                        filePath = "test_crime_and_punishment.txt",
                        format = "TXT",
                        totalPages = 672,
                        currentPage = 1,
                        description = "История Родиона Раскольникова, студента, совершившего убийство.",
                        publishedDate = "1866",
                        coverImageUrl = null
                    )
                )

                // Книга 3: 1984 (первые абзацы)
                bookRepository.insertBook(
                    Book(
                        title = "1984",
                        author = "Джордж Оруэлл",
                        filePath = "test_1984.txt",
                        format = "TXT",
                        totalPages = 328,
                        currentPage = 1,
                        description = "Антиутопия о тоталитарном обществе и контроле над сознанием.",
                        publishedDate = "1949",
                        coverImageUrl = null
                    )
                )

                // Книга 4: Мастер и Маргарита
                bookRepository.insertBook(
                    Book(
                        title = "Мастер и Маргарита",
                        author = "Михаил Булгаков",
                        filePath = "test_master_and_margarita.txt",
                        format = "TXT",
                        description = "Дьявол и его свита посещают Москву 1930-х годов.",
                        coverImageUrl = null,
                        totalPages = 480,
                        currentPage = 1,
                        publishedDate = "1967",
                        coverPath = null
                    )
                )

                // Книга 5: Гарри Поттер и философский камень
                bookRepository.insertBook(
                    Book(
                        title = "Гарри Поттер и философский камень",
                        author = "Дж.К. Роулинг",
                        filePath = "test_harry_potter.txt",
                        format = "TXT",
                        totalPages = 432,
                        currentPage = 1,
                        description = "Мальчик узнаёт, что он волшебник, и поступает в Хогвартс.",
                        publishedDate = "1997",
                        coverPath = null
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