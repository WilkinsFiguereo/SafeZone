package com.wilkins.safezone.backend.network.GlobalAssociation

import android.util.Log
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.postgrest.from
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "ReportsRepository"

class ReportsRepository {
    private val supabase = SupabaseService.getInstance()

    init {
        Log.d(TAG, "Repository inicializado")
    }

    /**
     * Obtener todos los reportes
     */
    suspend fun getAllReports(): Result<List<ReportDto>> {
        return try {
            Log.d(TAG, "Iniciando consulta de todos los reportes...")
            val reports = supabase.from("reports")
                .select()
                .decodeList<ReportDto>()
            Log.d(TAG, "Consulta exitosa: ${reports.size} reportes obtenidos")
            Log.d(TAG, "Primer reporte (si existe): ${reports.firstOrNull()}")
            Result.success(reports)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener reportes: ${e.message}", e)
            Log.e(TAG, "Tipo de excepción: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }

    /**
     * Obtener reportes filtrados por estado
     */
    suspend fun getReportsByStatus(statusId: Int): Result<List<ReportDto>> {
        return try {
            Log.d(TAG, "Consultando reportes con estado: $statusId")
            val reports = supabase.from("reports")
                .select {
                    filter {
                        eq("id_reporting_status", statusId)
                    }
                }
                .decodeList<ReportDto>()
            Log.d(TAG, "Reportes con estado $statusId: ${reports.size}")
            Result.success(reports)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener reportes por estado $statusId: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getReportsByMultipleStatus(
        statusIds: Set<Int> = setOf(1, 2, 3, 4)
    ): Result<List<ReportDto>> {
        return try {
            Log.d(TAG, "Consultando reportes con estados: $statusIds")
            val reports = supabase.from("reports")
                .select {
                    filter {
                        or {
                            statusIds.forEach { id ->
                                eq("id_reporting_status", id)
                            }
                        }
                    }
                }
                .decodeList<ReportDto>()
            Log.d(TAG, "Reportes obtenidos con múltiples estados: ${reports.size}")
            Result.success(reports)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener reportes por múltiples estados: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtener un reporte específico por ID
     */
    suspend fun getReportById(reportId: String): Result<ReportDto?> {
        return try {
            Log.d(TAG, "Consultando reporte con ID: $reportId")
            val report = supabase.from("reports")
                .select {
                    filter {
                        eq("id", reportId)
                    }
                }
                .decodeSingleOrNull<ReportDto>()
            Log.d(TAG, "Reporte encontrado: ${report != null}")
            Result.success(report)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener reporte por ID $reportId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Actualizar el estado de un reporte
     */
    suspend fun updateReportStatus(reportId: String, newStatusId: Int): Result<Unit> {
        return try {
            Log.d(TAG, "Actualizando estado del reporte $reportId a estado $newStatusId")
            supabase.from("reports")
                .update({
                    set("id_reporting_status", newStatusId)
                    set("last_update", getCurrentTimestamp())
                }) {
                    filter {
                        eq("id", reportId)
                    }
                }
            Log.d(TAG, "Estado actualizado exitosamente para reporte $reportId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar estado del reporte $reportId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtener todos los affairs/asuntos
     * CORREGIDO: La tabla se llama "affair" (singular) no "affairs"
     */
    suspend fun getAllAffairs(): Result<List<AffairDto>> {
        return try {
            Log.d(TAG, "Consultando todos los affairs desde la tabla 'affair'...")
            val affairs = supabase.from("affair")  // ← CAMBIO AQUÍ: "affair" en lugar de "affairs"
                .select()
                .decodeList<AffairDto>()
            Log.d(TAG, "Affairs obtenidos: ${affairs.size}")
            Log.d(TAG, "Primer affair (si existe): ${affairs.firstOrNull()}")
            Result.success(affairs)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener affairs: ${e.message}", e)
            Log.e(TAG, "Tipo de excepción: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }

    /**
     * Obtener un affair específico por ID
     * CORREGIDO: La tabla se llama "affair" (singular) no "affairs"
     */
    suspend fun getAffairById(affairId: Int): Result<AffairDto?> {
        return try {
            Log.d(TAG, "Consultando affair con ID: $affairId")
            val affair = supabase.from("affair")  // ← CAMBIO AQUÍ: "affair" en lugar de "affairs"
                .select {
                    filter {
                        eq("id", affairId)
                    }
                }
                .decodeSingleOrNull<AffairDto>()
            Log.d(TAG, "Affair encontrado: ${affair != null}")
            Result.success(affair)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener affair por ID $affairId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtener todos los estados de reporte
     */
    suspend fun getAllReportingStatuses(): Result<List<ReportingStatusDto>> {
        return try {
            Log.d(TAG, "Consultando todos los estados de reporte...")
            val statuses = supabase.from("reporting_status")
                .select()
                .decodeList<ReportingStatusDto>()
            Log.d(TAG, "Estados obtenidos: ${statuses.size}")
            Log.d(TAG, "Primer estado (si existe): ${statuses.firstOrNull()}")
            Result.success(statuses)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener estados de reporte: ${e.message}", e)
            Log.e(TAG, "Tipo de excepción: ${e.javaClass.simpleName}")
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
            Log.d(TAG, "Creando nuevo reporte para usuario: $userId")
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
            Log.d(TAG, "Reporte creado exitosamente con ID: ${report.id}")
            Result.success(report)
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear reporte: ${e.message}", e)
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
            Log.d(TAG, "Actualizando reporte: $reportId")
            val updates = mutableMapOf<String, Any?>()
            description?.let {
                updates["description"] = it
                Log.d(TAG, "  - Actualizando descripción")
            }
            location?.let {
                updates["report_location"] = it
                Log.d(TAG, "  - Actualizando ubicación")
            }
            affairId?.let {
                updates["id_affair"] = it
                Log.d(TAG, "  - Actualizando affair ID")
            }
            imageUrl?.let {
                updates["image_url"] = it
                Log.d(TAG, "  - Actualizando imagen URL")
            }
            updates["last_update"] = getCurrentTimestamp()

            supabase.from("reports")
                .update(updates) {
                    filter {
                        eq("id", reportId)
                    }
                }
            Log.d(TAG, "Reporte $reportId actualizado exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar reporte $reportId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Eliminar un reporte (soft delete - cambiar a estado cancelado)
     */
    suspend fun deleteReport(reportId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Eliminando reporte (soft delete): $reportId")
            supabase.from("reports")
                .update({
                    set("id_reporting_status", 4) // 4 = Cancelado
                    set("last_update", getCurrentTimestamp())
                }) {
                    filter {
                        eq("id", reportId)
                    }
                }
            Log.d(TAG, "Reporte $reportId eliminado exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar reporte $reportId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Buscar reportes por texto
     */
    suspend fun searchReports(query: String): Result<List<ReportDto>> {
        return try {
            Log.d(TAG, "Buscando reportes con query: $query")
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
            Log.d(TAG, "Reportes encontrados: ${reports.size}")
            Result.success(reports)
        } catch (e: Exception) {
            Log.e(TAG, "Error al buscar reportes: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtener reportes de un usuario específico
     */
    suspend fun getReportsByUser(userId: String): Result<List<ReportDto>> {
        return try {
            Log.d(TAG, "Consultando reportes del usuario: $userId")
            val reports = supabase.from("reports")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<ReportDto>()
            Log.d(TAG, "Reportes del usuario $userId: ${reports.size}")
            Result.success(reports)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener reportes del usuario $userId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtener estadísticas de reportes
     */
    suspend fun getReportsStatistics(): Result<Map<Int, Int>> {
        return try {
            Log.d(TAG, "Calculando estadísticas de reportes...")
            val reports = getAllReports().getOrThrow()
            val stats = reports.groupingBy { it.idReportingStatus }
                .eachCount()
            Log.d(TAG, "Estadísticas calculadas: $stats")
            Result.success(stats)
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular estadísticas: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtener timestamp actual en formato UTC
     */
    private fun getCurrentTimestamp(): String {
        val timestamp = try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            dateFormat.format(Date())
        } catch (e: Exception) {
            Log.e(TAG, "Error al generar timestamp: ${e.message}", e)
            throw e
        }
        Log.d(TAG, "Timestamp generado: $timestamp")
        return timestamp
    }
}