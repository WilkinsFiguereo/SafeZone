package com.wilkins.safezone.backend.network.auth

import android.content.Context
import android.util.Log
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

/**
 * Modelo de datos para el perfil de usuario
 * Debe coincidir con la estructura de tu tabla 'profiles' en Supabase
 */
@Serializable
data class ProfileGoogle(
    val id: String,
    val name: String,
    val phone: String? = null,
    val photo_profile: String? = null,
    val role_id: Int = 1,
    val status_id: Int = 1
)

/**
 * Autenticación con Google usando el ID Token
 * Este token se obtiene del Google Sign-In SDK
 *
 * @param context Contexto de Android para guardar la sesión
 * @param idToken Token de ID obtenido de Google Sign-In
 * @return true si la autenticación fue exitosa, false en caso contrario
 */
suspend fun signInWithGoogle(
    context: Context,
    idToken: String
): Boolean {
    val supabase = SupabaseService.getInstance()

    return try {
        Log.i("GoogleAuth", "🔄 Iniciando autenticación con Google...")

        // Autenticar con Supabase usando el ID Token de Google
        supabase.auth.signInWith(IDToken) {
            this.idToken = idToken
            this.provider = Google
        }

        // Obtener y guardar la sesión
        val session = supabase.auth.currentSessionOrNull()

        if (session != null) {
            // 🔥 Guardar sesión marcándola como autenticación de Google
            SessionManager.saveSession(context, session, isGoogleAuth = true)

            // Obtener información del usuario
            val user = supabase.auth.currentUserOrNull()
            Log.i("GoogleAuth", "✅ Usuario autenticado: ${user?.email}")
            Log.i("GoogleAuth", "📋 User ID: ${user?.id}")
            Log.i("GoogleAuth", "📋 Metadata: ${user?.userMetadata}")

            // Verificar/crear perfil en la base de datos
            user?.let {
                ensureProfileExists(context, it.id, it.userMetadata)
            }

            true
        } else {
            Log.e("GoogleAuth", "❌ No se pudo obtener la sesión")
            false
        }
    } catch (e: Exception) {
        Log.e("GoogleAuth", "❌ Error en autenticación con Google: ${e.message}", e)
        e.printStackTrace()
        false
    }
}

/**
 * 🔥 Autenticación con Google que retorna el usuario completo
 * Usa esta función si necesitas el objeto AppUser después del login
 */
suspend fun signInWithGoogleAndGetUser(
    context: Context,
    idToken: String
): Result<AppUser> {
    val supabase = SupabaseService.getInstance()

    return try {
        Log.i("GoogleAuth", "🔄 Iniciando autenticación con Google...")

        // Autenticar con Supabase usando el ID Token de Google
        supabase.auth.signInWith(IDToken) {
            this.idToken = idToken
            this.provider = Google
        }

        // Obtener y guardar la sesión
        val session = supabase.auth.currentSessionOrNull()

        if (session != null) {
            // 🔥 Guardar sesión marcándola como autenticación de Google
            SessionManager.saveSession(context, session, isGoogleAuth = true)

            // Obtener información del usuario
            val user = supabase.auth.currentUserOrNull()
            Log.i("GoogleAuth", "✅ Usuario autenticado: ${user?.email}")
            Log.i("GoogleAuth", "📋 User ID: ${user?.id}")
            Log.i("GoogleAuth", "📋 Metadata: ${user?.userMetadata}")

            // Verificar/crear perfil en la base de datos
            user?.let {
                ensureProfileExists(context, it.id, it.userMetadata)
            }

            // 🔥 Obtener el perfil completo del usuario
            val profile = getCurrentUserProfile(context)

            if (profile != null) {
                Log.i("GoogleAuth", "✅ Perfil obtenido exitosamente: ${profile.name}")
                Result.success(profile)
            } else {
                Log.e("GoogleAuth", "❌ No se pudo obtener el perfil del usuario")
                Result.failure(Exception("No se pudo obtener el perfil del usuario"))
            }
        } else {
            Log.e("GoogleAuth", "❌ No se pudo obtener la sesión")
            Result.failure(Exception("No se pudo obtener la sesión"))
        }
    } catch (e: Exception) {
        Log.e("GoogleAuth", "❌ Error en autenticación con Google: ${e.message}", e)
        e.printStackTrace()
        Result.failure(e)
    }
}

