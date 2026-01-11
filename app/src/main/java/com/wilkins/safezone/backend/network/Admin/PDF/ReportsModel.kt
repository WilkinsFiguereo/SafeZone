package com.wilkins.safezone.backend.network.Admin.PDF

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Modelo para Status
@Serializable
data class Status(
    @SerialName("id")
    val id: Int = 0,

    @SerialName("status")
    val status: String = ""
)

// Modelo para Affair (Categoría de Incidencia)
@Serializable
data class Affair(
    @SerialName("id")
    val id: Int = 0,

    @SerialName("type")
    val type: String = "",

    @SerialName("category_id")
    val category_Id: Int
)

// Modelo para Profile (Usuario que reporta)
@Serializable
data class ReportProfile(
    @SerialName("id")
    val id: String = "",

    @SerialName("name")
    val name: String = "",

    @SerialName("email")
    val email: String? = null,

    @SerialName("phone")
    val phone: String? = null
)

// Modelo completo para Report
@Serializable
data class ReportDetail(
    @SerialName("id")
    val id: Int = 0,

    @SerialName("user_id")
    val userId: String = "",

    @SerialName("affair_id")
    val affairId: Int = 0,

    @SerialName("title")
    val title: String = "",

    @SerialName("description")
    val description: String = "",

    @SerialName("location")
    val location: String = "",

    @SerialName("media_url")
    val mediaUrl: String? = null,

    @SerialName("status_id")
    val statusId: Int = 0,

    @SerialName("created_at")
    val createdAt: String = "",

    @SerialName("updated_at")
    val updatedAt: String = "",

    // Relaciones
    @SerialName("status")
    val status: Status? = null,

    @SerialName("affair")
    val affair: Affair? = null,

    @SerialName("profiles")
    val profile: ReportProfile? = null
)