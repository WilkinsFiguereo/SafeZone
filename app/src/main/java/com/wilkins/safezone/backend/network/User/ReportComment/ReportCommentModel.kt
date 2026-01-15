// En: com/wilkins/safezone/backend/network/User/ReportComment/ReportCommentDto.kt
package com.wilkins.safezone.backend.network.User.ReportComment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportCommentDto(
    val id: String,

    @SerialName("user_id")
    val userId: String,

    val message: String,

    @SerialName("report_id")
    val reportId: String? = null,

    @SerialName("news_id")
    val newsId: String? = null,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("parent_comment_id")
    val parentCommentId: String? = null,

    @SerialName("author_name")
    val authorName: String? = null,

    // Campos del join con perfiles
    val user_name: String? = null,

    @SerialName("user_photo_url")
    val userPhotoUrl: String? = null
) {
    // Función helper para obtener nombre de usuario
    fun getDisplayName(): String {
        return user_name ?: authorName ?: "Usuario"
    }
}