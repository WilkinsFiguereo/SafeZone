package com.wilkins.safezone.backend.network.User.ReportSystem

import android.content.Intent
import android.util.Log
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ========================================
// MODELOS DE DATOS
// ========================================

@Serializable
data class ReportType(
    @SerialName("id")
    val id: Int,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("is_active")
    val isActive: Boolean = true
)

@Serializable
data class UserReport(
    @SerialName("id")
    val id: String? = null,

    @SerialName("reporter_id")
    val reporterId: String,

    @SerialName("reported_user_id")
    val reportedUserId: String? = null,

    @SerialName("report_type_id")
    val reportTypeId: Int,

    @SerialName("message")
    val message: String,

    @SerialName("status")
    val status: String = "pending",

    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class UserReportDetailed(
    @SerialName("id")
    val id: String,

    @SerialName("reporter_name")
    val reporterName: String?,

    @SerialName("reporter_email")
    val reporterEmail: String?,

    @SerialName("reported_user_name")
    val reportedUserName: String?,

    @SerialName("reported_user_email")
    val reportedUserEmail: String?,

    @SerialName("report_type_name")
    val reportTypeName: String,

    @SerialName("message")
    val message: String,

    @SerialName("status")
    val status: String,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("reviewed_by")
    val reviewedBy: String? = null,

    @SerialName("reviewer_name")
    val reviewerName: String? = null,

    @SerialName("resolution_notes")
    val resolutionNotes: String? = null
)

@Serializable
data class ReportUpdateDTO(
    @SerialName("status")
    val status: String? = null,

    @SerialName("reviewed_by")
    val reviewedBy: String? = null,

    @SerialName("reviewed_at")
    val reviewedAt: String? = null,

    @SerialName("resolution_notes")
    val resolutionNotes: String? = null
)

@Serializable
data class StatusUpdateDTO(
    @SerialName("status_id")
    val statusId: Int
)

// ========================================
// SERVICIO DE REPORTES
// ========================================

class ReportService {
    private val supabase = SupabaseService.getInstance()

    /**
     * Obtener todos los tipos de reporte disponibles
     */
    suspend fun getAllReportTypes(): Result<List<ReportType>> {
        return try {
            val types = supabase
                .from("report_types")
                .select {
                    filter {
                        eq("is_active", true)
                    }
                }
                .decodeList<ReportType>()

            Log.d("ReportService", "✅ Tipos de reporte obtenidos: ${types.size}")
            Result.success(types)
        } catch (e: Exception) {
            Log.e("ReportService", "❌ Error al obtener tipos de reporte: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Crear un nuevo reporte de usuario
     */
    suspend fun createUserReport(
        reporterId: String,
        reportedUserId: String?,
        reportTypeId: Int,
        message: String
    ): Result<UserReport> {
        return try {
            val report = UserReport(
                reporterId = reporterId,
                reportedUserId = reportedUserId,
                reportTypeId = reportTypeId,
                message = message
            )

            Log.d("ReportService", "📝 Creando reporte:")
            Log.d("ReportService", "   Reporter: $reporterId")
            Log.d("ReportService", "   Reported User: $reportedUserId")
            Log.d("ReportService", "   Type: $reportTypeId")

            val createdReport = supabase
                .from("user_reports")
                .insert(report) {
                    select()
                }
                .decodeSingle<UserReport>()

            Log.d("ReportService", "✅ Reporte creado exitosamente: ${createdReport.id}")
            Result.success(createdReport)
        } catch (e: Exception) {
            Log.e("ReportService", "❌ Error al crear reporte: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtener reportes con información detallada
     */
    suspend fun getAllReportsDetailed(): Result<List<UserReportDetailed>> {
        return try {
            val reports = supabase
                .from("user_reports_detailed")
                .select()
                .decodeList<UserReportDetailed>()

            Log.d("ReportService", "✅ Reportes detallados obtenidos: ${reports.size}")
            Result.success(reports)
        } catch (e: Exception) {
            Log.e("ReportService", "❌ Error al obtener reportes: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtener reportes pendientes
     */
    suspend fun getPendingReports(): Result<List<UserReportDetailed>> {
        return try {
            val reports = supabase
                .from("user_reports_detailed")
                .select {
                    filter {
                        eq("status", "pending")
                    }
                }
                .decodeList<UserReportDetailed>()

            Log.d("ReportService", "✅ Reportes pendientes: ${reports.size}")
            Result.success(reports)
        } catch (e: Exception) {
            Log.e("ReportService", "❌ Error al obtener reportes pendientes: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Actualizar estado de un reporte
     */
    suspend fun updateReportStatus(
        reportId: String,
        status: String,
        reviewerId: String,
        resolutionNotes: String? = null
    ): Result<Boolean> {
        return try {
            val updateData = ReportUpdateDTO(
                status = status,
                reviewedBy = reviewerId,
                reviewedAt = java.time.Instant.now().toString(),
                resolutionNotes = resolutionNotes
            )

            supabase
                .from("user_reports")
                .update(updateData) {
                    filter {
                        eq("id", reportId)
                    }
                }

            Log.d("ReportService", "✅ Reporte actualizado: $reportId -> $status")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("ReportService", "❌ Error al actualizar reporte: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Banear usuario (cambiar status_id a 3)
     */
    suspend fun banUser(userId: String, moderatorId: String, reason: String): Result<Boolean> {
        return try {
            Log.d("ReportService", "🔨 Baneando usuario: $userId")
            Log.d("ReportService", "   Moderador: $moderatorId")
            Log.d("ReportService", "   Razón: $reason")

            // Actualizar status_id a 3 (baneado)
            val statusUpdate = StatusUpdateDTO(statusId = 3)

            supabase
                .from("profiles")
                .update(statusUpdate) {
                    filter {
                        eq("id", userId)
                    }
                }

            Log.d("ReportService", "✅ Usuario baneado exitosamente")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("ReportService", "❌ Error al banear usuario: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Desbanear usuario (cambiar status_id a 1)
     */
    suspend fun unbanUser(userId: String): Result<Boolean> {
        return try {
            Log.d("ReportService", "✅ Desbaneando usuario: $userId")

            val statusUpdate = StatusUpdateDTO(statusId = 1)

            supabase
                .from("profiles")
                .update(statusUpdate) {
                    filter {
                        eq("id", userId)
                    }
                }

            Log.d("ReportService", "✅ Usuario desbaneado exitosamente")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("ReportService", "❌ Error al desbanear usuario: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Compartir perfil (crear Intent)
     */
    fun createShareProfileIntent(userName: String, userId: String): Intent {
        val shareText = """
            ¡Mira el perfil de $userName en SafeZone!
            
            ID de usuario: $userId
            
            #SafeZone #Profile
        """.trimIndent()

        return Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
    }
}