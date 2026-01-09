package com.wilkins.safezone.backend.network.Admin.PDF

import android.util.Log
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo actualizado para los reportes de incidencias según la estructura real de la BD
 */
@Serializable
data class ReportData(
    val id: String,

    @SerialName("id_affair")
    val idAffair: Int? = null,

    val description: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("is_anonymous")
    val isAnonymous: Boolean = false,

    @SerialName("user_name")
    val userName: String? = null,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("last_update")
    val lastUpdate: String? = null,

    @SerialName("report_location")
    val reportLocation: String? = null,

    @SerialName("id_reporting_status")
    val idReportingStatus: Int
) {
    // Propiedades calculadas para compatibilidad con el código existente
    val title: String
        get() = reportLocation ?: description?.take(50) ?: "Sin título"

    val statusId: Int
        get() = idReportingStatus

    val affairId: Int?
        get() = idAffair
}

/**
 * Repositorio actualizado para obtener reportes de incidencias
 */
class ReportRepository {
    private val client = SupabaseService.getInstance()

    /**
     * Obtiene todos los reportes
     */
    suspend fun getAllReports(): List<ReportData> {
        return try {
            Log.d("ReportRepository", "🔍 Obteniendo todos los reportes...")

            val reports = client.postgrest
                .from("reports")
                .select()
                .decodeList<ReportData>()

            Log.d("ReportRepository", "✅ Reportes obtenidos: ${reports.size}")

            if (reports.isNotEmpty()) {
                Log.d("ReportRepository", """
                    📊 Primer reporte: 
                    - ID: ${reports[0].id}
                    - Título: ${reports[0].title}
                    - Status: ${reports[0].idReportingStatus}
                    - Usuario: ${reports[0].userName ?: "Anónimo"}
                    - Ubicación: ${reports[0].reportLocation}
                """.trimIndent())
            }

            reports
        } catch (e: Exception) {
            Log.e("ReportRepository", "❌ Error obteniendo reportes: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtiene reportes por estado
     * @param statusId 1=Pendiente, 2=En Proceso, 3=Completado, 4=Cancelado
     */
    suspend fun getReportsByStatus(statusId: Int): List<ReportData> {
        return try {
            Log.d("ReportRepository", "🔍 Obteniendo reportes con id_reporting_status: $statusId...")

            val reports = client.postgrest
                .from("reports")
                .select {
                    filter {
                        eq("id_reporting_status", statusId)
                    }
                }
                .decodeList<ReportData>()

            Log.d("ReportRepository", "✅ Reportes obtenidos: ${reports.size}")

            if (reports.isEmpty()) {
                Log.w("ReportRepository", "⚠️ No se encontraron reportes con id_reporting_status=$statusId")
                // Intentar obtener todos y filtrar manualmente
                val allReports = getAllReports()
                val filtered = allReports.filter { it.idReportingStatus == statusId }
                Log.d("ReportRepository", "🔄 Filtrado manual: ${filtered.size} reportes con status $statusId")
                return filtered
            }

            reports
        } catch (e: Exception) {
            Log.e("ReportRepository", "❌ Error obteniendo reportes por estado: ${e.message}", e)
            e.printStackTrace()
            // Fallback: intentar obtener todos y filtrar
            try {
                val allReports = getAllReports()
                allReports.filter { it.idReportingStatus == statusId }
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Obtiene reportes por categoría (id_affair)
     */
    suspend fun getReportsByCategory(categoryId: Int): List<ReportData> {
        return try {
            Log.d("ReportRepository", "🔍 Obteniendo reportes con id_affair: $categoryId...")

            val reports = client.postgrest
                .from("reports")
                .select {
                    filter {
                        eq("id_affair", categoryId)
                    }
                }
                .decodeList<ReportData>()

            Log.d("ReportRepository", "✅ Reportes obtenidos: ${reports.size}")
            reports
        } catch (e: Exception) {
            Log.e("ReportRepository", "❌ Error obteniendo reportes por categoría: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Obtiene reportes anónimos
     */
    suspend fun getAnonymousReports(): List<ReportData> {
        return try {
            val reports = client.postgrest
                .from("reports")
                .select {
                    filter {
                        eq("is_anonymous", true)
                    }
                }
                .decodeList<ReportData>()

            Log.d("ReportRepository", "✅ Reportes anónimos obtenidos: ${reports.size}")
            reports
        } catch (e: Exception) {
            Log.e("ReportRepository", "❌ Error obteniendo reportes anónimos: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Cuenta reportes por estado para debug
     */
    suspend fun countReportsByStatus(): Map<Int, Int> {
        return try {
            val allReports = getAllReports()
            val counts = allReports.groupBy { it.idReportingStatus }
                .mapValues { it.value.size }

            Log.d("ReportRepository", "📊 Conteo de reportes por estado:")
            counts.forEach { (status, count) ->
                val statusName = when(status) {
                    1 -> "Pendiente"
                    2 -> "En Proceso"
                    3 -> "Completado"
                    4 -> "Cancelado"
                    else -> "Desconocido"
                }
                Log.d("ReportRepository", "   - Status $status ($statusName): $count reportes")
            }

            counts
        } catch (e: Exception) {
            Log.e("ReportRepository", "❌ Error contando reportes: ${e.message}", e)
            emptyMap()
        }
    }

    /**
     * Cuenta reportes por categoría
     */
    suspend fun countReportsByCategory(): Map<Int, Int> {
        return try {
            val allReports = getAllReports()
            val counts = allReports
                .filter { it.idAffair != null }
                .groupBy { it.idAffair!! }
                .mapValues { it.value.size }

            Log.d("ReportRepository", "📊 Conteo de reportes por categoría:")
            counts.forEach { (categoryId, count) ->
                Log.d("ReportRepository", "   - Categoría $categoryId: $count reportes")
            }

            counts
        } catch (e: Exception) {
            Log.e("ReportRepository", "❌ Error contando reportes por categoría: ${e.message}", e)
            emptyMap()
        }
    }

    /**
     * Obtiene información detallada de la base de datos para debug
     */
    suspend fun debugDatabaseInfo() {
        try {
            Log.d("ReportRepository", "🔍 Iniciando diagnóstico de base de datos...")

            // Obtener todos los reportes
            val allReports = getAllReports()
            Log.d("ReportRepository", "📊 Total de reportes en BD: ${allReports.size}")

            if (allReports.isNotEmpty()) {
                // Mostrar estructura del primer reporte
                val first = allReports[0]
                Log.d("ReportRepository", """
                    📝 Estructura del primer reporte:
                    - ID: ${first.id}
                    - Título/Ubicación: ${first.reportLocation}
                    - Status ID: ${first.idReportingStatus}
                    - User ID: ${first.userId}
                    - User Name: ${first.userName}
                    - Affair ID: ${first.idAffair}
                    - Descripción: ${first.description?.take(50)}
                    - Es Anónimo: ${first.isAnonymous}
                    - Fecha Creación: ${first.createdAt}
                """.trimIndent())

                // Contar por estado
                countReportsByStatus()

                // Contar por categoría
                countReportsByCategory()

                // Contar anónimos
                val anonymousCount = allReports.count { it.isAnonymous }
                Log.d("ReportRepository", "👤 Reportes anónimos: $anonymousCount")
            }
        } catch (e: Exception) {
            Log.e("ReportRepository", "❌ Error en diagnóstico: ${e.message}", e)
            e.printStackTrace()
        }
    }
}