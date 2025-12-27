package com.wilkins.safezone.frontend.ui.Map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
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
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportsRepository
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Modelo para los marcadores en el mapa
 */
data class ReportMarker(
    val id: String,
    val position: LatLng,
    val title: String,
    val description: String,
    val distance: Float = 0f,
    val userImageUrl: String? = null,
    val reportImageUrl: String? = null,
    val createdAt: String? = null,
    val affairId: Int? = null,
    val affairName: String? = null
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
 *
 * @param modifier Modificador para el layout
 * @param config Configuración del mapa
 * @param onReportClick Callback cuando se hace click en un marcador
 * @param onBackClick Callback cuando se presiona el botón de volver
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun GoogleMapScreen(
    modifier: Modifier = Modifier,
    config: MapConfig = MapConfig(),
    onReportClick: ((ReportDto) -> Unit)? = null,
    onBackClick: (() -> Unit)? = null
) {
    Log.i("GoogleMapScreen", "🔵 Composable iniciado")

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados de permisos
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

    // Estados del mapa
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var reportMarkers by remember { mutableStateOf<List<ReportMarker>>(emptyList()) }
    var allReports by remember { mutableStateOf<List<ReportDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Estado para el marcador seleccionado
    var selectedMarker by remember { mutableStateOf<ReportMarker?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Solicitar permisos
    LaunchedEffect(Unit) {
        Log.i("GoogleMapScreen", "📍 Permiso ubicación: ${if (hasLocationPermission) "OTORGADO" else "DENEGADO"}")
        if (!hasLocationPermission && config.showUserLocation) {
            Log.i("GoogleMapScreen", "🚨 Solicitando permiso de ubicación")
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Cargar datos del mapa
    LaunchedEffect(hasLocationPermission, config) {
        scope.launch {
            try {
                // 1. Obtener ubicación del usuario (si está habilitado y hay permiso)
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

                // 2. Obtener reportes desde el repositorio
                val repository = ReportsRepository()
                val result = repository.getAllReports()

                // Obtener affairs para mapear los tipos
                val affairsResult = repository.getAllAffairs()
                val affairsMap = affairsResult.getOrNull()?.associateBy { it.id } ?: emptyMap()

                if (result.isSuccess) {
                    val reports = result.getOrNull() ?: emptyList()
                    allReports = reports
                    Log.i("GoogleMapScreen", "📡 Reportes obtenidos: ${reports.size}")
                    Log.i("GoogleMapScreen", "📋 Affairs obtenidos: ${affairsMap.size}")

                    // 3. Geocodificar y crear marcadores
                    val markers = mutableListOf<ReportMarker>()

                    withContext(Dispatchers.IO) {
                        reports.forEach { report ->
                            try {
                                // Validar que el reporte tenga los datos necesarios
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

                                    // Calcular distancia si hay ubicación del usuario
                                    val distance = if (location != null) {
                                        calculateDistance(
                                            location.latitude, location.longitude,
                                            address.latitude, address.longitude
                                        )
                                    } else {
                                        0f
                                    }

                                    // Decidir si agregar el marcador
                                    val shouldAdd = config.showAllReports ||
                                            location == null ||
                                            distance <= config.maxDistanceKm

                                    if (shouldAdd) {
                                        val affairName = report.idAffair?.let { affairsMap[it]?.affairName }

                                        // LOG IMPORTANTE: Verificar URL de imagen/video
                                        Log.d("GoogleMapScreen", "🎬 Reporte ID: ${report.id}")
                                        Log.d("GoogleMapScreen", "📎 imageUrl: ${report.imageUrl}")
                                        Log.d("GoogleMapScreen", "📏 Longitud URL: ${report.imageUrl?.length ?: 0}")
                                        Log.d("GoogleMapScreen", "🔍 ¿Es null?: ${report.imageUrl == null}")
                                        Log.d("GoogleMapScreen", "🔍 ¿Es blank?: ${report.imageUrl.isNullOrBlank()}")

                                        markers.add(
                                            ReportMarker(
                                                id = report.id,
                                                position = reportLatLng,
                                                title = report.userName ?: "Usuario anónimo",
                                                description = reportDescription,
                                                distance = distance,
                                                userImageUrl = null,
                                                reportImageUrl = report.imageUrl,
                                                createdAt = report.createdAt,
                                                affairId = report.idAffair,
                                                affairName = affairName
                                            )
                                        )
                                        Log.i("GoogleMapScreen", "✅ Marcador: $reportDescription - ${if (distance > 0) "${String.format("%.2f", distance)}km" else "sin filtro"} - Tipo: $affairName")
                                    }
                                } else {
                                    Log.w("GoogleMapScreen", "⚠️ No se geocodificó: $addressLocation")
                                }
                            } catch (e: Exception) {
                                Log.e("GoogleMapScreen", "❌ Error geocodificando ${report.reportLocation ?: "ubicación desconocida"}", e)
                            }
                        }
                    }

                    // Ordenar por distancia si hay ubicación
                    reportMarkers = if (location != null && !config.showAllReports) {
                        markers.sortedBy { it.distance }
                    } else {
                        markers
                    }

                    Log.i("GoogleMapScreen", "✅ Total marcadores: ${markers.size}")
                } else {
                    Log.e("GoogleMapScreen", "❌ Error cargando reportes: ${result.exceptionOrNull()?.message}")
                }

                isLoading = false

            } catch (e: Exception) {
                Log.e("GoogleMapScreen", "❌ Error general", e)
                isLoading = false
            }
        }
    }

    // Configurar cámara del mapa
    val defaultLocation = LatLng(18.4861, -69.9312) // Santo Domingo
    val mapCenter = userLocation ?: defaultLocation

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapCenter, config.initialZoom)
    }

    // Actualizar cámara cuando cambia la ubicación del usuario
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
            Log.i("GoogleMapScreen", "🗺 Dibujando ${reportMarkers.size} marcadores")

            // Marcador por defecto de Santo Domingo (opcional)
            if (config.showDefaultMarker && userLocation == null) {
                Marker(
                    state = MarkerState(position = defaultLocation),
                    title = "Santo Domingo",
                    snippet = "Capital RD"
                )
            }

            // Marcador de ubicación del usuario (azul)
            userLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Tu ubicación",
                    snippet = "Estás aquí",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            // Marcadores de reportes con click
            reportMarkers.forEach { marker ->
                Marker(
                    state = MarkerState(position = marker.position),
                    title = marker.title,
                    snippet = marker.description.take(50) + "...",
                    icon = createCustomMarkerIcon(context, marker.userImageUrl),
                    onClick = {
                        Log.d("GoogleMapScreen", "🖱️ Click en marcador: ${marker.id}")
                        Log.d("GoogleMapScreen", "📎 URL del marcador: ${marker.reportImageUrl}")
                        selectedMarker = marker
                        true // Retorna true para evitar el comportamiento por defecto
                    }
                )
            }
        }

        // Botón de volver - Posicionado en la esquina superior izquierda
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

        // Indicador de carga
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Bottom Sheet con detalles completos del reporte
        selectedMarker?.let { marker ->
            Log.d("GoogleMapScreen", "📋 Mostrando Bottom Sheet para marcador: ${marker.id}")
            Log.d("GoogleMapScreen", "📎 URL en Bottom Sheet: ${marker.reportImageUrl}")

            ModalBottomSheet(
                onDismissRequest = { selectedMarker = null },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                ReportDetailSheet(
                    marker = marker,
                    onClose = { selectedMarker = null }
                )
            }
        }
    }

    Log.i("GoogleMapScreen", "🟢 GoogleMap renderizado")
}

