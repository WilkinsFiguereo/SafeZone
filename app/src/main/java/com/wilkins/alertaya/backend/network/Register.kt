// En: Register.kt (Versión Corregida)

package com.wilkins.alertaya.backend.network

import android.util.Log // Es mejor usar Logcat para los errores en Android
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

suspend fun registerUser(name: String, email: String, password: String): Boolean {
    val supabase = SupabaseService.getInstance()

    try {
        // SOLUCIÓN: Usa signUpWith(Email) y configura las propiedades dentro del bloque lambda.
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            // SOLUCIÓN 2: Los metadatos se pasan a la propiedad 'data' como un JsonObject.
            this.data = buildJsonObject {
                put("name", name)
            }
        }

        // Si la línea anterior no lanzó una excepción, el registro fue enviado.
        // El usuario recién creado está ahora disponible en la sesión de 'auth'.
        val newUser = supabase.auth.currentUserOrNull()
        println("✅ Petición de registro enviada para: ${email}")
        println("   ID del usuario: ${newUser?.id}")
        println("   Revisa el email para confirmar la cuenta (si la opción está activada en Supabase).")
        return true

    } catch (e: Exception) {
        // El bloque catch capturará errores como "User already registered", etc.
        Log.e("SupabaseRegister", "❌ Error al registrar usuario: ${e.message}", e)
        return false
    }
}
