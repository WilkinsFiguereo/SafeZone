package com.wilkins.safezone.frontend.ui.Map

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportsRepository
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportDto
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.User.Interaction.EntityType
import com.wilkins.safezone.backend.network.User.ReportSystem.ReportService
import com.wilkins.safezone.backend.network.User.ReportSystem.ReportType
import com.wilkins.safezone.frontend.ui.Map.Components.LikeButton
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import com.wilkins.safezone.backend.network.User.ReportComment.ReportCommentDto
import com.wilkins.safezone.backend.network.User.ReportComment.ReportCommentService
import com.wilkins.safezone.backend.network.auth.SessionManager


/**
 * Modelo para los marcadores en el mapa
 */
data class ReportMarker(
    val id: String,
    val user_id: String?,
    val position: LatLng,
    val title: String,
    val description: String,
    val distance: Float = 0f,
    val userImageUrl: String? = null, // Este será el photo_profile del usuario
    val reportImageUrl: String? = null,
    val createdAt: String? = null,
    val affairId: Int? = null,
    val affairName: String? = null,
    val userName: String? = null // Nombre real del usuario (no anónimo)
)

/**
 * Configuración del mapa
 */
data class MapConfig(
    val showUserLocation: Boolean = true,
    val showAllReports: Boolean = false,
    val maxDistanceKm: Float = 10f,
    val initialZoom: Float = 12f,
    val showDefaultMarker: Boolean = true
)

