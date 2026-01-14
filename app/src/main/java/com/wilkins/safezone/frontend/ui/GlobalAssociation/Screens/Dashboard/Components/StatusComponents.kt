package com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.Dashboard.Components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.safezone.navigation.theme.PrimaryColor

// Badge de estado con colores según el tipo
@Composable
fun StatusBadge(
    statusId: Int,
    statusName: String
) {
    val (backgroundColor, textColor) = getStatusColors(statusId)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = statusName,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

// Badge de estado grande (para pantallas de detalle)
@Composable
fun StatusBadgeLarge(
    statusId: Int,
    statusName: String
) {
    val (backgroundColor, textColor) = getStatusColors(statusId)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getStatusIcon(statusId),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

// Diálogo para cambiar el estado de un reporte
@Composable
fun StatusChangeDialog(
    currentStatusId: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedStatusId by remember { mutableStateOf(currentStatusId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cambiar Estado del Reporte",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Seleccione el nuevo estado:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                StatusOption(
                    statusId = 1,
                    statusName = "Pendiente",
                    statusDescription = "El reporte está esperando revisión",
                    isSelected = selectedStatusId == 1,
                    onClick = { selectedStatusId = 1 }
                )
                Spacer(modifier = Modifier.height(8.dp))

                StatusOption(
                    statusId = 2,
                    statusName = "En Proceso",
                    statusDescription = "El reporte está siendo atendido",
                    isSelected = selectedStatusId == 2,
                    onClick = { selectedStatusId = 2 }
                )
                Spacer(modifier = Modifier.height(8.dp))

                StatusOption(
                    statusId = 3,
                    statusName = "Finalizado",
                    statusDescription = "El reporte ha sido resuelto",
                    isSelected = selectedStatusId == 3,
                    onClick = { selectedStatusId = 3 }
                )
                Spacer(modifier = Modifier.height(8.dp))

                StatusOption(
                    statusId = 4,
                    statusName = "Cancelado",
                    statusDescription = "El reporte fue cancelado",
                    isSelected = selectedStatusId == 4,
                    onClick = { selectedStatusId = 4 }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedStatusId) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                enabled = selectedStatusId != currentStatusId
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}

// Opción de estado individual en el diálogo
@Composable
fun StatusOption(
    statusId: Int,
    statusName: String,
    statusDescription: String = "",
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) PrimaryColor.copy(alpha = 0.1f) else Color.Transparent,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) PrimaryColor else Color.LightGray
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusName,
                    fontSize = 14.sp,
                    color = if (isSelected) PrimaryColor else Color.DarkGray,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
                if (statusDescription.isNotBlank()) {
                    Text(
                        text = statusDescription,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Icon(
                imageVector = getStatusIcon(statusId),
                contentDescription = null,
                tint = if (isSelected) PrimaryColor else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Función auxiliar para obtener colores según el estado
private fun getStatusColors(statusId: Int): Pair<Color, Color> {
    return when (statusId) {
        1 -> Pair(Color(0xFFFFF3CD), Color(0xFF856404)) // Pendiente - Amarillo
        2 -> Pair(Color(0xFFCCE5FF), Color(0xFF004085)) // En Proceso - Azul
        3 -> Pair(Color(0xFFD4EDDA), Color(0xFF155724)) // Finalizado - Verde
        4 -> Pair(Color(0xFFF8D7DA), Color(0xFF721C24)) // Cancelado - Rojo
        else -> Pair(Color.LightGray, Color.DarkGray)
    }
}

// Función auxiliar para obtener icono según el estado
private fun getStatusIcon(statusId: Int) = when (statusId) {
    1 -> Icons.Default.Pending
    2 -> Icons.Default.Update
    3 -> Icons.Default.CheckCircle
    4 -> Icons.Default.Cancel
    else -> Icons.Default.Help
}

// Filtro de estados para listas
@Composable
fun StatusFilterChips(
    selectedStatusIds: Set<Int>,
    onStatusToggle: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusFilterChip(
            statusId = 1,
            statusName = "Pendientes",
            isSelected = selectedStatusIds.contains(1),
            onClick = { onStatusToggle(1) }
        )
        StatusFilterChip(
            statusId = 2,
            statusName = "En Proceso",
            isSelected = selectedStatusIds.contains(2),
            onClick = { onStatusToggle(2) }
        )
        StatusFilterChip(
            statusId = 3,
            statusName = "Finalizados",
            isSelected = selectedStatusIds.contains(3),
            onClick = { onStatusToggle(3) }
        )
        StatusFilterChip(
            statusId = 4,
            statusName = "Cancelados",
            isSelected = selectedStatusIds.contains(4),
            onClick = { onStatusToggle(4) }
        )
    }
}

// Chip individual de filtro
@Composable
fun StatusFilterChip(
    statusId: Int,
    statusName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (backgroundColor, textColor) = if (isSelected) {
        getStatusColors(statusId)
    } else {
        Pair(Color.LightGray.copy(alpha = 0.3f), Color.DarkGray)
    }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(statusName, fontSize = 12.sp) },
        leadingIcon = {
            Icon(
                imageVector = getStatusIcon(statusId),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = backgroundColor,
            selectedLabelColor = textColor
        )
    )
}