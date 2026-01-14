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
            println("📥 NotificationRepository: Obteniendo notificaciones para userId=$userId")

            val notifications = supabase.from("notifications")
                .select() {
                    filter {
                        eq("receiver_id", userId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<NotificationData>()

            println("✅ NotificationRepository: Se obtuvieron ${notifications.size} notificaciones")
            notifications
        } catch (e: Exception) {
            println("❌ NotificationRepository: Error al obtener notificaciones: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtiene notificaciones no leídas del usuario
     */
    suspend fun getUnreadNotifications(userId: String): List<NotificationData> {
        return try {
            println("📥 NotificationRepository: Obteniendo notificaciones NO LEÍDAS para userId=$userId")

            val notifications = supabase.from("notifications")
                .select() {
                    filter {
                        eq("receiver_id", userId)
                        eq("is_read", false)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<NotificationData>()

            println("✅ NotificationRepository: Se obtuvieron ${notifications.size} notificaciones no leídas")
            notifications
        } catch (e: Exception) {
            println("❌ NotificationRepository: Error al obtener notificaciones no leídas: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtiene notificaciones por tipo
     */
    suspend fun getNotificationsByType(userId: String, type: String): List<NotificationData> {
        return try {
            println("📥 NotificationRepository: Obteniendo notificaciones tipo=$type para userId=$userId")

            val notifications = supabase.from("notifications")
                .select() {
                    filter {
                        eq("receiver_id", userId)
                        eq("type", type)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<NotificationData>()

            println("✅ NotificationRepository: Se obtuvieron ${notifications.size} notificaciones tipo $type")
            notifications
        } catch (e: Exception) {
            println("❌ NotificationRepository: Error al obtener notificaciones por tipo: ${e.message}")
            e.printStackTrace()
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

            val count = result.countOrNull()?.toInt() ?: 0
            println("📊 NotificationRepository: Contador de no leídas = $count")
            count
        } catch (e: Exception) {
            println("❌ NotificationRepository: Error al contar notificaciones: ${e.message}")
            0
        }
    }

    /**
     * Crea una nueva notificación
     * IMPORTANTE: Esto automáticamente dispara el Realtime para que el receptor la reciba
     */
    suspend fun createNotification(notification: NotificationCreate): NotificationData? {
        return try {
            println("📤 NotificationRepository: Creando notificación para receiver=${notification.receiverId}")

            val result = supabase.from("notifications")
                .insert(notification) {
                    select()
                }
                .decodeSingle<NotificationData>()

            println("✅ NotificationRepository: Notificación creada con ID=${result.id}")
            println("📡 Realtime enviará esta notificación automáticamente al receptor")

            result
        } catch (e: Exception) {
            println("❌ NotificationRepository: Error al crear notificación: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Marca una notificación como leída
     */
    suspend fun markAsRead(notificationId: String): Boolean {
        return try {
            println("✔️ NotificationRepository: Marcando como leída notificationId=$notificationId")

            supabase.from("notifications")
                .update({
                    set("is_read", true)
                }) {
                    filter {
                        eq("id", notificationId)
                    }
                }

            println("✅ NotificationRepository: Notificación marcada como leída")
            true
        } catch (e: Exception) {
            println("❌ NotificationRepository: Error al marcar notificación como leída: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Marca todas las notificaciones como leídas
     */
    suspend fun markAllAsRead(userId: String): Boolean {
        return try {
            println("✔️ NotificationRepository: Marcando TODAS como leídas para userId=$userId")

            supabase.from("notifications")
                .update({
                    set("is_read", true)
                }) {
                    filter {
                        eq("receiver_id", userId)
                        eq("is_read", false)
                    }
                }

            println("✅ NotificationRepository: Todas las notificaciones marcadas como leídas")
            true
        } catch (e: Exception) {
            println("❌ NotificationRepository: Error al marcar todas como leídas: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Elimina una notificación
     */
    suspend fun deleteNotification(notificationId: String): Boolean {
        return try {
            println("🗑️ NotificationRepository: Eliminando notificationId=$notificationId")

            supabase.from("notifications")
                .delete {
                    filter {
                        eq("id", notificationId)
                    }
                }

            println("✅ NotificationRepository: Notificación eliminada")
            true
        } catch (e: Exception) {
            println("❌ NotificationRepository: Error al eliminar notificación: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Elimina todas las notificaciones leídas del usuario
     */
    suspend fun deleteAllRead(userId: String): Boolean {
        return try {
            println("🗑️ NotificationRepository: Eliminando todas las leídas para userId=$userId")

            supabase.from("notifications")
                .delete {
                    filter {
                        eq("receiver_id", userId)
                        eq("is_read", true)
                    }
                }

            println("✅ NotificationRepository: Notificaciones leídas eliminadas")
            true
        } catch (e: Exception) {
            println("❌ NotificationRepository: Error al eliminar notificaciones leídas: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}