package com.wilkins.safezone.frontend.ui.Moderator.News

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.wilkins.safezone.backend.network.Moderator.News
import com.wilkins.safezone.backend.network.Moderator.NewsViewModel
import com.wilkins.safezone.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsListScreen(
    onNavigateBack: () -> Unit,
    viewModel: NewsViewModel = viewModel()
) {
    val context = LocalContext.current
    val newsList by viewModel.newsList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedNews by remember { mutableStateOf<News?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadNews()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Noticias") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                newsList.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Newspaper,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No hay noticias disponibles", color = Color.Gray)
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(newsList) { news ->
                            NewsItemCard(
                                news = news,
                                onEdit = {
                                    selectedNews = news
                                    showEditDialog = true
                                },
                                onDelete = {
                                    selectedNews = news
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // ================= EDITAR =================
        if (showEditDialog && selectedNews != null) {
            EditNewsDialog(
                news = selectedNews!!,
                onDismiss = { showEditDialog = false },
                onConfirm = { updatedNews, newImageUri ->

                    viewModel.updateNews(
                        context = context,
                        newsId = updatedNews.id ?: "",
                        title = updatedNews.title,
                        description = updatedNews.description,
                        isImportant = updatedNews.isImportant,

                        // ✅ IMAGEN
                        newImageUri = newImageUri,
                        currentImageUrl = updatedNews.imageUrl,

                        // ✅ VIDEO (OBLIGATORIOS)
                        newVideoUri = null,
                        currentVideoUrl = updatedNews.videoUrl,

                        onSuccess = {
                            showEditDialog = false
                            selectedNews = null
                        },
                        onError = { }
                    )
                }
            )
        }

        // ================= ELIMINAR =================
        if (showDeleteDialog && selectedNews != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = {
                    Icon(Icons.Default.Warning, contentDescription = null)
                },
                title = { Text("Confirmar eliminación") },
                text = {
                    Text("¿Eliminar la noticia '${selectedNews!!.title}'?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteNews(
                                newsId = selectedNews!!.id ?: "",
                                imageUrl = selectedNews!!.imageUrl,
                                videoUrl = selectedNews!!.videoUrl,
                                onSuccess = {
                                    showDeleteDialog = false
                                    selectedNews = null
                                },
                                onError = {}
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun NewsItemCard(
    news: News,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(Modifier.padding(12.dp)) {

            AsyncImage(
                model = news.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    news.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    news.description,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Gray
                )
            }

            Column {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun EditNewsDialog(
    news: News,
    onDismiss: () -> Unit,
    onConfirm: (News, Uri?) -> Unit
) {
    var title by remember { mutableStateOf(news.title) }
    var description by remember { mutableStateOf(news.description) }
    var isImportant by remember { mutableStateOf(news.isImportant) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { selectedImageUri = it }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                Switch(
                    checked = isImportant,
                    onCheckedChange = { isImportant = it }
                )

                AsyncImage(
                    model = selectedImageUri ?: news.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                OutlinedButton(
                    onClick = { launcher.launch("image/*") }
                ) {
                    Text("Cambiar imagen")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        onConfirm(
                            news.copy(
                                title = title,
                                description = description,
                                isImportant = isImportant
                            ),
                            selectedImageUri
                        )
                    }
                ) {
                    Text( "Guardar")
                }
            }
        }
    }
}
