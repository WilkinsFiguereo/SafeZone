package com.wilkins.safezone.navigation

import android.content.Context
import android.util.Log
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.Moderator.News.NewsViewModel
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.frontend.ui.Map.GoogleMapScreen
import com.wilkins.safezone.frontend.ui.Moderator.screens.Survey.SurveyCreateScreen
import com.wilkins.safezone.frontend.ui.user.Screens.Form.FormScreen
import com.wilkins.safezone.frontend.ui.user.Screens.Form.ReportResultScreen
import com.wilkins.safezone.frontend.ui.user.Screens.Homepage.UserHomeScreen
import com.wilkins.safezone.frontend.ui.user.Screens.NavigationDrawer.NavigationDrawer
import com.wilkins.safezone.frontend.ui.user.Screens.NavigationDrawer.Profile
import com.wilkins.safezone.frontend.ui.user.Screens.NavigationDrawer.SettingsScreen
import com.wilkins.safezone.frontend.ui.user.Screens.News.NewsDetailScreen
import com.wilkins.safezone.frontend.ui.user.Screens.News.NewsScreen
import com.wilkins.safezone.frontend.ui.user.Screens.Notification.NotificationsScreen
import com.wilkins.safezone.frontend.ui.user.Screens.RecordResports.ReportHistoryScreen
import com.wilkins.safezone.frontend.ui.user.Screens.Survey.SurveyDetailScreen
import com.wilkins.safezone.frontend.ui.user.Screens.Survey.SurveysScreen
import com.wilkins.safezone.frontend.ui.user.Screens.Survey.UserSurveyAnswerScreen
import com.wilkins.safezone.frontend.ui.user.Screens.Survey.UserSurveyListScreen
import com.wilkins.safezone.frontend.ui.user.Screens.profile.ProfileScreenWithMenu
import io.github.jan.supabase.gotrue.auth

/**
 * 👤 RUTAS DE USUARIO (Role ID: 1)
 * Requieren autenticación
 *
 * Total: 10 rutas
 *
 * Incluye:
 * - userHome
 * - navigationDrawer
 * - profile
 * - MyProfile
 * - settings
 * - NewsUser
 * - Notification
 * - FormUser
 * - MapReports
 * - RecordReports
 */
