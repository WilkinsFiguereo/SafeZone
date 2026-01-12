package com.wilkins.safezone.com.wilkins.safezone.frontend.ui.user.News.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.wilkins.safezone.backend.network.SupabaseClient.client
import com.wilkins.safezone.frontend.ui.user.News.Comment
import com.wilkins.safezone.frontend.ui.user.News.News
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class SupabaseComment(
    val id: String? = null,
    val message: String,
    val news_id: String,
    val user_id: String,
    val author_name: String? = null,
    val created_at: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailDialog(
    news: News,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isFullScreen by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var commentsList by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var isAddingComment by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // CARGAR COMENTARIOS
    LaunchedEffect(news.id) {
        try {
            val response = client.from("comments")
                .select { filter { eq("news_id", news.id.toString()) } }
                .decodeList<SupabaseComment>()
            commentsList = response.map { it.toUiComment() }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Noticia Detallada") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                    },
                    actions = {
                        IconButton(onClick = { shareNewsInternal(context, news) }) {
                            Icon(Icons.Default.Share, null)
                        }
                    }
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Contenido Multimedia
                    if (!news.videoUrl.isNullOrBlank()) {
                        VideoPlayerBox(news.videoUrl) { isFullScreen = true }
                    } else if (news.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = news.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Text(
                        text = news.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = news.description, style = MaterialTheme.typography.bodyLarge)

                    HorizontalDivider()

                    Text(text = "Comentarios (${commentsList.size})", fontWeight = FontWeight.Bold)

                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        CommentSection(commentsList)
                    }
                }

                // --- SECCIÓN DE ENVÍO DE COMENTARIO ---
                Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Escribe un comentario...") },
                            enabled = !isAddingComment,
                            shape = RoundedCornerShape(12.dp)
                        )

                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    isAddingComment = true
                                    scope.launch {
                                        try {
                                            // Obtener usuario real de la sesión
                                            val user = client.auth.currentUserOrNull()

                                            if (user != null) {
                                                val newDbComment = SupabaseComment(
                                                    message = commentText.trim(),
                                                    news_id = news.id.toString(),
                                                    user_id = user.id,
                                                    author_name = user.userMetadata?.get("full_name")
                                                        ?.toString() ?: "Moderador"
                                                )

                                                // Insertar en Supabase
                                                client.from("comments").insert(newDbComment)

                                                // Actualizar UI local
                                                val uiComment = Comment(
                                                    id = System.currentTimeMillis().toInt(),
                                                    author = newDbComment.author_name ?: "Tú",
                                                    content = commentText,
                                                    timestamp = "Ahora",
                                                    likes = 0,
                                                    replies = emptyList()
                                                )
                                                commentsList = commentsList + uiComment
                                                commentText = ""

                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        "Comentario enviado",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        "Error: Sesión no encontrada",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Error al enviar: ${e.localizedMessage}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                            e.printStackTrace()
                                        } finally {
                                            isAddingComment = false
                                        }
                                    }
                                }
                            },
                            enabled = commentText.isNotBlank() && !isAddingComment
                        ) {
                            if (isAddingComment) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Send,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isFullScreen) {
        FullScreenVideo(news.videoUrl!!) { isFullScreen = false }
    }
}

// Mapeo de BD a UI
fun SupabaseComment.toUiComment(): Comment = Comment(
    id = this.id?.hashCode() ?: System.currentTimeMillis().toInt(),
    author = this.author_name ?: "Anónimo",
    content = this.message,
    timestamp = this.created_at?.take(10) ?: "Hoy",
    likes = 0,
    replies = emptyList()
)

@Composable
fun CommentSection(comments: List<Comment>) {
    if (comments.isEmpty()) {
        Text(
            "No hay comentarios aún.",
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            comments.forEach { comment ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(comment.author.firstOrNull()?.uppercase()?.toString() ?: "?")
                        }
                    }
                    Column {
                        Text(
                            comment.author,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(comment.content, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayerBox(url: String, onFullScreen: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
    ) {
        AndroidView(factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(Uri.parse(url))
                setMediaController(MediaController(ctx).apply { setAnchorView(this@apply) })
                setOnPreparedListener { start() }
            }
        }, modifier = Modifier.fillMaxSize())
        IconButton(onClick = onFullScreen, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Default.Fullscreen, null, tint = Color.White)
        }
    }
}

@Composable
fun FullScreenVideo(url: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)) {
            AndroidView(factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(Uri.parse(url))
                    setMediaController(MediaController(ctx))
                    setOnPreparedListener { start() }
                }
            }, modifier = Modifier.fillMaxSize())
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }
    }
}

private fun shareNewsInternal(context: Context, news: News) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "${news.title}\n${news.description}")
    }
    context.startActivity(Intent.createChooser(intent, "Compartir"))
}