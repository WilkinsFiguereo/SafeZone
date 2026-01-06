package com.wilkins.safezone.frontend.ui.Moderator.screens.Dasbhoard.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class StatItem(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val trend: String,
    val trendUp: Boolean
)

@Composable
fun DashboardStatsCard(
    stats: List<StatItem>,
    modifier: Modifier = Modifier,
    primaryColor: Color
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Estadísticas del Sistema",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            stats.chunked(2).forEach { rowStats ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowStats.forEach { stat ->
                        StatItemCard(
                            stat = stat,
                            modifier = Modifier.weight(1f),
                            primaryColor = primaryColor
                        )
                    }
                    if (rowStats.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun StatItemCard(
    stat: StatItem,
    modifier: Modifier = Modifier,
    primaryColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = primaryColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = stat.icon,
                contentDescription = stat.title,
                tint = primaryColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stat.value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stat.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = if (stat.trendUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (stat.trendUp) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stat.trend,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (stat.trendUp) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}