package com.wilkins.safezone.frontend.Moderator.News

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.wilkins.safezone.backend.network.Moderator.News
import com.wilkins.safezone.backend.network.Moderator.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (News) -> Unit,
    viewModel: NewsViewModel = viewModel()
) {
    val newsList by viewModel.newsList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var newsToDelete by remember { mutableStateOf<News?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    // Cargar noticias al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadNews()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Noticias") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadNews() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Noticia")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                newsList.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay noticias publicadas",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Presiona el botón + para crear una",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(newsList) { news ->
                            NewsCard(
                                news = news,
                                onEdit = { onNavigateToEdit(news) },
                                onDelete = {
                                    newsToDelete = news
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // Mensaje de error si hay
            deleteError?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { deleteError = null }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }

        // Diálogo de confirmación de eliminación
        if (showDeleteDialog && newsToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    newsToDelete = null
                },
                icon = {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = {
                    Text("¿Eliminar noticia?")
                },
                text = {
                    Text("Esta acción no se puede deshacer. ¿Estás seguro de eliminar '${newsToDelete?.title}'?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            newsToDelete?.let { news ->
                                viewModel.deleteNews(
                                    newsId = news.id!!,
                                    imageUrl = news.imageUrl,
                                    onSuccess = {
                                        showDeleteDialog = false
                                        newsToDelete = null
                                        deleteError = null
                                    },
                                    onError = { error ->
                                        deleteError = error
                                        showDeleteDialog = false
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            newsToDelete = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun NewsCard(
    news: News,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Imagen
            Card(
                modifier = Modifier.size(100.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                AsyncImage(
                    model = news.imageUrl,
                    contentDescription = news.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Contenido
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = news.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (news.isImportant) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Importante",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = news.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Botones de acción
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}