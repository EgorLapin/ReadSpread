package com.example.readspread

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.readspread.ui.library.LibraryScreen
import com.example.readspread.ui.theme.ReadSpreadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadSpreadTheme {
                // Surface нужен для корректного отображения фона темы
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    LibraryScreen(
                        onBookClick = { book ->
                            // Пока просто выводим в лог при клике
                            android.util.Log.d("MainActivity", "Clicked: ${book.title}")
                        }
                    )
                }
            }
        }
    }
}