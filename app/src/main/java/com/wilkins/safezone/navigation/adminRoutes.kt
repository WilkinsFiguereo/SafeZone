package com.wilkins.safezone.navigation

import android.content.Context
import android.util.Log
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.wilkins.safezone.frontend.ui.Admin.Affair.AffairScreen
import com.wilkins.safezone.frontend.ui.Admin.Affair.IncidentCategoryScreen
import com.wilkins.safezone.frontend.ui.Admin.CrudUser.CreateUserScreen
import com.wilkins.safezone.frontend.ui.Admin.CrudUser.CrudUsuarios
import com.wilkins.safezone.frontend.ui.Admin.CrudUser.CrudUsuariosDisabled
import com.wilkins.safezone.frontend.ui.Admin.CrudUser.UserProfileCrud
import com.wilkins.safezone.frontend.ui.Admin.Dasbhoard.AdminDashboard
import com.wilkins.safezone.frontend.ui.screens.auth.AccountDisabledScreen

/**
 * 🛡️ RUTAS DE ADMINISTRADOR (Role ID: 2)
 * Requieren autenticación y rol admin
 *
 * Total: 8 rutas
 *
 * Incluye:
 * - accountDisabled (Gestión de cuentas suspendidas)
 * - DashboardAdmin (Panel principal)
 * - crudUsuarios (Usuarios activos)
 * - crudUsuariosDisabled (Usuarios deshabilitados)
 * - userProfileCrud (Perfil de usuario)
 * - CreateUserCrud (Crear usuario)
 * - affair_categories (Categorías de asuntos)
 * - incident_categories (Categorías de incidentes)
 */
fun NavGraphBuilder.adminRoutes(
    navController: NavHostController,
    context: Context,
    hasActiveSession: () -> Boolean
) {

    // ════════════════════════════════════════════
    // ACCOUNT DISABLED
    // ════════════════════════════════════════════
    composable(
        route = "accountDisabled/{statusId}",
        arguments = listOf(navArgument("statusId") { type = NavType.IntType })
    ) { backStackEntry ->
        val statusId = backStackEntry.arguments?.getInt("statusId") ?: 0
        Log.i("AdminRoutes", "🚫 Navegando a AccountDisabled con statusId: $statusId")

        AccountDisabledScreen(
            statusId = statusId,
            onBackToLogin = {
                navController.navigate("login") {
                    popUpTo("accountDisabled/{statusId}") { inclusive = true }
                }
            }
        )
    }

    // ════════════════════════════════════════════
    // DASHBOARD ADMIN
    // ════════════════════════════════════════════
    composable("DashboardAdmin") {
        if (!hasActiveSession()) {
            Log.w("AdminRoutes", "⚠️ Intento de acceso sin sesión a DashboardAdmin")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            AdminDashboard(navController)
        }
    }

    // ════════════════════════════════════════════
    // CRUD USUARIOS (ACTIVOS)
    // ════════════════════════════════════════════
    composable("crudUsuarios") {
        if (!hasActiveSession()) {
            Log.w("AdminRoutes", "⚠️ Intento de acceso sin sesión a crudUsuarios")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            CrudUsuarios(navController)
        }
    }

    // ════════════════════════════════════════════
    // CRUD USUARIOS (DESHABILITADOS)
    // ════════════════════════════════════════════
    composable("crudUsuariosDisabled") {
        if (!hasActiveSession()) {
            Log.w("AdminRoutes", "⚠️ Intento de acceso sin sesión a crudUsuariosDisabled")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            CrudUsuariosDisabled(navController)
        }
    }

    // ════════════════════════════════════════════
    // USER PROFILE CRUD
    // ════════════════════════════════════════════
    composable(
        route = "userProfileCrud/{uuid}",
        arguments = listOf(navArgument("uuid") { type = NavType.StringType })
    ) { backStackEntry ->
        if (!hasActiveSession()) {
            Log.w("AdminRoutes", "⚠️ Intento de acceso sin sesión a userProfileCrud")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val uuid = backStackEntry.arguments?.getString("uuid") ?: ""
            UserProfileCrud(userId = uuid, navController = navController)
        }
    }

    // ════════════════════════════════════════════
    // CREATE USER CRUD
    // ════════════════════════════════════════════
    composable("CreateUserCrud") {
        if (!hasActiveSession()) {
            Log.w("AdminRoutes", "⚠️ Intento de acceso sin sesión a CreateUserCrud")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            CreateUserScreen(navController)
        }
    }

    // ════════════════════════════════════════════
    // AFFAIR CATEGORIES
    // ════════════════════════════════════════════
    composable("affair_categories") {
        if (!hasActiveSession()) {
            Log.w("AdminRoutes", "⚠️ Intento de acceso sin sesión a affair_categories")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            AffairScreen(navController, context = context)
        }
    }

    // ════════════════════════════════════════════
    // INCIDENT CATEGORIES
    // ════════════════════════════════════════════
    composable("incident_categories") {
        if (!hasActiveSession()) {
            Log.w("AdminRoutes", "⚠️ Intento de acceso sin sesión a incident_categories")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            IncidentCategoryScreen(navController)
        }
    }
}