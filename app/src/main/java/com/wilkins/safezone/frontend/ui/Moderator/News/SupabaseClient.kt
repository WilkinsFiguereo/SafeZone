package com.wilkins.safezone.frontend.ui.Moderator.News

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.storage

object SupabaseClientInstance {

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://qkaknqaxezztqnugawrd.supabase.co",
        supabaseKey = "sb_publishable_KPy7OjlicNHvX6JarLTGVw_PMGX1xRE"
    ) {
        install(io.github.jan.supabase.storage.Storage)
    }
}
