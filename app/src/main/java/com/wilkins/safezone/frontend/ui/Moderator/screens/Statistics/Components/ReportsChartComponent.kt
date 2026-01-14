package com.wilkins.safezone.frontend.ui.Moderator.screens.Statistics.Components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ReportsChartCard(
    stats: List<ReportStatItem>,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Distribución de Reportes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                DonutChart(stats = stats)
            }

            Spacer(modifier = Modifier.height(16.dp))

            ChartLegend(stats = stats)
        }
    }
}

@Composable
private fun DonutChart(stats: List<ReportStatItem>) {
    val total = stats.sumOf { it.value }.toFloat()

    Canvas(modifier = Modifier.size(180.dp)) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2 * 0.8f
        val strokeWidth = 40f

        var startAngle = -90f

        stats.forEach { stat ->
            val sweepAngle = if (total > 0) (stat.value / total) * 360f else 0f

            drawArc(
                color = stat.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    (canvasSize - radius * 2) / 2,
                    (canvasSize - radius * 2) / 2
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
private fun ChartLegend(stats: List<ReportStatItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        stats.forEach { stat ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(16.dp),
                    color = stat.color,
                    shape = RoundedCornerShape(4.dp)
                ) {}
                Text(
                    text = "${stat.label}: ${stat.value}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}