package com.wilkins.safezone.navigation

import android.content.Context
import android.util.Log
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.frontend.ui.Moderator.Dashboard.ModeratorDashboard

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
}