package com.wilkins.safezone.frontend.ui.Map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
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
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportsRepository
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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
 */
@SuppressLint("MissingPermission")
@Composable
fun GoogleMapScreen(
    modifier: Modifier = Modifier,
    config: MapConfig = MapConfig(),
    onReportClick: ((ReportDto) -> Unit)? = null
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

                                        markers.add(
                                            ReportMarker(
                                                id = report.id,
                                                position = reportLatLng,
                                                title = report.userName ?: "Usuario anónimo",
                                                description = reportDescription,
                                                distance = distance,
                                                userImageUrl = null, // TODO: Agregar URL de perfil cuando esté disponible
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
                MarkerInfoWindowContent(
                    state = MarkerState(position = location),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                ) {
                    Card(
                        modifier = Modifier
                            .width(150.dp)
                            .padding(4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📍 Tu ubicación",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Estás aquí",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Marcadores de reportes con InfoWindow personalizado
            reportMarkers.forEach { marker ->
                MarkerInfoWindowContent(
                    state = MarkerState(position = marker.position),
                    icon = createCustomMarkerIcon(context, marker.userImageUrl)
                ) {
                    CustomReportInfoWindow(marker = marker)
                }
            }
        }

        // Indicador de carga
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    Log.i("GoogleMapScreen", "🟢 GoogleMap renderizado")
}

/**
 * Versión simple del mapa (sin configuración)
 */
@Composable
fun SimpleGoogleMapScreen(
    modifier: Modifier = Modifier,
    onReportClick: ((ReportDto) -> Unit)? = null
) {
    GoogleMapScreen(
        modifier = modifier,
        config = MapConfig(
            showUserLocation = true,
            showAllReports = false,
            maxDistanceKm = 10f,
            initialZoom = 12f,
            showDefaultMarker = true
        ),
        onReportClick = onReportClick
    )
}

/**
 * InfoWindow personalizado para los reportes
 */
/**
 * InfoWindow personalizado para los reportes - Versión Mejorada
 */
/**
 * InfoWindow personalizado para los reportes - Versión Rediseñada
 */
// Reemplaza la función CustomReportInfoWindow completa con esta versión:

// Reemplaza la función CustomReportInfoWindow completa con esta versión:

@Composable
fun CustomReportInfoWindow(marker: ReportMarker) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .padding(horizontal = 2.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Encabezado con información del usuario
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1976D2),
                                Color(0xFF2196F3)
                            )
                        )
                    )
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar del usuario
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Usuario",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(22.dp)
                        )

                        // Anillo decorativo
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = Color(0xFF1976D2).copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Información del usuario
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = marker.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Distancia",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (marker.distance > 0) {
                                    "${String.format("%.1f", marker.distance)} km"
                                } else {
                                    "Cerca de ti"
                                },
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Badge de tipo (si existe)
                    marker.affairName?.let { affairName ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.4f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = affairName.take(3).uppercase(),
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Fecha y hora
                marker.createdAt?.let { date ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Hora",
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatDateTime(date),
                            fontSize = 12.sp,
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Tipo de Affair
                marker.affairName?.let { affairName ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Tipo",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = affairName,
                            fontSize = 13.sp,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Título del reporte
                Text(
                    text = "📢 Descripción",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Descripción
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8F9FA)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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

                // Imagen del reporte - VERSIÓN CORREGIDA
                marker.reportImageUrl?.let { imageUrl ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Usar AsyncImage directamente en lugar de painter
                            coil.compose.AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .error(android.R.drawable.ic_menu_report_image)
                                    .build(),
                                contentDescription = "Imagen del reporte",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                // Estados de carga manejados por AsyncImage internamente
                            )

                            // Overlay de protección
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.05f)
                                            ),
                                            startY = 0.7f
                                        )
                                    )
                            )
                        }
                    }

                    // Pie de foto
                    Text(
                        text = "📷 Imagen relacionada al reporte",
                        fontSize = 10.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .align(Alignment.Start)
                    )
                }

                // Pie de página con acciones
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    // Nivel de prioridad
                    Row(
                        verticalAlignment = Alignment.CenterVertically
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
                            fontSize = 10.sp,
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botón de acción
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1976D2))
                            .clickable { /* Acción al hacer click */ }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Ver detalles",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
/**
 * Formatear fecha y hora de forma más legible
 */
private fun formatDateTime(dateString: String): String {
    return try {
        // Intentar parsear diferentes formatos
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

                    // Si es hoy, mostrar solo la hora
                    return if (now.get(Calendar.YEAR) == reportDate.get(Calendar.YEAR) &&
                        now.get(Calendar.MONTH) == reportDate.get(Calendar.MONTH) &&
                        now.get(Calendar.DAY_OF_MONTH) == reportDate.get(Calendar.DAY_OF_MONTH)) {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                    } else {
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
                    }
                }
            } catch (e: Exception) {
                continue
            }
        }

        // Si no se pudo parsear, devolver el string original
        dateString
    } catch (e: Exception) {
        dateString
    }
}
/**
 * Componente Badge para mostrar el tipo de asunto
 */
@Composable
private fun BadgeComponent(affairName: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .border(
                width = 1.dp,
                color = Color(0xFF1976D2).copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = affairName.uppercase(),
            fontSize = 11.sp,
            color = Color(0xFF1976D2),
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

/**
 * Crear icono personalizado para el marcador (avatar circular)
 */
private fun createCustomMarkerIcon(context: Context, imageUrl: String?): BitmapDescriptor {
    val size = 120
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Dibujar círculo de fondo
    val paint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.parseColor("#E53935") // Rojo para reportes
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
 * Formatear fecha para mostrar
 */
private fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split(" ")
        if (parts.isNotEmpty()) {
            parts[0] // Retorna solo la fecha (YYYY-MM-DD)
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

/**
 * Calcular distancia entre dos puntos usando la fórmula de Haversine
 */
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val earthRadius = 6371.0 // Radio de la Tierra en km

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return (earthRadius * c).toFloat()
}