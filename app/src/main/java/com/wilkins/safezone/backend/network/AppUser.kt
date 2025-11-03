package com.wilkins.safezone.backend.network
import kotlinx.serialization.Serializable

@Serializable
data class AppUser(
    val id: String,
    val name: String?,
    val role_id: Int?,
    val status_id: Int?,
    val email: String? = null,
    val emailConfirmedAt: String? = null,
    val confirmedAt: String? = null
)

