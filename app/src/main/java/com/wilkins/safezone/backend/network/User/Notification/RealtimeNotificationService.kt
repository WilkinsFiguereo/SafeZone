package com.wilkins.safezone.backend.network.User.Notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.wilkins.safezone.MainActivity
import com.wilkins.safezone.R
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class RealtimeNotificationService(
    private val context: Context,
    private val supabase: SupabaseClient
) {
    companion object {
        private const val CHANNEL_ID = "safezone_notifications"
        private const val CHANNEL_NAME = "SafeZone Notificaciones"
        private const val PREFS_NAME = "safezone_prefs"
        private const val KEY_DEVICE_ID = "device_id"

        @Volatile
        private var instance: RealtimeNotificationService? = null

        fun getInstance(context: Context): RealtimeNotificationService {
            return instance ?: synchronized(this) {
                instance ?: RealtimeNotificationService(
                    context.applicationContext,
                    SupabaseService.getInstance()
                ).also { instance = it }
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private var realtimeJob: Job? = null
    private var isSubscribed = false
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    /**
     * Inicia la escucha de notificaciones en tiempo real
     */
    fun startListening(userId: String) {
        if (isSubscribed) {
            println("⚠️ Ya hay una suscripción activa")
            return
        }

        println("🎧 Iniciando escucha de notificaciones en tiempo real para userId=$userId")

        scope.launch {
            try {
                // Registrar dispositivo
                registerDevice(userId)

                // Crear canal de Realtime
                val channel = supabase.channel("notifications:$userId")

                // Escuchar cambios en la tabla notifications
                val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "notifications"
                    filter = "receiver_id=eq.$userId"
                }

                // Procesar los cambios
                realtimeJob = changeFlow
                    .onEach { action ->
                        when (action) {
                            is PostgresAction.Insert -> {
                                println("📩 Nueva notificación recibida via Realtime")
                                handleNewNotification(action.record)
                            }
                            is PostgresAction.Update -> {
                                println("📝 Notificación actualizada")
                                // Opcional: manejar actualizaciones
                            }
                            is PostgresAction.Delete -> {
                                println("🗑️ Notificación eliminada")
                                // Opcional: cancelar notificación local
                            }
                            else -> {
                                println("❓ Acción desconocida: $action")
                            }
                        }
                    }
                    .launchIn(scope)

                // Suscribirse al canal
                channel.subscribe()
                isSubscribed = true

                println("✅ Suscripción activa a notificaciones en tiempo real")

            } catch (e: Exception) {
                println("❌ Error al iniciar escucha de notificaciones: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Detiene la escucha de notificaciones
     */
    fun stopListening(userId: String) {
        println("🛑 Deteniendo escucha de notificaciones")

        scope.launch {
            try {
                realtimeJob?.cancel()

                // Desuscribirse del canal
                val channel = supabase.channel("notifications:$userId")
                channel.unsubscribe()

                isSubscribed = false

                // Opcional: limpiar dispositivo
                unregisterDevice(userId)

                println("✅ Escucha de notificaciones detenida")
            } catch (e: Exception) {
                println("❌ Error al detener escucha: ${e.message}")
            }
        }
    }

    /**
     * Maneja una nueva notificación recibida
     */
    private fun handleNewNotification(record: kotlinx.serialization.json.JsonObject) {
        try {
            val id = record["id"]?.jsonPrimitive?.content ?: ""
            val senderId = record["sender_id"]?.jsonPrimitive?.content
            val message = record["message"]?.jsonPrimitive?.content ?: ""
            val type = record["type"]?.jsonPrimitive?.content ?: "MESSAGE"
            val senderName = record["sender_name"]?.jsonPrimitive?.content

            println("📨 Procesando notificación: ID=$id, Type=$type")

            // Determinar el título
            val title = when {
                type == "SYSTEM" -> "Sistema SafeZone"
                senderName != null -> senderName
                else -> "Nueva notificación"
            }

            // Mostrar notificación local
            showLocalNotification(
                notificationId = id,
                title = title,
                message = message,
                type = type
            )

        } catch (e: Exception) {
            println("❌ Error al procesar notificación: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Muestra una notificación local en Android
     */
    private fun showLocalNotification(
        notificationId: String,
        title: String,
        message: String,
        type: String
    ) {
        // Intent para abrir la app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_id", notificationId)
            putExtra("open_notifications", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Configurar según el tipo
        val priority = when (type) {
            "IMPORTANT" -> NotificationCompat.PRIORITY_HIGH
            "SYSTEM" -> NotificationCompat.PRIORITY_DEFAULT
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val icon = when (type) {
            "IMPORTANT" -> android.R.drawable.ic_dialog_alert
            "SYSTEM" -> android.R.drawable.ic_dialog_info
            else -> android.R.drawable.ic_dialog_email
        }

        // Construir la notificación
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .build()

        // Mostrar la notificación
        notificationManager.notify(notificationId.hashCode(), notification)

        println("✅ Notificación local mostrada: $title")
    }

    /**
     * Crea el canal de notificaciones (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de la aplicación SafeZone"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(channel)
            println("✅ Canal de notificaciones creado")
        }
    }

    /**
     * Registra el dispositivo en la BD
     */
    private suspend fun registerDevice(userId: String) {
        try {
            val deviceId = getDeviceId()
            val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"

            supabase.from("user_devices")
                .upsert(
                    mapOf(
                        "user_id" to userId,
                        "device_id" to deviceId,
                        "device_name" to deviceName,
                        "last_seen" to "now()"
                    )
                )

            println("✅ Dispositivo registrado: $deviceName")
        } catch (e: Exception) {
            println("❌ Error al registrar dispositivo: ${e.message}")
        }
    }

    /**
     * Desregistra el dispositivo
     */
    private suspend fun unregisterDevice(userId: String) {
        try {
            val deviceId = getDeviceId()

            supabase.from("user_devices")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("device_id", deviceId)
                    }
                }

            println("✅ Dispositivo desregistrado")
        } catch (e: Exception) {
            println("❌ Error al desregistrar dispositivo: ${e.message}")
        }
    }

    /**
     * Obtiene o genera un ID único para el dispositivo
     */
    private fun getDeviceId(): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)

        if (deviceId == null) {
            deviceId = java.util.UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
            println("🆔 Nuevo Device ID generado: $deviceId")
        }

        return deviceId
    }

    /**
     * Carga notificaciones pendientes al iniciar sesión
     */
    suspend fun loadPendingNotifications(userId: String) {
        try {
            println("📥 Cargando notificaciones pendientes para userId=$userId")

            val repository = NotificationRepository(supabase)
            val unreadNotifications = repository.getUnreadNotifications(userId)

            println("📬 Notificaciones pendientes: ${unreadNotifications.size}")

            // Mostrar notificaciones locales para las no leídas
            unreadNotifications.take(5).forEach { notification ->
                val title = when {
                    notification.type == "SYSTEM" -> "Sistema SafeZone"
                    notification.senderName != null -> notification.senderName
                    else -> "Nueva notificación"
                }

                showLocalNotification(
                    notificationId = notification.id,
                    title = title,
                    message = notification.message,
                    type = notification.type
                )
            }

            if (unreadNotifications.size > 5) {
                showLocalNotification(
                    notificationId = "summary",
                    title = "SafeZone",
                    message = "Tienes ${unreadNotifications.size} notificaciones pendientes",
                    type = "SYSTEM"
                )
            }

        } catch (e: Exception) {
            println("❌ Error al cargar notificaciones pendientes: ${e.message}")
        }
    }
}