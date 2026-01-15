package com.wilkins.safezone.frontend.ui.user.News.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
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

@Serializable
data class SupabaseComment(
    val id: String? = null,
    val message: String,
    val news_id: String,
    val user_id: String,
    val author_name: String? = null
)

// Data class para obtener el UUID real desde la base de datos
@Serializable
data class NewsIdMapping(
    val id: String  // El UUID real de la base de datos
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

    var commentText by remember { mutableStateOf("") }
    var commentsList by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var isAddingComment by remember { mutableStateOf(false) }
    var isFullScreen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var realNewsUuid by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Obtener el UUID real de la noticia desde la base de datos
    LaunchedEffect(news.id) {
        try {
            // Buscar la noticia por título (o cualquier otro campo único que tengas)
            val newsResult = client.from("news")
                .select {
                    filter {
                        eq("title", news.title)
                    }
                }
                .decodeSingleOrNull<NewsIdMapping>()

            if (newsResult != null) {
                realNewsUuid = newsResult.id
                println("DEBUG: UUID real encontrado = ${realNewsUuid}")

                // Ahora cargar los comentarios con el UUID correcto
                val response = client.from("comments")
                    .select {
                        filter { eq("news_id", realNewsUuid!!) }
                    }
                    .decodeList<SupabaseComment>()

                commentsList = response.map { db ->
                    Comment(
                        id = db.id?.hashCode() ?: 0,
                        author = db.author_name ?: "Anónimo",
                        content = db.message,
                        timestamp = "Reciente",
                        likes = 0,
                        replies = emptyList()
                    )
                }
            } else {
                errorMessage = "No se encontró la noticia en la base de datos"
            }
        } catch (e: Exception) {
            errorMessage = "Error al cargar la noticia: ${e.message}"
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
                    title = { Text("Detalle de Noticia") },
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

                    Text(text = news.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(text = news.description, style = MaterialTheme.typography.bodyLarge)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(text = "Comentarios (${commentsList.size})", fontWeight = FontWeight.Bold)

                    if (isLoading) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        CommentListUI(commentsList)
                    }
                }

                // BARRA DE COMENTARIOS
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
                            enabled = !isAddingComment && realNewsUuid != null,
                            shape = RoundedCornerShape(12.dp)
                        )

                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank() && realNewsUuid != null) {
                                    isAddingComment = true
                                    scope.launch {
                                        try {
                                            val user = client.auth.currentUserOrNull()
                                            if (user == null) {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        "Error: Debes iniciar sesión",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                                return@launch
                                            }

                                            val dbComment = SupabaseComment(
                                                message = commentText.trim(),
                                                news_id = realNewsUuid!!,  // Usar el UUID real
                                                user_id = user.id,
                                                author_name = user.userMetadata?.get("full_name")?.toString()
                                                    ?: user.userMetadata?.get("name")?.toString()
                                                    ?: "Usuario"
                                            )

                                            client.from("comments").insert(dbComment)

                                            val newUi = Comment(
                                                id = System.currentTimeMillis().toInt(),
                                                author = dbComment.author_name ?: "Tú",
                                                content = commentText,
                                                timestamp = "Ahora",
                                                likes = 0,
                                                replies = emptyList()
                                            )
                                            commentsList = commentsList + newUi
                                            commentText = ""
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Comentario enviado", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Error al enviar: ${e.message}",
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
                            enabled = commentText.isNotBlank() && !isAddingComment && realNewsUuid != null
                        ) {
                            if (isAddingComment) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Send, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (isFullScreen && !news.videoUrl.isNullOrBlank()) {
        FullScreenVideoDialog(news.videoUrl) { isFullScreen = false }
    }
}

@Composable
fun CommentListUI(comments: List<Comment>) {
    if (comments.isEmpty()) {
        Text("No hay comentarios aún.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
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
                        Text(comment.author, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Text(comment.content, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayerBox(url: String, onFullScreen: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(240.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Color.Black)
    ) {
        AndroidView(factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(Uri.parse(url))
                val mc = MediaController(ctx)
                mc.setAnchorView(this)
                setMediaController(mc)
                setOnPreparedListener { start() }
            }
        }, modifier = Modifier.fillMaxSize())

        IconButton(
            onClick = onFullScreen,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) {
            Icon(Icons.Default.Fullscreen, null, tint = Color.White)
        }
    }
}

@Composable
fun FullScreenVideoDialog(url: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(Uri.parse(url))
                    val mc = MediaController(ctx)
                    setMediaController(mc)
                    setOnPreparedListener { start() }
                }
            }, modifier = Modifier.fillMaxSize())

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }
    }
}

private fun shareNewsInternal(context: Context, news: News) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "${news.title}\n\n${news.description}")
    }
    context.startActivity(Intent.createChooser(intent, "Compartir noticia"))
}