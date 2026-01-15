package com.wilkins.safezone.backend.network.User.Interaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo para las interacciones (likes, comentarios, etc.)
 */
@Serializable
data class InteractionDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("target_id")
    val targetId: String,

    @SerialName("interaction_type")
    val interactionType: InteractionType,

    @SerialName("entity_type")
    val entityType: EntityType,

    @SerialName("created_at")
    val createdAt: String? = null
)

/**
 * Enum para tipos de interacción
 */
@Serializable
enum class InteractionType {
    @SerialName("like")
    LIKE,

    @SerialName("comment")
    COMMENT,

    @SerialName("share")
    SHARE
}

/**
 * Enum para tipos de entidad
 */
@Serializable
enum class EntityType {

    @SerialName("report")
    REPORT,

    @SerialName("news")
    NEWS,

    @SerialName("comment")
    COMMENT,

    @SerialName("post")
    POST,

    @SerialName("survey")
    SURVEY
}

/**
 * Modelo para la respuesta del contador de likes
 */
@Serializable
data class LikeCountDto(
    @SerialName("count")
    val count: Int
)