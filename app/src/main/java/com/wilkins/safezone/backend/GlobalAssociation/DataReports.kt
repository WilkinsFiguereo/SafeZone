package com.wilkins.safezone.backend.GlobalAssociation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Modelo de Reporte desde la BD
@Serializable
data class ReportDto(
    @SerialName("id")
    val id: String,

    @SerialName("id_affair")
    val idAffair: Int? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("is_anonymous")
    val isAnonymous: Boolean = false,

    @SerialName("user_name")
    val userName: String? = null,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("last_update")
    val lastUpdate: String,

    @SerialName("report_location")
    val reportLocation: String? = null,

    @SerialName("id_reporting_status")
    val idReportingStatus: Int
)

// Modelo de Affair/Asunto
@Serializable
data class AffairDto(
    @SerialName("id")
    val id: Int,

    @SerialName("affair_name")
    val affairName: String,

    @SerialName("description")
    val description: String? = null
)

// Modelo de Estado de Reporte
@Serializable
data class ReportingStatusDto(
    @SerialName("id")
    val id: Int,

    @SerialName("status_name")
    val statusName: String,

    @SerialName("description")
    val description: String? = null
)
