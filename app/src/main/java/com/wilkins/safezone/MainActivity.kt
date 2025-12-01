//package com.wilkins.safezone
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.wilkins.safezone.frontend.ui.Moderator.NewsSave.NewsSaveScreen
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContent {
//
//            val navController = rememberNavController()
//
//            Surface(
//                color = MaterialTheme.colorScheme.background
//            ) {
//
//                NavHost(
//                    navController = navController,
//                    startDestination = "news_save"
//                ) {
//
//                    composable("news_save") {
//                        NewsSaveScreen(
//                            navController = navController,
//                            userId = "123" // Temporal para pruebas
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
