package com.wilkins.safezone.frontend.ui.user.Screens.News

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wilkins.safezone.GenericUserUi.AdminMenu
import com.wilkins.safezone.GenericUserUi.BottomNavigationMenu
import com.wilkins.safezone.GenericUserUi.SideMenu
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.Moderator.News.NewsViewModel
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.auth.SessionManager
import com.wilkins.safezone.backend.network.auth.SessionManager.getUserProfile
import com.wilkins.safezone.frontend.ui.GlobalAssociation.GovernmentMenu
import com.wilkins.safezone.frontend.ui.Moderator.ModeratorSideMenu
import com.wilkins.safezone.frontend.ui.user.News.NewsFilter
import com.wilkins.safezone.frontend.ui.user.News.NewsSearchBar
import com.wilkins.safezone.frontend.ui.user.Screens.News.components.NewsCard
import com.wilkins.safezone.frontend.ui.user.Screens.News.components.NewsListItem
import com.wilkins.safezone.frontend.ui.user.Screens.profile.RoleBasedMenu
import io.github.jan.supabase.SupabaseClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    navController: NavController,
    userId: String,
    context: Context,
    supabaseClient: SupabaseClient,
    viewModel: NewsViewModel // ✅ Ya no usa = viewModel(), se pasa desde fuera
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(NewsFilter.ALL) }

    val userState = produceState<AppUser?>(initialValue = null) {
        value = getUserProfile(context)
    }

    val newsList by viewModel.newsList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val user = userState.value
    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""
    var isMenuOpen by remember { mutableStateOf(false) }

    // Cargar noticias al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadNews()
    }

    val featuredNews = remember(newsList) {
        newsList
            .filter { it.isImportant }
            .maxByOrNull { it.createdAt ?: "" }
    }

    val regularNews = remember(newsList) {
        newsList
            .filter { !it.isImportant }
            .sortedByDescending { it.createdAt ?: "" }
    }

    val filteredNews = remember(regularNews, searchQuery) {
        if (searchQuery.isEmpty()) {
            regularNews
        } else {
            regularNews.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val showFeaturedNews = remember(featuredNews, searchQuery) {
        featuredNews != null && (
                searchQuery.isEmpty() ||
                        featuredNews.title.contains(searchQuery, ignoreCase = true) ||
                        featuredNews.description.contains(searchQuery, ignoreCase = true)
                )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 72.dp, bottom = 100.dp)
            ) {
                item {
                    NewsSearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        selectedFilter = selectedFilter,
                        onFilterChange = { selectedFilter = it }
                    )
                }

                if (isLoading && newsList.isEmpty()) {
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

                if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (showFeaturedNews && featuredNews != null) {
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
                                        text = "⭐ DESTACADA",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            NewsCard(
                                news = featuredNews,
                                onCommentClick = {
                                    navController.navigate("news_detail/${featuredNews.id}")
                                }
                            )
                        }
                    }
                }

                if (filteredNews.isNotEmpty()) {
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
                }

                if (filteredNews.isEmpty() && !isLoading) {
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
                    items(filteredNews) { news ->
                        NewsListItem(
                            news = news,
                            onCommentClick = {
                                navController.navigate("news_detail/${news.id}")
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(2f)
        ) {
            BottomNavigationMenu(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                navController = navController,
                supabaseClient = supabaseClient
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(3f)
        ) {
            RoleBasedMenu(
                navController = navController,
                currentRoute = currentRoute,
                isMenuOpen = isMenuOpen,
                onMenuToggle = { isMenuOpen = it },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun RoleBasedMenu(
    navController: NavController,
    currentRoute: String,
    isMenuOpen: Boolean,
    onMenuToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val supabaseClient = SupabaseService.getInstance()

    // 🔥 Rol desde SessionManager (fuente oficial)
    val roleId = remember {
        SessionManager.getUserRole(context)
    }

    when (roleId) {

        // 🧍 USUARIO NORMAL
        1 -> {
            SideMenu(
                navController = navController,
                userId = SessionManager.loadSession(context)?.user?.id ?: "",
                userName = "Usuario",
                currentRoute = currentRoute,
                isMenuOpen = isMenuOpen,
                onMenuToggle = onMenuToggle,
                context = context,
                supabaseClient = supabaseClient,
                modifier = modifier
            )
        }

        // 👑 ADMIN
        2 -> {
            AdminMenu(
                navController = navController,
                currentRoute = currentRoute,
                isMenuOpen = isMenuOpen,
                onMenuToggle = onMenuToggle,
                modifier = modifier,
                content = content
            )
        }

        // 🛡️ MODERADOR
        3 -> {
            ModeratorSideMenu(
                navController = navController,
                moderatorId = SessionManager.loadSession(context)?.user?.id ?: "",
                moderatorName = "Moderador",
                currentRoute = currentRoute,
                modifier = modifier,
                isMenuOpen = isMenuOpen,
                onMenuToggle = onMenuToggle,
                context = context,
                supabaseClient = supabaseClient
            )
        }

        // 🏛️ GOBIERNO
        4 -> {
            GovernmentMenu(
                navController = navController,
                currentRoute = currentRoute,
                isMenuOpen = isMenuOpen,
                onMenuToggle = onMenuToggle,
                modifier = modifier,
                content = content
            )
        }

        // 🚨 SIN ROL
        else -> {
            LaunchedEffect(Unit) {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
}