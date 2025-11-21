package com.wilkins.safezone.backend.network.Admin.CrudUser

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

    @SerialName("email")
    val email: String? = null,

    @SerialName("address")
    val address: String? = null,

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

// 🔹 DTO para actualizar el perfil
@Serializable
data class ProfileUpdateDTO(
    @SerialName("name")
    val name: String? = null,

    @SerialName("phone")
    val phone: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("address")
    val address: String? = null,

    @SerialName("role_id")
    val roleId: Int? = null,

    @SerialName("status_id")
    val statusId: Int? = null
)

// 🔹 Servicio para manejar operaciones de Profile
class CrudUser {

    private val supabase = SupabaseService.getInstance()

    /**
     * Obtener todos los roles disponibles
     */
    suspend fun getAllRoles(): List<Role> {
        return try {
            supabase
                .from("roles")
                .select()
                .decodeList<Role>()
        } catch (e: Exception) {
            println("❌ Error al obtener roles: ${e.message}")
            emptyList()
        }
    }

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

    /**
     * Actualizar perfil de usuario
     */
    suspend fun updateUserProfile(user: Usuario): Boolean {
        return try {
            // Convertir estado a status_id
            val statusId = when (user.estado.lowercase()) {
                "activo" -> 1
                "inactivo" -> 2
                "pendiente" -> 3
                "bloqueado" -> 4
                else -> 1
            }

            val updateData = ProfileUpdateDTO(
                name = user.nombre,
                phone = user.telefono,
                email = user.email,
                address = user.address,
                roleId = user.roleId,
                statusId = statusId
            )

            println("🔄 Actualizando perfil en Supabase:")
            println("   ID: ${user.id}")
            println("   Nombre: ${updateData.name}")
            println("   Email: ${updateData.email}")
            println("   Teléfono: ${updateData.phone}")
            println("   Dirección: ${updateData.address}")
            println("   Role ID: ${updateData.roleId}")
            println("   Status ID: ${updateData.statusId}")

            supabase.from("profiles")
                .update(updateData) {
                    filter {
                        eq("id", user.id)
                    }
                }

            println("✅ Perfil actualizado exitosamente")
            true
        } catch (e: Exception) {
            println("❌ Error actualizando perfil: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Cambiar solo el estado del usuario (habilitar/deshabilitar)
     */
    suspend fun toggleUserStatus(userId: String, currentStatusId: Int): Boolean {
        return try {
            val newStatusId = if (currentStatusId == 1) 2 else 1

            println("🔄 Cambiando estado del usuario:")
            println("   ID: $userId")
            println("   Estado actual: $currentStatusId")
            println("   Nuevo estado: $newStatusId")

            val updateData = ProfileUpdateDTO(statusId = newStatusId)

            supabase.from("profiles")
                .update(updateData) {
                    filter {
                        eq("id", userId)
                    }
                }

            println("✅ Estado cambiado exitosamente")
            true
        } catch (e: Exception) {
            println("❌ Error cambiando estado: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}