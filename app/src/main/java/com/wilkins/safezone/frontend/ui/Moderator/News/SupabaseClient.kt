package com.wilkins.safezone.backend.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://qkaknqaxezztqnugawrd.supabase.co",
        supabaseKey = "sb_publishable_KPy7OjlicNHvX6JarLTGVw_PMGX1xRE"
    ) {
        install(Postgrest)
        install(Storage)
    }
}
