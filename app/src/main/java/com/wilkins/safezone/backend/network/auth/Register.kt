package com.wilkins.safezone.backend.network.auth

import android.content.Context
import android.util.Log
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class Profile(
    val id: String,
    val name: String,
    val phone: String? = null,
    val photo_profile: String? = null,
    val role_id: Int = 1,
    val status_id: Int = 1
)

suspend fun registerUser(
    context: Context,
    name: String,
    email: String,
    password: String,
    phone: String? = null,
    photoProfile: String? = null
): Boolean {
    val supabase = SupabaseService.Companion.getInstance()

    return try {
        val metadata = buildJsonObject {
            put("name", name)
            phone?.let { put("phone", it) }
            photoProfile?.let { put("photo_profile", it) }
        }

        // 🚀 Registro con email y password
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            this.data = metadata
        }

        // 🔐 Persistir sesión automáticamente
        supabase.auth.currentSessionOrNull()?.let { session ->
            SessionManager.saveSession(context, session)
            Log.i("SupabaseRegister", "✅ Sesión guardada correctamente")
        } ?: Log.i("SupabaseRegister", "⚠️ No hay sesión activa (correo aún no confirmado)")

        true
    } catch (e: Exception) {
        Log.e("SupabaseRegister", "❌ Error durante el registro: ${e.message}", e)
        false
    }
}
