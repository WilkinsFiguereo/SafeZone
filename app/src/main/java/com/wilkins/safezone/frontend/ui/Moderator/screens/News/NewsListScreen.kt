package com.wilkins.safezone.frontend.ui.Moderator.screens.News

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.wilkins.safezone.backend.network.Moderator.News.News
import com.wilkins.safezone.backend.network.Moderator.News.NewsViewModel
import com.wilkins.safezone.navigation.theme.PrimaryColor

// Colores modernos
private val BackgroundGradient = listOf(
    Color(0xFFF8F9FA),
    Color(0xFFE9ECEF)
)
private val CardGradient = listOf(
    Color(0xFFFFFFFF),
    Color(0xFFF8F9FA)
)
private val AccentColor = Color(0xFF6C63FF)
private val DangerColor = Color(0xFFFF6B6B)
private val SuccessColor = Color(0xFF4ECDC4)

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(BackgroundGradient))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header moderno con gradiente
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(PrimaryColor, PrimaryColor.copy(alpha = 0.8f))
                            )
                        )
                        .padding(vertical = 20.dp, horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column {
                            Text(
                                "Gestión de Noticias",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "${newsList.size} noticias publicadas",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Contenido principal
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = AccentColor,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Cargando noticias...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                newsList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(
                                        AccentColor.copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Newspaper,
                                    contentDescription = null,
                                    tint = AccentColor,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "No hay noticias aún",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Las noticias publicadas aparecerán aquí",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(newsList) { news ->
                            ModernNewsCard(
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

        // Diálogo de edición mejorado
        if (showEditDialog && selectedNews != null) {
            ModernEditDialog(
                news = selectedNews!!,
                onDismiss = { showEditDialog = false },
                onConfirm = { updatedNews, newImageUri ->
                    viewModel.updateNews(
                        context = context,
                        newsId = updatedNews.id ?: "",
                        title = updatedNews.title,
                        description = updatedNews.description,
                        isImportant = updatedNews.isImportant,
                        newImageUri = newImageUri,
                        currentImageUrl = updatedNews.imageUrl,
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

        // Diálogo de eliminación moderno
        if (showDeleteDialog && selectedNews != null) {
            ModernDeleteDialog(
                newsTitle = selectedNews!!.title,
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
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
                }
            )
        }
    }
}

@Composable
fun ModernNewsCard(
    news: News,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column {
            // Imagen con overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = news.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradiente overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )

                // Badge de importante
                if (news.isImportant) {
                    Surface(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopEnd),
                        shape = RoundedCornerShape(20.dp),
                        color = DangerColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Importante",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Título sobre la imagen
                Text(
                    news.title,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Contenido de la card
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    news.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(16.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AccentColor
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            AccentColor
                        )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Editar")
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DangerColor
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            DangerColor
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}

@Composable
fun ModernEditDialog(
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
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header del diálogo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Editar Noticia",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Campo de título
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentColor,
                        focusedLabelColor = AccentColor
                    )
                )

                Spacer(Modifier.height(16.dp))

                // Campo de descripción
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentColor,
                        focusedLabelColor = AccentColor
                    )
                )

                Spacer(Modifier.height(16.dp))

                // Switch de importante
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8F9FA)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (isImportant) DangerColor else Color.Gray
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Marcar como importante",
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Switch(
                            checked = isImportant,
                            onCheckedChange = { isImportant = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = DangerColor
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Imagen preview
                AsyncImage(
                    model = selectedImageUri ?: news.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(12.dp))

                // Botón cambiar imagen
                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentColor
                    )
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cambiar imagen")
                }

                Spacer(Modifier.height(20.dp))

                // Botón guardar
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
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar cambios", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ModernDeleteDialog(
    newsTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(DangerColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = DangerColor,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "¿Eliminar noticia?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "¿Estás seguro de eliminar '$newsTitle'? Esta acción no se puede deshacer.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerColor
                        )
                    ) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}