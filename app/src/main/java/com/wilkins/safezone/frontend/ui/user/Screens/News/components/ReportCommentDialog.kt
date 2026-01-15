package com.wilkins.safezone.frontend.ui.user.Screens.News.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.wilkins.safezone.backend.network.User.ReportSystem.ReportType

@Composable
fun ReportCommentDialog(
    commentAuthor: String,
    reportTypes: List<ReportType>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (reportTypeId: Int, message: String) -> Unit
) {
    var selectedTypeId by remember { mutableStateOf<Int?>(null) }
    var reportMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Flag,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Reportar comentario",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Comentario de $commentAuthor",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                // Tipos de reporte
                Text(
                    text = "Motivo del reporte",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reportTypes) { type ->
                        ReportTypeItem(
                            reportType = type,
                            isSelected = selectedTypeId == type.id,
                            onClick = { selectedTypeId = type.id }
                        )
                    }
                }

                // Campo de mensaje
                OutlinedTextField(
                    value = reportMessage,
                    onValueChange = { reportMessage = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descripción (opcional)") },
                    placeholder = { Text("Proporciona más detalles sobre el reporte...") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            selectedTypeId?.let { typeId ->
                                onSubmit(typeId, reportMessage)
                            }
                        },
                        enabled = selectedTypeId != null && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Enviar reporte")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportTypeItem(
    reportType: ReportType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) {
            Color(0xFFE53935).copy(alpha = 0.1f)
        } else {
            Color.Gray.copy(alpha = 0.05f)
        },
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE53935))
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reportType.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFFE53935) else Color.Black
                )
                if (!reportType.description.isNullOrBlank()) {
                    Text(
                        text = reportType.description ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}