/**
 * Composable principal del mapa reutilizable
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun GoogleMapScreen(
    modifier: Modifier = Modifier,
    config: MapConfig = MapConfig(),
    onReportClick: ((ReportDto) -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    navController: NavController,
) {
    Log.i("GoogleMapScreen", "🔵 Composable iniciado")

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            Log.i("GoogleMapScreen", "📌 Resultado permiso: $granted")
            hasLocationPermission = granted
        }
    )

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var reportMarkers by remember { mutableStateOf<List<ReportMarker>>(emptyList()) }
    var allReports by remember { mutableStateOf<List<ReportDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedMarker by remember { mutableStateOf<ReportMarker?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        Log.i("GoogleMapScreen", "📍 Permiso ubicación: ${if (hasLocationPermission) "OTORGADO" else "DENEGADO"}")
        if (!hasLocationPermission && config.showUserLocation) {
            Log.i("GoogleMapScreen", "🚨 Solicitando permiso de ubicación")
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(hasLocationPermission, config) {
        scope.launch {
            try {
                var location: Location? = null
                if (config.showUserLocation && hasLocationPermission) {
                    try {
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                        location = suspendCoroutine { continuation ->
                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { loc ->
                                    continuation.resume(loc)
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("GoogleMapScreen", "❌ Error obteniendo ubicación", exception)
                                    continuation.resume(null)
                                }
                        }

                        if (location != null) {
                            userLocation = LatLng(location.latitude, location.longitude)
                            Log.i("GoogleMapScreen", "📍 Ubicación obtenida: ${location.latitude}, ${location.longitude}")
                        }
                    } catch (e: Exception) {
                        Log.e("GoogleMapScreen", "❌ Error con GPS", e)
                    }
                }

                val repository = ReportsRepository()
                val result = repository.getAllReports()
                val affairsResult = repository.getAllAffairs()
                val affairsMap = affairsResult.getOrNull()?.associateBy { it.id } ?: emptyMap()

                if (result.isSuccess) {
                    val reports = result.getOrNull() ?: emptyList()

                    val filteredReports = reports.filter { report ->
                        report.idReportingStatus == 1 || report.idReportingStatus == 2
                    }

                    allReports = filteredReports
                    Log.i("GoogleMapScreen", "📡 Reportes totales obtenidos: ${reports.size}")
                    Log.i("GoogleMapScreen", "✅ Reportes filtrados (pendientes/en proceso): ${filteredReports.size}")
                    Log.i("GoogleMapScreen", "❌ Reportes excluidos (cancelados/completados): ${reports.size - filteredReports.size}")
                    Log.i("GoogleMapScreen", "📋 Affairs obtenidos: ${affairsMap.size}")

                    // 👇 NUEVO: Obtener IDs únicos de usuarios que hicieron reportes
                    val userIds = filteredReports.mapNotNull { it.userId }.distinct()

                    // 👇 NUEVO: Obtener todos los perfiles de usuarios en una sola consulta
                    val userProfilesMap = if (userIds.isNotEmpty()) {
                        withContext(Dispatchers.IO) {
                            try {
                                val profiles = SupabaseService.getInstance().postgrest
                                    .from("profiles")
                                    .select {
                                        filter {
                                            isIn("id", userIds)
                                        }
                                    }
                                    .decodeList<AppUser>()

                                Log.i("GoogleMapScreen", "👤 Perfiles obtenidos: ${profiles.size}")
                                profiles.associateBy { it.id }
                            } catch (e: Exception) {
                                Log.e("GoogleMapScreen", "❌ Error obteniendo perfiles de usuarios", e)
                                emptyMap()
                            }
                        }
                    } else {
                        emptyMap()
                    }

                    val markers = mutableListOf<ReportMarker>()

                    withContext(Dispatchers.IO) {
                        filteredReports.forEach { report ->
                            try {
                                val addressLocation = report.reportLocation
                                val reportDescription = report.description ?: "Sin descripción"

                                if (addressLocation.isNullOrBlank()) {
                                    Log.w("GoogleMapScreen", "⚠️ Reporte sin ubicación: ${report.id}")
                                    return@forEach
                                }

                                val geocoder = Geocoder(context)

                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocationName(addressLocation, 1)

                                if (!addresses.isNullOrEmpty()) {
                                    val address = addresses[0]
                                    val reportLatLng = LatLng(address.latitude, address.longitude)

                                    val distance = if (location != null) {
                                        calculateDistance(
                                            location.latitude, location.longitude,
                                            address.latitude, address.longitude
                                        )
                                    } else {
                                        0f
                                    }

                                    val shouldAdd = config.showAllReports ||
                                            location == null ||
                                            distance <= config.maxDistanceKm

                                    if (shouldAdd) {
                                        val affairName = report.idAffair?.let { affairsMap[it]?.affairName }

                                        // 👇 NUEVO: Obtener perfil del usuario
                                        val userProfile = report.userId?.let { userProfilesMap[it] }

                                        // Determinar nombre y foto
                                        val displayName = userProfile?.name ?: report.userName
                                        val photoProfile = userProfile?.photoProfile


                                        markers.add(
                                            ReportMarker(
                                                id = report.id,
                                                position = reportLatLng,
                                                title = displayName ?: "Usuario anónimo",
                                                description = reportDescription,
                                                distance = distance,
                                                userImageUrl = photoProfile, // 👈 Foto del perfil
                                                reportImageUrl = report.imageUrl,
                                                createdAt = report.createdAt,
                                                affairId = report.idAffair,
                                                affairName = affairName,
                                                userName = displayName,
                                                user_id = report.userId
                                            )
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("GoogleMapScreen", "❌ Error geocodificando ${report.reportLocation ?: "ubicación desconocida"}", e)
                            }
                        }
                    }

                    reportMarkers = if (location != null && !config.showAllReports) {
                        markers.sortedBy { it.distance }
                    } else {
                        markers
                    }

                    Log.i("GoogleMapScreen", "✅ Total marcadores: ${markers.size}")
                }

                isLoading = false

            } catch (e: Exception) {
                Log.e("GoogleMapScreen", "❌ Error general", e)
                isLoading = false
            }
        }
    }
    val defaultLocation = LatLng(18.4861, -69.9312)
    val mapCenter = userLocation ?: defaultLocation

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapCenter, config.initialZoom)
    }

    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, config.initialZoom)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission && config.showUserLocation
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
                myLocationButtonEnabled = hasLocationPermission && config.showUserLocation
            )
        ) {
            if (config.showDefaultMarker && userLocation == null) {
                Marker(
                    state = MarkerState(position = defaultLocation),
                    title = "Santo Domingo",
                    snippet = "Capital RD"
                )
            }

            userLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Tu ubicación",
                    snippet = "Estás aquí",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            reportMarkers.forEach { marker ->
                Marker(
                    state = MarkerState(position = marker.position),
                    title = marker.title,
                    snippet = marker.description.take(50) + "...",
                    icon = createCustomMarkerIcon(context, marker.userImageUrl),
                    onClick = {
                        selectedMarker = marker
                        true
                    }
                )
            }
        }

        onBackClick?.let { backAction ->
            FloatingActionButton(
                onClick = backAction,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 40.dp, start = 16.dp)
                    .shadow(12.dp, CircleShape)
                    .size(56.dp),
                containerColor = Color.White,
                contentColor = Color(0xFF1976D2),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        selectedMarker?.let { marker ->
            ModalBottomSheet(
                onDismissRequest = { selectedMarker = null },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                ReportDetailSheet(
                    marker = marker,
                    onClose = { selectedMarker = null },
                    navController = navController
                )
            }
        }
    }
}

/**
 * Función para compartir el reporte en redes sociales con imagen/video
 */

