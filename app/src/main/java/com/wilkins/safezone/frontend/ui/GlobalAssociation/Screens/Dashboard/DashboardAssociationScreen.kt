package com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.GlobalAssociation.GovernmentDashboardViewModel
import com.wilkins.safezone.frontend.ui.GlobalAssociation.GovernmentMenu
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components.DashboardHeader
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components.DashboardSection
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components.EmptyCard
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components.ErrorScreenAssoication
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components.LoadingScreenAssoication
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components.StatisticsSection
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.ReportSent.ReportList.ReportCard
import com.wilkins.safezone.frontend.ui.GlobalAssociation.screens.Dashboard.Components.ReportCard
import com.wilkins.safezone.ui.theme.PrimaryColor

private const val TAG = "GovDashboardScreen"

@Composable
fun GovernmentDashboardScreen(
    navController: NavController,
    viewModel: GovernmentDashboardViewModel = viewModel()
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()

    // Log del estado actual
    LaunchedEffect(state) {
        Log.d(TAG, "Estado actualizado:")
        Log.d(TAG, "  - isLoading: ${state.isLoading}")
        Log.d(TAG, "  - error: ${state.error}")
        Log.d(TAG, "  - total reportes: ${state.statistics.totalReports}")
        Log.d(TAG, "  - reportes recientes: ${state.recentReports.size}")
        Log.d(TAG, "  - affairs: ${state.affairs.size}")
        Log.d(TAG, "  - estados: ${state.reportingStatuses.size}")
    }

    GovernmentMenu(
        navController = navController,
        isMenuOpen = isMenuOpen,
        onMenuToggle = { isMenuOpen = it },
        currentRoute = "government_dashboard"
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
                            viewModel.loadDashboardData()
                        }
                    )
                }
                else -> {
                    Log.d(TAG, "Mostrando contenido del dashboard")
                    DashboardContent(
                        state = state,
                        onStatusChange = { reportId, newStatusId ->
                            Log.d(TAG, "Usuario cambió estado: reportId=$reportId, newStatus=$newStatusId")
                            viewModel.updateReportStatus(reportId, newStatusId)
                        },
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    state: com.wilkins.safezone.backend.network.GlobalAssociation.DashboardState,
    onStatusChange: (String, Int) -> Unit,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header del Dashboard
        item {
            DashboardHeader(
                title = "Dashboard",
                subtitle = "Panel de Control Gubernamental"
            )
        }

        // Sección de Estadísticas
        item {
            Log.d(TAG, "Renderizando sección de estadísticas")
            StatisticsSection(
                totalReports = state.statistics.totalReports,
                pendingReports = state.statistics.pendingReports,
                inProgressReports = state.statistics.inProgressReports,
                completedReports = state.statistics.completedReports,
                cancelledReports = state.statistics.cancelledReports,
                primaryColor = PrimaryColor,
                onTotalClick = {
                    Log.d(TAG, "Navegando a ReportSentList")
                    navController.navigate("ReportSentList")
                },
                onPendingClick = {
                    Log.d(TAG, "Navegando a PendingReports")
                    navController.navigate("PendingReports")
                },
                onInProgressClick = {
                    Log.d(TAG, "Navegando a ReportsProgress")
                    navController.navigate("ReportsProgress")
                },
                onCompletedClick = {
                    Log.d(TAG, "Navegando a ReportsCompleted")
                    navController.navigate("ReportsCompleted")
                },
                onCancelledClick = {
                    Log.d(TAG, "Navegando a ReportsCancelled")
                    navController.navigate("ReportsCancelled")
                }
            )
        }

        // Sección de Reportes Recientes
        item {
            DashboardSection(
                title = "Reportes Recientes",
                subtitle = "Últimos ${state.recentReports.size} reportes"
            ) {
                if (state.recentReports.isEmpty()) {
                    Log.d(TAG, "No hay reportes recientes para mostrar")
                    EmptyCard(message = "No hay reportes recientes")
                }
            }
        }

        // Lista de Reportes
        items(state.recentReports) { report ->
            Log.d(TAG, "Renderizando reporte: ${report.id}")
            ReportCard(
                report = report,
                affairName = state.affairs[report.idAffair]?.affairName,
                statusName = state.reportingStatuses[report.idReportingStatus]?.statusName,
                onStatusChange = { newStatusId ->
                    onStatusChange(report.id, newStatusId)
                },
                onNavigateToDetail = { reportId ->
                    Log.d(TAG, "Navegando a detalle del reporte: $reportId")
                    navController.navigate("report_detail/$reportId")  // ← NAVEGACIÓN AQUÍ
                }
            )
        }

        // Espaciado final
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}