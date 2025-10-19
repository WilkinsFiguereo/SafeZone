package com.wilkins.alertaya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wilkins.alertaya.frontend.ui.screens.auth.LoginScreen
import com.wilkins.alertaya.frontend.ui.screens.auth.RegisterScreen
import com.wilkins.alertaya.frontend.ui.screens.user.UserScreen
import com.wilkins.alertaya.frontend.ui.theme.AlertaYaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AlertaYaTheme {
                RegisterScreen()
//                val navController = rememberNavController()
//
//                NavHost(navController = navController, startDestination = "login") {
//                    composable("login") {
//                        LoginScreen { userId ->
//                            navController.navigate("user/$userId")
//                        }
//                    }
//                    composable("user/{userId}") { backStackEntry ->
//                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
//                        UserScreen(userId)
//                    }
//                }
            }
        }
    }
}

