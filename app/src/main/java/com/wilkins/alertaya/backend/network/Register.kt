package com.wilkins.alertaya.backend.network

import android.util.Log
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

suspend fun registerUser(
    name: String,
    email: String,
    password: String,
    phone: String? = null,
    photoProfile: String? = null
): Boolean {
    val supabase = SupabaseService.getInstance()

    try {
        // --- PASO 1: Registrar el usuario en Auth ---
        // Esta función no devuelve nada. Lanza una excepción si falla.
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            // Pasamos 'name' en los metadatos, que puede ser útil para un Trigger
            this.data = buildJsonObject { put("name", name) }
        }

        println("✅ Petición de registro en Auth enviada exitosamente.")

        // --- PASO 2: Obtener el ID del usuario recién creado de la sesión ---
        val newUser = supabase.auth.currentUserOrNull() ?: run {
            // Este caso puede ocurrir si tienes "Confirm Email" activado.
            // El registro se envió, pero el usuario no ha iniciado sesión.
            // Para insertar el perfil, necesitaríamos el ID.
            // En este escenario, la mejor práctica es usar un TRIGGER en la base de datos.
            Log.w("SupabaseRegister", "Registro enviado, pero el usuario no está en la sesión (probablemente requiere confirmación de email). La inserción del perfil debe ser manejada por un Trigger de BD.")
            // Consideramos el registro exitoso a nivel de la app, ya que el email fue enviado.
            return true
        }

        println("Usuario en sesión con ID: ${newUser.id}")

        // --- PASO 3: Crear el perfil en la tabla `profiles` (si el usuario ya está en sesión) ---
        supabase.postgrest
            .from("profiles") // Asegúrate de que tu tabla se llame "profiles"
            .insert(
                mapOf(
                    "id" to newUser.id,          // El ID del usuario de auth.users
                    "name" to name,
                    "phone" to phone,
                    "photo_profile" to photoProfile,
                    "role_id" to 1,              // Asumiendo 1 = user
                    "status_id" to 1             // Asumiendo 1 = active
                )
            )

        println("✅ Perfil creado/insertado correctamente en la tabla 'profiles'.")
        return true

    } catch (e: Exception) {
        Log.e("SupabaseRegister", "❌ Error durante el proceso de registro: ${e.message}", e)
        return false
    }
}

