package com.wilkins.safezone.backend.network.auth

import android.util.Log
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import android.content.Context
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.SupabaseService

// Clase sellada para manejar los diferentes resultados del login
sealed class LoginResult {
    data class Success(val user: AppUser) : LoginResult()
    data class AccountDisabled(val statusId: Int) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

suspend fun login(context: Context, email: String, password: String): LoginResult {
    val client = SupabaseService.getInstance()

    return try {
        // 🔐 Autenticación
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        // 🔄 Guardar sesión
        client.auth.currentSessionOrNull()?.let { session ->
            SessionManager.saveSession(context, session)
            Log.i("SupabaseLogin", "✅ Sesión guardada correctamente para ${session.user?.email}")
        } ?: Log.w("SupabaseLogin", "⚠️ No hay sesión activa tras login")

        // Obtener ID del usuario
        val userId = client.auth.currentUserOrNull()?.id
        if (userId == null) {
            Log.w("SupabaseLogin", "Login exitoso pero no se pudo obtener el ID del usuario.")
            return LoginResult.Error("No se pudo obtener la información del usuario")
        }

        // Obtener perfil del usuario
        val user = client.postgrest
            .from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull<AppUser>()

        if (user == null) {
            Log.e("SupabaseLogin", "❌ No se encontró el perfil del usuario")
            return LoginResult.Error("No se encontró el perfil del usuario")
        }

        // ✅ Verificar el estado del usuario
        when (user.status_id) {
            1 -> {
                // Usuario activo - login exitoso
                Log.i("SupabaseLogin", "✅ Usuario activo: ${user.name}")
                SessionManager.saveUserData(context, user)
                LoginResult.Success(user)
            }
            2 -> {
                // Usuario deshabilitado
                Log.w("SupabaseLogin", "⚠️ Usuario deshabilitado: ${user.name}")
                // Cerrar sesión
                client.auth.signOut()
                SessionManager.clearSession(context)
                LoginResult.AccountDisabled(2)
            }
            3 -> {
                // Usuario baneado
                Log.w("SupabaseLogin", "🚫 Usuario baneado: ${user.name}")
                // Cerrar sesión
                client.auth.signOut()
                SessionManager.clearSession(context)
                LoginResult.AccountDisabled(3)
            }
            else -> {
                // Estado desconocido
                Log.w("SupabaseLogin", "⚠️ Estado desconocido del usuario: ${user.status_id}")
                client.auth.signOut()
                SessionManager.clearSession(context)
                LoginResult.AccountDisabled(user.status_id ?: 0)
            }
        }

    } catch (e: Exception) {
        Log.e("SupabaseLogin", "❌ Error durante login: ${e.message}", e)
        LoginResult.Error(e.message ?: "Error desconocido durante el login")
    }
}

// Mantener la función original para compatibilidad (deprecated)
@Deprecated("Usa la nueva función login que retorna LoginResult")
suspend fun loginLegacy(context: Context, email: String, password: String): AppUser? {
    return when (val result = login(context, email, password)) {
        is LoginResult.Success -> result.user
        else -> null
    }
}