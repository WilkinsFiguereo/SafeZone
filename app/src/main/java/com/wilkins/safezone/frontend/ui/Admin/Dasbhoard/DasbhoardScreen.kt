//package com.wilkins.safezone.frontend.ui.Admin.Dasbhoard
//
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.wilkins.safezone.GenericUserUi.AdminMenu
//import com.wilkins.safezone.frontend.ui.Admin.Dasbhoard.components.RecentUpdatesSection
//import com.wilkins.safezone.ui.theme.PrimaryColor
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//@Composable
//fun AdminDashboard(navController: NavController) {
//    var isMenuOpen by remember { mutableStateOf(false) }
//
//    // Estados para animaciones de carga
//    var isLoading by remember { mutableStateOf(true) }
//
//    LaunchedEffect(Unit) {
//        delay(1000) // Simular carga de datos
//        isLoading = false
//    }
//
//    AdminMenu(
//        navController = navController,
//        modifier = Modifier.fillMaxSize(),
//        showHeader = false,
//        isMenuOpen = isMenuOpen,
//        onMenuToggle = { isMenuOpen = !isMenuOpen }
//    ) {
//        Column(modifier = Modifier.fillMaxSize()) {
//            // Header personalizado del Dashboard
//            DashboardHeader(
//                onMenuClick = { isMenuOpen = !isMenuOpen },
//                onNotificationClick = { navController.navigate("notifications") }
//            )
//
//            // Contenido del dashboard
//            if (isLoading) {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator(color = PrimaryColor)
//                }
//            } else {
//                DashboardContent(navController)
//            }
//        }
//    }
//}
//
//@Composable
//fun DashboardHeader(
//    onMenuClick: () -> Unit,
//    onNotificationClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(
//                brush = Brush.horizontalGradient(
//                    colors = listOf(PrimaryColor, PrimaryColor.copy(alpha = 0.8f))
//                )
//            )
//            .padding(horizontal = 16.dp, vertical = 12.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            IconButton(onClick = onMenuClick) {
//                Icon(
//                    Icons.Default.Menu,
//                    contentDescription = "Menu",
//                    tint = Color.White,
//                    modifier = Modifier.size(24.dp)
//                )
//            }
//            Spacer(modifier = Modifier.width(8.dp))
//            Column {
//                Text(
//                    text = "Dashboard",
//                    color = Color.White,
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold
//                )
//                Text(
//                    text = "Panel de Administración",
//                    color = Color.White.copy(alpha = 0.8f),
//                    fontSize = 12.sp
//                )
//            }
//        }
//
//        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//            IconButton(onClick = onNotificationClick) {
//                Badge(
//                    containerColor = Color.Red,
//                    modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
//                ) {
//                    Text("3", fontSize = 10.sp, color = Color.White)
//                }
//                Icon(
//                    Icons.Default.Notifications,
//                    contentDescription = "Notificaciones",
//                    tint = Color.White,
//                    modifier = Modifier.size(24.dp)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun DashboardContent(navController: NavController) {
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF5F5F5)),
//        contentPadding = PaddingValues(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        // Sección de estadísticas principales
//        item {
//            Text(
//                text = "Resumen General",
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(vertical = 8.dp)
//            )
//        }
//
//        item {
//            StatsCardsSection()
//        }
//
//        // Gráfico de barras
//        item {
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = "Actividad Mensual",
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(vertical = 8.dp)
//            )
//        }
//
//        item {
//            BarChartCard()
//        }
//
//        // Últimos reportes enviados
//        item {
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = "Últimos Reportes Enviados",
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(vertical = 8.dp)
//            )
//        }
//
//        item {
//            RecentReportsSection()
//        }
//
//        // Últimos mensajes
//        item {
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = "Últimos Mensajes",
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(vertical = 8.dp)
//            )
//        }
//
//        item {
//            RecentMessagesSection()
//        }
//
//        // Últimas actualizaciones
//        item {
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = "Últimas Actualizaciones",
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(vertical = 8.dp)
//            )
//        }
//
//        item {
//            RecentUpdatesSection()
//        }
//
//        // Espaciado final
//        item {
//            Spacer(modifier = Modifier.height(16.dp))
//        }
//    }
//}
//
//@Composable
//fun StatsCardsSection() {
//    val stats = listOf(
//        StatData("Reportes enviados", "30", Icons.Default.Send, Color(0xFF4CAF50)),
//        StatData("Reportes recibidos", "30", Icons.Default.Inbox, Color(0xFF2196F3)),
//        StatData("Reportes resueltos", "30", Icons.Default.CheckCircle, Color(0xFFFF9800)),
//        StatData("Reportes cancelados", "30", Icons.Default.Cancel, Color(0xFFF44336))
//    )
//
//    LazyRow(
//        horizontalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        items(stats) { stat ->
//            StatCard(stat)
//        }
//    }
//}
//
//@Composable
//fun StatCard(stat: StatData) {
//    var isVisible by remember { mutableStateOf(false) }
//    val scale by animateFloatAsState(
//        targetValue = if (isVisible) 1f else 0.8f,
//        animationSpec = tween(300),
//        label = "scale"
//    )
//
//    LaunchedEffect(Unit) {
//        delay(100)
//        isVisible = true
//    }
//
//    Card(
//        modifier = Modifier
//            .width(160.dp)
//            .height(120.dp),
//        shape = RoundedCornerShape(16.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.SpaceBetween
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.Top
//            ) {
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = stat.label,
//                        fontSize = 12.sp,
//                        color = Color.Gray,
//                        maxLines = 2,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        text = stat.value,
//                        fontSize = 28.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = stat.color
//                    )
//                }
//
//                Box(
//                    modifier = Modifier
//                        .size(40.dp)
//                        .background(stat.color.copy(alpha = 0.1f), CircleShape),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        stat.icon,
//                        contentDescription = null,
//                        tint = stat.color,
//                        modifier = Modifier.size(24.dp)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun BarChartCard() {
//    val barHeights = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f)
//    val months = listOf("Ene", "Feb", "Mar", "Abr", "May")
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(280.dp),
//        shape = RoundedCornerShape(16.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(20.dp)
//        ) {
//            Text(
//                text = "Estadísticas de Actividad",
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Bold
//            )
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // Gráfico de barras
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f),
//                horizontalArrangement = Arrangement.SpaceEvenly,
//                verticalAlignment = Alignment.Bottom
//            ) {
//                barHeights.forEachIndexed { index, height ->
//                    AnimatedBar(
//                        height = height,
//                        label = months[index],
//                        color = PrimaryColor
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun AnimatedBar(height: Float, label: String, color: Color) {
//    var animatedHeight by remember { mutableStateOf(0f) }
//
//    LaunchedEffect(Unit) {
//        animatedHeight = height
//    }
//
//    val animatedValue by animateFloatAsState(
//        targetValue = animatedHeight,
//        animationSpec = tween(1000),
//        label = "barHeight"
//    )
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier.width(40.dp)
//    ) {
//        Box(
//            modifier = Modifier
//                .width(32.dp)
//                .fillMaxHeight(animatedValue)
//                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
//                .background(
//                    brush = Brush.verticalGradient(
//                        colors = listOf(color, color.copy(alpha = 0.6f))
//                    )
//                )
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(
//            text = label,
//            fontSize = 11.sp,
//            color = Color.Gray,
//            fontWeight = FontWeight.Medium
//        )
//    }
//}
//
//@Composable
//fun RecentReportsSection() {
//    val reports = listOf(
//        ReportItem(
//            "Reporte 1",
//            "Usuario Juan Pérez - Incidente reportado...",
//            "Hace 10 min",
//            ReportStatus.PENDING
//        ),
//        ReportItem(
//            "Reporte 2",
//            "Usuario Ana García - Problema de acceso...",
//            "Hace 25 min",
//            ReportStatus.IN_PROGRESS
//        ),
//        ReportItem(
//            "Reporte 3",
//            "Usuario Carlos López - Solicitud de ayuda...",
//            "Hace 1 hora",
//            ReportStatus.COMPLETED
//        )
//    )
//
//    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//        reports.forEach { report ->
//            ReportCard(report)
//        }
//    }
//}
//
//@Composable
//fun ReportCard(report: ReportItem) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(12.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Icono de imagen/miniatura
//            Box(
//                modifier = Modifier
//                    .size(56.dp)
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(Color(0xFFE0E0E0)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    Icons.Default.Description,
//                    contentDescription = null,
//                    tint = Color.Gray,
//                    modifier = Modifier.size(28.dp)
//                )
//            }
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            // Información del reporte
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = report.title,
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Bold,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = report.description,
//                    fontSize = 12.sp,
//                    color = Color.Gray,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
//                )
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = report.time,
//                    fontSize = 11.sp,
//                    color = Color.Gray
//                )
//            }
//
//            // Status badge
//            StatusBadge(report.status)
//        }
//    }
//}
//
//@Composable
//fun StatusBadge(status: ReportStatus) {
//    val (text, color) = when (status) {
//        ReportStatus.PENDING -> "Pendiente" to Color(0xFFFF9800)
//        ReportStatus.IN_PROGRESS -> "En proceso" to Color(0xFF2196F3)
//        ReportStatus.COMPLETED -> "Completado" to Color(0xFF4CAF50)
//        ReportStatus.CANCELLED -> "Cancelado" to Color(0xFFF44336)
//    }
//
//    Surface(
//        shape = RoundedCornerShape(6.dp),
//        color = color.copy(alpha = 0.15f)
//    ) {
//        Text(
//            text = text,
//            fontSize = 10.sp,
//            fontWeight = FontWeight.Medium,
//            color = color,
//            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
//        )
//    }
//}
//
//@Composable
//fun RecentMessagesSection() {
//    val messages = listOf(
//        MessageItem("Usuario 1", "Mensaje de ejemplo aquí...", "10:30 AM"),
//        MessageItem("Usuario 2", "Otro mensaje de prueba...", "09:15 AM"),
//        MessageItem("Usuario 3", "Consulta sobre el sistema...", "Ayer")
//    )
//
//    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//        messages.forEach { message ->
//            MessageCard(message)
//        }
//    }
//}
//
//@Composable
//fun MessageCard(message: MessageItem) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(12.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Avatar
//            Box(
//                modifier = Modifier
//                    .size(48.dp)
//                    .clip(CircleShape)
//                    .background(
//                        brush = Brush.linearGradient(
//                            colors = listOf(PrimaryColor, PrimaryColor.copy(alpha = 0.7f))
//                        )
//                    ),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = message.sender.first().uppercase(),
//                    color = Color.White,
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            // Contenido del mensaje
//            Column(modifier = Modifier.weight(1f)) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(
//                        text = message.sender,
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Text(
//                        text = message.time,
//                        fontSize = 11.sp,
//                        color = Color.Gray
//                    )
//                }
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = message.content,
//                    fontSize = 13.sp,
//                    color = Color.Gray,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//        }
//    }
//}
//
//// Data classes
//data class StatData(
//    val label: String,
//    val value: String,
//    val icon: ImageVector,
//    val color: Color
//)
//
//data class ReportItem(
//    val title: String,
//    val description: String,
//    val time: String,
//    val status: ReportStatus
//)
//
//data class MessageItem(
//    val sender: String,
//    val content: String,
//    val time: String
//)
//
//enum class ReportStatus {
//    PENDING, IN_PROGRESS, COMPLETED, CANCELLED
//}