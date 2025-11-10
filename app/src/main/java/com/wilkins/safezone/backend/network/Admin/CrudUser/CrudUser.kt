package com.wilkins.safezone.backend.network.Admin.CrudUser

import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    @SerialName("email")
    val email: String? = null,

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
    val updatedAt: String = ""
)


// 🔹 Servicio para manejar operaciones de Profile
class CrudUser {

    private val supabase = SupabaseService.getInstance()

    /**
     * Obtener todos los perfiles con roles
     */
    suspend fun getAllProfiles(): List<Profile> {
        return try {
            supabase
                .from("profiles")
                .select(Columns.raw("*, roles(name)"))
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
                .select(Columns.raw("*, roles(name)")) {
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
     * Obtener perfiles filtrados por rol
     */
    suspend fun getProfilesByRole(roleId: Int): List<Profile> {
        return try {
            supabase
                .from("profiles")
                .select(Columns.raw("*, roles(name)")) {
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

    suspend fun updateUserProfile(user: Usuario): Boolean {
        return try {
            val response = supabase.from("users")
                .update(user) {
                    filter {
                        eq("id", user.id)
                    }
                }

            response.data != null
        } catch (e: Exception) {
            println("❌ Error actualizando perfil: ${e.message}")
            false
        }
    }

}

