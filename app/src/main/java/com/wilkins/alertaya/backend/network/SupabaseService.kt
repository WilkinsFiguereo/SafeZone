//package com.wilkins.alertaya.backend.network
//
//import io.supabase.SupabaseClient
//
//class SupabaseService private constructor() {
//
//    companion object {
//        private var instance: SupabaseClient? = null
//
//        fun getInstance(): SupabaseClient {
//            if (instance == null) {
//                instance = SupabaseClient(
//                    url = "https://qkaknqaxezztqnugawrd.supabase.cohttps://supabase.com/dashboard/project/qkaknqaxezztqnugawrd", // Reemplaza con tu URL
//                    key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFrYWtucWF4ZXp6dHFudWdhd3JkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk1MTkwMzAsImV4cCI6MjA3NTA5NTAzMH0.fYVmKEWqojR4jSut48j1c3a3oaqqkeR48-D3cBn764s"                            // Reemplaza con tu anon/public key
//                )
//            }
//            return instance!!
//        }
//    }
//}
