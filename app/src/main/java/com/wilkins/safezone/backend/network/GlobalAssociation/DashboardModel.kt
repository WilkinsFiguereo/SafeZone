package com.wilkins.safezone.backend.network.GlobalAssociation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilkins.safezone.backend.network.GlobalAssociation.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardStatistics(
    val totalReports: Int = 0,
    val pendingReports: Int = 0,
    val inProgressReports: Int = 0,
    val completedReports: Int = 0,
    val cancelledReports: Int = 0
)

data class DashboardState(
    val isLoading: Boolean = true,
    val statistics: DashboardStatistics = DashboardStatistics(),
    val recentReports: List<ReportDto> = emptyList(),
    val affairs: Map<Int, AffairDto> = emptyMap(),
    val reportingStatuses: Map<Int, ReportingStatusDto> = emptyMap(),
    val error: String? = null
)

class GovernmentDashboardViewModel : ViewModel() {
    private val repository = ReportsRepository()

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                // Cargar datos en paralelo
                val reportsResult = repository.getAllReports()
                val affairsResult = repository.getAllAffairs()
                val statusesResult = repository.getAllReportingStatuses()

                if (reportsResult.isSuccess && affairsResult.isSuccess && statusesResult.isSuccess) {
                    val reports = reportsResult.getOrThrow()
                    val affairs = affairsResult.getOrThrow().associateBy { it.id }
                    val statuses = statusesResult.getOrThrow().associateBy { it.id }

                    // Calcular estadísticas
                    val stats = DashboardStatistics(
                        totalReports = reports.size,
                        pendingReports = reports.count { it.idReportingStatus == 1 },
                        inProgressReports = reports.count { it.idReportingStatus == 2 },
                        completedReports = reports.count { it.idReportingStatus == 3 },
                        cancelledReports = reports.count { it.idReportingStatus == 4 }
                    )

                    // Obtener reportes recientes (últimos 10)
                    val recentReports = reports.sortedByDescending { it.createdAt }.take(10)

                    _state.value = DashboardState(
                        isLoading = false,
                        statistics = stats,
                        recentReports = recentReports,
                        affairs = affairs,
                        reportingStatuses = statuses
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Error al cargar los datos del dashboard"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }

    fun updateReportStatus(reportId: String, newStatusId: Int) {
        viewModelScope.launch {
            try {
                val result = repository.updateReportStatus(reportId, newStatusId)
                if (result.isSuccess) {
                    // Recargar datos después de actualizar
                    loadDashboardData()
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al actualizar el estado: ${e.message}"
                )
            }
        }
    }
}