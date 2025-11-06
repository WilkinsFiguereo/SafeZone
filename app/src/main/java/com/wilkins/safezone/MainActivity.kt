package com.wilkins.safezone

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wilkins.safezone.GenericUserUi.SplashScreen
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.frontend.ui.NavigationDrawer.NavigationDrawer
import com.wilkins.safezone.frontend.ui.NavigationDrawer.Profile
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

        // 🔹 Activa pantalla completa antes de setContent
        enableFullScreen()

        setContent {
            SafeZoneTheme {
                // 🔹 Aplica el modo pantalla completa a toda la app
                FullScreenTheme {
                    val navController = rememberNavController()
                    var savedEmail by remember { mutableStateOf("") }
                    var savedPassword by remember { mutableStateOf("") }

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashScreen(navController)
                        }
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
                        composable("userHome/{userId}") {
                            UserHomeScreen(navController)
                        }
                        composable("adminHome/{userId}") {
                            // AdminHomeScreen()
                        }
                        composable("NavigationDrawer") {
                            val context = LocalContext.current
                            val supabaseClient = SupabaseService.getInstance()
                            NavigationDrawer(navController, context, supabaseClient)
                        }

                        composable("profile") { Profile(navController) }
                    }
                }
            }
        }
    }

    // 🔹 Función para habilitar pantalla completa (compatible con API 24+)
    private fun enableFullScreen() {
        // Configura la ventana para que no ajuste el contenido a las system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Configura colores transparentes para las barras del sistema
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // Para dispositivos con notch (muesca) - API 28+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // Enfoque legacy para versiones anteriores a API 30
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                            android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )
        }
    }
}

// 🔹 Composable que aplica el modo pantalla completa (compatible con API 24+)
@Composable
fun FullScreenTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    val window = (view.context as? ComponentActivity)?.window

    DisposableEffect(Unit) {
        if (window != null) {
            val windowInsetsController = WindowCompat.getInsetsController(window, view)

            // Configura el comportamiento de las barras del sistema
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            // 🔹 Oculta las barras del sistema usando WindowInsetsCompat
            // IMPORTANTE: Siempre usar WindowInsetsCompat.Type, no android.view.WindowInsets.Type
            windowInsetsController.hide(
                WindowInsetsCompat.Type.statusBars() or
                        WindowInsetsCompat.Type.navigationBars()
            )

            onDispose {
                // Opcional: Mostrar las barras cuando se destruye
                // windowInsetsController.show(
                //     WindowInsetsCompat.Type.statusBars() or
                //     WindowInsetsCompat.Type.navigationBars()
                // )
            }
        } else {
            onDispose {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        content()
    }
}