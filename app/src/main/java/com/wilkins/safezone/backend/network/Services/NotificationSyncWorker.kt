package com.wilkins.safezone.backend.network.Services


import android.content.Context
import androidx.work.*
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.User.Notification.RealtimeNotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Worker para sincronizar notificaciones periódicamente cuando la app está cerrada
 */
class NotificationSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val WORK_NAME = "notification_sync_work"
        const val KEY_USER_ID = "user_id"

        /**
         * Programa la sincronización periódica de notificaciones
         */
        fun schedule(context: Context, userId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<NotificationSyncWorker>(
                15, TimeUnit.MINUTES // Cada 15 minutos (mínimo permitido por Android)
            )
                .setConstraints(constraints)
                .setInputData(
                    workDataOf(KEY_USER_ID to userId)
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )

            println("✅ Worker de sincronización programado")
        }

        /**
         * Cancela la sincronización periódica
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            println("🛑 Worker de sincronización cancelado")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val userId = inputData.getString(KEY_USER_ID)

            if (userId == null) {
                println("❌ Worker: No se encontró userId")
                return@withContext Result.failure()
            }

            println("🔄 Worker: Sincronizando notificaciones para userId=$userId")

            // Cargar notificaciones pendientes
            val realtimeService = RealtimeNotificationService.getInstance(applicationContext)
            realtimeService.loadPendingNotifications(userId)

            println("✅ Worker: Sincronización completada")
            Result.success()

        } catch (e: Exception) {
            println("❌ Worker: Error en sincronización: ${e.message}")
            e.printStackTrace()

            // Reintentar si es un error temporal
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}