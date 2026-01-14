package com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.ReportSent.ReportDetail

import android.R
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wilkins.safezone.backend.network.GlobalAssociation.DateUtils
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportDto
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportUtils
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportsRepository
import com.wilkins.safezone.frontend.ui.GlobalAssociation.GovernmentMenu
import com.wilkins.safezone.navigation.theme.PrimaryColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReportDetailScreenAssociation(
    navController: NavController,
    reportId: String
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var selectedNewStatus by remember { mutableStateOf<Int?>(null) }

    // Estados para los datos
    var reportData by remember { mutableStateOf<ReportDto?>(null) }
    var affairName by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isUpdating by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var isVideo by remember { mutableStateOf(false) }

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

                    // Detectar si es video por la extensión del archivo
                    if (report.imageUrl != null) {
                        Log.d("ReportDetailScreen", "━━━━━━━━━━━━━━━━━━━━━━━")
                        Log.d("ReportDetailScreen", "📎 imageUrl: ${report.imageUrl}")
                        val url = report.imageUrl.lowercase()
                        isVideo = url.endsWith(".mp4") ||
                                url.endsWith(".mov") ||
                                url.endsWith(".avi") ||
                                url.endsWith(".mkv") ||
                                url.endsWith(".webm") ||
                                url.endsWith(".3gp") ||
                                url.endsWith(".flv")
                        Log.d("ReportDetailScreen", "🎬 ¿Es video?: $isVideo")
                        Log.d("ReportDetailScreen", "📏 URL length: ${report.imageUrl.length}")
                        Log.d("ReportDetailScreen", "━━━━━━━━━━━━━━━━━━━━━━━")
                    } else {
                        Log.w("ReportDetailScreen", "⚠️ No hay imageUrl")
                    }

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
    fun updateReportStatus(newStatusId: Int) {
        scope.launch {
            try {
                isUpdating = true

                val result = repository.updateReportStatus(reportId, newStatusId)

                if (result.isSuccess) {
                    // Actualizar el estado local
                    reportData = reportData?.copy(idReportingStatus = newStatusId)
                    showSuccessMessage = true
                    showStatusDialog = false

                    // Ocultar mensaje después de 3 segundos
                    delay(3000)
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

    // Función para obtener el nombre del estado
    fun getStatusName(statusId: Int): String {
        return when (statusId) {
            1 -> "Pendiente"
            2 -> "En Proceso"
            3 -> "Finalizado"
            4 -> "Cancelado"
            else -> "Desconocido"
        }
    }

    // Función para obtener el color del estado
    fun getStatusColor(statusId: Int): Color {
        return when (statusId) {
            1 -> Color(0xFFFFC107) // Amarillo para pendiente
            2 -> Color(0xFF2196F3) // Azul para en proceso
            3 -> Color(0xFF4CAF50) // Verde para finalizado
            4 -> Color(0xFFF44336) // Rojo para cancelado
            else -> Color.Gray
        }
    }

    // Función para obtener el ícono del estado
    fun getStatusIcon(statusId: Int) = when (statusId) {
        1 -> Icons.Default.Pending
        2 -> Icons.Default.Update
        3 -> Icons.Default.CheckCircle
        4 -> Icons.Default.Cancel
        else -> Icons.Default.Help
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
                    val currentStatusId = report.idReportingStatus

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F5F5))
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Imagen o Video del reporte
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Box {
                                when {
                                    report.imageUrl != null && isVideo -> {
                                        // Mostrar video
                                        Log.d("ReportDetailScreen", "🎥 Mostrando VideoPlayer")
                                        VideoPlayer(
                                            videoUrl = report.imageUrl,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    report.imageUrl != null -> {
                                        // Mostrar imagen
                                        Log.d("ReportDetailScreen", "🖼️ Cargando imagen con AsyncImage")
                                        Log.d("ReportDetailScreen", "📎 URL: ${report.imageUrl}")

                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(report.imageUrl)
                                                .crossfade(true)
                                                .placeholder(R.drawable.ic_menu_gallery)
                                                .error(R.drawable.ic_menu_report_image)
                                                .listener(
                                                    onStart = {
                                                        Log.d("ReportDetailScreen", "▶️ Carga de imagen iniciada")
                                                    },
                                                    onSuccess = { _, result ->
                                                        Log.d("ReportDetailScreen", "✅ Imagen cargada exitosamente")
                                                    },
                                                    onError = { _, result ->
                                                        Log.e("ReportDetailScreen", "❌ Error cargando imagen: ${result.throwable.message}")
                                                    }
                                                )
                                                .build(),
                                            contentDescription = "Imagen del reporte",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    else -> {
                                        // Sin multimedia
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
                                }

                                // Badge de estado actual
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = getStatusColor(currentStatusId)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = getStatusIcon(currentStatusId),
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = getStatusName(currentStatusId),
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Badge indicador de tipo de media
                                if (report.imageUrl != null) {
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(16.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.Black.copy(alpha = 0.6f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isVideo) Icons.Default.VideoLibrary
                                                else Icons.Default.Photo,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isVideo) "Video" else "Imagen",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
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

                            // Botones de Cambio de Estado
                            // Reemplaza la sección de botones (aproximadamente línea 565-630)
// desde "// Botones de Cambio de Estado" hasta el cierre de Row

// Botones de Cambio de Estado
                            Text(
                                text = "CAMBIAR ESTADO",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

// Primera fila de botones
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Botón En Revisión (Estado 2)
                                Button(
                                    onClick = {
                                        selectedNewStatus = 2
                                        showStatusDialog = true
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(70.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2196F3),
                                        disabledContainerColor = Color(0xFF90CAF9)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isUpdating && currentStatusId != 2
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Update,
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "En proceso",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (currentStatusId == 2) {
                                            Text(
                                                text = "(Actual)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }
                                }

                                // Botón Finalizado (Estado 3)
                                Button(
                                    onClick = {
                                        selectedNewStatus = 3
                                        showStatusDialog = true
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(70.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50),
                                        disabledContainerColor = Color(0xFFA5D6A7)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isUpdating && currentStatusId != 3
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Finalizado",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (currentStatusId == 3) {
                                            Text(
                                                text = "(Actual)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

// Segunda fila - Botón Cancelar centrado
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Botón Cancelado (Estado 4)
                                Button(
                                    onClick = {
                                        selectedNewStatus = 4
                                        showStatusDialog = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(70.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFF44336),
                                        disabledContainerColor = Color(0xFFEF9A9A)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isUpdating && currentStatusId != 4
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Cancelado",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (currentStatusId == 4) {
                                            Text(
                                                text = "(Actual)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Normal
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
        if (showStatusDialog && selectedNewStatus != null) {
            val statusName = getStatusName(selectedNewStatus!!)
            val statusColor = getStatusColor(selectedNewStatus!!)
            val statusIcon = getStatusIcon(selectedNewStatus!!)

            AlertDialog(
                onDismissRequest = {
                    if (!isUpdating) {
                        showStatusDialog = false
                    }
                },
                icon = {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
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
                        text = "¿Está seguro que desea marcar este reporte como '$statusName'?",
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            updateReportStatus(selectedNewStatus!!)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = statusColor
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

@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Log.d("VideoPlayer", "🎬 Inicializando VideoPlayer")
    Log.d("VideoPlayer", "📎 URL: $videoUrl")

    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                Log.d("VideoPlayer", "🎥 Configurando VideoView")

                // Configurar controles de media
                val mediaController = MediaController(ctx)
                mediaController.setAnchorView(this)
                setMediaController(mediaController)

                // Establecer URI del video
                setVideoURI(Uri.parse(videoUrl))

                // Preparar y auto-reproducir
                setOnPreparedListener { mp ->
                    Log.d("VideoPlayer", "✅ Video preparado y listo")
                    mp.isLooping = false
                    mp.setVolume(1f, 1f)
                    start() // Auto-iniciar
                }

                // Manejar errores
                setOnErrorListener { _, what, extra ->
                    Log.e("VideoPlayer", "❌ Error reproduciendo video")
                    Log.e("VideoPlayer", "❌ What: $what, Extra: $extra")
                    Log.e("VideoPlayer", "❌ URL: $videoUrl")
                    true
                }

                setOnCompletionListener {
                    Log.d("VideoPlayer", "🏁 Video completado")
                }

                // Iniciar preparación
                requestFocus()
            }
        },
        update = { videoView ->
            Log.d("VideoPlayer", "🔄 Update VideoView")
            videoView.setVideoURI(Uri.parse(videoUrl))
        },
        modifier = modifier
    )
}