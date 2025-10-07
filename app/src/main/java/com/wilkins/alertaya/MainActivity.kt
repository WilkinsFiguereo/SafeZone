package com.wilkins.alertaya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.wilkins.alertaya.frontend.ui.screens.auth.RegisterScreen
import com.wilkins.alertaya.frontend.ui.theme.AlertaYaTheme
import com.wilkins.alertaya.backend.network.SupabaseService
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cargar pantalla principal
        setContent {
            AlertaYaTheme {
                RegisterScreen()
            }
        }

        // ✅ Probar conexión a Supabase
        testSupabaseConnection()
    }

    // 🔍 Función para probar si la conexión funciona correctamente
    private fun testSupabaseConnection() {
        val supabase = SupabaseService.getInstance()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = supabase.postgrest["users"].select()
                println("✅ Conexión exitosa con Supabase: ${response.data}")
            } catch (e: Exception) {
                println("❌ Error al conectar con Supabase: ${e.message}")
            }
        }
    }
}
