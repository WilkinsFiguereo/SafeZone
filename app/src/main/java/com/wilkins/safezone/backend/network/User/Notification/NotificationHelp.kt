package com.wilkins.safezone.backend.network.User.Notification


/**
 * Utilidad para enviar diferentes tipos de notificaciones
 */
class NotificationHelper(private val repository: NotificationRepository) {

    /**
     * Envía una notificación de mensaje entre usuarios
     */
    suspend fun sendMessageNotification(
        senderId: String,
        receiverId: String,
        message: String
    ): Boolean {
        val notification = NotificationCreate(
            senderId = senderId,
            receiverId = receiverId,
            message = message,
            type = "MESSAGE"
        )
        return repository.createNotification(notification) != null
    }

    /**
     * Envía una notificación del sistema
     */
    suspend fun sendSystemNotification(
        receiverId: String,
        message: String
    ): Boolean {
        val notification = NotificationCreate(
            senderId = null, // Las notificaciones del sistema no tienen remitente
            receiverId = receiverId,
            message = message,
            type = "SYSTEM"
        )
        return repository.createNotification(notification) != null
    }

    /**
     * Envía una notificación importante
     */
    suspend fun sendImportantNotification(
        senderId: String?,
        receiverId: String,
        message: String
    ): Boolean {
        val notification = NotificationCreate(
            senderId = senderId,
            receiverId = receiverId,
            message = message,
            type = "IMPORTANT"
        )
        return repository.createNotification(notification) != null
    }

    /**
     * Envía notificaciones a múltiples usuarios
     */
    suspend fun sendBulkNotifications(
        senderId: String?,
        receiverIds: List<String>,
        message: String,
        type: String = "MESSAGE"
    ): Int {
        var successCount = 0
        receiverIds.forEach { receiverId ->
            val notification = NotificationCreate(
                senderId = senderId,
                receiverId = receiverId,
                message = message,
                type = type
            )
            if (repository.createNotification(notification) != null) {
                successCount++
            }
        }
        return successCount
    }

    /**
     * Envía una notificación de nuevo comentario en un reporte
     */
    suspend fun notifyNewComment(
        senderId: String,
        receiverId: String,
        reportId: String,
        senderName: String
    ): Boolean {
        return sendMessageNotification(
            senderId = senderId,
            receiverId = receiverId,
            message = "$senderName ha comentado en tu reporte #$reportId"
        )
    }

    /**
     * Envía una notificación de solicitud de acceso
     */
    suspend fun notifyAccessRequest(
        senderId: String,
        receiverId: String,
        reportId: String,
        senderName: String
    ): Boolean {
        return sendImportantNotification(
            senderId = senderId,
            receiverId = receiverId,
            message = "$senderName ha solicitado acceso al reporte #$reportId"
        )
    }

    /**
     * Envía una alerta de seguridad a todos los usuarios de una zona
     */
    suspend fun sendSecurityAlert(
        userIds: List<String>,
        alertMessage: String
    ): Int {
        return sendBulkNotifications(
            senderId = null,
            receiverIds = userIds,
            message = "⚠️ Alerta de Seguridad: $alertMessage",
            type = "IMPORTANT"
        )
    }

    /**
     * Envía una notificación de actualización de reporte
     */
    suspend fun notifyReportUpdate(
        receiverId: String,
        reportId: String,
        status: String
    ): Boolean {
        return sendSystemNotification(
            receiverId = receiverId,
            message = "Tu reporte #$reportId ha sido actualizado a: $status"
        )
    }

    /**
     * Envía una notificación de bienvenida a un nuevo usuario
     */
    suspend fun sendWelcomeNotification(userId: String): Boolean {
        return sendSystemNotification(
            receiverId = userId,
            message = "¡Bienvenido a SafeZone! Gracias por unirte a nuestra comunidad de seguridad."
        )
    }
}

/**
 * Ejemplos de uso:
 *
 * val notificationHelper = NotificationHelper(notificationRepository)
 *
 * // Enviar notificación de comentario
 * notificationHelper.notifyNewComment(
 *     senderId = currentUserId,
 *     receiverId = reportOwnerId,
 *     reportId = "12345",
 *     senderName = "Juan Pérez"
 * )
 *
 * // Enviar alerta de seguridad
 * notificationHelper.sendSecurityAlert(
 *     userIds = usersInZone,
 *     alertMessage = "Se ha reportado actividad sospechosa en tu área"
 * )
 *
 * // Enviar notificación del sistema
 * notificationHelper.sendSystemNotification(
 *     receiverId = userId,
 *     message = "Tu reporte ha sido verificado por las autoridades"
 * )
 */