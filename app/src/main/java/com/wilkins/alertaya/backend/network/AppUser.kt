package com.wilkins.alertaya.backend.network

import kotlinx.serialization.Serializable

@Serializable
data class AppUser(
    val id: String,
    val role: String,
    val status: String,
    // ¡AGREGA ESTE CAMPO para coincidir con la SELECT!
    val name: String? = null // Usa nullable si no lo necesitas siempre
)