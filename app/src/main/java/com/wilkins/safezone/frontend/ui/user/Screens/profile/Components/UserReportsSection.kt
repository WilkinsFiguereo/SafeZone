package com.wilkins.safezone.frontend.ui.user.Screens.profile.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.safezone.backend.network.User.Profile.ReportWithAffair
import com.wilkins.safezone.ui.theme.PrimaryColor
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserReportsSection(
    reports: List<ReportWithAffair>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Assignment,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Últimos Reportes Públicos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1F1F)
                )
            }

            if (reports.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${reports.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Contenido
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PrimaryColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            reports.isEmpty() -> {
                EmptyReportsState()
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    reports.take(3).forEach { report ->
                        ReportCard(report = report)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyReportsState(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8F9FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AssignmentLate,
                contentDescription = null,
                tint = Color(0xFF9E9E9E),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "No hay reportes públicos",
                fontSize = 14.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Los reportes públicos aparecerán aquí",
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}

@Composable
fun ReportCard(
    report: ReportWithAffair,
    modifier: Modifier = Modifier
) {
    val statusColor = when (report.id_reporting_status) {
        1 -> Color(0xFFFFA726) // Pendiente
        2 -> Color(0xFF42A5F5) // Revisado
        3 -> Color(0xFF66BB6A) // Resuelto
        4 -> Color(0xFFEF5350) // Rechazado
        5 -> Color(0xFFDA7300) // En revisión
        else -> Color(0xFF9E9E9E)
    }

    val statusText = when (report.id_reporting_status) {
        1 -> "Pendiente"
        2 -> "Revisado"
        3 -> "Resuelto"
        4 -> "Rechazado"
        5 -> "En revisión"
        else -> "Desconocido"
    }

    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "ES"))
    }

    val formattedDate = try {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            .parse(report.created_at)
        date?.let { dateFormatter.format(it) } ?: report.created_at
    } catch (e: Exception) {
        report.created_at
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navegar a detalle */ },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Reporte #${report.id.take(8).uppercase()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryColor.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = PrimaryColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = report.affairType,
                                fontSize = 11.sp,
                                color = PrimaryColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Descripción
            Text(
                text = report.description,
                fontSize = 13.sp,
                color = Color(0xFF616161),
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Footer
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ver detalle",
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (!report.report_location.isNullOrBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = report.report_location,
                            fontSize = 11.sp,
                            color = Color(0xFF9E9E9E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}