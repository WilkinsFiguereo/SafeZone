package com.wilkins.safezone.frontend.ui.user.Screens.News.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
fun NewsListItem(
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
            interactionsRepo.hasUserLiked(newsId, EntityType.NEWS).onSuccess {
                liked = it
            }

            interactionsRepo.getLikesCount(newsId, EntityType.NEWS).onSuccess {
                likesCount = it
            }
        }
    }

    // Formatear fecha
    val formattedDate = remember(news.createdAt) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val now = Date()
            val date = parser.parse(news.createdAt ?: "")

            if (date != null) {
                val diff = now.time - date.time
                val hours = diff / (1000 * 60 * 60)
                val days = hours / 24

                when {
                    hours < 1 -> "Hace ${diff / (1000 * 60)} min"
                    hours < 24 -> "Hace $hours h"
                    days < 7 -> "Hace $days días"
                    else -> {
                        val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
                        formatter.format(date)
                    }
                }
            } else {
                "Fecha desconocida"
            }
        } catch (e: Exception) {
            "Fecha desconocida"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                Toast
                    .makeText(
                        context,
                        "Proximamente: Ver detalles completos de \"${news.title}\"",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con autor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = news.userId.firstOrNull()?.uppercase() ?: "A",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Administrador",
                        style = MaterialTheme.typography.bodyMedium,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Imagen miniatura
                if (news.imageUrl.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = news.imageUrl,
                        contentDescription = "Imagen miniatura",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop,
                        error = {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Error al cargar imagen",
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Sin imagen",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Contenido
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = news.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = news.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Barra de acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Likes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                                    isLoadingLike = false
                                }
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        enabled = !isLoadingLike
                    ) {
                        if (isLoadingLike) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Me gusta",
                                tint = if (liked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = "$likesCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Comentarios (placeholder)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comentarios",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Compartir
                IconButton(
                    onClick = {
                        shareNews(context, news)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
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