/**
 * Asegura que el perfil del usuario exista en la base de datos
 * Si es la primera vez que inicia sesión con Google, crea el perfil
 *
 * @param context Contexto de Android
 * @param userId ID del usuario autenticado
 * @param metadata Metadatos del usuario de Google (nombre, foto, etc.)
 */
private suspend fun ensureProfileExists(
    context: Context,
    userId: String,
    metadata: Map<String, Any?>?
) {
    val supabase = SupabaseService.getInstance()

    try {
        Log.i("GoogleAuth", "🔍 Verificando si el perfil existe para user: $userId")

        // Intentar obtener el perfil existente
        val response = supabase.from("profiles")
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingleOrNull<ProfileGoogle>()

        if (response == null) {
            Log.i("GoogleAuth", "📝 Perfil no existe, creando uno nuevo...")

            // Si no existe, crear uno nuevo con los datos de Google
            // Intentar obtener el nombre de diferentes campos posibles
            val name = metadata?.get("name") as? String
                ?: metadata?.get("full_name") as? String
                ?: metadata?.get("given_name") as? String
                ?: "Usuario"

            // Intentar obtener la foto de perfil
            val photoUrl = metadata?.get("avatar_url") as? String
                ?: metadata?.get("picture") as? String
                ?: metadata?.get("photo_url") as? String

            Log.i("GoogleAuth", "📋 Datos del nuevo perfil - Name: $name, Photo: $photoUrl")

            val newProfile = mapOf(
                "id" to userId,
                "name" to name,
                "photo_profile" to photoUrl,
                "role_id" to 1,
                "status_id" to 1
            )

            supabase.from("profiles").insert(newProfile)
            Log.i("GoogleAuth", "✅ Perfil creado exitosamente para usuario de Google")
        } else {
            Log.i("GoogleAuth", "✅ Perfil existente encontrado: ${response.name}")
        }
    } catch (e: Exception) {
        Log.e("GoogleAuth", "⚠️ Error al verificar/crear perfil: ${e.message}", e)
        e.printStackTrace()
        // No lanzamos error porque la autenticación fue exitosa
        // El perfil se puede crear manualmente después o en otro momento
    }
}

/**
 * 🔥 Obtiene el perfil del usuario actual después de Google Sign-In
 * Útil si necesitas los datos del usuario después del login
 */
suspend fun getCurrentUserProfile(context: Context): AppUser? {
    val supabase = SupabaseService.getInstance()

    return try {
        // Obtener el ID del usuario autenticado
        val userId = supabase.auth.currentUserOrNull()?.id

        if (userId == null) {
            Log.e("GoogleAuth", "❌ No hay usuario autenticado")
            return null
        }

        Log.i("GoogleAuth", "🔍 Obteniendo perfil para user: $userId")

        // Obtener el perfil de la base de datos
        val profile = supabase.from("profiles")
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingleOrNull<AppUser>()

        if (profile != null) {
            Log.i("GoogleAuth", "✅ Perfil obtenido: ${profile.name}")
            Log.i("GoogleAuth", "📋 Role ID: ${profile.role_id}")
            Log.i("GoogleAuth", "📋 Status ID: ${profile.status_id}")

            // Guardar los datos del usuario
            SessionManager.saveUserData(context, profile)
        } else {
            Log.e("GoogleAuth", "❌ No se encontró el perfil del usuario")
        }

        profile
    } catch (e: Exception) {
        Log.e("GoogleAuth", "❌ Error al obtener perfil: ${e.message}", e)
        e.printStackTrace()
        null
    }
}