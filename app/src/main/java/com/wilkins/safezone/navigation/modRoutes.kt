package com.wilkins.safezone.navigation

import android.content.Context
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.auth.SessionManager
import com.wilkins.safezone.frontend.ui.Moderator.screens.ReviewReports.ReportDetail.ReportStatusScreen
import com.wilkins.safezone.frontend.ui.Moderator.Dashboard.ModeratorDashboard
import com.wilkins.safezone.frontend.ui.Moderator.ModeratorUsersScreen
import com.wilkins.safezone.frontend.ui.Moderator.screens.ModeratorUser.ModeratorBlockedUsersScreen
import com.wilkins.safezone.frontend.ui.Moderator.screens.News.NewsListScreen
import com.wilkins.safezone.frontend.ui.Moderator.screens.News.NewsSaveScreen
import com.wilkins.safezone.frontend.ui.Moderator.screens.ReviewReports.ReportsList.RewiewReportsListScreen
import com.wilkins.safezone.frontend.ui.Moderator.screens.Statistics.ModeratorStatisticsScreen
import com.wilkins.safezone.frontend.ui.Moderator.screens.Survey.SurveyResultsScreen

/**
 * ⚙️ RUTAS DE MODERADOR (Role ID: 3)
 * Requieren autenticación y rol moderador
 *
 * Total: 1 ruta principal
 *
 * Nota: El moderador puede acceder también a las rutas de asociación
 * para gestionar reportes (ver AssociationRoutes.kt)
 *
 * Incluye:
 * - DashboardMod (Panel principal del moderador)
 */
fun NavGraphBuilder.moderatorRoutes(
    navController: NavHostController,
    context: Context,
    hasActiveSession: () -> Boolean
) {

    // ════════════════════════════════════════════
    // DASHBOARD MODERATOR
    // ════════════════════════════════════════════
    composable("DashboardMod") {
        if (!hasActiveSession()) {
            Log.w("ModeratorRoutes", "⚠️ Intento de acceso sin sesión a DashboardMod")
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

    composable("report_review_detail/{reportId}") { backStackEntry ->
        if (!hasActiveSession()) {
            Log.w("ModRoutes", "⚠️ Intento de acceso sin sesión a report_detail")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val reportId = backStackEntry.arguments?.getString("reportId") ?: return@composable
            val supabaseClient = SupabaseService.getInstance()
            ReportStatusScreen(navController = navController, reportId = reportId, moderatorId = "MOD001",  supabaseClient = supabaseClient )
        }
    }

    composable("ReportReviewList") {
        if (!hasActiveSession()) {
            Log.w("ModRoutes", "⚠️ Intento de acceso sin sesión a ReportSentList")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val supabaseClient = SupabaseService.getInstance()
            RewiewReportsListScreen(navController = navController, initialStatusId = 5, moderatorId = "MOD001", moderatorName = "Moderador", supabaseClient = supabaseClient)
        }
    }

    // ════════════════════════════════════════════
    // News save
    // ════════════════════════════════════════════
    composable("newsSave") {
        if (!hasActiveSession()) {
            Log.w("ModeratorRoutes", "⚠️ Intento de acceso sin sesión a DashboardMod")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {

            NewsSaveScreen(onNavigateBack = { navController.popBackStack() })
        }
    }

    // ════════════════════════════════════════════
    // News save
    // ════════════════════════════════════════════
    composable("newsList") {
        if (!hasActiveSession()) {
            Log.w("ModeratorRoutes", "⚠️ Intento de acceso sin sesión a DashboardMod")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {

            NewsListScreen(onNavigateBack = { navController.popBackStack() })
        }
    }

    // ════════════════════════════════════════════
    // Survery
    // ════════════════════════════════════════════
//    composable("survery") {
//        if (!hasActiveSession()) {
//            Log.w("ModeratorRoutes", "⚠️ Intento de acceso sin sesión a DashboardMod")
//            navController.navigate("login") {
//                popUpTo(0) { inclusive = true }
//            }
//        } else {
//
//            SurveyResultsScreen(navController = navController)
//        }
//    }

    // ════════════════════════════════════════════
    // Moderator User
    // ════════════════════════════════════════════

    composable("moderatorUser") {
        if (!hasActiveSession()) {
            Log.w("ModeratorRoutes", "⚠️ Intento de acceso sin sesión a DashboardMod")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val userId = SessionManager
                .loadSession(context)
                ?.user
                ?.id

            if (userId == null) {
                Log.e("ModeratorRoutes", "❌ userId nulo, redirigiendo a login")
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
                return@composable
            }

            ModeratorUsersScreen(navController = navController, moderatorId = userId)
        }
    }

    composable("moderatorUserDisable") {
        if (!hasActiveSession()) {
            Log.w("ModeratorRoutes", "⚠️ Intento de acceso sin sesión a DashboardMod")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val userId = SessionManager
                .loadSession(context)
                ?.user
                ?.id

            if (userId == null) {
                Log.e("ModeratorRoutes", "❌ userId nulo, redirigiendo a login")
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
                return@composable
            }

            ModeratorBlockedUsersScreen(navController = navController, moderatorId = userId)
        }
    }

    // ════════════════════════════════════════════
    // Moderator Statistics
    // ════════════════════════════════════════════

    composable("moderatorStatistics") {
        if (!hasActiveSession()) {
            Log.w("ModeratorRoutes", "⚠️ Intento de acceso sin sesión a DashboardMod")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val userId = SessionManager
                .loadSession(context)
                ?.user
                ?.id

            if (userId == null) {
                Log.e("ModeratorRoutes", "❌ userId nulo, redirigiendo a login")
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
                return@composable
            }

            val supabaseClient = SupabaseService.getInstance()
            ModeratorStatisticsScreen(navController = navController, moderatorId = userId, context = context, supabaseClient = supabaseClient)
        }
    }
}