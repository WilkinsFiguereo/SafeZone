package com.wilkins.safezone.frontend.ui.user.News

import SessionManager.getUserProfile
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wilkins.safezone.GenericUserUi.BottomNavigationMenu
import com.wilkins.safezone.GenericUserUi.SideMenu
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.Moderator.NewsViewModel
import com.wilkins.safezone.frontend.ui.user.News.components.NewsCard
import com.wilkins.safezone.frontend.ui.user.News.components.NewsListItem
import io.github.jan.supabase.SupabaseClient
import com.wilkins.safezone.backend.network.Moderator.News as SupabaseNews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    navController: NavController,
    userId: String,
    context: Context,
    supabaseClient: SupabaseClient,
    viewModel: NewsViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(NewsFilter.ALL) }

    // 🔥 Estados del ViewModel
    val supabaseNewsList by viewModel.newsList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val userState = produceState<AppUser?>(initialValue = null) {
        value = getUserProfile(context)
    }
    val user = userState.value

    // 🔥 Cargar noticias al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadNews()
    }

    // 🔥 Convertir noticias de Supabase al formato UI
    val convertedNews = remember(supabaseNewsList) {
        supabaseNewsList.map { convertSupabaseNewsToUINews(it) }
    }

    // 🔥 Separar noticias destacadas de las normales
    val featuredNews = remember(convertedNews) {
        convertedNews.filter { it.category == "Destacada" }
    }

    val regularNews = remember(convertedNews) {
        convertedNews.filter { it.category != "Destacada" }
    }

    // 🔥 Filtrar noticias según búsqueda
    val filteredRegularNews = remember(regularNews, searchQuery) {
        if (searchQuery.isEmpty()) {
            regularNews
        } else {
            regularNews.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val filteredFeaturedNews = remember(featuredNews, searchQuery) {
        if (searchQuery.isEmpty()) {
            featuredNews
        } else {
            featuredNews.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Contenido principal
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 72.dp, bottom = 100.dp)
            ) {
                // Buscador y botón de refresh
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        NewsSearchBar(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            selectedFilter = selectedFilter,
                            onFilterChange = { selectedFilter = it }
                        )

                        // Botón de refresh
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${convertedNews.size} noticias disponibles",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            IconButton(
                                onClick = { viewModel.loadNews() },
                                enabled = !isLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Actualizar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // 🔥 Indicador de carga
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                // 🔥 Noticias destacadas
                if (!isLoading && filteredFeaturedNews.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "⭐ DESTACADAS",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(filteredFeaturedNews) { news ->
                        NewsCard(
                            news = news,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // 🔥 Últimas noticias
                if (!isLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "📰 ÚLTIMAS NOTICIAS",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    if (filteredRegularNews.isEmpty() && !isLoading) {
                        item {
                            Text(
                                text = if (searchQuery.isEmpty()) "No hay noticias disponibles" else "No se encontraron noticias",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(filteredRegularNews) { news ->
                            NewsListItem(
                                news = news,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Navigation Menu
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(2f)
        ) {
            BottomNavigationMenu(
                selectedItem = 0,
                onNewsClick = {
                    navController.navigate("NewsUser") {
                        launchSingleTop = true
                    }
                },
                onAlertClick = {
                    navController.navigate("AlertUser") {
                        launchSingleTop = true
                    }
                },
                onMyAlertsClick = {
                    navController.navigate("MyAlertsUser") {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Side Menu
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(3f)
        ) {
            SideMenu(
                navController = navController,
                userId = userId,
                userName = user?.name ?: "Usuario",
                context = context,
                supabaseClient = supabaseClient
            )
        }
    }
}

// 🔥 Función para convertir News de Supabase al formato UI (News del NewsData.kt)
private fun convertSupabaseNewsToUINews(supabaseNews: SupabaseNews): News {
    return News(
        id = supabaseNews.id?.hashCode() ?: 0, // Convertir UUID string a Int
        title = supabaseNews.title,
        description = supabaseNews.description,
        imageUrl = supabaseNews.imageUrl,
        date = formatTimestamp(supabaseNews.createdAt),
        author = "SafeZone Moderador", // Puedes mejorarlo obteniendo el nombre real del usuario
        authorAvatar = "", // Vacío por ahora
        likes = (0..500).random(), // Likes aleatorios por ahora
        comments = emptyList(), // Sin comentarios por ahora
        category = if (supabaseNews.isImportant) "Destacada" else "General"
    )
}

// 🔥 Formatear timestamp
private fun formatTimestamp(timestamp: String?): String {
    if (timestamp == null) return "Hace un momento"

    return try {
        // Formato: 2024-01-15T10:30:00Z -> "Hace X horas/días"
        val dateTime = timestamp.substringBefore("T")
        val parts = dateTime.split("-")
        if (parts.size == 3) {
            val day = parts[2].toIntOrNull() ?: 1
            val currentDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
            val diff = currentDay - day

            when {
                diff == 0 -> "Hoy"
                diff == 1 -> "Ayer"
                diff < 7 -> "Hace $diff días"
                else -> dateTime
            }
        } else {
            "Hace un momento"
        }
    } catch (e: Exception) {
        "Hace un momento"
    }
}