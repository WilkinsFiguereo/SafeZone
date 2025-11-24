import android.content.Context
import android.util.Log
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SessionManager {

    private const val PREFS_NAME = "supabase_session_prefs"
    private const val KEY_SESSION = "session_data"

    fun saveSession(context: Context, session: UserSession) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonSession = Json.encodeToString(session)
            prefs.edit().putString(KEY_SESSION, jsonSession).apply()
            Log.i("SessionManager", "✅ Sesión guardada correctamente.")
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

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_SESSION).apply()
        Log.i("SessionManager", "🧹 Sesión eliminada correctamente.")
    }

    suspend fun logout(context: Context, supabaseClient: SupabaseClient) {
        try {
            // 1. Cerrar sesión en Supabase
            supabaseClient.auth.signOut()
            Log.i("SessionManager", "🚪 Sesión cerrada en Supabase.")

            // 2. Limpiar datos locales
            clearSession(context)

            Log.i("SessionManager", "✅ Logout completado exitosamente.")
        } catch (e: Exception) {
            Log.e("SessionManager", "❌ Error durante logout: ${e.message}", e)
            // Limpiar sesión local aunque falle la llamada a Supabase
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
        Log.i("SessionManager", "   - Role ID (con ?:-1): ${user.role_id ?: -1}")
        Log.i("SessionManager", "═══════════════════════════════════")

        val editor = prefs.edit()
        editor.putString("user_id", user.id)
        editor.putInt("role_id", user.role_id ?: -1)
        val success = editor.commit() // Usar commit() en vez de apply() para verificar

        Log.i("SessionManager", "   - Commit exitoso: $success")

        // Verificar inmediatamente después de guardar
        val verificacion = prefs.getInt("role_id", -999)
        Log.i("SessionManager", "✅ Verificación inmediata: role_id = $verificacion")

        if (verificacion != (user.role_id ?: -1)) {
            Log.e("SessionManager", "❌ ERROR CRÍTICO: El valor NO se guardó correctamente!")
            Log.e("SessionManager", "   Esperado: ${user.role_id ?: -1}, Obtenido: $verificacion")
        } else {
            Log.i("SessionManager", "✅ Datos guardados correctamente")
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



}
