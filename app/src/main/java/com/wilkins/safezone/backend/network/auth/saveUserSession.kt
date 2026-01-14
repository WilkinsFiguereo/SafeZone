package com.wilkins.safezone.backend.network.auth

import android.content.Context
import android.util.Log
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.bridge.auth.GoogleSignInBridge
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SessionManager {

    private const val PREFS_NAME = "supabase_session_prefs"
    private const val KEY_SESSION = "session_data"
    private const val KEY_IS_GOOGLE_AUTH = "is_google_auth"
    private const val KEY_STATUS_ID = "status_id" // 🔥 Nueva clave para status_id

    fun saveSession(context: Context, session: UserSession, isGoogleAuth: Boolean = false) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonSession = Json.encodeToString(session)
            prefs.edit()
                .putString(KEY_SESSION, jsonSession)
                .putBoolean(KEY_IS_GOOGLE_AUTH, isGoogleAuth)
                .apply()

            Log.i("SessionManager", "✅ Sesión guardada correctamente.")
            Log.i(
                "SessionManager",
                "🔐 Tipo de autenticación: ${if (isGoogleAuth) "Google" else "Email/Password"}"
            )
        } catch (e: Exception) {
            Log.e("SessionManager", "❌ Error guardando sesión: ${e.message}", e)
        }
    }

    fun loadSession(context: Context): UserSession? {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonSession = prefs.getString(KEY_SESSION, null) ?: return null
            Json.decodeFromString<UserSession>(jsonSession)
        } catch (e: Exception) {
            Log.e("SessionManager", "❌ Error restaurando sesión: ${e.message}", e)
            null
        }
    }

    fun isGoogleAuth(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_GOOGLE_AUTH, false)
    }

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_SESSION)
            .remove(KEY_IS_GOOGLE_AUTH)
            .remove("user_id")
            .remove("role_id")
            .remove(KEY_STATUS_ID) // 🔥 Limpiar también el status_id
            .apply()
        Log.i("SessionManager", "🧹 Sesión eliminada correctamente.")
    }

    suspend fun logout(context: Context, supabaseClient: SupabaseClient) {
        try {
            Log.i("SessionManager", "═══════════════════════════════════")
            Log.i("SessionManager", "🚪 INICIANDO PROCESO DE LOGOUT")

            val isGoogle = isGoogleAuth(context)
            Log.i(
                "SessionManager",
                "🔐 Tipo de autenticación: ${if (isGoogle) "Google" else "Email/Password"}"
            )

            // 1. Cerrar sesión en Supabase
            supabaseClient.auth.signOut()
            Log.i("SessionManager", "✅ Sesión cerrada en Supabase.")

            // 2. Si la autenticación fue con Google, cerrar sesión de Google también
            if (isGoogle) {
                Log.i("SessionManager", "🔄 Cerrando sesión de Google...")
                val result = GoogleSignInBridge.signOut(context)

                result.onSuccess {
                    Log.i("SessionManager", "✅ Sesión de Google cerrada correctamente")
                }.onFailure { e ->
                    Log.e("SessionManager", "⚠️ Error al cerrar sesión de Google: ${e.message}")
                }
            }

            // 3. Limpiar datos locales
            clearSession(context)

            Log.i("SessionManager", "✅ Logout completado exitosamente.")
            Log.i("SessionManager", "═══════════════════════════════════")
        } catch (e: Exception) {
            Log.e("SessionManager", "❌ Error durante logout: ${e.message}", e)
            clearSession(context)
        }
    }

    suspend fun getUserProfile(context: Context): AppUser? {
        val client = SupabaseService.getInstance()

        // 1. Cargar sesión guardada
        val session = SessionManager.loadSession(context) ?: return null
        val userId = session.user?.id ?: return null

        // 2. Consultar perfil en la tabla profiles
        return client.postgrest
            .from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull<AppUser>()
    }

    fun saveUserData(context: Context, user: AppUser) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        Log.i("SessionManager", "═══════════════════════════════════")
        Log.i("SessionManager", "💾 GUARDANDO DATOS DEL USUARIO")
        Log.i("SessionManager", "   - User ID: ${user.id}")
        Log.i("SessionManager", "   - Role ID recibido: ${user.role_id}")
        Log.i("SessionManager", "   - Status ID recibido: ${user.status_id}") // 🔥 Log del status
        Log.i("SessionManager", "   - Role ID (con ?:-1): ${user.role_id ?: -1}")
        Log.i("SessionManager", "   - Status ID (con ?:0): ${user.status_id ?: 0}") // 🔥 Log del status
        Log.i("SessionManager", "═══════════════════════════════════")

        val editor = prefs.edit()
        editor.putString("user_id", user.id)
        editor.putInt("role_id", user.role_id ?: -1)
        editor.putInt(KEY_STATUS_ID, user.status_id ?: 0) // 🔥 Guardar status_id
        val success = editor.commit()

        Log.i("SessionManager", "   - Commit exitoso: $success")

        // Verificar inmediatamente después de guardar
        val verificacionRole = prefs.getInt("role_id", -999)
        val verificacionStatus = prefs.getInt(KEY_STATUS_ID, -999) // 🔥 Verificar status

        Log.i("SessionManager", "✅ Verificación inmediata:")
        Log.i("SessionManager", "   - role_id = $verificacionRole")
        Log.i("SessionManager", "   - status_id = $verificacionStatus") // 🔥 Log verificación

        if (verificacionRole != (user.role_id ?: -1)) {
            Log.e("SessionManager", "❌ ERROR CRÍTICO: El role_id NO se guardó correctamente!")
            Log.e("SessionManager", "   Esperado: ${user.role_id ?: -1}, Obtenido: $verificacionRole")
        }

        if (verificacionStatus != (user.status_id ?: 0)) {
            Log.e("SessionManager", "❌ ERROR CRÍTICO: El status_id NO se guardó correctamente!")
            Log.e("SessionManager", "   Esperado: ${user.status_id ?: 0}, Obtenido: $verificacionStatus")
        }

        if (verificacionRole == (user.role_id ?: -1) && verificacionStatus == (user.status_id ?: 0)) {
            Log.i("SessionManager", "✅ Todos los datos guardados correctamente")
        }
    }

    fun getUserRole(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val role = prefs.getInt("role_id", -1)

        Log.i("SessionManager", "═══════════════════════════════════")
        Log.i("SessionManager", "📖 LEYENDO ROL DEL USUARIO")
        Log.i("SessionManager", "   - Role ID leído: $role")

        // Listar todas las claves guardadas para debug
        val allKeys = prefs.all
        Log.i("SessionManager", "   - Todas las claves en SharedPreferences:")
        allKeys.forEach { (key, value) ->
            Log.i("SessionManager", "     $key = $value")
        }
        Log.i("SessionManager", "═══════════════════════════════════")

        return role
    }

    // 🔥 FUNCIÓN CORREGIDA - Ahora usa el mismo PREFS_NAME
    fun getUserStatus(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val statusId = if (prefs.contains(KEY_STATUS_ID)) {
            prefs.getInt(KEY_STATUS_ID, 0)
        } else null

        Log.i("SessionManager", "═══════════════════════════════════")
        Log.i("SessionManager", "📖 LEYENDO STATUS DEL USUARIO")
        Log.i("SessionManager", "   - Status ID leído: $statusId")
        Log.i("SessionManager", "   - Clave existe: ${prefs.contains(KEY_STATUS_ID)}")

        // Debug: Listar todas las claves
        val allKeys = prefs.all
        Log.i("SessionManager", "   - Todas las claves en SharedPreferences:")
        allKeys.forEach { (key, value) ->
            Log.i("SessionManager", "     $key = $value")
        }
        Log.i("SessionManager", "═══════════════════════════════════")

        return statusId
    }

    // 🔥 FUNCIÓN ADICIONAL - Por si necesitas guardar status manualmente
    fun saveUserStatus(context: Context, statusId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val success = prefs.edit()
            .putInt(KEY_STATUS_ID, statusId)
            .commit()

        Log.i("SessionManager", "═══════════════════════════════════")
        Log.i("SessionManager", "💾 GUARDANDO STATUS_ID")
        Log.i("SessionManager", "   - Status ID: $statusId")
        Log.i("SessionManager", "   - Guardado exitoso: $success")

        // Verificar
        val verificacion = prefs.getInt(KEY_STATUS_ID, -999)
        Log.i("SessionManager", "   - Verificación: $verificacion")
        Log.i("SessionManager", "═══════════════════════════════════")
    }
}