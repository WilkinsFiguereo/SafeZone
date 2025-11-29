package com.wilkins.safezone.backend.network.Moderator

import kotlinx.serialization.Serializable

@Serializable
data class NewsRequest(
    val title: String,
    val description: String,
    val important: Boolean,
    val userId: String,
    val imageBase64: String? = null
)
