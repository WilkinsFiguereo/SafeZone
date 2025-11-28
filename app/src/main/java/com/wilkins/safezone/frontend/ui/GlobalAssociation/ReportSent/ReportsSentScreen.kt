package com.wilkins.safezone.frontend.ui.GlobalAssociation.ReportSent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wilkins.safezone.ui.theme.PrimaryColor

// Data class para el reporte en lista
data class ReportItem(
    val id: String,
    val title: String,
    val incidentType: String,
    val location: String,
    val date: String,
    val time: String,
    val status: ReportStatus,
    val priority: ReportPriority,
    val reporterName: String,
    val description: String
)

enum class ReportStatus(val label: String, val color: Color, val icon: ImageVector) {
    PENDING("Pendiente", Color(0xFFFFA726), Icons.Default.Pending),
    IN_PROGRESS("En Proceso", Color(0xFF2196F3), Icons.Default.Update),
    COMPLETED("Finalizado", Color(0xFF4CAF50), Icons.Default.CheckCircle),
    CANCELLED("Cancelado", Color(0xFFE53935), Icons.Default.Cancel)
}

enum class ReportPriority(val label: String, val color: Color) {
    LOW("Baja", Color(0xFF66BB6A)),
    MEDIUM("Media", Color(0xFFFFA726)),
    HIGH("Alta", Color(0xFFEF5350)),
    CRITICAL("Crítica", Color(0xFFB71C1C))
}

