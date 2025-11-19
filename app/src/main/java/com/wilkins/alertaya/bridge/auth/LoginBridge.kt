package com.wilkins.alertaya.bridge.auth

import com.wilkins.alertaya.frontend.ui.network.AppUser
import com.wilkins.alertaya.frontend.ui.network.login
import android.content.Context


/**
 * Esta clase actúa como un puente entre la UI (Compose)
 * y la capa de red (Supabase, API, etc).
 */
object LoginBridge {

    /**
     * Llama al backend para autenticar al usuario.
     * Devuelve AppUser si el login fue exitoso, o null si falló.
     */
    suspend fun performLogin(context: Context, email: String, password: String): Result<AppUser> {
        return try {
            val user = login(context, email, password) // ✅ pasar context
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Credenciales inválidas o cuenta no verificada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}