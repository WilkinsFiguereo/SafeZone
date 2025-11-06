import android.content.Context
import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
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

}
