package com.wilkins.safezone.backend.network.Moderator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class News(
    @SerialName("id")
    val id: String? = null,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String,

    @SerialName("image_url")
    val imageUrl: String,

    @SerialName("is_featured")  // ✅ TU TABLA TIENE ESTA COLUMNA
    val isImportant: Boolean = false,

    @SerialName("user_id")
    val userId: String,

    @SerialName("created_at")
    val createdAt: String? = null
)