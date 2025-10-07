package com.wilkins.alertaya.backend.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

class SupabaseService private constructor() {

    companion object {
        private var instance: SupabaseClient? = null

        fun getInstance(): SupabaseClient {
            if (instance == null) {
                instance = createSupabaseClient(
                    supabaseUrl = "https://qkaknqaxezztqnugawrd.supabase.co",
                    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFrYWtucWF4ZXp6dHFudWdhd3JkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk1MTkwMzAsImV4cCI6MjA3NTA5NTAzMH0.fYVmKEWqojR4jSut48j1c3a3oaqqkeR48-D3cBn764s"
                ) {
                    install(Postgrest)
                }
            }
            return instance!!
        }
    }
}


