package com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Statics.Components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.safezone.backend.network.GlobalAssociation.MonthlyStatistic
import com.wilkins.safezone.backend.network.GlobalAssociation.StatusStatistic

// Gráfico de barras para datos mensuales
@Composable
fun MonthlyBarChart(
    data: List<MonthlyStatistic>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF2196F3)
) {
    if (data.isEmpty()) {
        EmptyChartMessage("No hay datos mensuales disponibles")
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Reportes por Mes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            val maxValue = data.maxOfOrNull { it.count } ?: 1

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { stat ->
                    BarChartItem(
                        label = "${stat.month}\n${stat.year}",
                        value = stat.count,
                        maxValue = maxValue,
                        color = barColor
                    )
                }
            }
        }
    }
}

@Composable
private fun BarChartItem(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        // Valor encima de la barra
        Text(
            text = "$value",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Barra
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(180.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            val heightFraction = if (maxValue > 0) value.toFloat() / maxValue else 0f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(heightFraction)
                    .background(color, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Etiqueta
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.Gray,
            lineHeight = 12.sp
        )
    }
}

// Gráfico de dona para distribución de estados
@Composable
fun StatusPieChart(
    data: List<StatusStatistic>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        EmptyChartMessage("No hay datos de estados disponibles")
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Distribución por Estado",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gráfico de dona
                DonutChart(
                    data = data,
                    modifier = Modifier.size(180.dp)
                )

                // Leyenda
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    data.forEach { stat ->
                        LegendItem(
                            color = getStatusColor(stat.statusId),
                            label = stat.statusName,
                            value = "${stat.count} (${String.format("%.1f", stat.percentage)}%)"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    data: List<StatusStatistic>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val total = data.sumOf { it.count }.toFloat()
        var startAngle = -90f

        data.forEach { stat ->
            val sweepAngle = (stat.count / total) * 360f
            val color = getStatusColor(stat.statusId)

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(size.width * 0.1f, size.height * 0.1f),
                size = Size(size.width * 0.8f, size.height * 0.8f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 40f)
            )

            startAngle += sweepAngle
        }

        // Círculo central blanco
        drawCircle(
            color = Color.White,
            radius = size.width * 0.25f,
            center = Offset(size.width / 2, size.height / 2)
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, RoundedCornerShape(4.dp))
        )

        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = value,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

// Lista de ranking (ubicaciones, tipos de reporte)
@Composable
fun RankingList(
    title: String,
    items: List<Pair<String, Int>>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (items.isEmpty()) {
                Text(
                    text = "No hay datos disponibles",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else {
                items.forEachIndexed { index, (label, count) ->
                    RankingItem(
                        position = index + 1,
                        label = label,
                        count = count,
                        color = color,
                        maxCount = items.firstOrNull()?.second ?: 1
                    )

                    if (index < items.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingItem(
    position: Int,
    label: String,
    count: Int,
    color: Color,
    maxCount: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Número de posición
                Surface(
                    shape = CircleShape,
                    color = if (position <= 3) color else Color.LightGray.copy(alpha = 0.3f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$position",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (position <= 3) Color.White else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Etiqueta
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
            }

            // Contador
            Text(
                text = "$count",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Barra de progreso
        val progress = if (maxCount > 0) count.toFloat() / maxCount else 0f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun EmptyChartMessage(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

// Función auxiliar para obtener colores de estados
private fun getStatusColor(statusId: Int): Color {
    return when (statusId) {
        1 -> Color(0xFFFFC107) // Pendiente - Amarillo
        2 -> Color(0xFF2196F3) // En Proceso - Azul
        3 -> Color(0xFF4CAF50) // Finalizado - Verde
        4 -> Color(0xFFF44336) // Cancelado - Rojo
        else -> Color.Gray
    }
}