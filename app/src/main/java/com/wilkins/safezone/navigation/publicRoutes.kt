package com.wilkins.safezone.navigation

import androidx.compose.runtime.MutableState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.wilkins.safezone.GenericUserUi.SplashScreen
import com.wilkins.safezone.frontend.ui.auth.screens.Login.LoginScreen
import com.wilkins.safezone.frontend.ui.auth.screens.Register.RegisterScreen
import com.wilkins.safezone.frontend.ui.auth.screens.Register.VerificationScreen
import com.wilkins.safezone.ui.theme.PrimaryColor
import com.wilkins.safezone.backend.network.SupabaseService
import android.util.Log
import io.github.jan.supabase.gotrue.auth

/**
 * 🌐 RUTAS GENERALES (Públicas)
 * No requieren autenticación
 *
 * Incluye:
 * - Splash
 * - Login
 * - Register
 * - Verification
 */
fun NavGraphBuilder.generalRoutes(
    navController: NavHostController,
    savedEmail: MutableState<String>,
    savedPassword: MutableState<String>
) {

    // ════════════════════════════════════════════
    // SPLASH SCREEN
    // ════════════════════════════════════════════
    composable("splash") {
        SplashScreen(navController)
    }

    // ════════════════════════════════════════════
    // LOGIN
    // ════════════════════════════════════════════
    composable("login") {
        LoginScreen(
            navController = navController,
            onLoginSuccess = { user ->
                Log.i("GeneralRoutes", "🔍 Usuario logueado: id=${user.id}, role=${user.role_id}")

                when (user.role_id) {
                    1 -> {
                        Log.i("GeneralRoutes", "✅ Rol 1 → UserHome")
                        navController.navigate("userHome/${user.id}") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                    2 -> {
                        Log.i("GeneralRoutes", "✅ Rol 2 → DashboardAdmin")
                        navController.navigate("DashboardAdmin") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                    3 -> {
                        Log.i("GeneralRoutes", "✅ Rol 3 → DashboardMod")
                        navController.navigate("DashboardMod") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                    4 -> {
                        Log.i("GeneralRoutes", "✅ Rol 4 → ReportSentList")
                        navController.navigate("ReportSentList") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                    else -> {
                        Log.e("GeneralRoutes", "❌ Rol desconocido: ${user.role_id}")
                    }
                }
            },
            onNavigateToRegister = {
                navController.navigate("register")
            }
        )
    }

    // ════════════════════════════════════════════
    // REGISTER
    // ════════════════════════════════════════════
    composable("register") {
        RegisterScreen(
            onNavigateToLogin = {
                navController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
            },
            onNavigateToVerification = { email, password ->
                savedEmail.value = email
                savedPassword.value = password
                navController.navigate("verification") {
                    popUpTo("register") { inclusive = true }
                }
            }
        )
    }

    // ════════════════════════════════════════════
    // VERIFICATION
    // ════════════════════════════════════════════
    composable("verification") {
        VerificationScreen(
            savedEmail = savedEmail.value,
            savedPassword = savedPassword.value,
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
}