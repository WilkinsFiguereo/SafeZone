package com.wilkins.safezone.backend.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://qkaknqaxezztqnugawrd.supabase.co",
        supabaseKey = "sb_publishable_KPy7OjlicNHvX6JarLTGVw_PMGX1xRE"
    ) {
        // 1. Instalar Auth (Fundamental para client.auth y login)
        install(Auth)

        // 2. Instalar Postgrest (Para consultas a la base de datos)
        install(Postgrest)

        // 3. Instalar Storage (Para manejo de imágenes/archivos)
        install(Storage)

        // 4. Instalar Realtime (Para que los comentarios aparezcan al instante)
        install(Realtime)
    }
}
