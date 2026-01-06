package com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Statics.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.safezone.backend.network.GlobalAssociation.MonthlyStatistic
import com.wilkins.safezone.ui.theme.PrimaryColor

// Tabla detallada de estadísticas mensuales
@Composable
fun MonthlyDetailTable(
    data: List<MonthlyStatistic>,
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
                text = "Detalle por Mes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Text(
                    text = "No hay datos mensuales disponibles",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else {
                // Header de la tabla
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Periodo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        modifier = Modifier.weight(1.5f)
                    )
                    Text(
                        text = "Reportes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "% del Total",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val totalReports = data.sumOf { it.count }

                // Filas de datos
                data.forEach { stat ->
                    MonthlyTableRow(
                        month = "${stat.month} ${stat.year}",
                        count = stat.count,
                        percentage = (stat.count.toFloat() / totalReports) * 100
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                HorizontalDivider(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Fila de totales
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TOTAL",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1.5f)
                    )
                    Text(
                        text = totalReports.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "100%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyTableRow(
    month: String,
    count: Int,
    percentage: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = month,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.weight(1.5f)
        )
        Text(
            text = count.toString(),
            fontSize = 14.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = String.format("%.1f%%", percentage),
            fontSize = 14.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

// Tabla de comparación de ubicaciones
@Composable
fun LocationComparisonTable(
    data: List<Pair<String, Int>>,
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
                text = "Comparación de Ubicaciones",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Text(
                    text = "No hay datos de ubicaciones disponibles",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else {
                val totalReports = data.sumOf { it.second }
                val maxCount = data.maxOfOrNull { it.second } ?: 1

                data.forEach { (location, count) ->
                    LocationComparisonRow(
                        location = location,
                        count = count,
                        total = totalReports,
                        maxCount = maxCount
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun LocationComparisonRow(
    location: String,
    count: Int,
    total: Int,
    maxCount: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = location,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "$count (${String.format("%.1f%%", (count.toFloat() / total) * 100)})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Barra de progreso
        val progress = count.toFloat() / maxCount

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(Color(0xFFFF9800), RoundedCornerShape(4.dp))
            )
        }
    }
}

// Tabla de resumen de estados
@Composable
fun StatusSummaryTable(
    pendingCount: Int,
    inProgressCount: Int,
    completedCount: Int,
    cancelledCount: Int,
    totalCount: Int,
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
                text = "Resumen de Estados",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            StatusSummaryRow(
                label = "Pendientes",
                count = pendingCount,
                total = totalCount,
                color = Color(0xFFFFC107)
            )
            Spacer(modifier = Modifier.height(12.dp))

            StatusSummaryRow(
                label = "En Proceso",
                count = inProgressCount,
                total = totalCount,
                color = Color(0xFF2196F3)
            )
            Spacer(modifier = Modifier.height(12.dp))

            StatusSummaryRow(
                label = "Finalizados",
                count = completedCount,
                total = totalCount,
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(12.dp))

            StatusSummaryRow(
                label = "Cancelados",
                count = cancelledCount,
                total = totalCount,
                color = Color(0xFFF44336)
            )
        }
    }
}

@Composable
private fun StatusSummaryRow(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Black
            )
        }

        Text(
            text = "$count",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = String.format("%.1f%%", (count.toFloat() / total) * 100),
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}