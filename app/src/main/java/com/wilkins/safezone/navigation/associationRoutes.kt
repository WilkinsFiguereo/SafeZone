package com.wilkins.safezone.navigation

import android.util.Log
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.wilkins.safezone.frontend.ui.GlobalAssociation.ReportSent.PendingReportsScreen
import com.wilkins.safezone.frontend.ui.GlobalAssociation.ReportSent.ReportDetailScreen
import com.wilkins.safezone.frontend.ui.GlobalAssociation.ReportSent.ReportsCancelledScreen
import com.wilkins.safezone.frontend.ui.GlobalAssociation.ReportSent.ReportsCompletedScreen
import com.wilkins.safezone.frontend.ui.GlobalAssociation.ReportSent.ReportsProgressScreen
import com.wilkins.safezone.frontend.ui.GlobalAssociation.ReportSent.ReportsSentScreen

/**
 * 🏢 RUTAS DE ASOCIACIÓN (Role ID: 4)
 * Requieren autenticación y rol asociación
 *
 * Total: 6 rutas
 *
 * Estas rutas también están disponibles para el moderador (Role ID: 3)
 *
 * Incluye:
 * - report_detail (Detalle de reporte)
 * - ReportSentList (Lista principal)
 * - PendingReports (Status 1: Pendientes)
 * - ReportsProgress (Status 2: En progreso)
 * - ReportsCompleted (Status 3: Completados)
 * - ReportsCancelled (Status 4: Cancelados)
 */
fun NavGraphBuilder.associationRoutes(
    navController: NavHostController,
    hasActiveSession: () -> Boolean
) {

    // ════════════════════════════════════════════
    // REPORT DETAIL
    // ════════════════════════════════════════════
    composable("report_detail/{reportId}") { backStackEntry ->
        if (!hasActiveSession()) {
            Log.w("AssociationRoutes", "⚠️ Intento de acceso sin sesión a report_detail")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val reportId = backStackEntry.arguments?.getString("reportId") ?: return@composable
            ReportDetailScreen(navController = navController, reportId)
        }
    }

    // ════════════════════════════════════════════
    // REPORT SENT LIST (Lista principal)
    // ════════════════════════════════════════════
    composable("ReportSentList") {
        if (!hasActiveSession()) {
            Log.w("AssociationRoutes", "⚠️ Intento de acceso sin sesión a ReportSentList")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            ReportsSentScreen(navController = navController)
        }
    }

    // ════════════════════════════════════════════
    // PENDING REPORTS (Status: 1)
    // ════════════════════════════════════════════
    composable("PendingReports") {
        if (!hasActiveSession()) {
            Log.w("AssociationRoutes", "⚠️ Intento de acceso sin sesión a PendingReports")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            PendingReportsScreen(
                navController = navController,
                initialStatusId = 1
            )
        }
    }

    // ════════════════════════════════════════════
    // REPORTS IN PROGRESS (Status: 2)
    // ════════════════════════════════════════════
    composable("ReportsProgress") {
        if (!hasActiveSession()) {
            Log.w("AssociationRoutes", "⚠️ Intento de acceso sin sesión a ReportsProgress")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            ReportsProgressScreen(
                navController = navController,
                initialStatusId = 1
            )
        }
    }

    // ════════════════════════════════════════════
    // REPORTS COMPLETED (Status: 3)
    // ════════════════════════════════════════════
    composable("ReportsCompleted") {
        if (!hasActiveSession()) {
            Log.w("AssociationRoutes", "⚠️ Intento de acceso sin sesión a ReportsCompleted")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            ReportsCompletedScreen(
                navController = navController,
                initialStatusId = 3
            )
        }
    }

    // ════════════════════════════════════════════
    // REPORTS CANCELLED (Status: 4)
    // ════════════════════════════════════════════
    composable("ReportsCancelled") {
        if (!hasActiveSession()) {
            Log.w("AssociationRoutes", "⚠️ Intento de acceso sin sesión a ReportsCancelled")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            ReportsCancelledScreen(
                navController = navController,
                initialStatusId = 4
            )
        }
    }
}