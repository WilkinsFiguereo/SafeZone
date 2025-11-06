package com.wilkins.safezone.backend.network.Admin

import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 🔹 Modelo de datos para Profile con email
@Serializable
data class Profile(
    @SerialName("idx")
    val idx: Int = 0,

    @SerialName("id")
    val id: String = "",

    @SerialName("name")
    val name: String = "",

    @SerialName("phone")
    val phone: String? = null,

    @SerialName("photo_profile")
    val photoProfile: String? = null,

    @SerialName("role_id")
    val roleId: Int = 0,

    @SerialName("status_id")
    val statusId: Int = 0,

    @SerialName("created_at")
    val createdAt: String = "",

    @SerialName("updated_at")
    val updatedAt: String = "",

    // Campo adicional para el email (no viene de la tabla profile)
    val email: String? = null
)

// 🔹 Servicio para manejar operaciones de Profile
class ProfileService {

    private val supabase = SupabaseService.getInstance()

    /**
     * Obtener el perfil de un usuario por ID con email desde auth
     * @param userId ID del usuario
     * @return Profile con email o null si no existe
     */
    suspend fun getProfileById(userId: String): Profile? {
        return try {
            // Obtener perfil base desde la tabla profile
            val profile = supabase
                .from("profile")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<Profile>()

            // Obtener email desde la tabla auth
            val email = getEmailFromAuth(userId)

            // Combinar los datos
            profile.copy(email = email)

        } catch (e: Exception) {
            println("Error al obtener perfil: ${e.message}")
            null
        }
    }

    /**
     * Obtener email desde la tabla auth de Supabase
     * @param userId ID del usuario
     * @return Email del usuario o null si no se encuentra
     */
    private suspend fun getEmailFromAuth(userId: String): String? {
        return try {
            // Usar el módulo de auth para obtener el usuario
            val user = supabase.auth.retrieveUser(userId)
            user.email
        } catch (e: Exception) {
            println("Error al obtener email desde auth: ${e.message}")
            null
        }
    }

    /**
     * Obtener todos los perfiles con sus emails
     * @return Lista de perfiles con emails
     */
    suspend fun getAllProfiles(): List<Profile> {
        return try {
            val profiles = supabase
                .from("profile")
                .select()
                .decodeList<Profile>()

            // Enriquecer cada perfil con su email
            profiles.map { profile ->
                val email = getEmailFromAuth(profile.id)
                profile.copy(email = email)
            }
        } catch (e: Exception) {
            println("Error al obtener perfiles: ${e.message}")
            emptyList()
        }
    }
}