package com.wilkins.safezone.backend.network.Admin.Affair

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncidentCategory(
    val id: Int? = null,
    val name: String
)

// Models/AffairCategory.kt
@Serializable
data class AffairCategory(
    val id: Int? = null,
    val name: String
)

// Models/Affair.kt
@Serializable
data class Affair(
    val id: Int? = null,
    val type: String,
    @SerialName("categories_id")
    val categoriesId: Int
)