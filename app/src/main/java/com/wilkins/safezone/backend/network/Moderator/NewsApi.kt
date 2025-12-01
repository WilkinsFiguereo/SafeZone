package com.wilkins.safezone.backend.network.Moderator

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

interface NewsApi {

    @POST("news/create")
    suspend fun saveNews(
        @Body request: NewsRequest
    ): Response<Unit>
}
