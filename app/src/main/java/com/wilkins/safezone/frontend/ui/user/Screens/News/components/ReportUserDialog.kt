package com.wilkins.safezone.frontend.ui.user.Screens.News.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import com.wilkins.safezone.backend.network.User.ReportSystem.ReportType
import com.wilkins.safezone.navigation.theme.PrimaryColor

@Composable
fun ReportUserDialog(
    userName: String,
    reportTypes: List<ReportType>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (reportTypeId: Int, message: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedReportType by remember { mutableStateOf<ReportType?>(null) }
    var message by remember { mutableStateOf("") }
    var showTypeSelector by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Reportar usuario",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = userName,
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF757575)
                        )
                    }
                }

                Divider(color = Color(0xFFE0E0E0))

                // Selector de tipo de reporte
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tipo de reporte *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121)
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTypeSelector = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF5F5F5),
                        border = if (selectedReportType == null) {
                            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                        } else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedReportType?.name ?: "Selecciona un tipo",
                                fontSize = 14.sp,
                                color = if (selectedReportType == null)
                                    Color(0xFF9E9E9E)
                                else
                                    Color(0xFF212121)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF757575)
                            )
                        }
                    }

                    if (selectedReportType?.description != null) {
                        Text(
                            text = selectedReportType!!.description!!,
                            fontSize = 12.sp,
                            color = Color(0xFF757575),
                            lineHeight = 16.sp
                        )
                    }
                }

                // Mensaje
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Describe el problema *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121)
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = {
                            Text(
                                text = "Proporciona detalles sobre el reporte...",
                                fontSize = 14.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = PrimaryColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 5
                    )

                    Text(
                        text = "${message.length}/500 caracteres",
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E),
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF757575)
                        )
                    ) {
                        Text("Cancelar", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            if (selectedReportType != null && message.isNotBlank()) {
                                onSubmit(selectedReportType!!.id, message)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedReportType != null && message.isNotBlank() && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Enviar reporte", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    // Selector de tipo de reporte
    if (showTypeSelector) {
        ReportTypeSelector(
            reportTypes = reportTypes,
            onSelect = { type ->
                selectedReportType = type
                showTypeSelector = false
            },
            onDismiss = { showTypeSelector = false }
        )
    }
}

@Composable
private fun ReportTypeSelector(
    reportTypes: List<ReportType>,
    onSelect: (ReportType) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selecciona el tipo de reporte",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF757575)
                        )
                    }
                }

                Divider(color = Color(0xFFE0E0E0))

                // Lista de tipos
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(reportTypes) { type ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(type) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                            ) {
                                Text(
                                    text = type.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF212121)
                                )
                                if (type.description != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = type.description,
                                        fontSize = 12.sp,
                                        color = Color(0xFF757575),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                        Divider(color = Color(0xFFF5F5F5))
                    }
                }
            }
        }
    }
}

@Composable
fun BanUserDialog(
    userName: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var reason by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header con icono de advertencia
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier
                                .padding(16.dp)
                                .size(32.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Banear usuario",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = userName,
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }

                Divider(color = Color(0xFFE0E0E0))

                // Razón del baneo
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Razón del baneo *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121)
                    )

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = {
                            Text(
                                text = "Explica por qué estás baneando a este usuario...",
                                fontSize = 14.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFFD32F2F)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )
                }

                // Advertencia
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3E0)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFFF6F00),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Esta acción impedirá que el usuario acceda a la plataforma.",
                            fontSize = 12.sp,
                            color = Color(0xFFE65100),
                            lineHeight = 16.sp
                        )
                    }
                }

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF757575)
                        )
                    ) {
                        Text("Cancelar", fontSize = 14.sp)
                    }

                    Button(
                        onClick = { onConfirm(reason) },
                        modifier = Modifier.weight(1f),
                        enabled = reason.isNotBlank() && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Banear", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}