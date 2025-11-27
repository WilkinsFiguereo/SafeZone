
package com.wilkins.safezone.backend.network

import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.gotrue.Auth


class SupabaseService private constructor() {

    companion object {
        private var instance: SupabaseClient? = null

        // 🔹 Constantes de configuración - ACTUALIZA ESTOS VALORES
        private const val SUPABASE_URL = "https://qkaknqaxezztqnugawrd.supabase.co"
        private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFrYWtucWF4ZXp6dHFudWdhd3JkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk1MTkwMzAsImV4cCI6MjA3NTA5NTAzMH0.fYVmKEWqojR4jSut48j1c3a3oaqqkeR48-D3cBn764s" // ← Reemplaza con tu anon key actual

        fun getInstance(): SupabaseClient {
            if (instance == null) {
                instance = createSupabaseClient(
                    supabaseUrl = SUPABASE_URL,
                    supabaseKey = SUPABASE_KEY
                ) {
                    install(Auth)
                    install(Postgrest)
                    install(Storage)
                }
            }
            return instance!!
        }

        // 🔹 Propiedades de extensión para acceder a la configuración
        val SupabaseClient.supabaseHttpUrl: String
            get() = SUPABASE_URL

        val SupabaseClient.supabaseKey: String
            get() = SUPABASE_KEY
    }
}