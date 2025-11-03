package com.wilkins.safezone

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
import com.wilkins.safezone.GenericUserUi.SplashScreen
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.frontend.ui.NavigationDrawer.NavigationDrawer
import com.wilkins.safezone.frontend.ui.screens.auth.LoginScreen
import com.wilkins.safezone.frontend.ui.screens.auth.RegisterScreen
import com.wilkins.safezone.frontend.ui.screens.auth.VerificationScreen
import com.wilkins.safezone.frontend.ui.user.Homepage.UserHomeScreen
import com.wilkins.safezone.ui.theme.SafeZoneTheme
import com.wilkins.safezone.ui.theme.PrimaryColor
import io.github.jan.supabase.gotrue.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SafeZoneTheme {
                val navController = rememberNavController()
                var savedEmail by remember { mutableStateOf("") }
                var savedPassword by remember { mutableStateOf("") }


                NavHost(
                    navController = navController,
                    startDestination = "splash" // siempre inicia en splash
                ) {
                    // 🟢 Pantalla Splasha
                    composable("splash") {
                        SplashScreen(navController)
                    }

                    // 🔵 Pantalla Login
                    composable("login") {
                        LoginScreen(
                            navController = navController,
                            onLoginSuccess = { user ->
                                when (user.role_id) {
                                    1 -> navController.navigate("userHome/${user.id}") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                    2 -> navController.navigate("adminHome/${user.id}") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            },
                            onNavigateToRegister = { navController.navigate("register") }
                        )
                    }

                    // 🟣 Pantalla Registro
                    composable("register") {
                        RegisterScreen(
                            onNavigateToLogin = {
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            },
                            onNavigateToVerification = { email, password ->
                                savedEmail = email
                                savedPassword = password
                                navController.navigate("verification") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 🟠 Pantalla Verificación
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

                    // 🏠 Pantalla Home Usuario
                    composable("userHome/{userId}") {
                        UserHomeScreen(navController)
                    }

                    // 🏢 Pantalla Home Admin (aún no implementada)
                    composable("adminHome/{userId}") {
                        // AdminHomeScreen()
                    }

                    composable("profile") { NavigationDrawer(navController) }
                }
            }
        }
    }
}
