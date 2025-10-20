// En: C:/Users/DUNDO/AndroidStudioProjects/AlertaYa/app/src/main/java/com/wilkins/alertaya/backend/network/Login.kt
// (Reemplaza todo el contenido del archivo con esto)

package com.wilkins.alertaya.backend.network

import android.util.Log
import io.github.jan.supabase.gotrue.auth // Necesario para la autenticación
import io.github.jan.supabase.gotrue.providers.builtin.Email // Necesario para el proveedor de email
import io.github.jan.supabase.postgrest.postgrest // Necesario para obtener el perfil
import io.github.jan.supabase.postgrest.from

// Tu data class AppUser debería reflejar los campos de tu tabla 'users'
// Ejemplo: data class AppUser(val id: String, val role: String, val status: String, val name: String)

suspend fun login(email: String, password: String): AppUser? {
    val client = SupabaseService.getInstance()

    try {
        // --- PASO 1: Autenticar al usuario de forma segura contra 'auth.users' ---
        println("Intentando autenticación segura para el email: $email")
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        // Si la línea anterior no lanza una excepción, el email y la contraseña son correctos.
        println("✅ Autenticación exitosa.")

        // --- PASO 2: Obtener el perfil del usuario desde tu tabla 'public.users' ---
        // Obtenemos el ID del usuario que acaba de iniciar sesión.
        val userId = client.auth.currentUserOrNull()?.id
        if (userId == null) {
            Log.w("SupabaseLogin", "Login exitoso pero no se pudo obtener el ID del usuario de la sesión.")
            return null
        }

        println("Obteniendo perfil de la tabla 'users' para el ID: $userId")

        // Hacemos una consulta a 'public.users' para obtener datos adicionales como 'role', 'status', etc.
        // Esta consulta funcionará porque el usuario ya está autenticado y RLS se lo permitirá (si está bien configurada).
        val appUser = client.postgrest.from("users").select {
            filter {
                eq("id", userId)
            }
        }.decodeSingleOrNull<AppUser>()

        if (appUser == null) {
            Log.w("SupabaseLogin", "Usuario autenticado pero no se encontró perfil en la tabla 'users'. Revisa tus Triggers.")
        } else {
            println("✅ Perfil de usuario encontrado: $appUser")
        }

        return appUser

    } catch (e: Exception) {
        // El bloque catch ahora capturará errores reales de autenticación como
        // "Invalid login credentials", "Email not confirmed", etc.
        Log.e("SupabaseLogin", "❌ Error durante el proceso de login: ${e.message}", e)
        return null
    }
}
