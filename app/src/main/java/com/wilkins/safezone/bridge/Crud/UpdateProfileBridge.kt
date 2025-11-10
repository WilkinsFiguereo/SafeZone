package com.wilkins.safezone.bridge.profile

import android.content.Context
import com.wilkins.safezone.backend.network.Admin.CrudUser.Usuario
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object UpdateProfileBridge {

    suspend fun handleUpdateProfile(
        context: Context,
        usuario: Usuario
    ): Result<Boolean> {
        return try {
            val supabase = SupabaseService.getInstance()

            // ✅ Usar tipos explícitos y evitar Any
            val updateData = buildJsonObject {
                put("name", usuario.nombre ?: "")
                put("email", usuario.email ?: "")
                put("phone", usuario.telefono ?: "")
                put("photo_profile", usuario.photoProfile ?: "")
                put("role_id", usuario.roleId ?: 1)
                put("status_id", if (usuario.estado == "Activo") 1 else 2)
                // Dejar que Supabase maneje el updated_at automáticamente
            }

            println("🔄 Actualizando perfil con datos: $updateData")

            val response = supabase.from("profiles")
                .update(updateData) {
                    filter { eq("id", usuario.idCompleto) }
                }

            if (response.data != null) {
                println("✅ Perfil actualizado correctamente.")
                Result.success(true)
            } else {
                println("❌ No se pudo actualizar el perfil.")
                Result.failure(Exception("No se pudo actualizar el perfil del usuario."))
            }
        } catch (e: Exception) {
            println("❌ Error actualizando perfil: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}