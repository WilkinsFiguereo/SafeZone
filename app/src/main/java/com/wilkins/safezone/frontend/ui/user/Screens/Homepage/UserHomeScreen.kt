package com.wilkins.safezone.frontend.ui.user.Screens.Homepage

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportDto
import com.wilkins.safezone.backend.network.Moderator.News.News
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.auth.SessionManager.getUserProfile
import com.wilkins.safezone.frontend.ui.Map.GoogleMapScreen
import com.wilkins.safezone.frontend.ui.Map.MapConfig
import com.wilkins.safezone.frontend.ui.user.Screens.Homepage.Components.NewsItem
import com.wilkins.safezone.frontend.ui.user.Screens.Homepage.Components.NewsSlider
import com.wilkins.safezone.frontend.ui.user.Screens.Homepage.Components.RecentReportsSection
import com.wilkins.safezone.frontend.ui.user.Screens.Homepage.Components.Report
import com.wilkins.safezone.frontend.ui.user.Screens.Homepage.Components.ReportType
import com.wilkins.safezone.frontend.ui.user.Screens.Homepage.Components.WelcomeBanner
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "UserHomeScreen"

@Composable
fun UserHomeScreen(navController: NavController, context: Context, supabaseClient: SupabaseClient) {
    val supabase = SupabaseService.getInstance()
    val userId = supabase.auth.currentUserOrNull()?.id ?: ""

    val userState = produceState<AppUser?>(initialValue = null) {
        value = getUserProfile(context)
    }

    // Estado para noticias
    var newsItems by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var isLoadingNews by remember { mutableStateOf(true) }

    // Estado para reportes
    var recentReports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isLoadingReports by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    // Cargar noticias al iniciar
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoadingNews = true
                Log.d(TAG, "📰 Cargando noticias...")

                val newsData = supabase.from("news")
                    .select()
                    .decodeList<News>()

                Log.d(TAG, "📊 Total noticias en BD: ${newsData.size}")

                // Filtrar noticias sin video y ordenar por más recientes
                val filteredNews = newsData
                    .filter { news ->
                        // Solo noticias sin video o con video_url vacío
                        news.videoUrl.isNullOrBlank()
                    }
                    .sortedByDescending { it.createdAt }
                    .take(3)

                Log.d(TAG, "✅ Noticias filtradas (sin video): ${filteredNews.size}")

                newsItems = filteredNews.map { news ->
                    NewsItem(
                        title = news.title,
                        date = formatDate(news.createdAt ?: ""),
                        description = news.description,
                        imageUrl = news.imageUrl.takeIf { it.isNotBlank() }, // URL desde Supabase
                        imageRes = R.drawable.personas_recogiendo // Fallback si no hay URL
                    )
                }

                Log.d(TAG, "✅ NewsItems creados: ${newsItems.size}")
                newsItems.forEachIndexed { index, item ->
                    Log.d(TAG, "  Noticia $index: ${item.title}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error cargando noticias: ${e.message}", e)
                e.printStackTrace()
            } finally {
                isLoadingNews = false
            }
        }
    }

    // Cargar reportes recientes (solo estados 1 y 2: Pendiente y En Proceso)
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoadingReports = true
                Log.d(TAG, "📋 Cargando reportes...")

                val reportsData = supabase.from("reports")
                    .select()
                    .decodeList<ReportDto>()

                Log.d(TAG, "📊 Total reportes en BD: ${reportsData.size}")

                // Filtrar solo reportes con estado 1 (Pendiente) o 2 (En Proceso)
                // Excluir estados: 3 (Resuelto), 4 (Cancelado), 5 (Rechazado)
                val filteredReports = reportsData
                    .filter { report ->
                        report.idReportingStatus == 1 || report.idReportingStatus == 2
                    }
                    .sortedByDescending { it.createdAt }
                    .take(3)

                Log.d(TAG, "✅ Reportes filtrados (estado 1 o 2): ${filteredReports.size}")

                recentReports = filteredReports.mapIndexed { index, report ->
                    Log.d(TAG, "  Reporte $index: ${report.description} - Estado: ${report.idReportingStatus}")
                    Report(
                        id = report.id.toIntOrNull() ?: index,
                        type = mapReportType(report.idAffair),
                        description = report.description ?: "Sin descripción",
                        timeAgo = calculateTimeAgo(report.createdAt),
                        distance = "Calculando..."
                    )
                }

                Log.d(TAG, "✅ Reports UI creados: ${recentReports.size}")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error cargando reportes: ${e.message}", e)
                e.printStackTrace()
            } finally {
                isLoadingReports = false
            }
        }
    }

    val user = userState.value

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(75.dp))
            Spacer(modifier = Modifier.height(12.dp))

            // News Slider con loading
            if (isLoadingNews) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (newsItems.isNotEmpty()) {
                NewsSlider(
                    newsItems = newsItems,
                    onNewsClick = { }
                )
            } else {
                // Mensaje cuando no hay noticias
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay noticias disponibles",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))
            WelcomeBanner()
            Spacer(modifier = Modifier.height(16.dp))

            // 🗺️ Sección: Reportes cercanos con mapa
            NearbyReportsMapCard(
                onViewAllClick = {
                    navController.navigate("MapReports")
                },
                navController
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Reports con loading
            if (isLoadingReports) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                RecentReportsSection(
                    reports = recentReports,
                    onSeeAllClick = {
                        navController.navigate("MapReports")
                    }
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            BottomNavigationMenu(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                navController = navController,
                supabaseClient = supabaseClient
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
    onViewAllClick: () -> Unit = {},
    navController: NavController
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
                        println("Click en reporte: ${report.description}")
                    },
                    navController = navController
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

// Funciones auxiliares
private fun formatDate(dateString: String): String {
    if (dateString.isBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        Log.e(TAG, "Error formateando fecha: $dateString", e)
        dateString.substringBefore("T")
    }
}

private fun calculateTimeAgo(dateString: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = format.parse(dateString)
        val now = Date()
        val diff = now.time - (date?.time ?: 0)

        val minutes = diff / (60 * 1000)
        val hours = diff / (60 * 60 * 1000)
        val days = diff / (24 * 60 * 60 * 1000)

        when {
            minutes < 1 -> "Ahora"
            minutes < 60 -> "$minutes min"
            hours < 24 -> "$hours h"
            days < 7 -> "$days días"
            else -> "${days / 7} sem"
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error calculando tiempo: $dateString", e)
        "Hace poco"
    }
}

private fun mapReportType(affairId: Int?): ReportType {
    return when (affairId) {
        1 -> ReportType.EMERGENCY    // Emergencia
        2 -> ReportType.THEFT        // Robo
        3 -> ReportType.ACCIDENT     // Accidente
        4 -> ReportType.SUSPICIOUS   // Actividad sospechosa
        else -> ReportType.SUSPICIOUS
    }
}