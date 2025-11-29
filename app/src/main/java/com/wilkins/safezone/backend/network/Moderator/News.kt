package com.wilkins.safezone.backend.network.Moderator

data class News(
    val id: Int,
    val title: String,
    val description: String,
    val important: Boolean,
    val imageUrl: String?,
    val userId: String
)