private fun shareReport(context: Context, marker: ReportMarker) {
    val shareText = buildString {
        append("━━━━━━━━━━━━━━━━━━━━━━━\n")
        append("🚨 ALERTA DE SEGURIDAD - SAFEZONE\n")
        append("━━━━━━━━━━━━━━━━━━━━━━━\n\n")

        append("📋 DETALLES DEL INCIDENTE:\n\n")

        marker.affairName?.let {
            append("🏷️ Tipo: $it\n")
        }

        append("📍 Descripción:\n")
        append("   ${marker.description}\n\n")

        marker.createdAt?.let {
            append("📅 Fecha: ${formatDateTime(it)}\n")
        }

        if (marker.distance > 0) {
            append("📏 Distancia: ${String.format("%.1f", marker.distance)} km de tu ubicación\n")
        }

        append("\n━━━━━━━━━━━━━━━━━━━━━━━\n")
        append("⚠️ Mantente seguro y alerta\n")
        append("📱 Descarga SafeZone en Play Store\n")
        append("#SafeZone #SeguridadCiudadana #Alerta\n")
    }

    // Si hay imagen o video, compartir con multimedia
    if (!marker.reportImageUrl.isNullOrBlank()) {
        try {
            // Descargar y compartir la imagen/video
            val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
            scope.launch {
                try {
                    val url = java.net.URL(marker.reportImageUrl)
                    val connection = url.openConnection()
                    connection.connect()

                    val inputStream = connection.getInputStream()
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

                    // Guardar imagen temporalmente
                    val cachePath = java.io.File(context.cacheDir, "shared_images")
                    cachePath.mkdirs()

                    val fileName = "safezone_report_${System.currentTimeMillis()}.jpg"
                    val file = java.io.File(cachePath, fileName)

                    val fileOutputStream = java.io.FileOutputStream(file)
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()

                    // Obtener URI del archivo
                    val imageUri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )

                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        // Compartir con imagen
                        val shareIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                            putExtra(android.content.Intent.EXTRA_STREAM, imageUri)
                            type = "image/*"
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        val chooserIntent = android.content.Intent.createChooser(shareIntent, "Compartir reporte de SafeZone")
                        chooserIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(chooserIntent)

                        Log.d("GoogleMapScreen", "📤 Compartiendo reporte con imagen: ${marker.id}")
                    }
                } catch (e: Exception) {
                    Log.e("GoogleMapScreen", "❌ Error descargando imagen, compartiendo solo texto", e)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        shareTextOnly(context, shareText, marker.id)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleMapScreen", "❌ Error al compartir con imagen", e)
            shareTextOnly(context, shareText, marker.id)
        }
    } else {
        // Compartir solo texto si no hay imagen
        shareTextOnly(context, shareText, marker.id)
    }
}

