package com.wilkins.safezone.backend.network.Moderator

import io.github.jan.supabase.postgrest.postgrest
import com.wilkins.safezone.backend.network.supabaseClient

class NewsRepository {

    suspend fun save(request: NewsRequest) {
        supabaseClient.postgrest["news"].insert(request)
    }
}
