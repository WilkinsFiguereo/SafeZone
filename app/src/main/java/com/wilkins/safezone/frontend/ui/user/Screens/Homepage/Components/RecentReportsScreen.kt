package com.wilkins.safezone.frontend.ui.user.Screens.Homepage.Components


import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.safezone.ui.theme.PrimaryColor

data class Report(
    val id: Int,
    val type: ReportType,
    val description: String,
    val timeAgo: String,
    val distance: String
)

enum class ReportType(val label: String, val icon: ImageVector) {
    EMERGENCY("Emergencia", Icons.Default.Warning),
    THEFT("Robo", Icons.Default.ShoppingCart),
    ACCIDENT("Accidente", Icons.Default.LocalHospital),
    SUSPICIOUS("Sospechoso", Icons.Default.Visibility)
}

@Composable
fun RecentReportsSection(
    reports: List<Report> = getSampleReports(),
    onSeeAllClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header with title and See All button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reportes Recientes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1F1F)
            )

            TextButton(
                onClick = onSeeAllClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Ver más",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryColor
                )
            }
        }

        // Reports List
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            reports.take(3).forEach { report ->
                SimpleReportCard(report = report)
            }
        }
    }
}

@Composable
fun SimpleReportCard(report: Report) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon Circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = report.type.icon,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = report.type.label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F1F1F)
                    )
                    Text(
                        text = report.timeAgo,
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }

                Text(
                    text = report.description,
                    fontSize = 13.sp,
                    color = Color(0xFF616161),
                    lineHeight = 18.sp,
                    maxLines = 2
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = report.distance,
                        fontSize = 11.sp,
                        color = PrimaryColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

fun getSampleReports(): List<Report> {
    return listOf(
        Report(
            id = 1,
            type = ReportType.EMERGENCY,
            description = "Situación de emergencia cerca de zona comercial",
            timeAgo = "5 min",
            distance = "0.8 km"
        ),
        Report(
            id = 2,
            type = ReportType.THEFT,
            description = "Intento de robo reportado en la zona",
            timeAgo = "15 min",
            distance = "1.2 km"
        ),
        Report(
            id = 3,
            type = ReportType.ACCIDENT,
            description = "Accidente vehicular menor",
            timeAgo = "25 min",
            distance = "2.1 km"
        ),
        Report(
            id = 4,
            type = ReportType.SUSPICIOUS,
            description = "Actividad sospechosa reportada",
            timeAgo = "1 hora",
            distance = "0.5 km"
        )
    )
}

@Preview(showBackground = true)
@Composable
fun RecentReportsSectionPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5)
    ) {
        RecentReportsSection()
    }
}