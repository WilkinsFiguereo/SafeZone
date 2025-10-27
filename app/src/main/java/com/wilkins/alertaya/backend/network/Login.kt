package com.wilkins.alertaya.backend.network

import android.util.Log
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.from

suspend fun login(email: String, password: String): AppUser? {
    val client = SupabaseService.getInstance()

    return try {
        // Autenticación
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        val userId = client.auth.currentUserOrNull()?.id
        if (userId == null) {
            Log.w("SupabaseLogin", "Login exitoso pero no se pudo obtener el ID del usuario.")
            return null
        }

        // Obtener perfil del usuario
        client.postgrest
            .from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull<AppUser>()

    } catch (e: Exception) {
        Log.e("SupabaseLogin", "Error durante login: ${e.message}", e)
        null
    }
}
