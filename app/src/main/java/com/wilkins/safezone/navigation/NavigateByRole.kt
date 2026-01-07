package com.wilkins.safezone.navigation

import android.util.Log
import androidx.navigation.NavHostController
import com.wilkins.safezone.backend.network.AppUser

fun navigateByRole(
    navController: NavHostController,
    user: AppUser
) {
    when (user.role_id) {
        1 -> {
            navController.navigate("userHome/${user.id}") {
                popUpTo("login") { inclusive = true }
            }
        }
        2 -> {
            navController.navigate("DashboardAdmin") {
                popUpTo("login") { inclusive = true }
            }
        }
        3 -> {
            navController.navigate("DashboardMod") {
                popUpTo("login") { inclusive = true }
            }
        }
        4 -> {
            navController.navigate("ReportSentList") {
                popUpTo("login") { inclusive = true }
            }
        }
        else -> {
            Log.e("GeneralRoutes", "❌ Rol desconocido: ${user.role_id}")
        }
    }
}