private fun createCustomMarkerIcon(context: Context, imageUrl: String?): BitmapDescriptor {
    val size = 120
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.parseColor("#E53935")
        style = Paint.Style.FILL
    }

    val circlePaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }

    val strokePaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.parseColor("#E53935")
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, circlePaint)
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, strokePaint)

    val iconPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.parseColor("#E53935")
        textSize = 50f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    canvas.drawText("!", size / 2f, size / 2f + 18f, iconPaint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val earthRadius = 6371.0

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return (earthRadius * c).toFloat()
}

@Composable
fun VideoPlayerComposable(videoUrl: String) {
    Log.d("VideoPlayerComposable", "🎬 Inicializando reproductor de video: $videoUrl")

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { ctx ->
                android.widget.VideoView(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    val mediaController = android.widget.MediaController(ctx)
                    mediaController.setAnchorView(this)
                    setMediaController(mediaController)

                    setVideoURI(android.net.Uri.parse(videoUrl))

                    setOnPreparedListener { mp ->
                        Log.d("VideoPlayerComposable", "✅ Video preparado")
                        mp.isLooping = false
                        mp.setVolume(1f, 1f)
                        start()
                    }

                    setOnErrorListener { _, what, extra ->
                        Log.e("VideoPlayerComposable", "❌ Error: what=$what, extra=$extra")
                        true
                    }

                    requestFocus()
                }
            },
            update = { videoView ->
                videoView.setVideoURI(android.net.Uri.parse(videoUrl))
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE53935))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "VIDEO",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


/**
 * Función auxiliar para compartir solo texto
 */
private fun shareTextOnly(context: Context, shareText: String, reportId: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Compartir reporte de SafeZone")
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(shareIntent)

    Log.d("GoogleMapScreen", "📤 Compartiendo reporte (solo texto): $reportId")
}

