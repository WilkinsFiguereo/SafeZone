package com.wilkins.safezone.frontend.ui.user.News

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.wilkins.safezone.GenericUserUi.BottomNavigationMenu
import com.wilkins.safezone.GenericUserUi.SideMenu
import com.wilkins.safezone.frontend.ui.user.News.components.NewsCard
import com.wilkins.safezone.frontend.ui.user.News.components.NewsListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    navController: NavController,
    userId: String
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(NewsFilter.ALL) }

    // Filtrar noticias según la búsqueda y categoría
    val filteredLatestNews = remember(searchQuery, selectedFilter) {
        var news = NewsData.latestNews

        // Filtrar por categoría
        if (selectedFilter != NewsFilter.ALL) {
            news = news.filter { it.category == selectedFilter.displayName }
        }

        // Filtrar por búsqueda
        if (searchQuery.isNotEmpty()) {
            news = news.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true) ||
                        it.author.contains(searchQuery, ignoreCase = true)
            }
        }

        news
    }

    val showFeaturedNews = (searchQuery.isEmpty() ||
            NewsData.featuredNews.title.contains(searchQuery, ignoreCase = true) ||
            NewsData.featuredNews.description.contains(searchQuery, ignoreCase = true) ||
            NewsData.featuredNews.author.contains(searchQuery, ignoreCase = true)) &&
            (selectedFilter == NewsFilter.ALL || NewsData.featuredNews.category == selectedFilter.displayName)

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
                // Buscador
                item {
                    NewsSearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        selectedFilter = selectedFilter,
                        onFilterChange = { selectedFilter = it }
                    )
                }

                // Noticia más relevante
                if (showFeaturedNews) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "⭐ DESTACADA",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            NewsCard(news = NewsData.featuredNews)
                        }
                    }
                }

                // Últimas noticias
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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

                if (filteredLatestNews.isEmpty()) {
                    item {
                        Text(
                            text = "No se encontraron noticias",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(filteredLatestNews) { news ->
                        NewsListItem(
                            news = news,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        // Bottom Navigation Menu (encima del contenido)
        Box(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
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

        // Side Menu (encima de todo, incluso del bottom menu)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(3f)
        ) {
            SideMenu(
                navController = navController,
                userId = userId
            )
        }
    }
}