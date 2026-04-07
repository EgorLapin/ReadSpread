package com.example.readspread.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.readspread.navigation.AppNavGraph
import com.example.readspread.ui.theme.ReadSpreadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadSpreadTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ✅ Создаём NavController
                    val navController = rememberNavController()

                    // ✅ Используем NavGraph
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}