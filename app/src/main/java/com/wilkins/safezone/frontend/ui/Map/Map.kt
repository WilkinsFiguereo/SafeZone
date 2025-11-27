package com.wilkins.safezone.frontend.ui.Map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@SuppressLint("MissingPermission")
@Composable
fun GoogleMapScreen() {

    Log.i("GoogleMapScreen", "🔵 Composable iniciado")

    val context = LocalContext.current

    // --- Estado de permisos ---
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

    // --- Solicitud de permiso ---
    LaunchedEffect(Unit) {
        Log.i(
            "GoogleMapScreen",
            "📍 Permiso ubicación actual: ${if (hasLocationPermission) "OTORGADO" else "DENEGADO"}"
        )
        if (!hasLocationPermission) {
            Log.i("GoogleMapScreen", "🚨 Solicitando permiso de ubicación")
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // --- Log de errores reales del mapa ---
    LaunchedEffect(hasLocationPermission) {
        try {
            Log.i("GoogleMapScreen", "⚙ Inicializando mapa con permisos: $hasLocationPermission")
        } catch (e: Exception) {
            Log.e("GoogleMapScreen", "❌ Error en initialization", e)
        }
    }

    val santoDomingo = LatLng(18.4861, -69.9312)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(santoDomingo, 12f)
    }

    // --- Google Map (sin try/catch aquí porque está prohibido) ---
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = hasLocationPermission // <- Esto puede causar crash
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = true
        )
    ) {
        Log.i("GoogleMapScreen", "🗺 Dibujando marcador")

        Marker(
            state = MarkerState(position = santoDomingo),
            title = "Santo Domingo",
            snippet = "Capital RD"
        )
    }

    Log.i("GoogleMapScreen", "🟢 GoogleMap dibujado correctamente")
}
