package com.wilkins.alertaya.backend.network

import android.util.Log
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import com.wilkins.alertaya.backend.network.AppUser

suspend fun login(email: String, password: String): AppUser? {
    val client = SupabaseService.getInstance()

    return try {
        println("Intentando login con email: $email y password: $password")
        client.postgrest["users"].select(
            // ¡Vuelve a solo los campos necesarios!
        columns = Columns.list("id", "role", "status")
        ) {
            filter {
                eq("email", email)
                // ¡Vuelve a incluir el filtro de password! (Asumiendo que ya verificaste que "12345" es exacto)
                eq("password", password)
                // ¡Vuelve a incluir el filtro de status!
                eq("status", "active")
            }
        }.decodeSingleOrNull<AppUser>()


    } catch (e: Exception) {
        Log.e("SupabaseLogin", "Error durante el login: ${e.message}")
        null
    }
}