/**
 * Bottom Sheet con detalles completos del reporte
 */
@Composable
fun ReportDetailSheet(
    marker: ReportMarker,
    onClose: () -> Unit
) {
    Log.d("ReportDetailSheet", "🎨 Renderizando detalles del reporte: ${marker.id}")
    Log.d("ReportDetailSheet", "📎 reportImageUrl: ${marker.reportImageUrl}")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1976D2).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Usuario",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = marker.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (marker.distance > 0) {
                                    "${String.format("%.1f", marker.distance)} km"
                                } else {
                                    "Ubicación cercana"
                                },
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Cerrar",
                    tint = Color(0xFF757575)
                )
            }
        }

        // Fecha y hora
        marker.createdAt?.let { date ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatDateTime(date),
                    fontSize = 14.sp,
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
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1976D2).copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, Color(0xFF1976D2).copy(alpha = 0.3f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Tipo de Incidente",
                            fontSize = 12.sp,
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = affairName,
                            fontSize = 16.sp,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Descripción
        Text(
            text = "Descripción del Reporte",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F9FA)
            ),
            border = BorderStroke(0.5.dp, Color(0xFFE0E0E0))
        ) {
            Text(
                text = marker.description,
                fontSize = 14.sp,
                color = Color(0xFF424242),
                lineHeight = 20.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Imagen o Video del reporte - AQUÍ ES DONDE SE CARGA
        marker.reportImageUrl?.let { mediaUrl ->
            Log.d("ReportDetailSheet", "🎬 Procesando media URL")
            Log.d("ReportDetailSheet", "📎 URL completa: $mediaUrl")
            Log.d("ReportDetailSheet", "🔍 ¿URL está vacía?: ${mediaUrl.isEmpty()}")
            Log.d("ReportDetailSheet", "🔍 ¿URL está en blanco?: ${mediaUrl.isBlank()}")

            Text(
                text = "Evidencia del Reporte",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val isVideo = isVideoUrl(mediaUrl)
            Log.d("ReportDetailSheet", "🎬 ¿Es video?: $isVideo")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isVideo) {
                        Log.d("ReportDetailSheet", "🎥 Mostrando reproductor de video")
                        // Reproductor de video usando AndroidView con VideoView
                        VideoPlayerComposable(videoUrl = mediaUrl)
                    } else {
                        Log.d("ReportDetailSheet", "🖼️ Cargando imagen con AsyncImage")
                        // Imagen con AsyncImage (como en la versión que funcionaba)
                        coil.compose.AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(mediaUrl)
                                .crossfade(true)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_report_image)
                                .listener(
                                    onStart = {
                                        Log.d("ReportDetailSheet", "▶️ Carga de imagen iniciada: $mediaUrl")
                                    },
                                    onSuccess = { _, result ->
                                        Log.d("ReportDetailSheet", "✅ Imagen cargada exitosamente")
                                    },
                                    onError = { _, result ->
                                        Log.e("ReportDetailSheet", "❌ Error cargando imagen: ${result.throwable.message}")
                                    }
                                )
                                .build(),
                            contentDescription = "Imagen del reporte",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Etiqueta
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = if (isVideo) Icons.Default.VideoLibrary else Icons.Default.Photo,
                    contentDescription = null,
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isVideo) "Video relacionado al reporte" else "Imagen relacionada al reporte",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }
        } ?: run {
            Log.w("ReportDetailSheet", "⚠️ NO HAY reportImageUrl - marker.reportImageUrl es NULL")
        }

        // Badge de prioridad
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ALTA PRIORIDAD",
                fontSize = 11.sp,
                color = Color(0xFFE53935),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Componente de imagen con manejo de estados de carga
 */
