package com.wilkins.safezone.frontend.ui.Admin.Screens.Dasbhoard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wilkins.safezone.GenericUserUi.AdminMenu
import com.wilkins.safezone.backend.network.Admin.Dashboard.*
import com.wilkins.safezone.backend.network.GlobalAssociation.DateUtils
import com.wilkins.safezone.ui.theme.PrimaryColor
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AdminDashboard(navController: NavController) {
    var isMenuOpen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estados para datos dinámicos
    var dashboardStats by remember { mutableStateOf<DashboardStats?>(null) }
    var monthlyActivity by remember { mutableStateOf<List<MonthlyActivity>>(emptyList()) }
    var recentReports by remember { mutableStateOf<List<RecentReport>>(emptyList()) }
    var recentActivities by remember { mutableStateOf<List<ActivityLog>>(emptyList()) }

    val repository = remember { DashboardRepository() }
    val scope = rememberCoroutineScope()

    // Función para cargar datos
    suspend fun loadDashboardData() {
        try {
            println("🔄 AdminDashboard: Iniciando carga de datos...")

            // Cargar datos en paralelo usando async/await
            val statsDeferred = scope.async {
                println("🔄 Lanzando getDashboardStats...")
                repository.getDashboardStats()
            }
            val activityDeferred = scope.async {
                println("🔄 Lanzando getMonthlyActivity...")
                repository.getMonthlyActivity()
            }
            val reportsDeferred = scope.async {
                println("🔄 Lanzando getRecentReports...")
                repository.getRecentReports(3)
            }
            val activitiesDeferred = scope.async {
                println("🔄 Lanzando getRecentActivities...")
                repository.getRecentActivities(5)
            }

            // Esperar a que todos completen
            println("⏳ Esperando resultados...")
            dashboardStats = statsDeferred.await()
            monthlyActivity = activityDeferred.await()
            recentReports = reportsDeferred.await()
            recentActivities = activitiesDeferred.await()

            println("✅ AdminDashboard: Todos los datos cargados exitosamente")
            println("   - Stats: \${dashboardStats != null}")
            println("   - Monthly: \${monthlyActivity.size} items")
            println("   - Reports: \${recentReports.size} items")
            println("   - Activities: \${recentActivities.size} items")

            errorMessage = null
        } catch (e: Exception) {
            System.err.println("❌ AdminDashboard: Error al cargar datos")
            System.err.println("   Mensaje: \${e.message}")
            e.printStackTrace()
            errorMessage = "Error al cargar datos: \${e.message}"
        }
    }

    // Cargar datos al inicio
    LaunchedEffect(Unit) {
        println("🚀 AdminDashboard: LaunchedEffect iniciado")
        isLoading = true
        loadDashboardData()
        isLoading = false
        println("🏁 AdminDashboard: LaunchedEffect completado")
    }

    AdminMenu(
        navController = navController,
        modifier = Modifier.fillMaxSize(),
        showHeader = false,
        isMenuOpen = isMenuOpen,
        onMenuToggle = { isMenuOpen = !isMenuOpen }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DashboardHeader(
                onMenuClick = { isMenuOpen = !isMenuOpen },
                onNotificationClick = { navController.navigate("notifications") }
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = PrimaryColor)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cargando dashboard...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Error al cargar",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage ?: "Error desconocido",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        isLoading = true
                                        loadDashboardData()
                                        isLoading = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reintentar")
                            }
                        }
                    }
                }
                else -> {
                    DashboardContent(
                        navController = navController,
                        stats = dashboardStats,
                        monthlyActivity = monthlyActivity,
                        recentReports = recentReports,
                        recentActivities = recentActivities,
                        onRefresh = {
                            scope.launch {
                                isLoading = true
                                loadDashboardData()
                                isLoading = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardHeader(
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(PrimaryColor, PrimaryColor.copy(alpha = 0.8f))
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Dashboard",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Panel de Administración",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }

        IconButton(onClick = onNotificationClick) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "Notificaciones",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun DashboardContent(
    navController: NavController,
    stats: DashboardStats?,
    monthlyActivity: List<MonthlyActivity>,
    recentReports: List<RecentReport>,
    recentActivities: List<ActivityLog>,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Botón de refresh
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Actualizar")
                }
            }
        }

        // Debug info
        if (stats == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF856404)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "No se pudieron cargar las estadísticas",
                            color = Color(0xFF856404),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Estadísticas principales
        item {
            Text(
                text = "Resumen General",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            StatsCardsSection(stats)
        }

        // Gráfico de actividad mensual
        if (monthlyActivity.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Actividad Mensual",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                BarChartCard(monthlyActivity)
            }
        }

        // Últimos reportes
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Últimos Reportes Enviados",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            if (recentReports.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No hay reportes recientes",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                RecentReportsSection(recentReports)
            }
        }

        // Últimos mensajes (estáticos por ahora)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Últimos Mensajes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            RecentMessagesSection()
        }

        // Últimas actividades (dinámico)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Últimas Actualizaciones",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            if (recentActivities.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No hay actividades recientes",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                RecentUpdatesSection(recentActivities)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatsCardsSection(stats: DashboardStats?) {
    val statsData = listOf(
        StatData(
            "Reportes enviados",
            stats?.reportsSent?.toString() ?: "0",
            Icons.Default.Send,
            Color(0xFF4CAF50)
        ),
        StatData(
            "Reportes recibidos",
            stats?.reportsReceived?.toString() ?: "0",
            Icons.Default.Inbox,
            Color(0xFF2196F3)
        ),
        StatData(
            "Reportes resueltos",
            stats?.reportsResolved?.toString() ?: "0",
            Icons.Default.CheckCircle,
            Color(0xFFFF9800)
        ),
        StatData(
            "Reportes cancelados",
            stats?.reportsCancelled?.toString() ?: "0",
            Icons.Default.Cancel,
            Color(0xFFF44336)
        )
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(statsData) { stat ->
            StatCard(stat)
        }
    }
}

