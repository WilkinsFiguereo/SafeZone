package com.wilkins.safezone.frontend.ui.Map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

import java.util.Locale

@SuppressLint("MissingPermission")
@Composable
fun GoogleMapPicker(
    onLocationSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Log.i("GoogleMapPicker", "🟦 Composable cargado")

    val context = LocalContext.current

    // -------------------------------------------------------------
    // 🔵 PERMISOS
    // -------------------------------------------------------------
    var hasLocationPermission by remember {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        Log.i("GoogleMapPicker", "📍 Permiso actual: $granted")

        mutableStateOf(granted)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->

        Log.i("GoogleMapPicker", "📌 Resultado del permiso: $granted")

        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            Log.w("GoogleMapPicker", "🚨 Solicitando permiso...")
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            Log.i("GoogleMapPicker", "✔ Permiso ya otorgado")
        }
    }

    // -------------------------------------------------------------
    // 🔵 CONFIGURACIÓN INICIAL DEL MAPA
    // -------------------------------------------------------------
    val santoDomingo = LatLng(18.4861, -69.9312)

    val cameraPositionState = rememberCameraPositionState {
        Log.i("GoogleMapPicker", "📸 Inicializando cámara en Santo Domingo")
        position = CameraPosition.fromLatLngZoom(santoDomingo, 12f)
    }

    var selectedPosition by remember { mutableStateOf<LatLng?>(null) }

    // -------------------------------------------------------------
    // 🔵 UI PRINCIPAL (DIALOG)
    // -------------------------------------------------------------
    Dialog(onDismissRequest = {
        Log.i("GoogleMapPicker", "🔙 Cerrar modal map")
        onDismiss()
    }) {

        Log.i("GoogleMapPicker", "🟩 Dibujando tarjeta del mapa")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {

            Column(modifier = Modifier.fillMaxWidth()) {

                Text(
                    text = "Selecciona una ubicación",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )

                // -------------------------------------------------------------
                // 🔵 MAPA
                // -------------------------------------------------------------
                Box(Modifier.height(400.dp)) {

                    Log.i("GoogleMapPicker", "🗺 Renderizando GoogleMap...")

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = hasLocationPermission
                        ),
                        onMapClick = { latLng ->
                            Log.i("GoogleMapPicker", "📍 Mapa clicado: $latLng")
                            selectedPosition = latLng
                        }
                    ) {
                        selectedPosition?.let {

                            Log.i("GoogleMapPicker", "📌 Dibujando marcador en: $it")

                            Marker(
                                state = MarkerState(position = it),
                                title = "Ubicación seleccionada"
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // 🔵 BOTONES
                // -------------------------------------------------------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        Log.i("GoogleMapPicker", "⚪ Cancelado por el usuario")
                        onDismiss()
                    }) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (selectedPosition != null) {
                                Log.i("GoogleMapPicker", "🟢 Selección final: $selectedPosition")

                                val address = getAddressFromLatLng(
                                    context,
                                    selectedPosition!!.latitude,
                                    selectedPosition!!.longitude
                                )

                                Log.i("GoogleMapPicker", "📍 Dirección obtenida: $address")

                                onLocationSelected(address)
                                onDismiss()
                            } else {
                                Log.w("GoogleMapPicker", "⚠ No se seleccionó ubicación")
                            }
                        }
                    ) {
                        Text("Seleccionar")
                    }
                }
            }
        }
    }
}


// -------------------------------------------------------------
// 🔵 GEOCODER
// -------------------------------------------------------------
fun getAddressFromLatLng(context: Context, lat: Double, lng: Double): String {
    return try {
        Log.i("GoogleMapPicker", "📡 Buscando dirección con Geocoder ($lat, $lng)")

        val geocoder = Geocoder(context, Locale.getDefault())
        val result = geocoder.getFromLocation(lat, lng, 1)

        val address = result?.firstOrNull()?.getAddressLine(0)
        Log.i("GoogleMapPicker", "🟢 Dirección encontrada: $address")

        address ?: "Dirección desconocida"

    } catch (e: Exception) {
        Log.e("GoogleMapPicker", "❌ Error en geocoder: ${e.message}")
        "Dirección desconocida"
    }
}
