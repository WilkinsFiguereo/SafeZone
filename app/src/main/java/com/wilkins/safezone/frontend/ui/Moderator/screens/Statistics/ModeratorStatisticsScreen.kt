package com.wilkins.safezone.frontend.ui.Moderator.screens.Statistics

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.Admin.CrudUser.CrudUser
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportsRepository
import com.wilkins.safezone.backend.network.Moderator.News.NewsViewModel
import com.wilkins.safezone.backend.network.auth.SessionManager.getUserProfile
import com.wilkins.safezone.frontend.ui.Moderator.ModeratorSideMenu
import com.wilkins.safezone.frontend.ui.Moderator.screens.Statistics.Components.*
import com.wilkins.safezone.navigation.theme.PrimaryColor
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorStatisticsScreen(
    navController: NavController,
    moderatorId: String,
    modifier: Modifier = Modifier,
    context: Context,
    supabaseClient: SupabaseClient
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // ViewModels y Repositories
    val newsViewModel: NewsViewModel = viewModel()
    val reportsRepository = remember { ReportsRepository() }
    val crudUser = remember { CrudUser() }

    // Estados
    val newsList by newsViewModel.newsList.collectAsState()
    var reportsList by remember { mutableStateOf<List<com.wilkins.safezone.backend.network.GlobalAssociation.ReportDto>>(emptyList()) }
    var usersCount by remember { mutableStateOf(0) }
    var activeUsersCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedPeriod by remember { mutableStateOf("Mes") }

    // Cargar datos
    LaunchedEffect(Unit) {
        isLoading = true

        newsViewModel.loadNews()

        scope.launch {
            reportsRepository.getAllReports().onSuccess { reports ->
                reportsList = reports
            }
        }

        scope.launch {
            val users = crudUser.getAllProfiles()
            usersCount = users.size
            activeUsersCount = users.count { it.statusId == 1 }
        }

        isLoading = false
    }

    // Calcular estadísticas
    val totalReports = reportsList.size
    val pendingReports = reportsList.count { it.idReportingStatus == 1 }
    val inProgressReports = reportsList.count { it.idReportingStatus == 2 }
    val resolvedReports = reportsList.count { it.idReportingStatus == 3 }
    val totalNews = newsList.size
    val importantNews = newsList.count { it.isImportant }

    // Estadísticas generales
    val generalStats = remember(totalReports, totalNews, usersCount, activeUsersCount) {
        listOf(
            StatCardData(
                title = "Total Reportes",
                value = totalReports.toString(),
                icon = Icons.Default.Report,
                color = Color(0xFF2196F3),
                trend = "+${totalReports}",
                trendUp = true
            ),
            StatCardData(
                title = "Noticias Totales",
                value = totalNews.toString(),
                icon = Icons.Default.Newspaper,
                color = Color(0xFF4CAF50),
                trend = "+${totalNews}",
                trendUp = true
            ),
            StatCardData(
                title = "Usuarios Totales",
                value = usersCount.toString(),
                icon = Icons.Default.People,
                color = Color(0xFF9C27B0),
                trend = "+${usersCount}",
                trendUp = true
            ),
            StatCardData(
                title = "Usuarios Activos",
                value = activeUsersCount.toString(),
                icon = Icons.Default.PersonOutline,
                color = Color(0xFFFF9800),
                trend = "+${activeUsersCount}",
                trendUp = true
            )
        )
    }

    // Estadísticas de reportes
    val reportsStats = remember(pendingReports, inProgressReports, resolvedReports) {
        listOf(
            ReportStatItem(
                label = "Pendientes",
                value = pendingReports,
                total = totalReports,
                color = Color(0xFFF44336)
            ),
            ReportStatItem(
                label = "En Proceso",
                value = inProgressReports,
                total = totalReports,
                color = Color(0xFF2196F3)
            ),
            ReportStatItem(
                label = "Resueltos",
                value = resolvedReports,
                total = totalReports,
                color = Color(0xFF4CAF50)
            )
        )
    }

    // Estadísticas de noticias
    val newsStats = remember(importantNews, totalNews) {
        listOf(
            NewsStatItem(
                label = "Noticias Destacadas",
                value = importantNews,
                icon = Icons.Default.Star,
                color = Color(0xFFFFD700)
            ),
            NewsStatItem(
                label = "Noticias Normales",
                value = totalNews - importantNews,
                icon = Icons.Default.Article,
                color = Color(0xFF2196F3)
            ),
            NewsStatItem(
                label = "Con Video",
                value = newsList.count { !it.videoUrl.isNullOrBlank() },
                icon = Icons.Default.VideoLibrary,
                color = Color(0xFF9C27B0)
            )
        )
    }

    // Estadísticas de encuestas (estático por ahora)
    val surveyStats = remember {
        listOf(
            SurveyStatItem(
                label = "Encuestas Activas",
                value = 0,
                icon = Icons.Default.Poll,
                color = Color(0xFF00BCD4)
            ),
            SurveyStatItem(
                label = "Respuestas Totales",
                value = 0,
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF4CAF50)
            ),
            SurveyStatItem(
                label = "Participación",
                value = 0,
                icon = Icons.Default.TrendingUp,
                color = Color(0xFFFF9800),
                suffix = "%"
            )
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Estadísticas",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Panel de Control",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryColor,
                        titleContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = { isMenuOpen = !isMenuOpen }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            scope.launch {
                                isLoading = true
                                newsViewModel.loadNews()
                                reportsRepository.getAllReports().onSuccess {
                                    reportsList = it
                                }
                                isLoading = false
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header con selector de período
                    item {
                        PeriodSelectorCard(
                            selectedPeriod = selectedPeriod,
                            onPeriodSelected = { selectedPeriod = it },
                            primaryColor = PrimaryColor
                        )
                    }

                    // Estadísticas generales
                    item {
                        GeneralStatsGrid(
                            stats = generalStats,
                            primaryColor = PrimaryColor
                        )
                    }

                    // Estadísticas de reportes
                    item {
                        ReportsStatsCard(
                            stats = reportsStats,
                            totalReports = totalReports,
                            primaryColor = PrimaryColor
                        )
                    }

                    // Gráfico de reportes por estado
                    item {
                        ReportsChartCard(
                            stats = reportsStats,
                            primaryColor = PrimaryColor
                        )
                    }

                    // Estadísticas de noticias
                    item {
                        NewsStatsCard(
                            stats = newsStats,
                            primaryColor = PrimaryColor
                        )
                    }

                    // Estadísticas de encuestas
                    item {
                        SurveyStatsCard(
                            stats = surveyStats,
                            primaryColor = PrimaryColor
                        )
                    }

                    // Resumen final
                    item {
                        SummaryCard(
                            totalReports = totalReports,
                            resolvedReports = resolvedReports,
                            totalNews = totalNews,
                            totalUsers = usersCount,
                            primaryColor = PrimaryColor
                        )
                    }
                }
            }
        }

        if (isMenuOpen) {
            val userState = produceState<AppUser?>(initialValue = null) {
                value = getUserProfile(context)
            }
            val user = userState.value

            ModeratorSideMenu(
                navController = navController,
                moderatorId = moderatorId,
                moderatorName = user?.name ?: "Moderator",
                currentRoute = "moderatorStatistics",
                isMenuOpen = isMenuOpen,
                onMenuToggle = { isMenuOpen = it },
                context = context,
                supabaseClient = supabaseClient
            )
        }
    }
}