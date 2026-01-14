package com.wilkins.safezone.frontend.ui.Moderator.screens.Statistics.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SummaryCard(
    totalReports: Int,
    resolvedReports: Int,
    totalNews: Int,
    totalUsers: Int,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = primaryColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Resumen del Sistema",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ResolutionRateIndicator(
                totalReports = totalReports,
                resolvedReports = resolvedReports,
                primaryColor = primaryColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            SystemSummaryText(
                totalUsers = totalUsers,
                totalNews = totalNews,
                totalReports = totalReports
            )
        }
    }
}

@Composable
private fun ResolutionRateIndicator(
    totalReports: Int,
    resolvedReports: Int,
    primaryColor: Color
) {
    val resolutionRate = if (totalReports > 0)
        (resolvedReports * 100 / totalReports) else 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Tasa de Resolución:",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )

        Surface(
            color = primaryColor,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "$resolutionRate%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SystemSummaryText(
    totalUsers: Int,
    totalNews: Int,
    totalReports: Int
) {
    Text(
        text = "El sistema está funcionando correctamente con $totalUsers usuarios registrados, " +
                "$totalNews noticias publicadas y $totalReports reportes en el sistema.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}