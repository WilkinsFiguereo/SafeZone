package com.wilkins.safezone.navigation

import androidx.compose.runtime.MutableState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.wilkins.safezone.GenericUserUi.SplashScreen
import com.wilkins.safezone.frontend.ui.auth.screens.Register.VerificationScreen
import com.wilkins.safezone.ui.theme.PrimaryColor
import com.wilkins.safezone.backend.network.SupabaseService
import android.util.Log
import com.wilkins.safezone.backend.network.auth.SessionManager
import com.wilkins.safezone.frontend.ui.auth.screens.AccountDisable.AccountStatusScreen
import com.wilkins.safezone.frontend.ui.auth.screens.AuthScreens.LoginScreen
import com.wilkins.safezone.frontend.ui.auth.screens.AuthScreens.RegisterScreen
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

            // 🔐 LOGIN NORMAL (email / password)
            onLoginSuccess = { user ->
                Log.i("GeneralRoutes", "🔐 Login normal: id=${user.id}, role=${user.role_id}")

                // Guardar status y rol para el Splash
                val context = navController.context

                SessionManager.saveUserStatus(context, user.status_id ?: 1)

                // ⛔ Si NO está verificado → Verification
                if (user.status_id != 1) {
                    Log.w("GeneralRoutes", "⚠️ Usuario no verificado → Verification")
                    navController.navigate("verification") {
                        popUpTo("login") { inclusive = true }
                    }
                    return@LoginScreen
                }


                if ((user.status_id ?: 0) in 2..3) {
                    navController.navigate("disable") {
                        popUpTo("login") { inclusive = true }
                    }
                    return@LoginScreen
                }


                // ✅ Usuario verificado → según rol
                navigateByRole(navController, user)
            },

            // 🔥 LOGIN CON GOOGLE
            onGoogleSignInSuccess = { user ->
                Log.i("GeneralRoutes", "🔥 Google Login exitoso: id=${user.id}, role=${user.role_id}")

                if ((user.status_id ?: 0) in 2..3) {
                    navController.navigate("disable") {
                        popUpTo("login") { inclusive = true }
                    }
                    return@LoginScreen
                }

                // 🚀 Google NUNCA pasa por verification
                navigateByRole(navController, user)
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

            // 🔥 REGISTRO NORMAL → VERIFICACIÓN
            onNavigateToVerification = { email, password ->
                savedEmail.value = email
                savedPassword.value = password

                navController.navigate("verification") {
                    popUpTo("register") { inclusive = true }
                }
            },

            // 🔥 REGISTRO CON GOOGLE → HOME DIRECTO
            onGoogleSignInSuccess = { user ->
                Log.i("GeneralRoutes", "✅ Registro con Google: ${user.email}")

                navigateByRole(navController, user)
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

    // ════════════════════════════════════════════
    // LOGIN DISABLE
    // ════════════════════════════════════════════
    composable("disable") {
        AccountStatusScreen(
            onNavigateBack = {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            },
            onLogoutComplete = {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

}