@Composable
fun ReportsSentScreen(
    navController: NavController
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<ReportStatus?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }

    // Datos de ejemplo
    val allReports = remember {
        listOf(
            ReportItem(
                id = "001",
                title = "Vandalismo en Parque Central",
                incidentType = "Vandalismo",
                location = "Parque Central, Calle Principal #123",
                date = "28 Nov 2025",
                time = "10:30 AM",
                status = ReportStatus.PENDING,
                priority = ReportPriority.HIGH,
                reporterName = "Juan Pérez",
                description = "Grafitis y bancas destruidas en el parque municipal"
            ),
            ReportItem(
                id = "002",
                title = "Bache Peligroso en Avenida",
                incidentType = "Infraestructura",
                location = "Av. Independencia #456",
                date = "27 Nov 2025",
                time = "03:15 PM",
                status = ReportStatus.IN_PROGRESS,
                priority = ReportPriority.CRITICAL,
                reporterName = "María González",
                description = "Bache de gran tamaño que causa accidentes"
            ),
            ReportItem(
                id = "003",
                title = "Alumbrado Público Dañado",
                incidentType = "Servicios Públicos",
                location = "Calle 5 de Mayo #789",
                date = "26 Nov 2025",
                time = "08:45 PM",
                status = ReportStatus.COMPLETED,
                priority = ReportPriority.MEDIUM,
                reporterName = "Carlos Ramírez",
                description = "Postes de luz sin funcionar en la zona"
            ),
            ReportItem(
                id = "004",
                title = "Basura Acumulada",
                incidentType = "Limpieza",
                location = "Mercado Municipal",
                date = "26 Nov 2025",
                time = "07:00 AM",
                status = ReportStatus.PENDING,
                priority = ReportPriority.MEDIUM,
                reporterName = "Ana López",
                description = "Acumulación de basura por falta de recolección"
            ),
            ReportItem(
                id = "005",
                title = "Semáforo Descompuesto",
                incidentType = "Seguridad Vial",
                location = "Cruce Av. Juárez y Calle 10",
                date = "25 Nov 2025",
                time = "12:20 PM",
                status = ReportStatus.IN_PROGRESS,
                priority = ReportPriority.HIGH,
                reporterName = "Roberto Silva",
                description = "Semáforo intermitente causando confusión"
            ),
            ReportItem(
                id = "006",
                title = "Fuga de Agua",
                incidentType = "Infraestructura",
                location = "Colonia Jardines #234",
                date = "25 Nov 2025",
                time = "09:10 AM",
                status = ReportStatus.CANCELLED,
                priority = ReportPriority.LOW,
                reporterName = "Laura Martínez",
                description = "Reporte duplicado - ya fue atendido"
            ),
            ReportItem(
                id = "007",
                title = "Árbol Caído",
                incidentType = "Emergencia",
                location = "Bosque Municipal",
                date = "24 Nov 2025",
                time = "11:30 AM",
                status = ReportStatus.COMPLETED,
                priority = ReportPriority.CRITICAL,
                reporterName = "José Hernández",
                description = "Árbol bloqueando camino principal"
            ),
            ReportItem(
                id = "008",
                title = "Ruido Excesivo",
                incidentType = "Disturbio",
                location = "Calle del Sol #567",
                date = "24 Nov 2025",
                time = "11:00 PM",
                status = ReportStatus.PENDING,
                priority = ReportPriority.LOW,
                reporterName = "Patricia Ruiz",
                description = "Construcción nocturna sin permiso"
            )
        )
    }

    // Filtrar reportes
    val filteredReports = allReports.filter { report ->
        val matchesSearch = searchQuery.isEmpty() ||
                report.title.contains(searchQuery, ignoreCase = true) ||
                report.incidentType.contains(searchQuery, ignoreCase = true) ||
                report.location.contains(searchQuery, ignoreCase = true)

        val matchesFilter = selectedFilter == null || report.status == selectedFilter

        matchesSearch && matchesFilter
    }

    // Estadísticas
    val stats = remember(allReports) {
        mapOf(
            ReportStatus.PENDING to allReports.count { it.status == ReportStatus.PENDING },
            ReportStatus.IN_PROGRESS to allReports.count { it.status == ReportStatus.IN_PROGRESS },
            ReportStatus.COMPLETED to allReports.count { it.status == ReportStatus.COMPLETED },
            ReportStatus.CANCELLED to allReports.count { it.status == ReportStatus.CANCELLED }
        )
    }

    GovernmentMenu(
        navController = navController,
        isMenuOpen = isMenuOpen,
        onMenuToggle = { isMenuOpen = it },
        currentRoute = "reports_sent"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Header con título y estadísticas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Reportes Enviados",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "${allReports.size} reportes totales",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimaryColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = PrimaryColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mini estadísticas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportStatus.entries.forEach { status ->
                            MiniStatCard(
                                status = status,
                                count = stats[status] ?: 0,
                                modifier = Modifier.weight(1f),
                                isSelected = selectedFilter == status,
                                onClick = {
                                    selectedFilter = if (selectedFilter == status) null else status
                                }
                            )
                        }
                    }
                }
            }

            // Barra de búsqueda y filtros
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Campo de búsqueda
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Buscar reportes...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar búsqueda",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Indicador de filtro activo
                    if (selectedFilter != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = selectedFilter!!.color.copy(alpha = 0.1f),
                            modifier = Modifier.clickable { selectedFilter = null }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = selectedFilter!!.icon,
                                    contentDescription = null,
                                    tint = selectedFilter!!.color,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = selectedFilter!!.label,
                                    color = selectedFilter!!.color,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Quitar filtro",
                                    tint = selectedFilter!!.color,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de reportes
            if (filteredReports.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No se encontraron reportes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        Text(
                            text = "Intenta cambiar los filtros de búsqueda",
                            fontSize = 14.sp,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredReports) { report ->
                        ReportCard(
                            report = report,
                            onClick = {
                                navController.navigate("report_detail/${report.id}")
                            }
                        )
                    }

                    // Espaciado final
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MiniStatCard(
    status: ReportStatus,
    count: Int,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) status.color.copy(alpha = 0.15f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, status.color) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = status.icon,
                contentDescription = null,
                tint = status.color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) status.color else Color.Black
            )
            Text(
                text = status.label,
                fontSize = 10.sp,
                color = if (isSelected) status.color else Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ReportCard(
    report: ReportItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Título y prioridad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reporte #${report.id}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Badge de prioridad
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = report.priority.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = report.priority.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = report.priority.color
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tipo de incidente
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = report.incidentType,
                    fontSize = 13.sp,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Descripción
            Text(
                text = report.description,
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Ubicación
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = report.location,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Fecha, hora, reportero y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Fecha y hora
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${report.date} • ${report.time}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    // Reportero
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = report.reporterName,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Estado
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = report.status.color.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = report.status.icon,
                            contentDescription = null,
                            tint = report.status.color,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = report.status.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = report.status.color
                        )
                    }
                }
            }
        }
    }
}