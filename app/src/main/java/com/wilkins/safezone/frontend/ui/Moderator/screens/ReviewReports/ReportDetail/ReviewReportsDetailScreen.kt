package com.wilkins.safezone.frontend.ui.Moderator.screens.ReviewReports.ReportDetail

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
import com.wilkins.safezone.frontend.ui.Moderator.ModeratorSideMenu
import com.wilkins.safezone.navigation.theme.PrimaryColor
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportStatusScreen(
    navController: NavController,
    reportId: String,
    moderatorId: String,
    moderatorName: String = "Moderador",
    supabaseClient: SupabaseClient
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
    val context = LocalContext.current

    // Cargar datos del reporte
    LaunchedEffect(reportId) {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null

                val reportResult = repository.getReportById(reportId)
                val report = reportResult.getOrNull()

                if (report != null) {
                    reportData = report

                    if (report.imageUrl != null) {
                        Log.d("ReportStatusScreen", "━━━━━━━━━━━━━━━━━━━━━━━")
                        Log.d("ReportStatusScreen", "📎 imageUrl: ${report.imageUrl}")
                        val url = report.imageUrl.lowercase()
                        isVideo = url.endsWith(".mp4") ||
                                url.endsWith(".mov") ||
                                url.endsWith(".avi") ||
                                url.endsWith(".mkv") ||
                                url.endsWith(".webm") ||
                                url.endsWith(".3gp") ||
                                url.endsWith(".flv")
                        Log.d("ReportStatusScreen", "🎬 ¿Es video?: $isVideo")
                        Log.d("ReportStatusScreen", "📏 URL length: ${report.imageUrl.length}")
                        Log.d("ReportStatusScreen", "━━━━━━━━━━━━━━━━━━━━━━━")
                    } else {
                        Log.w("ReportStatusScreen", "⚠️ No hay imageUrl")
                    }

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
                    reportData = reportData?.copy(idReportingStatus = newStatusId)
                    showSuccessMessage = true
                    showStatusDialog = false
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
            4 -> "Cancelado"
            else -> "Desconocido"
        }
    }

    // Función para obtener el color del estado
    fun getStatusColor(statusId: Int): Color {
        return when (statusId) {
            1 -> Color(0xFFFFA726)
            4 -> Color(0xFFE53935)
            else -> Color.Gray
        }
    }

    // ============ CONTENIDO PRINCIPAL (IGUAL QUE DASHBOARD) ============
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Detalle del Reporte",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "SafeZone",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryColor,
                        titleContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = { isMenuOpen = !isMenuOpen }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            // CONTENIDO DE LA PANTALLA
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryColor)
                        }
                    }

                    errorMessage != null -> {
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
                        val report = reportData!!
                        val currentStatusId = report.idReportingStatus

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF5F5F5))
                                .verticalScroll(rememberScrollState())
                        ) {
                            // ============ IMAGEN O VIDEO ============
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
                                            Log.d("ReportStatusScreen", "🎥 Mostrando VideoPlayer")
                                            VideoPlayer(
                                                videoUrl = report.imageUrl,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        report.imageUrl != null -> {
                                            Log.d("ReportStatusScreen", "🖼️ Cargando imagen")
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(report.imageUrl)
                                                    .crossfade(true)
                                                    .placeholder(R.drawable.ic_menu_gallery)
                                                    .error(R.drawable.ic_menu_report_image)
                                                    .build(),
                                                contentDescription = "Imagen del reporte",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        else -> {
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

                                    // Badge de estado
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
                                                imageVector = if (currentStatusId == 1) Icons.Default.Schedule else Icons.Default.Cancel,
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

                                    // Badge de tipo de media
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
                                                    imageVector = if (isVideo) Icons.Default.VideoLibrary else Icons.Default.Photo,
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

                            // ============ DETALLES DEL REPORTE ============
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

                                // Descripción
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
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
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

                                // ============ BOTONES DE ACCIÓN ============
                                Text(
                                    text = "CAMBIAR ESTADO",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Botón Pendiente
                                    Button(
                                        onClick = {
                                            selectedNewStatus = 1
                                            showStatusDialog = true
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(70.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFFA726),
                                            disabledContainerColor = Color(0xFFFFCC80)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = !isUpdating && currentStatusId != 1
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Pendiente",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (currentStatusId == 1) {
                                                Text(
                                                    text = "(Actual)",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                        }
                                    }

                                    // Botón Cancelado
                                    Button(
                                        onClick = {
                                            selectedNewStatus = 4
                                            showStatusDialog = true
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(70.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFE53935),
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
                                                fontSize = 14.sp,
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

                // ============ SNACKBAR DE ÉXITO ============
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
        }

        // ============ MENÚ LATERAL (IGUAL QUE DASHBOARD) ============
        if (isMenuOpen) {
            ModeratorSideMenu(
                navController = navController,
                moderatorId = moderatorId,
                moderatorName = moderatorName,
                currentRoute = "report_review_detail",
                isMenuOpen = isMenuOpen,
                onMenuToggle = { isOpen ->
                    isMenuOpen = isOpen
                },
                context = context,
                supabaseClient = supabaseClient
            )
        }
    }

    // ============ DIÁLOGO DE CONFIRMACIÓN ============
    if (showStatusDialog && selectedNewStatus != null) {
        val statusName = getStatusName(selectedNewStatus!!)
        val statusColor = getStatusColor(selectedNewStatus!!)

        AlertDialog(
            onDismissRequest = {
                if (!isUpdating) {
                    showStatusDialog = false
                }
            },
            icon = {
                Icon(
                    imageVector = if (selectedNewStatus == 1) Icons.Default.Schedule else Icons.Default.Cancel,
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

// ============ COMPONENTE DE VIDEO ============
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                val mediaController = MediaController(ctx)
                mediaController.setAnchorView(this)
                setMediaController(mediaController)

                setVideoURI(Uri.parse(videoUrl))

                setOnPreparedListener { mp ->
                    Log.d("VideoPlayer", "✅ Video preparado")
                    mp.isLooping = false
                    mp.setVolume(1f, 1f)
                    start()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("VideoPlayer", "❌ Error: What=$what, Extra=$extra")
                    true
                }

                requestFocus()
            }
        },
        update = { videoView ->
            videoView.setVideoURI(Uri.parse(videoUrl))
        },
        modifier = modifier
    )
}