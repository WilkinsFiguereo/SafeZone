package com.wilkins.alertaya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wilkins.alertaya.frontend.ui.screens.auth.LoginScreen
import com.wilkins.alertaya.frontend.ui.screens.user.UserHomeScreen
import com.wilkins.alertaya.frontend.ui.theme.AlertaYaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AlertaYaTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            navController = navController,
                            onLoginSuccess = { user ->
                                when (user.role_id) {
                                    1 -> navController.navigate("userHome/${user.id}")
                                    2 -> navController.navigate("adminHome/${user.id}")
                                    else -> {
                                        // Rol desconocido
                                    }
                                }
                            }
                        )
                    }

                    composable("userHome/{userId}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        UserHomeScreen(userId) // ✅ UserHomeScreen recibe String
                    }

                    composable("adminHome/{userId}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        // TODO: AdminHomeScreen(userId)
                    }
                }
            }
        }
    }
}
