package com.wilkins.safezone.frontend.ui.GlobalAssociation.screens.Dashboard.Components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.safezone.backend.network.GlobalAssociation.*
import com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components.StatusBadge
import com.wilkins.safezone.navigation.theme.PrimaryColor

@Composable
fun ReportCardAssociation(
    report: ReportDto,
    affairName: String?,
    statusName: String?,
    onStatusChange: (Int) -> Unit,
    onNavigateToDetail: (String) -> Unit  // ← NUEVO PARÁMETRO
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header del reporte
            ReportCardHeader(
                affairName = affairName,
                description = report.description,
                reportId = report.id,
                statusId = report.idReportingStatus,
                statusName = statusName
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Información básica
            ReportBasicInfo(
                isAnonymous = report.isAnonymous,
                userName = report.userName,
                createdAt = report.createdAt,
                reportLocation = report.reportLocation
            )

            // Contenido expandible
            if (isExpanded) {
                ReportExpandedContent(
                    description = report.description,
                    reportId = report.id,  // ← NUEVO
                    onNavigateToDetail = onNavigateToDetail  // ← NUEVO
                )
            }
        }
    }
}

@Composable
private fun ReportCardHeader(
    affairName: String?,
    description: String?,
    reportId: String,
    statusId: Int,
    statusName: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ReportUtils.generateTitle(affairName, description),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ID: ${ReportUtils.getShortId(reportId)}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        StatusBadge(
            statusId = statusId,
            statusName = statusName ?: "Desconocido"
        )
    }
}

@Composable
private fun ReportBasicInfo(
    isAnonymous: Boolean,
    userName: String?,
    createdAt: String,
    reportLocation: String?
) {
    Column {
        InfoRow(
            icon = Icons.Default.Person,
            label = "Reportado por",
            value = ReportUtils.getReporterName(isAnonymous, userName)
        )

        Spacer(modifier = Modifier.height(8.dp))

        InfoRow(
            icon = Icons.Default.Schedule,
            label = "Fecha",
            value = DateUtils.formatDateTime(createdAt)
        )

        if (!reportLocation.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(
                icon = Icons.Default.Place,
                label = "Ubicación",
                value = reportLocation
            )
        }
    }
}

@Composable
private fun ReportExpandedContent(
    description: String?,
    reportId: String,  // ← NUEVO
    onNavigateToDetail: (String) -> Unit  // ← NUEVO
) {
    Column {
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(12.dp))

        if (!description.isNullOrBlank()) {
            Text(
                text = "Descripción:",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Botón para ir a detalle y cambiar estado
        Button(
            onClick = { onNavigateToDetail(reportId) },  // ← CAMBIO AQUÍ
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver Detalle y Cambiar Estado")  // ← TEXTO ACTUALIZADO
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = Color.DarkGray
        )
    }
}

// Componente adicional para listas de reportes
@Composable
fun ReportsListAssociation(
    reports: List<ReportDto>,
    affairs: Map<Int, AffairDto>,
    statuses: Map<Int, ReportingStatusDto>,
    onStatusChange: (String, Int) -> Unit,
    onNavigateToDetail: (String) -> Unit  // ← NUEVO
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        reports.forEach { report ->
            ReportCardAssociation(
                report = report,
                affairName = affairs[report.idAffair]?.affairName,
                statusName = statuses[report.idReportingStatus]?.statusName,
                onStatusChange = { newStatusId ->
                    onStatusChange(report.id, newStatusId)
                },
                onNavigateToDetail = onNavigateToDetail  // ← NUEVO
            )
        }
    }
}

// Mensaje cuando no hay reportes
@Composable
fun EmptyReportsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Assignment,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay reportes disponibles",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }
    }
}