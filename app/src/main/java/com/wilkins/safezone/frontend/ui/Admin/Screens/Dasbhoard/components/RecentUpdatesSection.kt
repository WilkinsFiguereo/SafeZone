package com.wilkins.safezone.frontend.ui.Admin.Screens.Dasbhoard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecentUpdatesSection() {
    val updates = listOf(
        UpdateItem(
            title = "Se agregó un nuevo item-Name/Item Description full text 10:30",
            timestamp = "11:15:00 • 17 Nov, 2025",
            type = UpdateType.ADDED,
            icon = Icons.Default.Add
        ),
        UpdateItem(
            title = "Se editó un nuevo item-Name/Item Description full text 10:25",
            timestamp = "11:10:00 • 17 Nov, 2025",
            type = UpdateType.EDITED,
            icon = Icons.Default.Edit
        ),
        UpdateItem(
            title = "Se agregó un nuevo item-Name/Item Description full text 10:20",
            timestamp = "11:05:00 • 17 Nov, 2025",
            type = UpdateType.ADDED,
            icon = Icons.Default.Add
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        updates.forEach { update ->
            UpdateCard(update)
        }
    }
}

@Composable
fun UpdateCard(update: UpdateItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icono de tipo de actualización
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (update.type) {
                            UpdateType.ADDED -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                            UpdateType.EDITED -> Color(0xFF2196F3).copy(alpha = 0.15f)
                            UpdateType.DELETED -> Color(0xFFF44336).copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    update.icon,
                    contentDescription = null,
                    tint = when (update.type) {
                        UpdateType.ADDED -> Color(0xFF4CAF50)
                        UpdateType.EDITED -> Color(0xFF2196F3)
                        UpdateType.DELETED -> Color(0xFFF44336)
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenido de la actualización
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = update.title,
                    fontSize = 13.sp,
                    color = Color(0xFF212121),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = update.timestamp,
                    fontSize = 11.sp,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// Data class para las actualizaciones
data class UpdateItem(
    val title: String,
    val timestamp: String,
    val type: UpdateType,
    val icon: ImageVector
)

// Enum para los tipos de actualización
enum class UpdateType {
    ADDED,    // Verde - Elemento agregado
    EDITED,   // Azul - Elemento editado
    DELETED   // Rojo - Elemento eliminado
}