package com.wilkins.safezone.frontend.ui.user.News.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.wilkins.safezone.frontend.ui.user.News.News
import com.wilkins.safezone.frontend.ui.user.News.Comment

@Composable
fun NewsCard(
    news: News,
    modifier: Modifier = Modifier
) {
    var liked by remember { mutableStateOf(false) }
    var localLikes by remember { mutableStateOf(news.likes) }
    var showDialog by remember { mutableStateOf(false) }

    // Estados para comentarios en la tarjeta
    var commentText by remember { mutableStateOf("") }
    var commentsList by remember { mutableStateOf(news.comments) }

    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ===================== HEADER =====================
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = news.author.first().uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Column {
                    Text(text = news.author, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(text = news.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            // ===================== MULTIMEDIA =====================
            if (!news.videoUrl.isNullOrBlank()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black)) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(Uri.parse(news.videoUrl))
                                setMediaController(MediaController(ctx).apply { setAnchorView(this@apply) })
                                setOnPreparedListener { start() }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else if (news.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = news.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // ===================== TEXTO =====================
            Text(text = news.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = news.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3)

            // ===================== ACCIONES =====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        liked = !liked
                        localLikes = if (liked) localLikes + 1 else localLikes - 1
                    }) {
                        Icon(
                            imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (liked) Color.Red else Color.Gray
                        )
                    }
                    Text(text = "$localLikes", fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = { shareNewsInternal(context, news) }) {
                    Icon(Icons.Default.Share, contentDescription = "Compartir", tint = Color.Gray)
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

            // ===================== SECCIÓN DE COMENTARIOS RAPIDOS =====================
            CommentSection(comments = commentsList)

            // Caja para escribir comentario
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Añadir comentario...", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                )
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            val newComment = Comment(
                                id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                                author = "Yo",
                                content = commentText,
                                timestamp = "Ahora",
                                likes = 0,
                                replies = emptyList()
                            )
                            commentsList = commentsList + newComment
                            commentText = ""
                        }
                    },
                    enabled = commentText.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    // Dialog de detalle
    if (showDialog) {
        NewsDetailDialog(news = news, onDismiss = { showDialog = false })
    }
}

// Cambiamos el nombre o la hacemos privada para evitar el error de "Ambiguity"
private fun shareNewsInternal(context: Context, news: News) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Mira esta noticia: ${news.title}\n${news.description}")
    }
    context.startActivity(Intent.createChooser(intent, "Compartir vía"))
}
