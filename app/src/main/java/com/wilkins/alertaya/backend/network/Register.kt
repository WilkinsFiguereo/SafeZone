package com.wilkins.alertaya.backend.network

import io.github.jan.supabase.postgrest.from
import java.util.UUID

suspend fun registerUser(name: String, email: String, password: String) {
    val supabase = SupabaseService.getInstance() // tu instancia

    try {
        val userId = UUID.randomUUID().toString() // Genera UUID para la columna id

        val response = supabase
            .from("users")
            .insert(
                mapOf(
                    "id" to userId,
                    "name" to name,
                    "email" to email,
                    "password" to password,
                    "role" to "user",
                    "photo_profile" to null
                )
            )

        // response puede contener error o datos
        println("Usuario registrado: $response")

    } catch (e: Exception) {
        println("Error al registrar usuario: ${e.message}")
    }
}
