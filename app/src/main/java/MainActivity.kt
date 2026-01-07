package com.wilkins.safezone

import SessionManager.getUserProfile
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.frontend.Moderator.NewsSave.NewsSaveScreen
import com.wilkins.safezone.frontend.ui.Moderator.News.NewsListScreen
import com.wilkins.safezone.frontend.ui.Admin.CrudUser.CreateUserScreen
import com.wilkins.safezone.frontend.ui.Admin.CrudUser.CrudUsuarios
import com.wilkins.safezone.frontend.ui.Admin.CrudUser.UserProfileCrud
import com.wilkins.safezone.frontend.ui.Admin.Dasbhoard.AdminDashboard
import com.wilkins.safezone.frontend.ui.Map.GoogleMapScreen
import com.wilkins.safezone.frontend.ui.Moderator.Dashboard.ModeratorDashboard
import com.wilkins.safezone.frontend.ui.NavigationDrawer.NavigationDrawer
import com.wilkins.safezone.frontend.ui.NavigationDrawer.Profile
import com.wilkins.safezone.frontend.ui.NavigationDrawer.SettingsScreen
import com.wilkins.safezone.frontend.ui.screens.auth.LoginScreen
import com.wilkins.safezone.frontend.ui.screens.auth.RegisterScreen
import com.wilkins.safezone.frontend.ui.screens.auth.VerificationScreen
import com.wilkins.safezone.frontend.ui.user.Form.FormScreen
import com.wilkins.safezone.frontend.ui.user.Homepage.UserHomeScreen
import com.wilkins.safezone.frontend.ui.user.News.NewsScreen
import com.wilkins.safezone.frontend.ui.user.Notification.Notification
import com.wilkins.safezone.frontend.ui.user.Notification.NotificationsScreen
// 📋 IMPORTACIONES DE ENCUESTAS (MODERADOR)
import com.wilkins.safezone.frontend.ui.Moderator.SurveyListScreen
import com.wilkins.safezone.frontend.ui.Moderator.SurveyCreateScreen
import com.wilkins.safezone.frontend.ui.Moderator.SurveyResultsScreen
// 📋 IMPORTACIONES DE ENCUESTAS (USUARIO)
import com.wilkins.safezone.frontend.ui.User.Survey.UserSurveyListScreen
import com.wilkins.safezone.frontend.ui.User.Survey.UserSurveyAnswerScreen
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
                    val context = LocalContext.current
                    val userState = produceState<AppUser?>(initialValue = null) {
                        value = getUserProfile(context)
                    }

                    val user = userState.value

                    fun hasActiveSession(): Boolean {
                        val session = SessionManager.loadSession(context)
                        val hasSession = session != null
                        Log.i("MainActivity", "🔐 Verificando sesión: ${if (hasSession) "Activa" else "Inactiva"}")
                        return hasSession
                    }

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
                                    Log.i("MainActivity", "🔓 Usuario logueado: id=${user.id}, role=${user.role_id}")

                                    when (user.role_id) {
                                        1 -> {
                                            Log.i("MainActivity", "✅ Rol 1 → UserHome")
                                            navController.navigate("userHome/${user.id}") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                        2 -> {
                                            Log.i("MainActivity", "✅ Rol 2 → DashboardAdmin")
                                            navController.navigate("DashboardAdmin") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                        3 -> {
                                            Log.i("MainActivity", "✅ Rol 3 → DashboardMod")
                                            navController.navigate("DashboardMod") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                        else -> {
                                            Log.e("MainActivity", "❌ Rol desconocido: ${user.role_id}")
                                        }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                }
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
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a userHome")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                val supabaseClient = SupabaseService.getInstance()
                                UserHomeScreen(navController, context, supabaseClient)
                            }
                        }

                        composable("DashboardAdmin") {
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a DashboardAdmin")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                AdminDashboard(navController)
                            }
                        }

                        composable("crudUsuarios") {
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a crudUsuarios")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                CrudUsuarios(navController)
                            }
                        }

                        composable("navigationDrawer") {
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a navigationDrawer")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                val supabaseClient = SupabaseService.getInstance()
                                NavigationDrawer(navController, context, supabaseClient)
                            }
                        }

                        composable("profile") {
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a profile")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                val supabaseClient = SupabaseService.getInstance()
                                Profile(navController, context, supabaseClient, )
                            }
                        }

                        composable("settings") {
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a settings")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                SettingsScreen(
                                    navcontroller = navController,
                                    onBackClick = {
                                        navController.navigate("navigationDrawer") {
                                            popUpTo("settings") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }

                        composable(
                            route = "userProfileCrud/{uuid}",
                            arguments = listOf(navArgument("uuid") { type = NavType.StringType })
                        ) { backStackEntry ->
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a userProfileCrud")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                val uuid = backStackEntry.arguments?.getString("uuid") ?: ""
                                UserProfileCrud(userId = uuid, navController = navController)
                            }
                        }

                        composable("CreateUserCrud") {
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a CreateUserCrud")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                CreateUserScreen(navController)
                            }
                        }

                        composable("NewsUser") {
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a NewsUser")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                val supabase = SupabaseService.getInstance()
                                val userId = supabase.auth.currentUserOrNull()?.id ?: ""
                                val supabaseClient = SupabaseService.getInstance()
                                NewsScreen(navController, userId = userId, context, supabaseClient)
                            }
                        }

                        composable("Notification") {
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a Notification")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                val supabaseClient = SupabaseService.getInstance()
                                NotificationsScreen(navController, context, supabaseClient)
                            }
                        }

                        composable("FormUser") {
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a FormUser")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                val supabaseClient = SupabaseService.getInstance()
                                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: ""
                                FormScreen(navController, userId, user?.name ?: "Usuario", supabaseClient)
                            }
                        }

                        composable("SaveNews") {
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a SaveNews")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                NewsSaveScreen(onNavigateBack = { navController.popBackStack() })
                            }
                        }

                        composable("news_list") {
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a news_list")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                NewsListScreen(onNavigateBack = { navController.popBackStack() })
                            }
                        }

                        composable("DashboardMod") {
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a DashboardMod")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                val supabaseClient = SupabaseService.getInstance()

                                ModeratorDashboard(
                                    navController = navController,
                                    moderatorId = "MOD001",
                                    context = context,
                                    supabaseClient = supabaseClient
                                )
                            }
                        }

                        // ╔════════════════════════════════════════╗
                        // 📋 RUTAS DE ENCUESTAS (MODERADOR)
                        // ╚════════════════════════════════════════╝

                        composable("moderatorPollList") {
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a moderatorPollList")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                SurveyListScreen(navController = navController)
                            }
                        }

                        composable("moderatorCreatePoll") {
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a moderatorCreatePoll")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                SurveyCreateScreen(navController = navController)
                            }
                        }

                        composable(
                            route = "moderatorPollResults/{surveyId}",
                            arguments = listOf(navArgument("surveyId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a moderatorPollResults")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                val surveyId = backStackEntry.arguments?.getString("surveyId") ?: ""
                                SurveyResultsScreen(
                                    navController = navController,
                                    surveyId = surveyId
                                )
                            }
                        }

                        // ╔════════════════════════════════════════╗
                        // 📝 RUTAS DE ENCUESTAS (USUARIO)
                        // ╚════════════════════════════════════════╝

                        composable("userSurveys") {
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a userSurveys")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                UserSurveyListScreen(navController = navController)
                            }
                        }

                        composable(
                            route = "userSurveyAnswer/{surveyId}",
                            arguments = listOf(navArgument("surveyId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            if (!hasActiveSession()) {
                                Log.w("MainActivity", "⚠️ Intento de acceso sin sesión a userSurveyAnswer")
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                val surveyId = backStackEntry.arguments?.getString("surveyId") ?: ""
                                val supabase = SupabaseService.getInstance()
                                val userId = supabase.auth.currentUserOrNull()?.id ?: ""
                                UserSurveyAnswerScreen(
                                    navController = navController,
                                    surveyId = surveyId,
                                    userId = userId
                                )
                            }
                        }
                    }
                }
            }
        }
    }

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