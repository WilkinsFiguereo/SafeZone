package com.wilkins.safezone.backend.network.Admin.CrudUser.Dashboard

import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

// ============================================
// MODELOS DE DATOS
// ============================================

@Serializable
data class ActivityLog(
    @SerialName("id")
    val id: Int = 0,

    @SerialName("action_type")
    val actionType: String = "",

    @SerialName("table_name")
    val tableName: String = "",

    @SerialName("record_id")
    val recordId: String = "",

    @SerialName("record_title")
    val recordTitle: String = "",

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("user_name")
    val userName: String? = null,

    @SerialName("created_at")
    val createdAt: String = ""
)

@Serializable
data class DashboardStats(
    @SerialName("reports_sent")
    val reportsSent: Int = 0,

    @SerialName("reports_received")
    val reportsReceived: Int = 0,

    @SerialName("reports_resolved")
    val reportsResolved: Int = 0,

    @SerialName("reports_cancelled")
    val reportsCancelled: Int = 0,

    @SerialName("total_users")
    val totalUsers: Int = 0,

    @SerialName("total_reports")
    val totalReports: Int = 0
)

@Serializable
data class MonthlyActivity(
    @SerialName("month_name")
    val monthName: String = "",

    @SerialName("report_count")
    val reportCount: Int = 0
)

@Serializable
data class RecentReport(
    @SerialName("id")
    val id: String = "",

    @SerialName("title")
    val title: String = "",

    @SerialName("description")
    val description: String = "",

    @SerialName("created_at")
    val createdAt: String = "",

    @SerialName("status_id")
    val statusId: Int = 0,

    @SerialName("user_name")
    val userName: String = ""
)

@Serializable
data class ProfileBasic(
    @SerialName("id")
    val id: String = "",

    @SerialName("name")
    val name: String = ""
)

@Serializable
data class AffairBasic(
    @SerialName("id")
    val id: Int = 0,

    @SerialName("name")
    val name: String? = null,

    @SerialName("affair_name")
    val affairNameAlt: String? = null
) {
    val affairName: String
        get() = name ?: affairNameAlt ?: "Sin categoría"
}

// ============================================
// REPOSITORY - VERSIÓN CORREGIDA CON FECHAS
// ============================================

class DashboardRepository {
    private val supabase = SupabaseService.getInstance()

