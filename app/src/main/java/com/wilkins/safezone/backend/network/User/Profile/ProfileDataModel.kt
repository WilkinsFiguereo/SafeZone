package com.wilkins.safezone.backend.network.User.Profile

import android.util.Log
import com.wilkins.safezone.backend.network.User.Form.Affair
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

@Serializable
data class UserReport(
    val id: String,
    val id_affair: Int,
    val description: String,
    val image_url: String?,
    val user_id: String,
    val is_anonymous: Boolean,
    val user_name: String?,
    val report_location: String?,
    val id_reporting_status: Int,
    val created_at: String
)

@Serializable
data class ReportWithAffair(
    val id: String,
    val description: String,
    val id_reporting_status: Int,
    val created_at: String,
    val affairType: String,
    val report_location: String?
)

suspend fun getUserReports(supabaseClient: SupabaseClient, userId: String): List<ReportWithAffair> {
    try {
        Log.d("ProfileReports", "🔍 Iniciando carga de reportes para userId: ${userId.take(8)}...")

        if (userId.isBlank()) {
            Log.e("ProfileReports", "❌ userId está vacío")
            return emptyList()
        }

        val allReports = supabaseClient.postgrest
            .from("reports")
            .select()
            .decodeList<UserReport>()

        Log.d("ProfileReports", "📊 Total de reportes: ${allReports.size}")

        val userReports = allReports.filter {
            it.user_id == userId && !it.is_anonymous
        }

        Log.d("ProfileReports", "✅ Reportes del usuario (no anónimos): ${userReports.size}")

        if (userReports.isEmpty()) {
            return emptyList()
        }

        val affairs = supabaseClient.postgrest
            .from("affair")
            .select()
            .decodeList<Affair>()

        val reportsWithAffairs = userReports.map { report ->
            val affair = affairs.find { it.id == report.id_affair }
            val affairType = affair?.type ?: "Desconocido"

            ReportWithAffair(
                id = report.id,
                description = report.description,
                id_reporting_status = report.id_reporting_status,
                created_at = report.created_at,
                affairType = affairType,
                report_location = report.report_location
            )
        }.sortedByDescending { it.created_at }

        Log.d("ProfileReports", "✅ Reportes procesados: ${reportsWithAffairs.size}")

        return reportsWithAffairs

    } catch (e: Exception) {
        Log.e("ProfileReports", "❌ Error cargando reportes: ${e.message}", e)
        return emptyList()
    }
}