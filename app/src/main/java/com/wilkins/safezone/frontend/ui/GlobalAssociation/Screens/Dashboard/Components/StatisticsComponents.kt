package com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Tarjeta individual de estadística
@Composable
fun StatisticCard(
    title: String,
    value: Int,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color = Color.White,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value.toString(),
                    fontSize = 32.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

// Grid completo de estadísticas con todos los reportes
@Composable
fun StatisticsGrid(
    totalReports: Int,
    pendingReports: Int,
    inProgressReports: Int,
    completedReports: Int,
    cancelledReports: Int,
    primaryColor: Color,
    onTotalClick: () -> Unit = {},
    onPendingClick: () -> Unit = {},
    onInProgressClick: () -> Unit = {},
    onCompletedClick: () -> Unit = {},
    onCancelledClick: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Total de reportes (tarjeta completa)
        StatisticCard(
            title = "Total de Reportes",
            value = totalReports,
            icon = Icons.Default.Assignment,
            backgroundColor = primaryColor,
            onClick = onTotalClick
        )

        // Primera fila: Pendientes y En Proceso
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                StatisticCard(
                    title = "Pendientes",
                    value = pendingReports,
                    icon = Icons.Default.Pending,
                    backgroundColor = Color(0xFFFFC107),
                    onClick = onPendingClick
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                StatisticCard(
                    title = "En Proceso",
                    value = inProgressReports,
                    icon = Icons.Default.Update,
                    backgroundColor = Color(0xFF2196F3),
                    onClick = onInProgressClick
                )
            }
        }

        // Segunda fila: Finalizados y Cancelados
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                StatisticCard(
                    title = "Finalizados",
                    value = completedReports,
                    icon = Icons.Default.CheckCircle,
                    backgroundColor = Color(0xFF4CAF50),
                    onClick = onCompletedClick
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                StatisticCard(
                    title = "Cancelados",
                    value = cancelledReports,
                    icon = Icons.Default.Cancel,
                    backgroundColor = Color(0xFFF44336),
                    onClick = onCancelledClick
                )
            }
        }
    }
}

// Sección de estadísticas con título
@Composable
fun StatisticsSection(
    totalReports: Int,
    pendingReports: Int,
    inProgressReports: Int,
    completedReports: Int,
    cancelledReports: Int,
    primaryColor: Color,
    onTotalClick: () -> Unit = {},
    onPendingClick: () -> Unit = {},
    onInProgressClick: () -> Unit = {},
    onCompletedClick: () -> Unit = {},
    onCancelledClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Estadísticas Generales",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        StatisticsGrid(
            totalReports = totalReports,
            pendingReports = pendingReports,
            inProgressReports = inProgressReports,
            completedReports = completedReports,
            cancelledReports = cancelledReports,
            primaryColor = primaryColor,
            onTotalClick = onTotalClick,
            onPendingClick = onPendingClick,
            onInProgressClick = onInProgressClick,
            onCompletedClick = onCompletedClick,
            onCancelledClick = onCancelledClick
        )
    }
}