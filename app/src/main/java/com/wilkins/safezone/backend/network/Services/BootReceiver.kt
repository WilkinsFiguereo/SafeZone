package com.wilkins.safezone.backend.network.Services


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.Services.NotificationBackgroundService
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receiver para reiniciar el servicio de notificaciones cuando el dispositivo se reinicia
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                println("📱 Dispositivo reiniciado, verificando sesión activa")

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val supabase = SupabaseService.getInstance()
                        val userId = supabase.auth.currentUserOrNull()?.id

                        if (userId != null) {
                            println("✅ Sesión activa encontrada, reiniciando servicio de notificaciones")
                            NotificationBackgroundService.startService(context, userId)
                        } else {
                            println("ℹ️ No hay sesión activa, no se inicia el servicio")
                        }
                    } catch (e: Exception) {
                        println("❌ Error al verificar sesión: ${e.message}")
                    }
                }
            }
        }
    }
}