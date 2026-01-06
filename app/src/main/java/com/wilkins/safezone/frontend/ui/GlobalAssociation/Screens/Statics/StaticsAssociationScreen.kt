package com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Statics

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.GlobalAssociation.GovernmentAnalyticsViewModel
import com.wilkins.safezone.frontend.ui.GlobalAssociation.GovernmentMenu
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components.DashboardHeader
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components.DashboardSection
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components.ErrorScreenAssoication
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components.LoadingScreenAssoication
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Statics.Components.CompactMetricCard
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Statics.Components.MetricCard
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Statics.Components.MonthlyBarChart
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Statics.Components.MonthlyDetailTable
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Statics.Components.RankingList
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Statics.Components.StatusPieChart
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Statics.Components.TopItemCard
import com.wilkins.safezone.ui.theme.PrimaryColor

private const val TAG = "GovAnalyticsScreen"

@Composable
fun GovernmentAnalyticsScreen(
    navController: NavController,
    viewModel: GovernmentAnalyticsViewModel = viewModel()
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()

    // Log del estado actual
    LaunchedEffect(state) {
        Log.d(TAG, "Estado actualizado:")
        Log.d(TAG, "  - isLoading: ${state.isLoading}")
        Log.d(TAG, "  - error: ${state.error}")
        Log.d(TAG, "  - total reportes: ${state.totalReports}")
        Log.d(TAG, "  - datos mensuales: ${state.monthlyStats.size}")
        Log.d(TAG, "  - ubicaciones: ${state.locationStats.size}")
        Log.d(TAG, "  - affairs: ${state.affairStats.size}")
    }

    GovernmentMenu(
        navController = navController,
        isMenuOpen = isMenuOpen,
        onMenuToggle = { isMenuOpen = it },
        currentRoute = "government_analytics"
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            when {
                state.isLoading -> {
                    Log.d(TAG, "Mostrando pantalla de carga")
                    LoadingScreenAssoication()
                }
                state.error != null -> {
                    Log.e(TAG, "Mostrando pantalla de error: ${state.error}")
                    ErrorScreenAssoication (
                        error = state.error!!,
                        onRetry = {
                            Log.d(TAG, "Usuario solicitó reintentar carga")
                            viewModel.loadAnalytics()
                        }
                    )
                }
                else -> {
                    Log.d(TAG, "Mostrando contenido de análisis")
                    AnalyticsContent(
                        state = state,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsContent(
    state: com.wilkins.safezone.backend.network.GlobalAssociation.AnalyticsState,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            DashboardHeader(
                title = "Análisis y Estadísticas",
                subtitle = "Datos y métricas del sistema"
            )
        }

        // Sección de Métricas Principales
        item {
            DashboardSection(title = "Resumen General") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Total de reportes
                    MetricCard(
                        title = "Total de Reportes",
                        value = state.totalReports.toString(),
                        subtitle = "Todos los reportes registrados",
                        icon = Icons.Default.Assignment,
                        backgroundColor = PrimaryColor
                    )

                    // Promedio mensual
                    MetricCard(
                        title = "Promedio Mensual",
                        value = String.format("%.1f", state.averageReportsPerMonth),
                        subtitle = "Reportes por mes",
                        icon = Icons.Default.TrendingUp,
                        backgroundColor = Color(0xFF4CAF50)
                    )
                }
            }
        }

        // Sección de Mejores Registros (Top)
        item {
            DashboardSection(title = "Registros Destacados") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Mes con más reportes
                    state.topMonth?.let { topMonth ->
                        TopItemCard(
                            icon = Icons.Default.CalendarMonth,
                            title = "Mes con más reportes",
                            value = "${topMonth.month} ${topMonth.year}",
                            count = topMonth.count,
                            color = Color(0xFF2196F3)
                        )
                    }

                    // Ubicación con más reportes
                    state.topLocation?.let { topLocation ->
                        TopItemCard(
                            icon = Icons.Default.LocationOn,
                            title = "Ubicación con más reportes",
                            value = topLocation.location,
                            count = topLocation.count,
                            color = Color(0xFFFF9800)
                        )
                    }

                    // Tipo de reporte más común
                    state.topAffair?.let { topAffair ->
                        TopItemCard(
                            icon = Icons.Default.Category,
                            title = "Tipo de reporte más común",
                            value = topAffair.affairName,
                            count = topAffair.count,
                            color = Color(0xFF9C27B0)
                        )
                    }
                }
            }
        }

        // Sección de Tendencias Temporales
        item {
            DashboardSection(title = "Tendencias Temporales") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Métricas de rango de tiempo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.timeRangeStats.take(2).forEach { stat ->
                            Box(modifier = Modifier.weight(1f)) {
                                CompactMetricCard(
                                    label = stat.label,
                                    value = stat.count.toString(),
                                    color = PrimaryColor
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.timeRangeStats.drop(2).take(2).forEach { stat ->
                            Box(modifier = Modifier.weight(1f)) {
                                CompactMetricCard(
                                    label = stat.label,
                                    value = stat.count.toString(),
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Gráfico de barras mensuales
        item {
            DashboardSection(title = "Distribución Mensual") {
                MonthlyBarChart(
                    data = state.monthlyStats,
                    barColor = PrimaryColor
                )
            }
        }

        // Gráfico de dona de estados
        item {
            DashboardSection(title = "Estados de Reportes") {
                StatusPieChart(data = state.statusStats)
            }
        }

        // Ranking de ubicaciones
        item {
            DashboardSection(title = "Top Ubicaciones") {
                RankingList(
                    title = "Ubicaciones con Más Reportes",
                    items = state.locationStats.map { it.location to it.count },
                    color = Color(0xFFFF9800)
                )
            }
        }

        // Ranking de tipos de reporte
        item {
            DashboardSection(title = "Tipos de Reporte") {
                RankingList(
                    title = "Tipos de Reporte Más Comunes",
                    items = state.affairStats.map { it.affairName to it.count },
                    color = Color(0xFF9C27B0)
                )
            }
        }

        // Tabla detallada de estadísticas mensuales
        item {
            DashboardSection(title = "Detalle Mensual") {
                MonthlyDetailTable(data = state.monthlyStats)
            }
        }

        // Espaciado final
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}