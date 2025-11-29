package com.wilkins.safezone.backend.network.Moderator


import com.wilkins.safezone.backend.network.SupabaseService.supabase

class NewsService {

    suspend fun createNews(request: NewsRequest): News {
        val query = supabase
            .from("news")
            .insert(request)
            .select()q
            .single()

        return query.decodeAs<News>()
    }
}