    /**
     * Obtener estadísticas del dashboard
     */
    suspend fun getDashboardStats(): DashboardStats? = withContext(Dispatchers.IO) {
        try {
            println("📊 DashboardRepository: Iniciando obtención de estadísticas...")

            // Obtener todos los reportes
            println("   🔍 Consultando tabla 'reports'...")
            val allReports = try {
                supabase.from("reports")
                    .select()
                    .decodeList<ReportDto>()
            } catch (e: Exception) {
                System.err.println("   ❌ ERROR al consultar tabla 'reports':")
                System.err.println("      Mensaje: ${e.message}")
                System.err.println("      Causa: ${e.cause?.message}")
                System.err.println("      Tipo: ${e::class.qualifiedName}")
                e.printStackTrace()
                return@withContext null
            }

            println("   ✓ Total de reportes obtenidos: ${allReports.size}")

            // Contar por id_reporting_status
            val reportsSent = allReports.count { it.idReportingStatus == 1 }
            val reportsReceived = allReports.count { it.idReportingStatus == 2 }
            val reportsResolved = allReports.count { it.idReportingStatus == 3 }
            val reportsCancelled = allReports.count { it.idReportingStatus == 4 }

            println("   📈 Conteo por estado:")
            println("      - Enviados (status=1): $reportsSent")
            println("      - Recibidos (status=2): $reportsReceived")
            println("      - Resueltos (status=3): $reportsResolved")
            println("      - Cancelados (status=4): $reportsCancelled")

            // Contar usuarios activos
            println("   🔍 Consultando tabla 'profiles' (usuarios activos)...")
            val totalUsers = try {
                supabase.from("profiles")
                    .select() { filter { eq("status_id", 1) } }
                    .decodeList<ProfileBasic>()
                    .size
            } catch (e: Exception) {
                System.err.println("   ❌ ERROR al consultar tabla 'profiles':")
                System.err.println("      Mensaje: ${e.message}")
                e.printStackTrace()
                0
            }

            println("   ✓ Total usuarios activos: $totalUsers")

            val stats = DashboardStats(
                reportsSent = reportsSent,
                reportsReceived = reportsReceived,
                reportsResolved = reportsResolved,
                reportsCancelled = reportsCancelled,
                totalUsers = totalUsers,
                totalReports = allReports.size
            )

            println("✅ DashboardRepository: Estadísticas obtenidas exitosamente")
            stats
        } catch (e: Exception) {
            System.err.println("❌ DashboardRepository: Error GENERAL al obtener estadísticas")
            System.err.println("   Mensaje de error: ${e.message}")
            System.err.println("   Tipo de error: ${e::class.qualifiedName}")
            System.err.println("   Causa raíz: ${e.cause?.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Obtener actividad mensual (últimos 6 meses) - VERSIÓN CORREGIDA CON FECHAS
     */
    suspend fun getMonthlyActivity(): List<MonthlyActivity> = withContext(Dispatchers.IO) {
        try {
            println("📈 DashboardRepository: Obteniendo actividad mensual...")

            // Obtener todos los reportes
            val allReports = try {
                supabase.from("reports")
                    .select()
                    .decodeList<ReportDto>()
            } catch (e: Exception) {
                System.err.println("   ❌ ERROR al consultar reportes para actividad mensual")
                System.err.println("      Error: ${e.message}")
                return@withContext emptyList()
            }

            println("   ✓ Total reportes obtenidos: ${allReports.size}")

            // Parsear fechas y agrupar por mes
            val monthFormat = SimpleDateFormat("MMM", Locale("es", "ES"))
            val monthlyData = allReports
                .mapNotNull { report ->
                    try {
                        val date = DateParser.parseIsoDate(report.createdAt)
                        date?.let { monthFormat.format(it) }
                    } catch (e: Exception) {
                        println("   ⚠️ Error parseando fecha: ${report.createdAt}")
                        null
                    }
                }
                .groupingBy { it }
                .eachCount()

            println("   ✓ Datos agrupados por mes: ${monthlyData.size} meses")

            // Convertir a lista de MonthlyActivity
            val activities = monthlyData
                .map { (month, count) ->
                    MonthlyActivity(
                        monthName = month,
                        reportCount = count
                    )
                }
                .sortedBy {
                    // Ordenar por mes (puedes mejorar esto con lógica más compleja)
                    val monthOrder = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun",
                        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
                    monthOrder.indexOf(it.monthName)
                }
                .takeLast(6) // Últimos 6 meses

            println("✅ DashboardRepository: Actividad mensual obtenida: ${activities.size} meses")
            activities.forEach {
                println("   - ${it.monthName}: ${it.reportCount} reportes")
            }

            activities
        } catch (e: Exception) {
            System.err.println("❌ DashboardRepository: Error al obtener actividad mensual")
            System.err.println("   Error: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtener últimos reportes - VERSIÓN CORREGIDA CON ORDENAMIENTO
     */
    suspend fun getRecentReports(limit: Int = 3): List<RecentReport> = withContext(Dispatchers.IO) {
        try {
            println("📋 DashboardRepository: Obteniendo últimos $limit reportes...")

            // Obtener reportes recientes
            println("   🔍 Consultando tabla 'reports'...")
            val reportsRaw = try {
                val allReports = supabase.from("reports")
                    .select()
                    .decodeList<ReportDto>()

                // Ordenar por fecha usando el DateParser
                allReports.sortedByDescending { report ->
                    try {
                        DateParser.parseIsoDate(report.createdAt)?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                }.take(limit)

            } catch (e: Exception) {
                System.err.println("   ❌ ERROR al consultar tabla 'reports':")
                System.err.println("      Mensaje: ${e.message}")
                System.err.println("      Causa: ${e.cause?.message}")
                System.err.println("      Tipo: ${e::class.qualifiedName}")
                e.printStackTrace()
                return@withContext emptyList()
            }

            println("   ✓ Reportes obtenidos: ${reportsRaw.size}")

            // Convertir a RecentReport con título del affair y nombre real del usuario
            val reports = reportsRaw.mapIndexed { index, report ->
                println("   📄 Procesando reporte ${index + 1}:")
                println("      - ID: ${report.id}")
                println("      - User ID: ${report.userId}")
                println("      - Is Anonymous: ${report.isAnonymous}")
                println("      - User Name en reporte: ${report.userName}")
                println("      - ID Affair: ${report.idAffair}")
                println("      - Description: ${report.description?.take(50)}...")

                // Obtener el nombre del affair si existe
                val affairName = report.idAffair?.let { affairId ->
                    try {
                        println("      🔍 Buscando affair con ID: $affairId")
                        val affair = supabase.from("affair")
                            .select() {
                                filter { eq("id", affairId) }
                            }
                            .decodeSingle<AffairBasic>()
                        println("      ✓ Affair encontrado: ${affair.affairName}")
                        affair.affairName
                    } catch (e: Exception) {
                        System.err.println("      ⚠️ No se pudo obtener affair: ${e.message}")
                        null
                    }
                }

                // Determinar el título
                val title = when {
                    affairName != null && report.description != null -> {
                        val shortDesc = if (report.description.length > 30) {
                            report.description.take(27) + "..."
                        } else {
                            report.description
                        }
                        "$affairName - $shortDesc"
                    }
                    affairName != null -> affairName
                    report.description != null -> {
                        if (report.description.length > 40) {
                            report.description.take(37) + "..."
                        } else {
                            report.description
                        }
                    }
                    else -> "Reporte sin título"
                }
                println("      ✓ Título generado: $title")

                // Determinar el nombre del usuario
                val userName = if (report.isAnonymous) {
                    println("      👤 Usuario es anónimo")
                    "Usuario Anónimo"
                } else {
                    // Primero intentar usar el user_name del reporte
                    if (!report.userName.isNullOrBlank()) {
                        println("      ✓ Usando user_name del reporte: ${report.userName}")
                        report.userName
                    } else {
                        // Si no existe, buscar en profiles
                        try {
                            println("      🔍 Buscando usuario en profiles con ID: ${report.userId}")
                            val profile = supabase.from("profiles")
                                .select() {
                                    filter { eq("id", report.userId) }
                                }
                                .decodeSingle<ProfileBasic>()
                            println("      ✓ Usuario encontrado en profiles: ${profile.name}")
                            profile.name
                        } catch (e: Exception) {
                            System.err.println("      ⚠️ ERROR al obtener usuario de profiles:")
                            System.err.println("         Mensaje: ${e.message}")
                            System.err.println("         Tipo: ${e::class.simpleName}")
                            e.printStackTrace()
                            "Usuario Desconocido"
                        }
                    }
                }

                RecentReport(
                    id = report.id,
                    title = title,
                    description = report.description ?: "Sin descripción",
                    createdAt = report.createdAt,
                    statusId = report.idReportingStatus,
                    userName = userName
                )
            }

            println("✅ DashboardRepository: ${reports.size} reportes procesados exitosamente")
            reports
        } catch (e: Exception) {
            System.err.println("❌ DashboardRepository: Error GENERAL al obtener últimos reportes")
            System.err.println("   Mensaje de error: ${e.message}")
            System.err.println("   Tipo de error: ${e::class.qualifiedName}")
            System.err.println("   Causa raíz: ${e.cause?.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtener últimas actividades del log - VERSIÓN CORREGIDA CON ORDENAMIENTO
     */
    suspend fun getRecentActivities(limit: Int = 5): List<ActivityLog> = withContext(Dispatchers.IO) {
        try {
            println("📝 DashboardRepository: Obteniendo últimas $limit actividades...")
            println("   🔍 Consultando tabla 'activity_log'...")

            val activities = try {
                val result = supabase
                    .from("activity_log")
                    .select()

                println("   ✓ Query ejecutada, decodificando...")
                val decoded = result.decodeList<ActivityLog>()
                println("   ✓ ${decoded.size} actividades decodificadas")

                // Ordenar usando DateParser y limitar
                val sorted = decoded.sortedByDescending { activity ->
                    try {
                        DateParser.parseIsoDate(activity.createdAt)?.time ?: 0L
                    } catch (e: Exception) {
                        println("   ⚠️ Error parseando fecha de actividad: ${activity.createdAt}")
                        0L
                    }
                }
                println("   ✓ Actividades ordenadas")

                val limited = sorted.take(limit)
                println("   ✓ Tomando primeras $limit actividades")

                limited
            } catch (e: Exception) {
                System.err.println("   ❌ ERROR al consultar activity_log:")
                System.err.println("      Tipo: ${e::class.qualifiedName}")
                System.err.println("      Mensaje: ${e.message}")
                System.err.println("      Causa: ${e.cause?.message}")
                e.printStackTrace()
                emptyList()
            }

            if (activities.isEmpty()) {
                println("   ⚠️ No se encontraron actividades en la tabla")
            } else {
                println("✅ DashboardRepository: Últimas actividades obtenidas: ${activities.size}")
                activities.forEachIndexed { index, activity ->
                    println("   [${index + 1}] ${activity.actionType}: ${activity.recordTitle} (${activity.createdAt})")
                }
            }

            activities
        } catch (e: Exception) {
            System.err.println("❌ DashboardRepository: Error GENERAL al obtener actividades")
            System.err.println("   Error: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtener actividades filtradas por tipo de acción
     */
    suspend fun getActivitiesByType(actionType: String, limit: Int = 10): List<ActivityLog> =
        withContext(Dispatchers.IO) {
            try {
                println("📝 DashboardRepository: Obteniendo actividades tipo '$actionType' (límite: $limit)...")

                val activities = supabase
                    .from("activity_log")
                    .select() {
                        filter {
                            eq("action_type", actionType)
                        }
                        order("created_at", Order.DESCENDING)
                        limit(limit.toLong())
                    }
                    .decodeList<ActivityLog>()

                println("✅ DashboardRepository: Actividades tipo '$actionType' obtenidas: ${activities.size}")
                activities
            } catch (e: Exception) {
                System.err.println("❌ DashboardRepository: Error al obtener actividades por tipo")
                System.err.println("   Error: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }

    /**
     * Obtener actividades filtradas por tabla
     */
    suspend fun getActivitiesByTable(tableName: String, limit: Int = 10): List<ActivityLog> =
        withContext(Dispatchers.IO) {
            try {
                println("📝 DashboardRepository: Obteniendo actividades de tabla '$tableName' (límite: $limit)...")

                val activities = supabase
                    .from("activity_log")
                    .select() {
                        filter {
                            eq("table_name", tableName)
                        }
                        order("created_at", Order.DESCENDING)
                        limit(limit.toLong())
                    }
                    .decodeList<ActivityLog>()

                println("✅ DashboardRepository: Actividades de tabla '$tableName' obtenidas: ${activities.size}")
                activities
            } catch (e: Exception) {
                System.err.println("❌ DashboardRepository: Error al obtener actividades por tabla")
                System.err.println("   Error: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
}