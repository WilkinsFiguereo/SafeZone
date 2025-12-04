package com.wilkins.safezone.backend.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppUser(
    val id: String,
    val name: String?,
    val phone: String? = null,
    val pronouns: String? = null,
    val description: String? = null,
    val address: String? = null,
    @SerialName("photo_profile")
    val photoProfile: String? = null,
    @SerialName("role_id")
    val role_id: Int?,
    val status_id: Int?,
    val email: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    val emailConfirmedAt: String? = null,
    val confirmedAt: String? = null
)