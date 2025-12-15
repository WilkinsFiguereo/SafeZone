package com.wilkins.safezone.backend.network.User.Notification

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order

class NotificationRepository(private val supabase: SupabaseClient) {

    /**
     * Obtiene todas las notificaciones del usuario actual
     */
    suspend fun getUserNotifications(userId: String): List<NotificationData> {
        return try {
            supabase.from("notifications")
                .select(
                    columns = Columns.raw("""
                        *,
                        sender:sender_id (
                            name,
                            email
                        )
                    """.trimIndent())
                ) {
                    filter {
                        eq("receiver_id", userId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<NotificationData>()
        } catch (e: Exception) {
            println("Error al obtener notificaciones: ${e.message}")
            emptyList()
        }
    }

    /**
     * Obtiene notificaciones no leídas del usuario
     */
    suspend fun getUnreadNotifications(userId: String): List<NotificationData> {
        return try {
            supabase.from("notifications")
                .select(
                    columns = Columns.raw("""
                        *,
                        sender:sender_id (
                            name,
                            email
                        )
                    """.trimIndent())
                ) {
                    filter {
                        eq("receiver_id", userId)
                        eq("is_read", false)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<NotificationData>()
        } catch (e: Exception) {
            println("Error al obtener notificaciones no leídas: ${e.message}")
            emptyList()
        }
    }

    /**
     * Obtiene notificaciones por tipo
     */
    suspend fun getNotificationsByType(userId: String, type: String): List<NotificationData> {
        return try {
            supabase.from("notifications")
                .select(
                    columns = Columns.raw("""
                        *,
                        sender:sender_id (
                            name,
                            email
                        )
                    """.trimIndent())
                ) {
                    filter {
                        eq("receiver_id", userId)
                        eq("type", type)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<NotificationData>()
        } catch (e: Exception) {
            println("Error al obtener notificaciones por tipo: ${e.message}")
            emptyList()
        }
    }

    /**
     * Cuenta las notificaciones no leídas
     */
    suspend fun getUnreadCount(userId: String): Int {
        return try {
            val result = supabase.from("notifications")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("receiver_id", userId)
                        eq("is_read", false)
                    }
                    count(Count.EXACT)
                }

            // El count viene en los headers de la respuesta
            result.countOrNull()?.toInt() ?: 0
        } catch (e: Exception) {
            println("Error al contar notificaciones: ${e.message}")
            0
        }
    }

    /**
     * Crea una nueva notificación
     */
    suspend fun createNotification(notification: NotificationCreate): NotificationData? {
        return try {
            supabase.from("notifications")
                .insert(notification) {
                    select()
                }
                .decodeSingle<NotificationData>()
        } catch (e: Exception) {
            println("Error al crear notificación: ${e.message}")
            null
        }
    }

    /**
     * Marca una notificación como leída
     */
    suspend fun markAsRead(notificationId: String): Boolean {
        return try {
            supabase.from("notifications")
                .update({
                    set("is_read", true)
                }) {
                    filter {
                        eq("id", notificationId)
                    }
                }
            true
        } catch (e: Exception) {
            println("Error al marcar notificación como leída: ${e.message}")
            false
        }
    }

    /**
     * Marca todas las notificaciones como leídas
     */
    suspend fun markAllAsRead(userId: String): Boolean {
        return try {
            supabase.from("notifications")
                .update({
                    set("is_read", true)
                }) {
                    filter {
                        eq("receiver_id", userId)
                        eq("is_read", false)
                    }
                }
            true
        } catch (e: Exception) {
            println("Error al marcar todas como leídas: ${e.message}")
            false
        }
    }

    /**
     * Elimina una notificación
     */
    suspend fun deleteNotification(notificationId: String): Boolean {
        return try {
            supabase.from("notifications")
                .delete {
                    filter {
                        eq("id", notificationId)
                    }
                }
            true
        } catch (e: Exception) {
            println("Error al eliminar notificación: ${e.message}")
            false
        }
    }

    /**
     * Elimina todas las notificaciones leídas del usuario
     */
    suspend fun deleteAllRead(userId: String): Boolean {
        return try {
            supabase.from("notifications")
                .delete {
                    filter {
                        eq("receiver_id", userId)
                        eq("is_read", true)
                    }
                }
            true
        } catch (e: Exception) {
            println("Error al eliminar notificaciones leídas: ${e.message}")
            false
        }
    }
}