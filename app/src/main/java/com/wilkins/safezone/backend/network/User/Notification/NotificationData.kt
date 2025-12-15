package com.wilkins.safezone.backend.network.User.Notification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationData(
    val id: String = "",
    @SerialName("sender_id")
    val senderId: String? = null,
    @SerialName("receiver_id")
    val receiverId: String = "",
    val message: String = "",
    val type: String = "MESSAGE", // MESSAGE, SYSTEM, IMPORTANT
    @SerialName("is_read")
    val isRead: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = "",
    // Datos del remitente (join)
    @SerialName("sender_name")
    val senderName: String? = null,
    @SerialName("sender_email")
    val senderEmail: String? = null
)

@Serializable
data class NotificationCreate(
    @SerialName("sender_id")
    val senderId: String? = null,
    @SerialName("receiver_id")
    val receiverId: String,
    val message: String,
    val type: String = "MESSAGE"
)

@Serializable
data class NotificationUpdate(
    @SerialName("is_read")
    val isRead: Boolean
)

enum class NotificationTypeEnum(val value: String) {
    MESSAGE("MESSAGE"),
    SYSTEM("SYSTEM"),
    IMPORTANT("IMPORTANT")
}