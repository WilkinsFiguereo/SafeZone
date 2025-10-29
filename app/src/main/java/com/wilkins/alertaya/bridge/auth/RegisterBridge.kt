package com.wilkins.alertaya.bridge.auth

import android.content.Context
import com.wilkins.alertaya.backend.network.registerUser

object RegisterBridge {

    suspend fun handleRegister(
        context: Context,
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Result<Boolean> {
        // Validaciones básicas antes de tocar el backend
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Todos los campos son obligatorios."))
        }

        if (password != confirmPassword) {
            return Result.failure(Exception("Las contraseñas no coinciden."))
        }

        return try {
            val success = registerUser(context, name, email, password)
            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("No se pudo registrar el usuario."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
