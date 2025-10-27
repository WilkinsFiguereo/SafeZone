package com.wilkins.alertaya.bridge.network


import com.wilkins.alertaya.backend.network.registerUser

object RegisterBridge {

    suspend fun handleRegister(
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
            val success = registerUser(name, email, password)
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