@Composable
fun ImageWithLoading(imageUrl: String) {
    Log.d("ImageWithLoading", "🖼️ Iniciando carga de imagen")
    Log.d("ImageWithLoading", "📎 URL: $imageUrl")
    Log.d("ImageWithLoading", "📏 Longitud URL: ${imageUrl.length}")

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .listener(
                onStart = {
                    Log.d("ImageWithLoading", "▶️ Carga iniciada: $imageUrl")
                },
                onSuccess = { _, result ->
                    Log.d("ImageWithLoading", "✅ Imagen cargada exitosamente: $imageUrl")
                    Log.d("ImageWithLoading", "📊 Dimensiones: ${result.drawable.intrinsicWidth}x${result.drawable.intrinsicHeight}")
                },
                onError = { _, result ->
                    Log.e("ImageWithLoading", "❌ Error cargando imagen: $imageUrl")
                    Log.e("ImageWithLoading", "❌ Error: ${result.throwable.message}")
                    Log.e("ImageWithLoading", "❌ Tipo de error: ${result.throwable.javaClass.simpleName}")
                    Log.e("ImageWithLoading", "❌ Stack trace:", result.throwable)
                }
            )
            .build()
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = painter.state) {
            is AsyncImagePainter.State.Loading -> {
                Log.d("ImageWithLoading", "⏳ Estado: Cargando...")
                CircularProgressIndicator(
                    color = Color(0xFF1976D2),
                    modifier = Modifier.size(48.dp)
                )
            }
            is AsyncImagePainter.State.Error -> {
                Log.e("ImageWithLoading", "❌ Estado: Error")
                Log.e("ImageWithLoading", "❌ Detalle del error: ${state.result.throwable.message}")
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = null,
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No se pudo cargar la imagen",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.result.throwable.message ?: "Error desconocido",
                        fontSize = 10.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
            is AsyncImagePainter.State.Success -> {
                Log.d("ImageWithLoading", "✅ Estado: Éxito - Mostrando imagen")
                Image(
                    painter = painter,
                    contentDescription = "Imagen del reporte",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                Log.d("ImageWithLoading", "⚪ Estado: Empty/Inicial")
            }
        }
    }
}

