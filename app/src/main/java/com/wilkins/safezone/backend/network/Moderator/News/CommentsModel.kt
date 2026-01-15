package com.wilkins.safezone.backend.network.Moderator.News

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentUser(
    @SerialName("name")
    val name: String? = null,

    @SerialName("photo_url")
    val photoUrl: String? = null
)

@Serializable
data class Comment(
    @SerialName("id")
    val id: String = "",

    @SerialName("news_id")
    val newsId: String? = null,  // ✅ Nullable porque puede venir como null

    @SerialName("user_id")
    val userId: String = "",

    @SerialName("message")
    val message: String = "",

    @SerialName("created_at")
    val createdAt: String = "",

    @SerialName("parent_comment_id")
    val parentCommentId: String? = null,

    // ✅ CRÍTICO: author_name puede ser NULL en la DB
    @SerialName("author_name")
    val authorName: String? = null,  // ✅ Ahora es nullable

    // Este campo NO viene de la DB, lo calculamos nosotros
    val authorPhotoUrl: String? = null,

    // Este campo viene del JOIN con users
    val users: CommentUser? = null
) {
    // Propiedad calculada para obtener el nombre del autor
    val displayName: String
        get() = authorName ?: users?.name ?: "Usuario"

    // Propiedad calculada para obtener la foto del autor
    val displayPhotoUrl: String?
        get() = authorPhotoUrl ?: users?.photoUrl
}

