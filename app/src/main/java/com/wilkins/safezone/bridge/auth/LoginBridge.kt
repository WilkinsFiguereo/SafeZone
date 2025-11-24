package com.wilkins.safezone.bridge.auth

import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.auth.login
import android.content.Context
import android.util.Log


/**
 * Esta clase actúa como un puente entre la UI (Compose)
 * y la capa de red (Supabase, API, etc).
 */
object LoginBridge {

    suspend fun performLogin(context: Context, email: String, password: String): Result<AppUser> {
        return try {
            Log.i("LoginBridge", "═══════════════════════════════════")
            Log.i("LoginBridge", "🌉 LoginBridge.performLogin()")
            Log.i("LoginBridge", "   - Email: $email")

            val user = login(context, email, password)

            Log.i("LoginBridge", "═══════════════════════════════════")
            Log.i("LoginBridge", "📦 RESULTADO DEL LOGIN")
            Log.i("LoginBridge", "   - User: $user")
            Log.i("LoginBridge", "   - User es null?: ${user == null}")

            if (user != null) {
                Log.i("LoginBridge", "   - User ID: ${user.id}")
                Log.i("LoginBridge", "   - User Email: ${user.email}")
                Log.i("LoginBridge", "   - User Role: ${user.role_id}")

                // Verificar que se guardó correctamente
                val roleVerificado = SessionManager.getUserRole(context)
                Log.i("LoginBridge", "   - Role verificado en SP: $roleVerificado")

                if (roleVerificado != user.role_id) {
                    Log.e("LoginBridge", "❌ ADVERTENCIA: Discrepancia en role_id!")
                    Log.e("LoginBridge", "   Del usuario: ${user.role_id}")
                    Log.e("LoginBridge", "   De SharedPreferences: $roleVerificado")
                }

                Log.i("LoginBridge", "═══════════════════════════════════")
                Result.success(user)
            } else {
                Log.e("LoginBridge", "❌ Usuario es null")
                Log.i("LoginBridge", "═══════════════════════════════════")
                Result.failure(Exception("Credenciales inválidas o cuenta no verificada"))
            }
        } catch (e: Exception) {
            Log.e("LoginBridge", "❌ Excepción en LoginBridge: ${e.message}", e)
            Log.i("LoginBridge", "═══════════════════════════════════")
            Result.failure(e)
        }
    }
}