/**
 * Thumbnail de video con estado de carga
 */
@Composable
fun VideoThumbnailWithLoading(
    videoUrl: String,
    onClick: () -> Unit
) {
    Log.d("VideoThumbnailWithLoading", "🎥 Renderizando thumbnail de video")
    Log.d("VideoThumbnailWithLoading", "📎 URL: $videoUrl")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Botón de play
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.95f))
                    .border(4.dp, Color(0xFF1976D2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Reproducir video",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Toca para reproducir",
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }

        // Badge de "Video"
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
 * Función para detectar si una URL es de video basándose en la extensión
 */
fun isVideoUrl(url: String): Boolean {
    val videoExtensions = listOf(".mp4", ".mov", ".avi", ".mkv", ".webm", ".3gp", ".flv")
    val isVideo = videoExtensions.any { url.lowercase().contains(it) }

    Log.d("isVideoUrl", "🔍 Verificando URL: $url")
    Log.d("isVideoUrl", "🎬 ¿Es video?: $isVideo")

    return isVideo
}

/**
 * Formatear fecha y hora de forma más legible
 */
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

/**
 * Crear icono personalizado para el marcador (avatar circular)
 */
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

    // Círculo exterior (borde)
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)

    // Círculo interior (blanco)
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, circlePaint)

    // Borde
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, strokePaint)

    // Icono de alerta en el centro
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

/**
 * Calcular distancia entre dos puntos usando la fórmula de Haversine
 */
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

/**
 * Reproductor de video usando VideoView de Android
 */
@Composable
fun VideoPlayerComposable(videoUrl: String) {
    Log.d("VideoPlayerComposable", "🎬 Inicializando reproductor de video")
    Log.d("VideoPlayerComposable", "📎 URL: $videoUrl")

    var isPlaying by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // VideoView usando AndroidView
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { ctx ->
                android.widget.VideoView(ctx).apply {
                    Log.d("VideoPlayerComposable", "🎥 Creando VideoView")

                    setVideoPath(videoUrl)

                    setOnPreparedListener { mediaPlayer ->
                        Log.d("VideoPlayerComposable", "✅ Video preparado y listo")
                        mediaPlayer.isLooping = false
                        // Auto-start
                        start()
                        isPlaying = true
                    }

                    setOnCompletionListener {
                        Log.d("VideoPlayerComposable", "🏁 Video completado")
                        isPlaying = false
                        showControls = true
                    }

                    setOnErrorListener { _, what, extra ->
                        Log.e("VideoPlayerComposable", "❌ Error reproduciendo video")
                        Log.e("VideoPlayerComposable", "❌ What: $what, Extra: $extra")
                        Log.e("VideoPlayerComposable", "❌ URL: $videoUrl")
                        true
                    }
                }
            },
            update = { videoView ->
                Log.d("VideoPlayerComposable", "🔄 Update - isPlaying: $isPlaying")
                if (isPlaying) {
                    if (!videoView.isPlaying) {
                        videoView.start()
                        Log.d("VideoPlayerComposable", "▶️ Video iniciado")
                    }
                } else {
                    if (videoView.isPlaying) {
                        videoView.pause()
                        Log.d("VideoPlayerComposable", "⏸️ Video pausado")
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Controles superpuestos
        if (showControls || !isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable {
                        showControls = !showControls
                    },
                contentAlignment = Alignment.Center
            ) {
                // Botón de Play/Pause
                FloatingActionButton(
                    onClick = {
                        isPlaying = !isPlaying
                        showControls = false
                        Log.d("VideoPlayerComposable", "👆 Click - nuevo estado: $isPlaying")
                    },
                    containerColor = Color.White,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Badge de VIDEO en la esquina
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
    }
}