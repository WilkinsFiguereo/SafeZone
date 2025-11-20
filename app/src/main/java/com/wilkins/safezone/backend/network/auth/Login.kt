<<<<<<<< HEAD:app/src/main/java/com/wilkins/safezone/backend/network/auth/Login.kt
package com.wilkins.safezone.backend.network.auth
========
package com.wilkins.alertaya.frontend.ui.network
>>>>>>>> ca56ce16a5f9ae9a4b46b712c16bb89abf6ea575:app/src/main/java/com/wilkins/alertaya/frontend/ui/network/Login.kt

import android.util.Log
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import android.content.Context
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.SupabaseService

suspend fun login(context: Context, email: String, password: String): AppUser? {
    val client = SupabaseService.getInstance()

    return try {
        // 🔐 Autenticación
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        // 🔄 Guardar sesión
        client.auth.currentSessionOrNull()?.let { session ->
            SessionManager.saveSession(context, session)
            Log.i("SupabaseLogin", "✅ Sesión guardada correctamente para ${session.user?.email}")
        } ?: Log.w("SupabaseLogin", "⚠️ No hay sesión activa tras login")

        // Obtener ID del usuario
        val userId = client.auth.currentUserOrNull()?.id
        if (userId == null) {
            Log.w("SupabaseLogin", "Login exitoso pero no se pudo obtener el ID del usuario.")
            return null
        }

        // Obtener perfil del usuario
        val user = client.postgrest
            .from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull<AppUser>()

        // 🔥 Guardar id y role_id del usuario
        if (user != null) {
            SessionManager.saveUserData(context, user)
        }

        user

    } catch (e: Exception) {
        Log.e("SupabaseLogin", "❌ Error durante login: ${e.message}", e)
        null
    }
}