fun NavGraphBuilder.userRoutes(
    navController: NavHostController,
    context: Context,
    user: AppUser?,
    hasActiveSession: () -> Boolean
) {

    // ════════════════════════════════════════════
    // USER HOME
    // ════════════════════════════════════════════
    composable("userHome/{userId}") {
        if (!hasActiveSession()) {
            Log.w("UserRoutes", "⚠️ Intento de acceso sin sesión a userHome")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val supabaseClient = SupabaseService.getInstance()
            UserHomeScreen(navController, context, supabaseClient)
        }
    }

    // ════════════════════════════════════════════
    // NAVIGATION DRAWER
    // ════════════════════════════════════════════
    composable("navigationDrawer") {
        if (!hasActiveSession()) {
            Log.w("UserRoutes", "⚠️ Intento de acceso sin sesión a navigationDrawer")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val supabaseClient = SupabaseService.getInstance()
            NavigationDrawer(navController, context, supabaseClient)
        }
    }

    // ════════════════════════════════════════════
    // PROFILE
    // ════════════════════════════════════════════
    composable("profile") {
        if (!hasActiveSession()) {
            Log.w("UserRoutes", "⚠️ Intento de acceso sin sesión a profile")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val supabaseClient = SupabaseService.getInstance()
            Profile(navController, context, supabaseClient)
        }
    }

    // ════════════════════════════════════════════
    // MY PROFILE
    // ════════════════════════════════════════════
    composable("MyProfile") {
        if (!hasActiveSession()) {
            Log.w("UserRoutes", "⚠️ Intento de acceso sin sesión a MyProfile")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val supabase = SupabaseService.getInstance()
            val supabaseClient = SupabaseService.getInstance()
            val userId = supabase.auth.currentUserOrNull()?.id ?: ""
            val userName = user?.name ?: "Usuario"

            ProfileScreenWithMenu(
                userId = userId,
                userName = userName,
                navController = navController,
                supabaseClient = supabaseClient,
                onNavigateToChangePassword = {
                    navController.navigate("change_password")
                },
                onNavigateToChangeEmail = {
                    navController.navigate("change_email")
                }
            )
        }
    }

    composable("profileUser/{userId}/{userName}") { backStackEntry ->
        val userId = backStackEntry.arguments?.getString("userId") ?: ""
        val encodedUserName = backStackEntry.arguments?.getString("userName") ?: ""

        // Decodificar el userName
        val userName = try {
            java.net.URLDecoder.decode(encodedUserName, "UTF-8")
        } catch (e: Exception) {
            "Usuario"
        }

        if (!hasActiveSession()) {
            Log.w("UserRoutes", "⚠️ Intento de acceso sin sesión a Profile")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val supabaseClient = SupabaseService.getInstance()

            ProfileScreenWithMenu(
                userId = userId,
                userName = userName, // ⬅️ userName decodificado
                navController = navController,
                supabaseClient = supabaseClient,
                onNavigateToChangePassword = {
                    navController.navigate("change_password")
                },
                onNavigateToChangeEmail = {
                    navController.navigate("change_email")
                }
            )
        }
    }

    // ════════════════════════════════════════════
    // SETTINGS
    // ════════════════════════════════════════════
    composable("settings") {
        if (!hasActiveSession()) {
            Log.w("UserRoutes", "⚠️ Intento de acceso sin sesión a settings")
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

    // ════════════════════════════════════════════
    // NEWS USER
    // ════════════════════════════════════════════
    composable("NewsUser") {
        if (!hasActiveSession()) {
            Log.w("UserRoutes", "⚠️ Intento de acceso sin sesión a NewsUser")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val supabase = SupabaseService.getInstance()
            val userId = supabase.auth.currentUserOrNull()?.id ?: ""
            val supabaseClient = SupabaseService.getInstance()
            val viewModel: NewsViewModel = viewModel()
            NewsScreen(navController, userId = userId, context, supabaseClient, viewModel )
        }
    }

    composable("news_detail/{newsId}") { backStackEntry ->
        if (!hasActiveSession()) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val newsId = backStackEntry.arguments?.getString("newsId") ?: return@composable

            // ⚠️ IMPORTANTE: Obtener el ViewModel del backstack de "news"
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("NewsUser")
            }
            val viewModel: NewsViewModel = viewModel(parentEntry)  // ✅ Compartir ViewModel

            NewsDetailScreen(
                navController = navController,
                newsId = newsId,
                viewModel = viewModel  // ✅ Mismo ViewModel que NewsScreen
            )
        }
    }




    // ════════════════════════════════════════════
    // NOTIFICATIONS
    // ════════════════════════════════════════════
    composable("Notification") {
        if (!hasActiveSession()) {
            Log.w("UserRoutes", "⚠️ Intento de acceso sin sesión a Notification")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val supabaseClient = SupabaseService.getInstance()
            NotificationsScreen(navController, context, supabaseClient)
        }
    }

    // ════════════════════════════════════════════
    // FORM USER
    // ════════════════════════════════════════════
    composable("FormUser") {
        if (!hasActiveSession()) {
            Log.w("UserRoutes", "⚠️ Intento de acceso sin sesión a FormUser")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val supabaseClient = SupabaseService.getInstance()
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: ""
            FormScreen(navController, userId, user?.name ?: "Usuario", supabaseClient)
        }
    }

    composable(
        route = "reportResult/{userId}/{isSuccess}/{message}",
        arguments = listOf(
            navArgument("userId") { type = NavType.StringType },
            navArgument("isSuccess") { type = NavType.BoolType },
            navArgument("message") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val userId = backStackEntry.arguments?.getString("userId") ?: ""
        val isSuccess = backStackEntry.arguments?.getBoolean("isSuccess") ?: false
        val message = backStackEntry.arguments?.getString("message") ?: ""

        ReportResultScreen(
            navController = navController,
            userId = userId,
            isSuccess = isSuccess,
            message = message
        )
    }

    // ════════════════════════════════════════════
    // MAP REPORTS
    // ════════════════════════════════════════════
    composable("MapReports") {
        if (!hasActiveSession()) {
            Log.w("UserRoutes", "⚠️ Intento de acceso sin sesión a MapReports")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            GoogleMapScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                navController = navController
            )
        }
    }

    // ════════════════════════════════════════════
    // RECORD REPORTS
    // ════════════════════════════════════════════
    composable(
        route = "RecordReports/{userId}",
        arguments = listOf(navArgument("userId") { type = NavType.StringType })
    ) { backStackEntry ->
        if (!hasActiveSession()) {
            Log.w("UserRoutes", "⚠️ Intento de acceso sin sesión a RecordReports")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val supabaseClient = SupabaseService.getInstance()
            ReportHistoryScreen(
                userId = userId,
                navController = navController,
                context = context,
                supabaseClient = supabaseClient
            )
        }
    }

    // ════════════════════════════════════════════
    // Survey
    // ════════════════════════════════════════════
    // 🔹 Ruta para la lista de encuestas
    composable("SurveyUser") {
        if (!hasActiveSession()) {
            Log.w("UserRoutes", "⚠️ Intento de acceso sin sesión a surveys")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            SurveysScreen(
                navController = navController,
                currentRoute = "SurveyUser"  // ✅ Nombre de la ruta actual
            )
        }
    }

// 🔹 Ruta para el detalle de encuesta (CON PARÁMETRO)
    composable(
        route = "SurveyDetail/{surveyId}",  // ✅ Definir parámetro en la ruta
        arguments = listOf(
            navArgument("surveyId") {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        if (!hasActiveSession()) {
            Log.w("UserRoutes", "⚠️ Intento de acceso sin sesión a survey detail")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val surveyId = backStackEntry.arguments?.getString("surveyId") ?: ""  // ✅ Extraer el parámetro
            SurveyDetailScreen(
                navController = navController,
                surveyId = surveyId
            )
        }
    }

    composable("userSurveyList") {
        if (!hasActiveSession()) {
            Log.w("UserRoutes", "⚠️ Intento de acceso sin sesión a profile")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            UserSurveyListScreen(navController)
        }
    }

    composable("surveyAnswer") { backStackEntry ->

        if (!hasActiveSession()) {
            Log.w("ModeratorRoutes", "⚠️ Intento de acceso sin sesión a DashboardMod")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            return@composable
        }

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