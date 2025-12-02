package com.wilkins.safezone.backend.GlobalAssociation



import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.postgrest.from
import java.text.SimpleDateFormat
import java.util.*

class ReportsRepository {
    private val supabase = SupabaseService.getInstance()

    /**
     * Obtener todos los reportes
     */
    suspend fun getAllReports(): Result<List<ReportDto>> {
        return try {
            val reports = supabase.from("reports")
                .select()
                .decodeList<ReportDto>()
            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener reportes filtrados por estado
     */
    suspend fun getReportsByStatus(statusId: Int): Result<List<ReportDto>> {
        return try {
            val reports = supabase.from("reports")
                .select {
                    filter {
                        eq("id_reporting_status", statusId)
                    }
                }
                .decodeList<ReportDto>()
            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener un reporte específico por ID
     */
    suspend fun getReportById(reportId: String): Result<ReportDto?> {
        return try {
            val report = supabase.from("reports")
                .select {
                    filter {
                        eq("id", reportId)
                    }
                }
                .decodeSingleOrNull<ReportDto>()
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualizar el estado de un reporte
     */
    suspend fun updateReportStatus(reportId: String, newStatusId: Int): Result<Unit> {
        return try {
            supabase.from("reports")
                .update({
                    set("id_reporting_status", newStatusId)
                    set("last_update", getCurrentTimestamp())
                }) {
                    filter {
                        eq("id", reportId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener todos los affairs/asuntos
     */
    suspend fun getAllAffairs(): Result<List<AffairDto>> {
        return try {
            val affairs = supabase.from("affairs")
                .select()
                .decodeList<AffairDto>()
            Result.success(affairs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener un affair específico por ID
     */
    suspend fun getAffairById(affairId: Int): Result<AffairDto?> {
        return try {
            val affair = supabase.from("affairs")
                .select {
                    filter {
                        eq("id", affairId)
                    }
                }
                .decodeSingleOrNull<AffairDto>()
            Result.success(affair)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener todos los estados de reporte
     */
    suspend fun getAllReportingStatuses(): Result<List<ReportingStatusDto>> {
        return try {
            val statuses = supabase.from("reporting_status")
                .select()
                .decodeList<ReportingStatusDto>()
            Result.success(statuses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crear un nuevo reporte
     */
    suspend fun createReport(
        description: String,
        location: String,
        userId: String,
        affairId: Int?,
        isAnonymous: Boolean = false,
        userName: String? = null,
        imageUrl: String? = null
    ): Result<ReportDto> {
        return try {
            val report = supabase.from("reports")
                .insert(
                    mapOf(
                        "description" to description,
                        "report_location" to location,
                        "user_id" to userId,
                        "id_affair" to affairId,
                        "is_anonymous" to isAnonymous,
                        "user_name" to userName,
                        "image_url" to imageUrl,
                        "id_reporting_status" to 1, // Pendiente por defecto
                        "created_at" to getCurrentTimestamp(),
                        "last_update" to getCurrentTimestamp()
                    )
                ) {
                    select()
                }
                .decodeSingle<ReportDto>()
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualizar un reporte completo
     */
    suspend fun updateReport(
        reportId: String,
        description: String? = null,
        location: String? = null,
        affairId: Int? = null,
        imageUrl: String? = null
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any?>()
            description?.let { updates["description"] = it }
            location?.let { updates["report_location"] = it }
            affairId?.let { updates["id_affair"] = it }
            imageUrl?.let { updates["image_url"] = it }
            updates["last_update"] = getCurrentTimestamp()

            supabase.from("reports")
                .update(updates) {
                    filter {
                        eq("id", reportId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Eliminar un reporte (soft delete - cambiar a estado cancelado)
     */
    suspend fun deleteReport(reportId: String): Result<Unit> {
        return try {
            supabase.from("reports")
                .update({
                    set("id_reporting_status", 4) // 4 = Cancelado
                    set("last_update", getCurrentTimestamp())
                }) {
                    filter {
                        eq("id", reportId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Buscar reportes por texto
     */
    suspend fun searchReports(query: String): Result<List<ReportDto>> {
        return try {
            val reports = supabase.from("reports")
                .select {
                    filter {
                        or {
                            ilike("description", "%$query%")
                            ilike("report_location", "%$query%")
                        }
                    }
                }
                .decodeList<ReportDto>()
            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener reportes de un usuario específico
     */
    suspend fun getReportsByUser(userId: String): Result<List<ReportDto>> {
        return try {
            val reports = supabase.from("reports")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<ReportDto>()
            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener estadísticas de reportes
     */
    suspend fun getReportsStatistics(): Result<Map<Int, Int>> {
        return try {
            val reports = getAllReports().getOrThrow()
            val stats = reports.groupingBy { it.idReportingStatus }
                .eachCount()
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener timestamp actual en formato UTC
     */
    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date())
    }
}
