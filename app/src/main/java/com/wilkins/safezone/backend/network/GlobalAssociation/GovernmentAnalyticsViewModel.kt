package com.wilkins.safezone.backend.network.GlobalAssociation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilkins.safezone.backend.network.Admin.Dashboard.DateParser
import com.wilkins.safezone.backend.network.GlobalAssociation.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "GovAnalyticsViewModel"

// Modelos de datos para estadísticas
data class MonthlyStatistic(
    val month: String,
    val year: Int,
    val count: Int,
    val monthNumber: Int
)

data class LocationStatistic(
    val location: String,
    val count: Int
)

data class AffairStatistic(
    val affairName: String,
    val affairId: Int,
    val count: Int
)

data class StatusStatistic(
    val statusName: String,
    val statusId: Int,
    val count: Int,
    val percentage: Float
)

data class TimeRangeStatistic(
    val label: String,
    val count: Int
)

data class AnalyticsState(
    val isLoading: Boolean = true,
    val totalReports: Int = 0,
    val monthlyStats: List<MonthlyStatistic> = emptyList(),
    val locationStats: List<LocationStatistic> = emptyList(),
    val affairStats: List<AffairStatistic> = emptyList(),
    val statusStats: List<StatusStatistic> = emptyList(),
    val timeRangeStats: List<TimeRangeStatistic> = emptyList(),
    val topMonth: MonthlyStatistic? = null,
    val topLocation: LocationStatistic? = null,
    val topAffair: AffairStatistic? = null,
    val averageReportsPerMonth: Float = 0f,
    val error: String? = null
)

class GovernmentAnalyticsViewModel : ViewModel() {
    private val repository = ReportsRepository()

    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    init {
        Log.d(TAG, "Analytics ViewModel inicializado")
        loadAnalytics()
    }

    fun loadAnalytics() {
        Log.d(TAG, "Iniciando carga de estadísticas")
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)

                // Cargar todos los reportes
                Log.d(TAG, "Cargando reportes...")
                val reportsResult = repository.getAllReports()

