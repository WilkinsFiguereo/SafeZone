package com.wilkins.safezone.navigation

import android.content.Context
import android.util.Log
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.frontend.ui.Moderator.screens.ReviewReports.ReportDetail.ReportStatusScreen
import com.wilkins.safezone.frontend.ui.Moderator.Dashboard.ModeratorDashboard
import com.wilkins.safezone.frontend.ui.Moderator.screens.ReviewReports.ReportsList.RewiewReportsListScreen

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
}