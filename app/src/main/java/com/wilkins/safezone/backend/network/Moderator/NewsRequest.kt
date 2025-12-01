package com.wilkins.safezone.backend.network.Moderator

data class NewsRequest(
    val title: String,
    val description: String,
    val imageBase64: String?,
    val isImportant: Boolean
)