@Composable
fun ReportDetailSheet(
    marker: ReportMarker,
    navController: NavController,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Obtener URL de la foto de perfil si existe
    val profilePhotoUrl = remember(marker.userImageUrl) {
        if (!marker.userImageUrl.isNullOrEmpty()) {
            try {
                val bucket = SupabaseService.getInstance().storage.from("UserProfile")
                bucket.publicUrl(marker.userImageUrl!!)
            } catch (e: Exception) {
                Log.e("ReportDetailSheet", "Error obteniendo URL de perfil", e)
                null
            }
        } else {
            null
        }
    }

    // Variables para comentarios - usando ReportCommentDto
    var comments by remember { mutableStateOf<List<ReportCommentDto>>(emptyList()) }
    var isLoadingComments by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var isPostingComment by remember { mutableStateOf(false) }

    // Servicios
    val commentService = remember { ReportCommentService() }

    // Obtener usuario actual
    val currentUser = remember { SupabaseService.getInstance().auth.currentUserOrNull() }
    val currentUserId = currentUser?.id ?: ""

    // Cargar perfil del usuario actual
    var currentUserProfile by remember { mutableStateOf<AppUser?>(null) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            try {
                currentUserProfile = SessionManager.getUserProfile(context)
                Log.d("ReportDetailSheet", "Perfil cargado: ${currentUserProfile?.name}")
            } catch (e: Exception) {
                Log.e("ReportDetailSheet", "Error cargando perfil", e)
            }
        }
    }

    // Función para cargar comentarios desde la BD
    fun loadComments(reportId: String) {
        if (isLoadingComments) return

        Log.d("ReportDetailSheet", "🔄 loadComments() llamado para reporte: $reportId")
        isLoadingComments = true

        coroutineScope.launch {
            try {
                Log.d("ReportDetailSheet", "📥 Iniciando carga de comentarios...")

                val fetchedComments = commentService.getCommentsForReport(reportId)

                Log.d("ReportDetailSheet", "✅ Comentarios cargados: ${fetchedComments.size}")

                // DEBUG: Ver detalles de cada comentario
                fetchedComments.forEachIndexed { index, comment ->
                    Log.d("ReportDetailSheet", "   [${index + 1}] ${comment.getDisplayName()}: ${comment.message}")
                    Log.d("ReportDetailSheet", "       ID: ${comment.id}")
                    Log.d("ReportDetailSheet", "       user_id: ${comment.userId}")
                    Log.d("ReportDetailSheet", "       report_id: ${comment.reportId}")
                }

                comments = fetchedComments

            } catch (e: Exception) {
                Log.e("ReportDetailSheet", "❌ Error cargando comentarios", e)

                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Error al cargar comentarios: ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                }
            } finally {
                isLoadingComments = false
                Log.d("ReportDetailSheet", "🏁 loadComments() completado")
            }
        }
    }

    // Cargar comentarios cuando el sheet se abre
    LaunchedEffect(marker.id) {
        Log.d("ReportDetailSheet", "🎯 Sheet abierto para reporte: ${marker.id}")
        loadComments(marker.id)
    }

    // Función para postear comentario en la BD
    fun postComment() {
        if (commentText.isBlank() || isPostingComment || currentUserId.isBlank()) {
            if (currentUserId.isBlank()) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Debes iniciar sesión para comentar",
                        duration = SnackbarDuration.Short
                    )
                }
            }
            return
        }

        isPostingComment = true
        coroutineScope.launch {
            try {
                Log.d("ReportDetailSheet", "📤 Publicando comentario: ${commentText.take(50)}...")

                val result = commentService.createCommentForReport(
                    reportId = marker.id,
                    userId = currentUserId,
                    message = commentText.trim()
                )

                if (result.isSuccess) {
                    val newComment = result.getOrThrow()

                    Log.d("ReportDetailSheet", "✅ Comentario publicado: ${newComment.id}")
                    Log.d("ReportDetailSheet", "   Mensaje: ${newComment.message}")
                    Log.d("ReportDetailSheet", "   Usuario: ${newComment.getDisplayName()}")

                    // Agregar el nuevo comentario al inicio de la lista
                    comments = comments + newComment  // Cambié a agregar al final para mejor UX

                    // Limpiar el campo de texto
                    commentText = ""

                    snackbarHostState.showSnackbar(
                        message = "✅ Comentario publicado",
                        duration = SnackbarDuration.Short
                    )

                    // Recargar comentarios para asegurar que tenemos toda la info
                    loadComments(marker.id)

                } else {
                    throw result.exceptionOrNull() ?: Exception("Error desconocido al crear comentario")
                }

            } catch (e: Exception) {
                Log.e("ReportDetailSheet", "❌ Error publicando comentario", e)

                snackbarHostState.showSnackbar(
                    message = "❌ Error: ${e.message ?: "No se pudo publicar el comentario"}",
                    duration = SnackbarDuration.Long
                )
            } finally {
                isPostingComment = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Contenido principal con scroll
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ReportHeaderSection(
                    marker = marker,
                    profilePhotoUrl = profilePhotoUrl,
                    navController = navController,
                    onClose = onClose,
                    context = context
                )
            }

            item {
                ReportContentSection(marker = marker)
            }

            // Sección de comentarios
            item {
                CommentsHeaderSection(
                    commentsCount = comments.size,
                    modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                )
            }

            if (isLoadingComments) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp,
                                color = Color(0xFF1976D2)
                            )
                            Text(
                                text = "Cargando comentarios...",
                                fontSize = 14.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
            }

            if (comments.isEmpty() && !isLoadingComments) {
                item {
                    EmptyCommentsMessage(
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            } else {
                items(comments, key = { it.id }) { comment ->
                    CommentItemCompact(
                        comment = comment,
                        currentUserId = currentUserId,
                        navController = navController,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }

        // Input de comentarios en la parte inferior
        CommentInputBarCompact(
            commentText = commentText,
            onCommentChange = { commentText = it },
            onSendClick = { postComment() },
            isPosting = isPostingComment,
            isEnabled = currentUser != null,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun CommentItemCompact(
    comment: ReportCommentDto,  // Cambiado a ReportCommentDto
    currentUserId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    // Determinar si es el usuario actual
    val isCurrentUser = comment.userId == currentUserId
    val displayName = comment.getDisplayName()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFAFAFA),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Información del usuario
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1976D2).copy(alpha = 0.1f))
                            .clickable {
                                val encodedName = java.net.URLEncoder.encode(
                                    displayName,
                                    "UTF-8"
                                )
                                navController.navigate("profileUser/${comment.userId}/$encodedName")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (comment.userPhotoUrl != null) {
                            AsyncImage(
                                model = comment.userPhotoUrl,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Usuario",
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = displayName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121),
                            modifier = Modifier.clickable {
                                val encodedName = java.net.URLEncoder.encode(
                                    displayName,
                                    "UTF-8"
                                )
                                navController.navigate("profileUser/${comment.userId}/$encodedName")
                            }
                        )
                        Text(
                            text = formatCommentDate(comment.createdAt),
                            fontSize = 11.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }

                // Menú de opciones
                if (isCurrentUser) {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones",
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // Opción para eliminar (solo el propio usuario)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Eliminar",
                                        fontSize = 13.sp,
                                        color = Color(0xFFE53935)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    // TODO: Implementar eliminación de comentario
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Contenido del comentario
            Text(
                text = comment.message,
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


@Composable
private fun ReportHeaderSection(
    marker: ReportMarker,
    profilePhotoUrl: String?,
    navController: NavController,
    onClose: () -> Unit,
    context: Context
) {
    val isAnonymous = marker.userName.isNullOrEmpty() || marker.userName == "Usuario anónimo"
    val displayName = if (isAnonymous) "Usuario anónimo" else marker.userName!!

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Foto de perfil
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isAnonymous)
                            Color(0xFF757575).copy(alpha = 0.2f)
                        else
                            Color(0xFF1976D2).copy(alpha = 0.1f)
                    )
                    .clickable(
                        enabled = !isAnonymous && marker.user_id != null,
                        onClick = {
                            marker.user_id?.let { userId ->
                                val encodedUserName = java.net.URLEncoder.encode(
                                    marker.userName ?: "Usuario",
                                    "UTF-8"
                                )
                                navController.navigate("profileUser/$userId/$encodedUserName")
                                onClose()
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!isAnonymous && profilePhotoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profilePhotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto de perfil de $displayName",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Usuario",
                        tint = if (isAnonymous) Color(0xFF757575) else Color(0xFF1976D2),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAnonymous) Color(0xFF757575) else Color(0xFF212121)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (marker.distance > 0) {
                            "${String.format("%.1f", marker.distance)} km"
                        } else {
                            "Ubicación cercana"
                        },
                        fontSize = 11.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        }

        Row {
            IconButton(
                onClick = { shareReport(context, marker) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Compartir",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ReportContentSection(marker: ReportMarker) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Fecha
        marker.createdAt?.let { date ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatDateTime(date),
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Tipo de incidente
        marker.affairName?.let { affairName ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1976D2).copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, Color(0xFF1976D2).copy(alpha = 0.3f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Tipo de Incidente",
                            fontSize = 10.sp,
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = affairName,
                            fontSize = 13.sp,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Descripción
        Text(
            text = "Descripción",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F9FA)
            ),
            border = BorderStroke(0.5.dp, Color(0xFFE0E0E0))
        ) {
            Text(
                text = marker.description,
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 18.sp,
                modifier = Modifier.padding(12.dp)
            )
        }

        // Media (imagen/video)
        marker.reportImageUrl?.let { mediaUrl ->
            Text(
                text = "Evidencia",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            val isVideo = isVideoUrl(mediaUrl)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isVideo) {
                        VideoPlayerComposable(videoUrl = mediaUrl)
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(mediaUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Imagen del reporte",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        // Botón de like
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                LikeButton(
                    targetId = marker.id,
                    entityType = EntityType.REPORT,
                    modifier = Modifier.fillMaxWidth(),
                    showCount = true,
                    compactMode = true
                )
            }
        }

        // Separador
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = Color(0xFFEEEEEE)
        )
    }
}

@Composable
private fun CommentsHeaderSection(
    commentsCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Comentarios",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
        }

        Text(
            text = "$commentsCount",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF757575)
        )
    }
}


// Función mejorada para formatear fechas
private fun formatCommentDate(dateString: String): String {
    return try {
        // Manejar diferentes formatos de fecha
        val inputFormat = when {
            dateString.contains("T") && dateString.contains(".") ->
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault())
            dateString.contains("T") ->
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            else ->
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        }

        val date = inputFormat.parse(dateString)
        val now = Date()
        val diff = now.time - date.time

        // Mostrar tiempo relativo
        return when {
            diff < 60_000 -> "Hace un momento"
            diff < 3_600_000 -> "Hace ${(diff / 60_000).toInt()} min"
            diff < 86_400_000 -> "Hace ${(diff / 3_600_000).toInt()} h"
            diff < 7 * 86_400_000 -> {
                val days = (diff / 86_400_000).toInt()
                if (days == 1) "Ayer" else "Hace $days días"
            }
            else -> {
                val outputFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        Log.e("formatCommentDate", "Error formateando fecha: $dateString", e)
        "Fecha desconocida"
    }
}

@Composable
private fun CommentInputBarCompact(
    commentText: String,
    onCommentChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isPosting: Boolean,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Campo de texto
            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = if (isEnabled) "Escribe un comentario..." else "Inicia sesión para comentar",
                        fontSize = 13.sp
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    disabledBorderColor = Color(0xFFF0F0F0),
                    disabledPlaceholderColor = Color(0xFFBDBDBD)
                ),
                maxLines = 2,
                enabled = isEnabled && !isPosting,
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
            )

            // Botón de enviar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (commentText.isNotBlank() && !isPosting && isEnabled)
                            Color(0xFF1976D2)
                        else
                            Color(0xFFE0E0E0)
                    )
                    .clickable(
                        enabled = commentText.isNotBlank() && !isPosting && isEnabled,
                        onClick = onSendClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPosting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCommentsMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            tint = Color(0xFFE0E0E0),
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "No hay comentarios aún",
            fontSize = 14.sp,
            color = Color(0xFF9E9E9E)
        )
        Text(
            text = "¡Sé el primero en comentar!",
            fontSize = 12.sp,
            color = Color(0xFFBDBDBD)
        )
    }
}


// Modelo de datos para comentarios


fun isVideoUrl(url: String): Boolean {
    val videoExtensions = listOf(".mp4", ".mov", ".avi", ".mkv", ".webm", ".3gp", ".flv")
    return videoExtensions.any { url.lowercase().contains(it) }
}

private fun formatDateTime(dateString: String): String {
    return try {
        val formats = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "dd/MM/yyyy HH:mm:ss"
        )

        for (format in formats) {
            try {
                val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                val date = inputFormat.parse(dateString)
                if (date != null) {
                    val now = Calendar.getInstance()
                    val reportDate = Calendar.getInstance().apply { time = date }

                    return if (now.get(Calendar.YEAR) == reportDate.get(Calendar.YEAR) &&
                        now.get(Calendar.MONTH) == reportDate.get(Calendar.MONTH) &&
                        now.get(Calendar.DAY_OF_MONTH) == reportDate.get(Calendar.DAY_OF_MONTH)) {
                        "Hoy a las " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                    } else {
                        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date)
                    }
                }
            } catch (e: Exception) {
                continue
            }
        }

        dateString
    } catch (e: Exception) {
        dateString
    }
}