package com.wilkins.safezone.frontend.ui.Map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
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
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val santoDomingo = LatLng(18.4861, -69.9312)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(santoDomingo, 12f)
    }

    var selectedPosition by remember { mutableStateOf<LatLng?>(null) }

    Dialog(onDismissRequest = onDismiss) {
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

                Box(Modifier.height(400.dp)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = hasLocationPermission
                        ),
                        onMapClick = { latLng ->
                            selectedPosition = latLng
                        }
                    ) {
                        selectedPosition?.let {
                            Marker(
                                state = MarkerState(position = it),
                                title = "Ubicación seleccionada"
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (selectedPosition != null) {
                                val address = getAddressFromLatLng(
                                    context,
                                    selectedPosition!!.latitude,
                                    selectedPosition!!.longitude
                                )
                                onLocationSelected(address)
                                onDismiss()
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


fun getAddressFromLatLng(context: Context, lat: Double, lng: Double): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val result = geocoder.getFromLocation(lat, lng, 1)

        result?.firstOrNull()?.getAddressLine(0) ?: "Dirección desconocida"

    } catch (e: Exception) {
        "Dirección desconocida"
    }
}
