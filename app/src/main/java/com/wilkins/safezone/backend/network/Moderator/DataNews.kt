package com.wilkins.safezone.backend.network.Moderator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class News(
    val id: Int? = null,
    val title: String,
    val description: String,
    @SerialName("image_url")
    val imageUrl: String,
    @SerialName("is_important")
    val isImportant: Boolean = false,
    @SerialName("user_id")
    val userId: String,
    @SerialName("created_at")
    val createdAt: String? = null
)