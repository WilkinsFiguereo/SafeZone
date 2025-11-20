package com.wilkins.safezone.frontend.ui.user.Homepage

import SessionManager.getUserProfile
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.wilkins.safezone.GenericUserUi.BottomNavigationMenu
import com.wilkins.safezone.GenericUserUi.SideMenu
import com.wilkins.safezone.R
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.frontend.ui.user.Homepage.Components.NewsItem
import com.wilkins.safezone.frontend.ui.user.Homepage.Components.NewsSlider
import com.wilkins.safezone.frontend.ui.user.Homepage.Components.RecentReportsSection
import com.wilkins.safezone.frontend.ui.user.Homepage.Components.WelcomeBanner
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth

@Composable
fun UserHomeScreen(navController: NavController, context: Context, supabaseClient: SupabaseClient) {
    val supabase = SupabaseService.getInstance()
    val userId = supabase.auth.currentUserOrNull()?.id ?: ""
    val userState = produceState<AppUser?>(initialValue = null) {
        value = getUserProfile(context)
    }

    val user = userState.value
    val newsItems = listOf(
        NewsItem(
            title = "Nuevo programa de seguridad comunitaria",
            date = "12/10/2024",
            description = "Se implementa nuevo sistema de vigilancia en el sector norte de la ciudad.",
            imageRes = R.drawable.personas_recogiendo
        ),
        NewsItem(
            title = "Jornada de limpieza este sábado",
            date = "15/10/2024",
            description = "Participa en la jornada de limpieza comunitaria en el parque central.",
            imageRes = R.drawable.bandalismo
        ),
        NewsItem(
            title = "Mejoras en alumbrado público",
            date = "18/10/2024",
            description = "Instalación de nuevas luminarias en zonas estratégicas de la ciudad.",
            imageRes = R.drawable.baches
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(75.dp))
            Spacer(modifier = Modifier.height(12.dp))

            NewsSlider(
                newsItems = newsItems,
                onNewsClick = { }
            )

            Spacer(modifier = Modifier.height(15.dp))
            WelcomeBanner()
            Spacer(modifier = Modifier.height(16.dp))

            // 🗺️ Nueva sección: Reportes cercanos con mapa
            NearbyReportsMapSection()

            Spacer(modifier = Modifier.height(16.dp))
            RecentReportsSection()
            Spacer(modifier = Modifier.height(100.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            BottomNavigationMenu(
                onNewsClick = { },
                onAlertClick = { },
                onMyAlertsClick = { }
            )
        }

        SideMenu(
            navController = navController,
            modifier = Modifier.align(Alignment.TopCenter),
            userId = userId,
            userName = user?.name ?: "Usuario",
            context = context,
            supabaseClient = supabaseClient
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
fun NearbyReportsMapSection() {
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
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            Log.i("NearbyReportsMap", "📌 Resultado permiso: $granted")
            hasLocationPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            Log.i("NearbyReportsMap", "🚨 Solicitando permiso de ubicación")
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            // Título de la sección
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 Reportes cercanos a tu zona",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            }

            // Mapa de Google
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                GoogleMapWithReports(hasLocationPermission = hasLocationPermission)
            }

            // Información adicional
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🔴 5 reportes activos",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Ver todos →",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1976D2)
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun GoogleMapWithReports(hasLocationPermission: Boolean) {
    val santoDomingo = LatLng(18.4861, -69.9312)

    // Reportes de ejemplo cercanos
    val nearbyReports = listOf(
        LatLng(18.4900, -69.9400) to "Bache en vía principal",
        LatLng(18.4820, -69.9280) to "Alumbrado público dañado",
        LatLng(18.4880, -69.9350) to "Basura acumulada",
        LatLng(18.4840, -69.9320) to "Vandalismo en parque",
        LatLng(18.4910, -69.9310) to "Árbol caído"
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(santoDomingo, 13f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = hasLocationPermission
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            myLocationButtonEnabled = hasLocationPermission
        )
    ) {
        // Marcadores de reportes cercanos
        nearbyReports.forEach { (position, title) ->
            Marker(
                state = MarkerState(position = position),
                title = title,
                snippet = "Toca para ver detalles"
            )
        }
    }
}