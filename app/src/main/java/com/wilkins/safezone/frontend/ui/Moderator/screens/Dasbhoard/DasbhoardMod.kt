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
import androidx.navigation.NavController
import com.wilkins.safezone.frontend.ui.Moderator.screens.Dasbhoard.Components.DashboardStatsCard
import com.wilkins.safezone.frontend.ui.Moderator.screens.Dasbhoard.Components.LatestNewsCard
import com.wilkins.safezone.frontend.ui.Moderator.screens.Dasbhoard.Components.LatestReportsCard
import com.wilkins.safezone.frontend.ui.Moderator.screens.Dasbhoard.Components.NewsItem
import com.wilkins.safezone.frontend.ui.Moderator.screens.Dasbhoard.Components.QuickAction
import com.wilkins.safezone.frontend.ui.Moderator.screens.Dasbhoard.Components.QuickActionsCard
import com.wilkins.safezone.frontend.ui.Moderator.screens.Dasbhoard.Components.ReportItem
import com.wilkins.safezone.frontend.ui.Moderator.screens.Dasbhoard.Components.ReportPriority
import com.wilkins.safezone.frontend.ui.Moderator.screens.Dasbhoard.Components.ReportStatus
import com.wilkins.safezone.frontend.ui.Moderator.screens.Dasbhoard.Components.StatItem
import com.wilkins.safezone.frontend.ui.Moderator.ModeratorSideMenu
import com.wilkins.safezone.ui.theme.PrimaryColor
import io.github.jan.supabase.SupabaseClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorDashboard(
    navController: NavController,
    moderatorId: String,
    modifier: Modifier = Modifier,
    onNewsClick: (NewsItem) -> Unit = {},
    onReportClick: (ReportItem) -> Unit = {},
    onViewAllNewsClick: () -> Unit = {},
    onViewAllReportsClick: () -> Unit = {},
    context: Context,
    supabaseClient: SupabaseClient
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    // Sample data - Reemplaza esto con datos reales de tu ViewModel
    val stats = remember {
        listOf(
            StatItem(
                title = "Reportes Activos",
                value = "24",
                icon = Icons.Default.Warning,
                trend = "+12%",
                trendUp = true
            ),
            StatItem(
                title = "Noticias Publicadas",
                value = "156",
                icon = Icons.Default.Newspaper,
                trend = "+8%",
                trendUp = true
            ),
            StatItem(
                title = "Usuarios Activos",
                value = "1,847",
                icon = Icons.Default.People,
                trend = "+15%",
                trendUp = true
            ),
            StatItem(
                title = "Resoluciones Hoy",
                value = "18",
                icon = Icons.Default.CheckCircle,
                trend = "-5%",
                trendUp = false
            )
        )
    }

    val newsList = remember {
        listOf(
            NewsItem(
                id = "1",
                title = "Nueva política de seguridad implementada",
                summary = "Se han actualizado las normativas de seguridad en todas las zonas residenciales...",
                author = "Admin Principal",
                timestamp = "Hace 2 horas",
                views = 342,
                isPublished = true
            ),
            NewsItem(
                id = "2",
                title = "Mantenimiento programado del sistema",
                summary = "El próximo lunes se realizará mantenimiento preventivo...",
                author = "Moderador Tech",
                timestamp = "Hace 5 horas",
                views = 128,
                isPublished = false
            ),
            NewsItem(
                id = "3",
                title = "Alerta de zona de alto riesgo",
                summary = "Se ha identificado una zona con reportes frecuentes de incidentes...",
                author = "Moderador Seguridad",
                timestamp = "Hace 1 día",
                views = 567,
                isPublished = true
            )
        )
    }

    val reportsList = remember {
        listOf(
            ReportItem(
                id = "1",
                title = "Iluminación deficiente en parque central",
                description = "Varias luces del parque están fundidas creando zonas oscuras...",
                reportedBy = "Usuario123",
                timestamp = "Hace 30 min",
                priority = ReportPriority.HIGH,
                status = ReportStatus.PENDING,
                location = "Parque Central"
            ),
            ReportItem(
                id = "2",
                title = "Ruido excesivo en zona residencial",
                description = "Quejas de múltiples residentes por ruido nocturno...",
                reportedBy = "ResidenteX",
                timestamp = "Hace 1 hora",
                priority = ReportPriority.MEDIUM,
                status = ReportStatus.IN_PROGRESS,
                location = "Sector Norte"
            ),
            ReportItem(
                id = "3",
                title = "Basura acumulada en esquina",
                description = "Acumulación de basura en la esquina de la calle principal...",
                reportedBy = "Vecino45",
                timestamp = "Hace 3 horas",
                priority = ReportPriority.LOW,
                status = ReportStatus.RESOLVED,
                location = "Calle Principal"
            )
        )
    }

    val quickActions = remember {
        listOf(
            QuickAction(
                title = "Nueva Noticia",
                icon = Icons.Default.Add,
                onClick = { navController.navigate("moderatorCreateNews") }
            ),
            QuickAction(
                title = "Ver Reportes",
                icon = Icons.Default.Report,
                onClick = { navController.navigate("moderatorReports") }
            ),
            QuickAction(
                title = "Usuarios",
                icon = Icons.Default.People,
                onClick = { navController.navigate("moderatorUsers") }
            ),
            QuickAction(
                title = "Estadísticas",
                icon = Icons.Default.BarChart,
                onClick = { navController.navigate("moderatorStats") }
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
                                    Text("5")
                                }
                            }
                        ) {
                            IconButton(onClick = {
                                navController.navigate("moderatorNotifications")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificaciones",
                                    tint = Color.White
                                )
                            }
                        }
                        IconButton(onClick = {
                            navController.navigate("moderatorProfile")
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header con saludo
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
                        newsList = newsList,
                        onNewsClick = onNewsClick,
                        onViewAllClick = onViewAllNewsClick,
                        primaryColor = PrimaryColor
                    )
                }

                // Últimos Reportes
                item {
                    LatestReportsCard(
                        reportsList = reportsList,
                        onReportClick = onReportClick,
                        onViewAllClick = onViewAllReportsClick,
                        primaryColor = PrimaryColor
                    )
                }

                // Footer con información adicional
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

                            IconButton(onClick = { /* Refresh */ }) {
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

        if (isMenuOpen) {
            ModeratorSideMenu(
                navController = navController,
                moderatorId = moderatorId,
                moderatorName = "Nombre del Moderador", // Agrega el nombre del moderador
                currentRoute = "moderatorDashboard",
                isMenuOpen = isMenuOpen,
                onMenuToggle = { isOpen ->
                    isMenuOpen = isOpen
                },
                context = context, // Agrega el context
                supabaseClient = supabaseClient // Agrega el supabaseClient
            )
        }
    }
}