                if (reportsResult.isFailure) {
                    val error = reportsResult.exceptionOrNull()
                    Log.e(TAG, "Error al cargar reportes: ${error?.message}", error)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Error al cargar reportes: ${error?.message}"
                    )
                    return@launch
                }

                val reports = reportsResult.getOrThrow()
                Log.d(TAG, "Reportes cargados: ${reports.size}")

                // Cargar affairs
                Log.d(TAG, "Cargando affairs...")
                val affairsResult = repository.getAllAffairs()
                val affairs = affairsResult.getOrNull()?.associateBy { it.id } ?: emptyMap()
                Log.d(TAG, "Affairs cargados: ${affairs.size}")

                // Cargar estados
                Log.d(TAG, "Cargando estados...")
                val statusesResult = repository.getAllReportingStatuses()
                val statuses = statusesResult.getOrNull()?.associateBy { it.id } ?: emptyMap()
                Log.d(TAG, "Estados cargados: ${statuses.size}")

                // Calcular estadísticas por mes
                val monthlyStats = calculateMonthlyStats(reports)
                Log.d(TAG, "Estadísticas mensuales calculadas: ${monthlyStats.size} meses")

                // Calcular estadísticas por ubicación
                val locationStats = calculateLocationStats(reports)
                Log.d(TAG, "Estadísticas de ubicación calculadas: ${locationStats.size} ubicaciones")

                // Calcular estadísticas por tipo de reporte
                val affairStats = calculateAffairStats(reports, affairs)
                Log.d(TAG, "Estadísticas de affairs calculadas: ${affairStats.size} tipos")

                // Calcular estadísticas por estado
                val statusStats = calculateStatusStats(reports, statuses)
                Log.d(TAG, "Estadísticas de estados calculadas: ${statusStats.size} estados")

                // Calcular estadísticas por rango de tiempo
                val timeRangeStats = calculateTimeRangeStats(reports)
                Log.d(TAG, "Estadísticas de rango de tiempo calculadas")

                // Calcular promedio de reportes por mes
                val avgPerMonth = if (monthlyStats.isNotEmpty()) {
                    reports.size.toFloat() / monthlyStats.size
                } else 0f

                _state.value = AnalyticsState(
                    isLoading = false,
                    totalReports = reports.size,
                    monthlyStats = monthlyStats,
                    locationStats = locationStats,
                    affairStats = affairStats,
                    statusStats = statusStats,
                    timeRangeStats = timeRangeStats,
                    topMonth = monthlyStats.maxByOrNull { it.count },
                    topLocation = locationStats.maxByOrNull { it.count },
                    topAffair = affairStats.maxByOrNull { it.count },
                    averageReportsPerMonth = avgPerMonth,
                    error = null
                )

                Log.d(TAG, "Estadísticas cargadas exitosamente")

            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado al cargar estadísticas: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    private fun calculateMonthlyStats(reports: List<ReportDto>): List<MonthlyStatistic> {
        val monthFormat = SimpleDateFormat("MMM", Locale("es", "ES"))
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        return reports
            .mapNotNull { report ->
                try {
                    val date = DateParser.parseIsoDate(report.createdAt)

                    date?.let {
                        val calendar = Calendar.getInstance()
                        calendar.time = it
                        Triple(
                            monthFormat.format(it),
                            yearFormat.format(it).toInt(),
                            calendar.get(Calendar.MONTH) + 1
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando fecha: ${report.createdAt}", e)
                    null
                }
            }
            .groupBy { it }
            .map { (key, list) ->
                MonthlyStatistic(
                    month = key.first,
                    year = key.second,
                    monthNumber = key.third,
                    count = list.size
                )
            }
            .sortedWith(compareBy({ it.year }, { it.monthNumber }))
    }

    private fun calculateTimeRangeStats(reports: List<ReportDto>): List<TimeRangeStatistic> {
        val now = Calendar.getInstance()

        val last7Days = reports.count { report ->
            try {
                val date = DateParser.parseIsoDate(report.createdAt)
                if (date != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    calendar.add(Calendar.DAY_OF_YEAR, 7)
                    calendar.after(now)
                } else false
            } catch (e: Exception) {
                false
            }
        }

        val last30Days = reports.count { report ->
            try {
                val date = DateParser.parseIsoDate(report.createdAt)
                if (date != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    calendar.add(Calendar.DAY_OF_YEAR, 30)
                    calendar.after(now)
                } else false
            } catch (e: Exception) {
                false
            }
        }

        val last90Days = reports.count { report ->
            try {
                val date = DateParser.parseIsoDate(report.createdAt)
                if (date != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    calendar.add(Calendar.DAY_OF_YEAR, 90)
                    calendar.after(now)
                } else false
            } catch (e: Exception) {
                false
            }
        }

        return listOf(
            TimeRangeStatistic("Últimos 7 días", last7Days),
            TimeRangeStatistic("Últimos 30 días", last30Days),
            TimeRangeStatistic("Últimos 90 días", last90Days),
            TimeRangeStatistic("Total", reports.size)
        )
    }
    private fun calculateLocationStats(reports: List<ReportDto>): List<LocationStatistic> {
        return reports
            .mapNotNull { it.reportLocation }
            .filter { it.isNotBlank() }
            .groupBy { it }
            .map { (location, list) ->
                LocationStatistic(
                    location = location,
                    count = list.size
                )
            }
            .sortedByDescending { it.count }
            .take(10) // Top 10 ubicaciones
    }

    private fun calculateAffairStats(
        reports: List<ReportDto>,
        affairs: Map<Int, AffairDto>
    ): List<AffairStatistic> {
        return reports
            .mapNotNull { report ->
                report.idAffair?.let { affairId ->
                    Pair(affairId, affairs[affairId]?.affairName ?: "Sin categoría")
                }
            }
            .groupBy { it }
            .map { (key, list) ->
                AffairStatistic(
                    affairId = key.first,
                    affairName = key.second,
                    count = list.size
                )
            }
            .sortedByDescending { it.count }
    }

    private fun calculateStatusStats(
        reports: List<ReportDto>,
        statuses: Map<Int, ReportingStatusDto>
    ): List<StatusStatistic> {
        val total = reports.size.toFloat()

        return reports
            .groupBy { it.idReportingStatus }
            .map { (statusId, list) ->
                StatusStatistic(
                    statusId = statusId,
                    statusName = statuses[statusId]?.statusName ?: "Desconocido",
                    count = list.size,
                    percentage = (list.size / total) * 100
                )
            }
            .sortedByDescending { it.count }
    }



    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "Analytics ViewModel limpiado")
    }
}