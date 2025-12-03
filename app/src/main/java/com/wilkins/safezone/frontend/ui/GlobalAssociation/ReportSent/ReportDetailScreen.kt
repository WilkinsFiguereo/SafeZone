package com.wilkins.safezone.frontend.ui.GlobalAssociation.ReportSent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.wilkins.safezone.backend.network.GlobalAssociation.DateUtils
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportDto
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportUtils
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportsRepository
import com.wilkins.safezone.frontend.ui.GlobalAssociation.GovernmentMenu
import com.wilkins.safezone.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@Composable
fun ReportDetailScreen(
    navController: NavController,
    reportId: String
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf<ReportStatus?>(null) }

    // Estados para los datos
    var reportData by remember { mutableStateOf<ReportDto?>(null) }
    var affairName by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isUpdating by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val repository = remember { ReportsRepository() }

    // Cargar datos del reporte
    LaunchedEffect(reportId) {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null

                // Cargar el reporte
                val reportResult = repository.getReportById(reportId)
                val report = reportResult.getOrNull()

                if (report != null) {
                    reportData = report

                    // Cargar el affair si existe
                    if (report.idAffair != null) {
                        val affairResult = repository.getAffairById(report.idAffair)
                        affairName = affairResult.getOrNull()?.affairName
                    }
                }

                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Error al cargar el reporte: ${e.message}"
                isLoading = false
            }
        }
    }

    // Función para actualizar el estado
    fun updateReportStatus(newStatus: ReportStatus) {
        scope.launch {
            try {
                isUpdating = true

                val result = repository.updateReportStatus(reportId, newStatus.id)

                if (result.isSuccess) {
                    // Actualizar el estado local
                    reportData = reportData?.copy(idReportingStatus = newStatus.id)
                    showSuccessMessage = true
                    showStatusDialog = false

                    // Ocultar mensaje después de 3 segundos
                    kotlinx.coroutines.delay(3000)
                    showSuccessMessage = false
                } else {
                    errorMessage = "Error al actualizar el estado"
                }

                isUpdating = false
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                isUpdating = false
            }
        }
    }

    GovernmentMenu(
        navController = navController,
        isMenuOpen = isMenuOpen,
        onMenuToggle = { isMenuOpen = it },
        currentRoute = "report_detail"
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    // Estado de carga
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                }

                errorMessage != null -> {
                    // Estado de error
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color(0xFFDC3545)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: "Error desconocido",
                            color = Color(0xFF666666),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Text("Volver")
                        }
                    }
                }

                reportData != null -> {
                    // Contenido principal
                    val report = reportData!!
                    val currentStatus = ReportStatus.fromId(report.idReportingStatus)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F5F5))
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Imagen del reporte
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Box {
                                if (report.imageUrl != null) {
                                    AsyncImage(
                                        model = report.imageUrl,
                                        contentDescription = "Imagen del reporte",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFFE0E0E0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ImageNotSupported,
                                            contentDescription = null,
                                            modifier = Modifier.size(80.dp),
                                            tint = Color(0xFF9E9E9E)
                                        )
                                    }
                                }

                                // Badge de estado
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = currentStatus.color
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = currentStatus.icon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = currentStatus.label,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Contenido del reporte
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // ID y Fecha
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "REPORTE #${ReportUtils.getShortId(report.id)}",
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${DateUtils.formatDate(report.createdAt)} - ${DateUtils.formatTime(report.createdAt)}",
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Tipo de Incidente
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        tint = PrimaryColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Tipo de Incidente",
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = affairName ?: "Sin categoría",
                                            color = Color.Black,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Mensaje
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Message,
                                            contentDescription = null,
                                            tint = PrimaryColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Descripción",
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = report.description ?: "Sin descripción",
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Ubicación
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = PrimaryColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Ubicación",
                                                    color = Color.Gray,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = report.reportLocation ?: "Ubicación no especificada",
                                                    color = Color.Black,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Reportado por
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = PrimaryColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Reportado por",
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = ReportUtils.getReporterName(report.isAnonymous, report.userName),
                                            color = Color.Black,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Botones de Acción
                            Text(
                                text = "ACCIONES",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Botón Marcar como En Proceso
                            if (currentStatus != ReportStatus.IN_PROGRESS) {
                                Button(
                                    onClick = {
                                        selectedStatus = ReportStatus.IN_PROGRESS
                                        showStatusDialog = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isUpdating
                                ) {
                                    if (isUpdating && selectedStatus == ReportStatus.IN_PROGRESS) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Update,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Marcar como En Proceso",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Botón Finalizar
                                if (currentStatus != ReportStatus.COMPLETED) {
                                    Button(
                                        onClick = {
                                            selectedStatus = ReportStatus.COMPLETED
                                            showStatusDialog = true
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = !isUpdating
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Finalizar",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Botón Cancelar
                                if (currentStatus != ReportStatus.CANCELLED) {
                                    Button(
                                        onClick = {
                                            selectedStatus = ReportStatus.CANCELLED
                                            showStatusDialog = true
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = !isUpdating
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.Cancel,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Cancelar",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }

            // Mensaje de éxito
            if (showSuccessMessage) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = Color(0xFF4CAF50)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Estado actualizado correctamente", color = Color.White)
                    }
                }
            }
        }

        // Diálogo de confirmación
        if (showStatusDialog && selectedStatus != null) {
            AlertDialog(
                onDismissRequest = {
                    if (!isUpdating) {
                        showStatusDialog = false
                    }
                },
                icon = {
                    Icon(
                        imageVector = selectedStatus!!.icon,
                        contentDescription = null,
                        tint = selectedStatus!!.color,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = "Cambiar Estado del Reporte",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "¿Está seguro que desea marcar este reporte como '${selectedStatus!!.label}'?",
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            updateReportStatus(selectedStatus!!)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedStatus!!.color
                        ),
                        enabled = !isUpdating
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Confirmar")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showStatusDialog = false },
                        enabled = !isUpdating
                    ) {
                        Text("Cancelar", color = Color.Gray)
                    }
                }
            )
        }
    }
}