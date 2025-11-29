import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NewsController {

    @Headers(
        "apikey: TU_SUPABASE_KEY",
        "Authorization: Bearer TU_SUPABASE_KEY",
        "Content-Type: application/json"
    )
    @POST("rest/v1/news")
    suspend fun saveNews(
        @Body request: NewsRequest
    ): List<News> // Supabase SIEMPRE devuelve una lista
}
