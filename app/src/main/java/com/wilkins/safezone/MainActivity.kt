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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wilkins.safezone.GenericUserUi.SplashScreen
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.frontend.ui.Admin.CrudUser.CreateUserScreen
import com.wilkins.safezone.frontend.ui.Admin.CrudUser.CrudUsuarios
import com.wilkins.safezone.frontend.ui.Admin.CrudUser.UserProfileCrud
import com.wilkins.safezone.frontend.ui.Admin.Dasbhoard.AdminDashboard
import com.wilkins.safezone.frontend.ui.NavigationDrawer.NavigationDrawer
import com.wilkins.safezone.frontend.ui.NavigationDrawer.Profile
import com.wilkins.safezone.frontend.ui.NavigationDrawer.SettingsScreen
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

                        // 🔐 LOGIN
                        composable("login") {
                            LoginScreen(
                                navController = navController,
                                onLoginSuccess = { user ->
                                    when (user.role_id) {
                                        2 -> { // 🔹 Admin
                                            navController.navigate("DashboardAdmin") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                        1 -> { // 🔹 user
                                            navController.navigate("userHome/${user.id}") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                    }
                                },
                                onNavigateToRegister = { navController.navigate("register") }
                            )
                        }

                        // 📝 REGISTRO
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

                        // ✅ VERIFICACIÓN
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

                        // 👤 PANTALLA DE USUARIO
                        composable("userHome/{userId}") {
                            UserHomeScreen(navController)
                        }

                        // ⚙️ ADMIN - CRUD DE USUARIOS
                        composable("crudUsuarios") {
                            CrudUsuarios(navController)
                        }

                        // 🧭 Navigation Drawer
                        composable("navigationDrawer") {
                            val context = LocalContext.current
                            val supabaseClient = SupabaseService.getInstance()
                            NavigationDrawer(navController, context, supabaseClient)
                        }

                        // 👤 PERFIL
                        composable("profile") { Profile(navController) }

                        // ⚙️ CONFIGURACIÓN
                        composable("settings") {
                            SettingsScreen(
                                navcontroller = navController,
                                onBackClick = {
                                    navController.navigate("navigationDrawer") {
                                        popUpTo("settings") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 📋 PERFIL DETALLE DE USUARIO (Admin)
                        composable(
                            route = "userProfileCrud/{uuid}",
                            arguments = listOf(navArgument("uuid") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val uuid = backStackEntry.arguments?.getString("uuid") ?: ""
                            UserProfileCrud(userId = uuid, navController = navController)
                        }

                        composable("CreateUserCrud"){
                            CreateUserScreen(navController)
                        }

                        composable("DashboardAdmin"){
                            AdminDashboard(navController)
                        }
                    }
                }
            }
        }
    }

    // 🔹 Pantalla completa
    private fun enableFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

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

@Composable
fun FullScreenTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    val window = (view.context as? ComponentActivity)?.window

    DisposableEffect(Unit) {
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(
                WindowInsetsCompat.Type.statusBars() or
                        WindowInsetsCompat.Type.navigationBars()
            )
        }
        onDispose {}
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        content()
    }
}
