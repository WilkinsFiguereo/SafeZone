package com.wilkins.safezone.backend.network.Moderator.News


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

    @SerialName("video_url")  // 🔥 NUEVA COLUMNA
    val videoUrl: String? = null,

    @SerialName("is_featured")
    val isImportant: Boolean = false,

    @SerialName("user_id")
    val userId: String,

    @SerialName("created_at")
    val createdAt: String? = null
)