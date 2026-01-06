package com.wilkins.safezone.frontend.ui.user.Screens.Homepage

import SessionManager.getUserProfile
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wilkins.safezone.GenericUserUi.BottomNavigationMenu
import com.wilkins.safezone.GenericUserUi.SideMenu
import com.wilkins.safezone.R
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.frontend.ui.Map.GoogleMapScreen
import com.wilkins.safezone.frontend.ui.Map.MapConfig
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

            // 🗺️ Sección: Reportes cercanos con mapa
            NearbyReportsMapCard(
                onViewAllClick = {
                    // Navegar a la pantalla de mapa completo
                    navController.navigate("MapReports")
                }
            )

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

/**
 * Card que contiene el mapa de reportes cercanos
 */
@Composable
fun NearbyReportsMapCard(
    onViewAllClick: () -> Unit = {}
) {
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

            // Mapa integrado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                GoogleMapScreen(
                    modifier = Modifier.fillMaxSize(),
                    config = MapConfig(
                        showUserLocation = true,
                        showAllReports = false,
                        maxDistanceKm = 10f,
                        initialZoom = 13f,
                        showDefaultMarker = false
                    ),
                    onReportClick = { report ->
                        // Manejar click en marcador
                        // Puedes navegar a detalles del reporte o mostrar un dialog
                        println("Click en reporte: ${report.description}")
                    }
                )
            }

            // Botón para ver todos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Ver mapa completo →",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1976D2),
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { onViewAllClick() }
                )

            }
        }
    }
}