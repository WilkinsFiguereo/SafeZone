package com.wilkins.alertaya.bridge.network

import com.wilkins.alertaya.backend.network.AppUser
import com.wilkins.alertaya.backend.network.login

/**
 * Esta clase actúa como un puente entre la UI (Compose)
 * y la capa de red (Supabase, API, etc).
 */
object LoginBridge {

    /**
     * Llama al backend para autenticar al usuario.
     * Devuelve AppUser si el login fue exitoso, o null si falló.
     */
    suspend fun performLogin(email: String, password: String): Result<AppUser> {
        return try {
            val user = login(email, password)
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