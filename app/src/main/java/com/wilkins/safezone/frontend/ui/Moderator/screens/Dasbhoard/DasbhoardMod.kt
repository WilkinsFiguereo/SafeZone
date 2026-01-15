package com.wilkins.safezone.frontend.ui.Moderator.Dashboard

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
import com.wilkins.safezone.backend.network.auth.SessionManager
import com.wilkins.safezone.backend.network.auth.SessionManager.getUserProfile
import com.wilkins.safezone.frontend.ui.Moderator.ModeratorSideMenu
import com.wilkins.safezone.frontend.ui.Moderator.screens.Dasbhoard.Components.*
import com.wilkins.safezone.navigation.theme.PrimaryColor
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorDashboard(
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
    var isLoading by remember { mutableStateOf(true) }

    // Estadísticas calculadas
    val activeReportsCount = reportsList.filter { it.idReportingStatus in listOf(1, 2) }.size
    val newsCount = newsList.size

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        isLoading = true

        // Cargar noticias
        newsViewModel.loadNews()

        // Cargar reportes
        scope.launch {
            reportsRepository.getAllReports().onSuccess { reports ->
                reportsList = reports
            }
        }

        // Cargar usuarios
        scope.launch {
            val users = crudUser.getAllProfiles()
            usersCount = users.size
        }

        isLoading = false
    }

    // Convertir reportes a ReportItems para el dashboard
    val dashboardReports = reportsList.take(5).map { report ->
        val description = report.description ?: ""

        ReportItem(
            id = report.id,
            title = description.take(50) + if (description.length > 50) "..." else "",
            description = description,
            reportedBy = if (report.isAnonymous) "Anónimo" else (report.userName ?: "Usuario"),
            timestamp = formatTimestamp(report.createdAt),
            priority = when (report.idReportingStatus) {
                1 -> ReportPriority.HIGH
                2 -> ReportPriority.MEDIUM
                else -> ReportPriority.LOW
            },
            status = when (report.idReportingStatus) {
                1 -> ReportStatus.PENDING
                2 -> ReportStatus.IN_PROGRESS
                3 -> ReportStatus.RESOLVED
                else -> ReportStatus.PENDING
            },
            location = report.reportLocation ?: ""
        )
    }

    // Convertir noticias a NewsItems
    val dashboardNews = newsList.take(5).map { news ->
        NewsItem(
            id = news.id ?: "",
            title = news.title,
            summary = news.description.take(100) + if (news.description.length > 100) "..." else "",
            author = "Moderador",
            timestamp = formatTimestamp(news.createdAt),
            views = 0,
            isPublished = true
        )
    }

    // Estadísticas
    val stats = remember(activeReportsCount, newsCount, usersCount) {
        listOf(
            StatItem(
                title = "Reportes Activos",
                value = activeReportsCount.toString(),
                icon = Icons.Default.Warning,
                trend = "+${activeReportsCount}",
                trendUp = activeReportsCount > 0
            ),
            StatItem(
                title = "Noticias Publicadas",
                value = newsCount.toString(),
                icon = Icons.Default.Newspaper,
                trend = "+${newsCount}",
                trendUp = newsCount > 0
            ),
            StatItem(
                title = "Usuarios Activos",
                value = usersCount.toString(),
                icon = Icons.Default.People,
                trend = "+${usersCount}",
                trendUp = usersCount > 0
            ),
            StatItem(
                title = "Encuestas",
                value = "0",
                icon = Icons.Default.Poll,
                trend = "0%",
                trendUp = false
            )
        )
    }

    // Acciones rápidas
    val quickActions = remember {
        listOf(
            QuickAction(
                title = "Nueva Noticia",
                icon = Icons.Default.Add,
                onClick = { navController.navigate("newsSave") }
            ),
            QuickAction(
                title = "Ver Reportes",
                icon = Icons.Default.Report,
                onClick = { navController.navigate("ReportReviewList") }
            ),
            QuickAction(
                title = "Usuarios",
                icon = Icons.Default.People,
                onClick = { navController.navigate("moderatorUser") }
            ),
            QuickAction(
                title = "Estadísticas",
                icon = Icons.Default.BarChart,
                onClick = { navController.navigate("example_tar") }
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
                                text = "Panel de Moderador",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "SafeZone",
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
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                ) {
                                    Text(activeReportsCount.toString())
                                }
                            }
                        ) {
                            IconButton(onClick = {
                                navController.navigate("example_tar")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificaciones",
                                    tint = Color.White
                                )
                            }
                        }
                        IconButton(onClick = {
                            navController.navigate("example_tar")
                        }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Perfil",
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
                    // Header
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = PrimaryColor.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "¡Bienvenido de vuelta!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Aquí está el resumen de hoy",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Estadísticas
                    item {
                        DashboardStatsCard(
                            stats = stats,
                            primaryColor = PrimaryColor
                        )
                    }

                    // Acciones Rápidas
                    item {
                        QuickActionsCard(
                            actions = quickActions,
                            primaryColor = PrimaryColor
                        )
                    }

                    // Últimas Noticias
                    item {
                        LatestNewsCard(
                            newsList = dashboardNews,
                            onNewsClick = { navController.navigate("example_tar") },
                            onViewAllClick = { navController.navigate("moderatorCreateNews") },
                            primaryColor = PrimaryColor
                        )
                    }

                    // Últimos Reportes
                    item {
                        LatestReportsCard(
                            reportsList = dashboardReports,
                            onReportClick = { navController.navigate("example_tar") },
                            onViewAllClick = { navController.navigate("example_tar") },
                            primaryColor = PrimaryColor
                        )
                    }

                    // Footer
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Última actualización",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Hace 5 minutos",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

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
                                        tint = PrimaryColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        val userState = produceState<AppUser?>(initialValue = null) {
            value = getUserProfile(context)
        }
        val user = userState.value
        if (isMenuOpen) {
            ModeratorSideMenu(
                navController = navController,
                moderatorId = moderatorId,
                moderatorName = user?.name ?: "Moderator",
                currentRoute = "moderatorDashboard",
                isMenuOpen = isMenuOpen,
                onMenuToggle = { isMenuOpen = it },
                context = context,
                supabaseClient = supabaseClient
            )
        }
    }
}

private fun formatTimestamp(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return "Fecha desconocida"
    return "Hace 1 hora" // Implementa formato real si lo necesitas
}