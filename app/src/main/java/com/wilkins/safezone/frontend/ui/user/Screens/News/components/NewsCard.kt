package com.wilkins.safezone.frontend.ui.user.Screens.News.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.wilkins.safezone.backend.network.Moderator.News.News
import com.wilkins.safezone.backend.network.User.Interaction.EntityType
import com.wilkins.safezone.backend.network.User.Interaction.InteractionsRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NewsCard(
    news: News,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val interactionsRepo = remember { InteractionsRepository() }

    var liked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(0) }
    var isLoadingLike by remember { mutableStateOf(false) }

    // Cargar estado inicial de likes
    LaunchedEffect(news.id) {
        news.id?.let { newsId ->
            // Verificar si el usuario dio like
            interactionsRepo.hasUserLiked(newsId, EntityType.NEWS).onSuccess {
                liked = it
            }

            // Obtener conteo de likes
            interactionsRepo.getLikesCount(newsId, EntityType.NEWS).onSuccess {
                likesCount = it
            }
        }
    }

    // Formatear fecha
    val formattedDate = remember(news.createdAt) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val date = parser.parse(news.createdAt ?: "")
            date?.let { formatter.format(it) } ?: "Fecha desconocida"
        } catch (e: Exception) {
            "Fecha desconocida"
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con autor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = news.userId.firstOrNull()?.uppercase() ?: "A",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Administrador",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Imagen principal
            if (news.imageUrl.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = news.imageUrl,
                    contentDescription = "Imagen de noticia",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Error al cargar imagen",
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Sin imagen",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Título
            Text(
                text = news.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Descripción
            Text(
                text = news.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Barra de acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de like
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                news.id?.let { newsId ->
                                    isLoadingLike = true
                                    scope.launch {
                                        interactionsRepo.toggleLike(newsId, EntityType.NEWS)
                                            .onSuccess { nowLiked ->
                                                liked = nowLiked
                                                likesCount = if (nowLiked) likesCount + 1 else likesCount - 1
                                            }
                                            .onFailure {
                                                // Mostrar error si es necesario
                                            }
                                        isLoadingLike = false
                                    }
                                }
                            },
                            enabled = !isLoadingLike
                        ) {
                            if (isLoadingLike) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Me gusta",
                                    tint = if (liked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Text(
                            text = "$likesCount",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botón de compartir
                    IconButton(
                        onClick = {
                            shareNews(
                                context = context,
                                news = news
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun shareNews(context: Context, news: News) {
    val shareUtils = ShareUtils(context)
    shareUtils.shareNews(
        title = news.title,
        description = news.description,
        imageUrl = news.imageUrl,
        videoUrl = news.videoUrl
    )
}