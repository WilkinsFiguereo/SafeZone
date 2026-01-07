package com.wilkins.safezone.bridge.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.wilkins.safezone.R
import com.wilkins.safezone.backend.network.auth.signInWithGoogle
import com.wilkins.safezone.backend.network.auth.signInWithGoogleAndGetUser
import kotlinx.coroutines.tasks.await

object GoogleSignInBridge {

    private var googleSignInClient: GoogleSignInClient? = null

    /**
     * Inicializa el cliente de Google Sign-In
     * Llama esto en tu Activity/ViewModel antes de usar el sign-in
     */
    fun initializeGoogleSignIn(context: Context): GoogleSignInClient {
        if (googleSignInClient == null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(/* context = */ context, /* options = */ gso)
        }
        return googleSignInClient!!
    }

    /**
     * Obtiene el Intent para iniciar el flujo de Google Sign-In
     */
    fun getSignInIntent(context: Context): Intent {
        val client = initializeGoogleSignIn(context)
        return client.signInIntent
    }

    /**
     * Procesa el resultado del Intent de Google Sign-In
     * Llama esto en onActivityResult o con ActivityResultLauncher
     */
    suspend fun handleSignInResult(
        context: Context,
        data: Intent?
    ): Result<Boolean> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            Log.d("GoogleSignIn", "✅ Cuenta obtenida: ${account?.email}")
            Log.d("GoogleSignIn", "🔑 ID Token disponible: ${account?.idToken != null}")

            if (account?.idToken == null) {
                Log.e("GoogleSignIn", "❌ ID Token es NULL")
                return Result.failure(Exception("No se pudo obtener el token de Google"))
            }

            Log.d("GoogleSignIn", "🔑 Token obtenido correctamente")

            // 🔥 Usar la nueva función que retorna Result<AppUser>
            val result = signInWithGoogleAndGetUser(context, account.idToken!!)

            result.fold(
                onSuccess = { user ->
                    Log.i("GoogleSignIn", "✅ Login con Google exitoso: ${user.name}")
                    Result.success(true)
                },
                onFailure = { e ->
                    Log.e("GoogleSignIn", "❌ Error al autenticar: ${e.message}")
                    Result.failure(e)
                }
            )
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "❌ ApiException: ${e.statusCode} - ${e.message}")
            Result.failure(Exception("Error de Google Sign-In: ${e.message}"))
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "❌ Exception: ${e.message}", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    /**
     * 🔥 Cierra sesión de Google completamente
     * Esto fuerza al usuario a elegir una cuenta la próxima vez
     */
    suspend fun signOut(context: Context): Result<Boolean> {
        return try {
            Log.i("GoogleSignIn", "═══════════════════════════════════")
            Log.i("GoogleSignIn", "🚪 Cerrando sesión de Google...")

            // Reinicializar el cliente para asegurar que esté actualizado
            initializeGoogleSignIn(context)

            // Cerrar sesión - esto limpia la cuenta cached
            googleSignInClient?.signOut()?.await()

            // Verificar que se cerró correctamente
            val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
            if (lastAccount == null) {
                Log.i("GoogleSignIn", "✅ Sesión de Google cerrada exitosamente")
                Log.i("GoogleSignIn", "   No hay cuenta de Google cached")
            } else {
                Log.w("GoogleSignIn", "⚠️ Aún hay una cuenta cached: ${lastAccount.email}")
                // Intentar revocar acceso para limpiar completamente
                googleSignInClient?.revokeAccess()?.await()
                Log.i("GoogleSignIn", "✅ Acceso revocado para limpiar cache")
            }

            Log.i("GoogleSignIn", "═══════════════════════════════════")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "❌ Error al cerrar sesión: ${e.message}", e)
            Result.failure(Exception("Error al cerrar sesión: ${e.message}"))
        }
    }

    /**
     * 🔥 Revoca el acceso completamente (más drástico que signOut)
     * Útil si quieres que el usuario re-autorice la app completamente
     */
    suspend fun revokeAccess(context: Context): Result<Boolean> {
        return try {
            Log.i("GoogleSignIn", "🔒 Revocando acceso de Google...")

            initializeGoogleSignIn(context)
            googleSignInClient?.revokeAccess()?.await()

            Log.i("GoogleSignIn", "✅ Acceso revocado exitosamente")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "❌ Error al revocar acceso: ${e.message}", e)
            Result.failure(Exception("Error al revocar acceso: ${e.message}"))
        }
    }

    /**
     * 🔥 Obtiene la cuenta de Google actualmente conectada (si existe)
     */
    fun getLastSignedInAccount(context: Context) = GoogleSignIn.getLastSignedInAccount(context)

    /**
     * 🔥 Verifica si hay una sesión de Google activa
     */
    fun isSignedIn(context: Context): Boolean {
        val account = getLastSignedInAccount(context)
        val isSignedIn = account != null
        Log.d("GoogleSignIn", "🔍 Verificando estado de Google Sign-In: $isSignedIn")
        if (isSignedIn) {
            Log.d("GoogleSignIn", "   Cuenta actual: ${account?.email}")
        }
        return isSignedIn
    }

    /**
     * 🔥 Limpia completamente el estado de Google Sign-In
     * Útil para debugging o cuando quieres asegurar un estado limpio
     */
    suspend fun clearGoogleSignInState(context: Context): Result<Boolean> {
        return try {
            Log.i("GoogleSignIn", "🧹 Limpiando estado completo de Google Sign-In...")

            // Primero sign out
            signOut(context)

            // Luego revocar acceso para estar seguros
            revokeAccess(context)

            // Reinicializar el cliente
            googleSignInClient = null
            initializeGoogleSignIn(context)

            Log.i("GoogleSignIn", "✅ Estado de Google Sign-In limpiado completamente")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "❌ Error al limpiar estado: ${e.message}", e)
            Result.failure(Exception("Error al limpiar estado: ${e.message}"))
        }
    }
}