package com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.GlobalAssociation.GovernmentDashboardViewModel
import com.wilkins.safezone.backend.network.GlobalAssociation.DashboardState
import com.wilkins.safezone.frontend.ui.GlobalAssociation.GovernmentMenu
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components.DashboardHeader
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components.DashboardSection
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components.EmptyCard
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components.StatisticsSection
import com.wilkins.safezone.frontend.ui.GlobalAssociation.screens.Dashboard.Components.ReportCardAssociation
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
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "📊 Estado del Dashboard actualizado:")
        Log.d(TAG, "  - isLoading: ${state.isLoading}")
        Log.d(TAG, "  - error: ${state.error}")
        Log.d(TAG, "  - total reportes: ${state.statistics.totalReports}")
        Log.d(TAG, "  - reportes recientes: ${state.recentReports.size}")
        Log.d(TAG, "  - affairs: ${state.affairs.size}")
        Log.d(TAG, "  - estados: ${state.reportingStatuses.size}")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    GovernmentMenu(
        navController = navController,
        isMenuOpen = isMenuOpen,
        onMenuToggle = { isMenuOpen = it },
        currentRoute = "dashboardAssociation"
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            when {
                state.isLoading -> {
                    Log.d(TAG, "🔄 Mostrando pantalla de carga")
                    LoadingScreen()
                }
                state.error != null -> {
                    Log.e(TAG, "❌ Mostrando pantalla de error: ${state.error}")
                    ErrorScreen(
                        error = state.error!!,
                        onRetry = {
                            Log.d(TAG, "🔁 Usuario solicitó reintentar carga")
                            viewModel.loadDashboardData()
                        }
                    )
                }
                else -> {
                    Log.d(TAG, "✅ Mostrando contenido del dashboard")
                    DashboardContent(
                        state = state,
                        onStatusChange = { reportId, newStatusId ->
                            Log.d(TAG, "🔄 Usuario cambió estado: reportId=$reportId, newStatus=$newStatusId")
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
    state: DashboardState,
    onStatusChange: (String, Int) -> Unit,
    navController: NavController
) {
    Log.d(TAG, "📝 Renderizando DashboardContent")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header del Dashboard
        item {
            Log.d(TAG, "  - Renderizando Header")
            DashboardHeader(
                title = "Dashboard",
                subtitle = "Panel de Control Gubernamental"
            )
        }

        // Sección de Estadísticas
        item {
            Log.d(TAG, "  - Renderizando Estadísticas")
            Log.d(TAG, "    Total: ${state.statistics.totalReports}")
            Log.d(TAG, "    Pendientes: ${state.statistics.pendingReports}")
            Log.d(TAG, "    En Proceso: ${state.statistics.inProgressReports}")
            Log.d(TAG, "    Completados: ${state.statistics.completedReports}")
            Log.d(TAG, "    Cancelados: ${state.statistics.cancelledReports}")

            StatisticsSection(
                totalReports = state.statistics.totalReports,
                pendingReports = state.statistics.pendingReports,
                inProgressReports = state.statistics.inProgressReports,
                completedReports = state.statistics.completedReports,
                cancelledReports = state.statistics.cancelledReports,
                primaryColor = PrimaryColor,
                onTotalClick = {
                    Log.d(TAG, "📍 Navegando a ReportSentList")
                    navController.navigate("ReportSentList")
                },
                onPendingClick = {
                    Log.d(TAG, "📍 Navegando a PendingReports")
                    navController.navigate("PendingReports")
                },
                onInProgressClick = {
                    Log.d(TAG, "📍 Navegando a ReportsProgress")
                    navController.navigate("ReportsProgress")
                },
                onCompletedClick = {
                    Log.d(TAG, "📍 Navegando a ReportsCompleted")
                    navController.navigate("ReportsCompleted")
                },
                onCancelledClick = {
                    Log.d(TAG, "📍 Navegando a ReportsCancelled")
                    navController.navigate("ReportsCancelled")
                }
            )
        }

        // Sección de Reportes Recientes
        item {
            Log.d(TAG, "  - Renderizando sección de Reportes Recientes")
            DashboardSection(
                title = "Reportes Recientes",
                subtitle = "Últimos ${state.recentReports.size} reportes"
            ) {
                if (state.recentReports.isEmpty()) {
                    Log.d(TAG, "    ⚠️ No hay reportes recientes")
                    EmptyCard(message = "No hay reportes recientes")
                }
            }
        }

        // Lista de Reportes
        items(state.recentReports) { report ->
            Log.d(TAG, "  - Renderizando reporte: ${report.id}")
            ReportCardAssociation(
                report = report,
                affairName = state.affairs[report.idAffair]?.affairName,
                statusName = state.reportingStatuses[report.idReportingStatus]?.statusName,
                onStatusChange = { newStatusId ->
                    onStatusChange(report.id, newStatusId)
                },
                onNavigateToDetail = { reportId ->
                    Log.d(TAG, "📍 Navegando a detalle del reporte: $reportId")
                    navController.navigate("report_detail/$reportId")
                }
            )
        }

        // Espaciado final
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Componentes de Loading y Error (en caso de que no existan)
@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = PrimaryColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando dashboard...",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = Color.Red,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reintentar")
            }
        }
    }
}