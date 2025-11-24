package com.wilkins.safezone.backend.network.User.Form

import kotlinx.serialization.Serializable

@Serializable
data class Report(
    val id_affair: Int,
    val description: String,
    val image_url: String?,
    val user_id: String,
    val is_anonymous: Boolean,
    val user_name: String?,
    val report_location: String?,
    val id_reporting_status: Int
)

@Serializable
data class Affair(
    val id: Int,
    val type: String
)
