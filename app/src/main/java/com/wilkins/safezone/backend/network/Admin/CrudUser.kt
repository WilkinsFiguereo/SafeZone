package com.wilkins.safezone.backend.network.Admin

import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.jan.supabase.postgrest.query.Columns

// 🔹 Modelo de datos para Roles
@Serializable
data class Role(
    @SerialName("id")
    val id: Int = 0,

    @SerialName("name")
    val name: String = ""
)

// 🔹 Modelo de datos para Usuario de Auth
@Serializable
data class AuthUser(
    @SerialName("email")
    val email: String? = null
)

// 🔹 Modelo de datos para Perfiles
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

    @SerialName("roles")
    val rol: Role? = null,

    @SerialName("status_id")
    val statusId: Int = 0,

    @SerialName("created_at")
    val createdAt: String = "",

    @SerialName("updated_at")
    val updatedAt: String = "",

    // 👇 Relación con auth.users
    @SerialName("auth_users")
    val auth: AuthUser? = null
)

// 🔹 Servicio para manejar operaciones de Profile
class ProfileService {

    private val supabase = SupabaseService.getInstance()

    /**
     * Obtener todos los perfiles con roles y correos
     */
    suspend fun getAllProfiles(): List<Profile> {
        return try {
            supabase
                .from("profiles")
                .select(Columns.raw("*, roles(name), auth_users(email)"))
                .decodeList<Profile>()
        } catch (e: Exception) {
            println("Error al obtener perfiles: ${e.message}")
            emptyList()
        }
    }

    /**
     * Obtener perfil por ID (incluye email y rol)
     */
    suspend fun getProfileById(userId: String): Profile? {
        return try {
            supabase
                .from("profiles")
                .select(Columns.raw("*, roles(name), auth_users(email)")) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<Profile>()
        } catch (e: Exception) {
            println("Error al obtener perfil: ${e.message}")
            null
        }
    }

    /**
     * Obtener perfiles filtrados por rol (incluye email)
     */
    suspend fun getProfilesByRole(roleId: Int): List<Profile> {
        return try {
            supabase
                .from("profiles")
                .select(Columns.raw("*, roles(name), auth_users(email)")) {
                    filter {
                        eq("role_id", roleId)
                    }
                }
                .decodeList<Profile>()
        } catch (e: Exception) {
            println("Error al obtener perfiles por rol: ${e.message}")
            emptyList()
        }
    }
}
