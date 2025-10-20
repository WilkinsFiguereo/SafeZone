package com.wilkins.alertaya.backend.network

import android.util.Log
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.from
import com.wilkins.alertaya.backend.network.AppUser // <-- Importa tu data class existente

suspend fun login(email: String, password: String): AppUser? {
    val client = SupabaseService.getInstance()

    try {
        // --- PASO 1: Autenticar al usuario ---
        println("Intentando login para el email: $email")
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        val userId = client.auth.currentUserOrNull()?.id
        if (userId == null) {
            Log.w("SupabaseLogin", "Login exitoso pero no se pudo obtener el ID del usuario.")
            return null
        }

        println("Login exitoso. ID de usuario: $userId")

        // --- PASO 2: Obtener perfil del usuario ---
        val appUser = client.postgrest
            .from("profiles")
            .select {
                filter { eq("id", userId) }
            }
            .decodeSingleOrNull<AppUser>()

        if (appUser == null) {
            Log.w("SupabaseLogin", "Usuario autenticado pero no se encontró el perfil.")
            return null
        }

        println("Perfil obtenido: $appUser")

        // --- PASO 3: Verificar role ---
        when (appUser.role_id) {
            1 -> HomeUserScreen(appUser)
            2 -> HomeAdminScreen(appUser)
            else -> Log.w("SupabaseLogin", "Rol desconocido o no asignado.")
        }

        return appUser

    } catch (e: Exception) {
        Log.e("SupabaseLogin", "Error durante login: ${e.message}", e)
        return null
    }
}

// Funciones de navegación de ejemplo
fun HomeUserScreen(user: AppUser) {
    println("🏠 Bienvenido ${user.name} a la pantalla de usuario")
}

fun HomeAdminScreen(user: AppUser) {
    println("🏠 Bienvenido ${user.name} a la pantalla de administrador")
}
