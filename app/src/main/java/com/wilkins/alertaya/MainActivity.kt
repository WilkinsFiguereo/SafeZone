package com.wilkins.alertaya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wilkins.alertaya.backend.network.SupabaseService
import com.wilkins.alertaya.frontend.ui.screens.auth.LoginScreen
import com.wilkins.alertaya.frontend.ui.screens.auth.RegisterScreen
import com.wilkins.alertaya.frontend.ui.screens.auth.VerificationScreen
import com.wilkins.alertaya.frontend.ui.screens.user.UserHomeScreen
import com.wilkins.alertaya.frontend.ui.theme.AlertaYaTheme
import com.wilkins.alertaya.frontend.ui.theme.PrimaryColor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AlertaYaTheme {
                val navController = rememberNavController()
                var savedEmail by remember { mutableStateOf("") }
                var savedPassword by remember { mutableStateOf("") }

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
                                }
                            },
                            onNavigateToRegister = { navController.navigate("register") }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onNavigateToLogin = {
                                navController.navigate("login") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToVerification = { email: String, password: String ->
                                savedEmail = email
                                savedPassword = password
                                navController.navigate("verification") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        )

                    }

                    composable("verification") {
                        VerificationScreen(
                            savedEmail = savedEmail,
                            savedPassword = savedPassword,
                            primaryColor = PrimaryColor,
                            onBackClick = {
                                navController.navigate("login") {
                                    popUpTo("verification") { inclusive = true }
                                }
                            },
                            onVerified = {
                                val supabase = SupabaseService.getInstance()
                                val userId = supabase.auth.currentUserOrNull()?.id ?: ""
                                navController.navigate("userHome/$userId") {
                                    popUpTo("verification") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("userHome/{userId}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        UserHomeScreen(userId)
                    }

                    composable("adminHome/{userId}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        // AdminHomeScreen(userId)
                    }
                }
            }
        }

    }
}
