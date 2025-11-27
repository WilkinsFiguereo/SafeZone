package com.wilkins.safezone.bridge.auth

import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.auth.login
import com.wilkins.safezone.backend.network.auth.LoginResult
import android.content.Context
import android.util.Log

/**
 * Excepción personalizada para cuentas deshabilitadas/baneadas
 */
class AccountDisabledException(val statusId: Int, message: String) : Exception(message)

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

            // ✅ Usar la función correcta que retorna LoginResult
            when (val result = login(context, email, password)) {
                is LoginResult.Success -> {
                    val user = result.user
                    Log.i("LoginBridge", "═══════════════════════════════════")
                    Log.i("LoginBridge", "✅ LOGIN EXITOSO")
                    Log.i("LoginBridge", "   - User ID: ${user.id}")
                    Log.i("LoginBridge", "   - User Email: ${user.email}")
                    Log.i("LoginBridge", "   - User Role: ${user.role_id}")
                    Log.i("LoginBridge", "   - User Status: ${user.status_id}")
                    Log.i("LoginBridge", "═══════════════════════════════════")

                    Result.success(user)
                }

                is LoginResult.AccountDisabled -> {
                    Log.w("LoginBridge", "═══════════════════════════════════")
                    Log.w("LoginBridge", "⚠️ CUENTA DESHABILITADA/BANEADA")
                    Log.w("LoginBridge", "   - Status ID: ${result.statusId}")

                    val message = when (result.statusId) {
                        2 -> "Tu cuenta ha sido deshabilitada temporalmente"
                        3 -> "Tu cuenta ha sido baneada permanentemente"
                        else -> "Tu cuenta no está activa"
                    }

                    Log.w("LoginBridge", "   - Mensaje: $message")
                    Log.w("LoginBridge", "═══════════════════════════════════")

                    // ✅ Lanzar excepción personalizada
                    Result.failure(AccountDisabledException(result.statusId, message))
                }

                is LoginResult.Error -> {
                    Log.e("LoginBridge", "═══════════════════════════════════")
                    Log.e("LoginBridge", "❌ ERROR EN LOGIN")
                    Log.e("LoginBridge", "   - Mensaje: ${result.message}")
                    Log.e("LoginBridge", "═══════════════════════════════════")

                    Result.failure(Exception(result.message))
                }
            }

        } catch (e: Exception) {
            Log.e("LoginBridge", "❌ Excepción inesperada en LoginBridge: ${e.message}", e)
            Log.i("LoginBridge", "═══════════════════════════════════")
            Result.failure(e)
        }
    }
}