package com.wilkins.safezone.backend.network.Services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.wilkins.safezone.MainActivity
import com.wilkins.safezone.R
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.User.Notification.RealtimeNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

/**
 * Servicio de foreground para mantener la conexión de notificaciones en tiempo real
 */
class NotificationBackgroundService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "safezone_service"
        private const val CHANNEL_NAME = "SafeZone Service"

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_USER_ID = "EXTRA_USER_ID"

        fun startService(context: Context, userId: String) {
            val intent = Intent(context, NotificationBackgroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_USER_ID, userId)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, NotificationBackgroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var realtimeService: RealtimeNotificationService? = null
    private var currentUserId: String? = null

    override fun onCreate() {
        super.onCreate()
        println("🚀 NotificationBackgroundService creado")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val userId = intent.getStringExtra(EXTRA_USER_ID)
                if (userId != null) {
                    startForegroundService(userId)
                } else {
                    println("❌ No se proporcionó userId")
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                stopForegroundService()
            }
        }

        return START_STICKY
    }

    private fun startForegroundService(userId: String) {
        println("▶️ Iniciando servicio en foreground para userId=$userId")

        currentUserId = userId

        // Crear notificación de foreground
        val notification = createForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Iniciar escucha de notificaciones en tiempo real
        realtimeService = RealtimeNotificationService.getInstance(this)
        realtimeService?.startListening(userId)

        println("✅ Servicio de notificaciones activo")
    }

    private fun stopForegroundService() {
        println("⏹️ Deteniendo servicio de notificaciones")

        currentUserId?.let { userId ->
            realtimeService?.stopListening(userId)
        }

        scope.cancel()
        stopForeground(true)
        stopSelf()

        println("✅ Servicio detenido")
    }

    private fun createForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SafeZone activo")
            .setContentText("Recibiendo notificaciones en tiempo real")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene SafeZone activo para recibir notificaciones"
                setShowBadge(false)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        println("💥 NotificationBackgroundService destruido")
        currentUserId?.let { userId ->
            realtimeService?.stopListening(userId)
        }
        scope.cancel()
        super.onDestroy()
    }
}