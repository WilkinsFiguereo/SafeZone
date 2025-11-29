package com.wilkins.safezone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wilkins.safezone.NewsSave.NewsSaveScreen
import com.wilkins.safezone.Moderator.Dashboard.DashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "news_save" // cambia aquí para elegir pantalla inicial
            ) {
                composable("news_save") {
                    NewsSaveScreen(navController)
                }

                composable("dashboard") {
                    DashboardScreen(navController)
                }
            }
        }
    }
}