@Composable
fun StatCard(stat: StatData) {
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(300),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stat.label,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stat.value,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = stat.color
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(stat.color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        stat.icon,
                        contentDescription = null,
                        tint = stat.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BarChartCard(monthlyActivity: List<MonthlyActivity>) {
    val maxCount = monthlyActivity.maxOfOrNull { it.reportCount } ?: 1
    val barHeights = monthlyActivity.map { (it.reportCount.toFloat() / maxCount).coerceIn(0.2f, 1f) }
    val months = monthlyActivity.map { it.monthName }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Estadísticas de Actividad",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                barHeights.zip(months).forEach { (height, label) ->
                    AnimatedBar(
                        height = height,
                        label = label,
                        color = PrimaryColor
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedBar(height: Float, label: String, color: Color) {
    var animatedHeight by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        animatedHeight = height
    }

    val animatedValue by animateFloatAsState(
        targetValue = animatedHeight,
        animationSpec = tween(1000),
        label = "barHeight"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(40.dp)
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(animatedValue)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = 0.6f))
                    )
                )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RecentReportsSection(reports: List<RecentReport>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        reports.forEach { report ->
            ReportCard(report)
        }
    }
}

@Composable
fun ReportCard(report: RecentReport) {
    val status = when (report.statusId) {
        1 -> ReportStatus.PENDING
        2 -> ReportStatus.IN_PROGRESS
        3 -> ReportStatus.COMPLETED
        4 -> ReportStatus.CANCELLED
        else -> ReportStatus.PENDING
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Por: ${report.userName}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateUtils.formatDateTime(report.createdAt),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            StatusBadge(status)
        }
    }
}

@Composable
fun StatusBadge(status: ReportStatus) {
    val (text, color) = when (status) {
        ReportStatus.PENDING -> "Pendiente" to Color(0xFFFF9800)
        ReportStatus.IN_PROGRESS -> "En proceso" to Color(0xFF2196F3)
        ReportStatus.COMPLETED -> "Completado" to Color(0xFF4CAF50)
        ReportStatus.CANCELLED -> "Cancelado" to Color(0xFFF44336)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun RecentMessagesSection() {
    val messages = listOf(
        MessageItem("Usuario 1", "Mensaje de ejemplo aquí...", "10:30 AM"),
        MessageItem("Usuario 2", "Otro mensaje de prueba...", "09:15 AM"),
        MessageItem("Usuario 3", "Consulta sobre el sistema...", "Ayer")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        messages.forEach { message ->
            MessageCard(message)
        }
    }
}

@Composable
fun MessageCard(message: MessageItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(PrimaryColor, PrimaryColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.sender.first().uppercase(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = message.sender,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = message.time,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.content,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RecentUpdatesSection(activities: List<ActivityLog>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        activities.forEach { activity ->
            UpdateCard(activity)
        }
    }
}

@Composable
fun UpdateCard(activity: ActivityLog) {
    val (icon, color) = when (activity.actionType) {
        "ADDED" -> Icons.Default.Add to Color(0xFF4CAF50)
        "EDITED" -> Icons.Default.Edit to Color(0xFF2196F3)
        "DELETED" -> Icons.Default.Delete to Color(0xFFF44336)
        else -> Icons.Default.Notifications to Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.recordTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateUtils.formatDateTime(activity.createdAt),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// Data classes
data class StatData(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

data class MessageItem(
    val sender: String,
    val content: String,
    val time: String
)

enum class ReportStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED
}