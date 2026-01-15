package com.wilkins.safezone.frontend.ui.user.Screens.News

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.Moderator.News.Comment
import com.wilkins.safezone.backend.network.Moderator.News.NewsViewModel
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.User.ReportSystem.ReportService
import com.wilkins.safezone.backend.network.User.ReportSystem.ReportType
import com.wilkins.safezone.backend.network.auth.SessionManager
import com.wilkins.safezone.frontend.ui.user.Screens.News.components.NewsCard
import com.wilkins.safezone.frontend.ui.user.Screens.News.components.ReportCommentDialog
import com.wilkins.safezone.navigation.theme.PrimaryColor
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    navController: NavController,
    newsId: String,
    viewModel: NewsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val newsList by viewModel.newsList.collectAsState()
    val news = remember(newsList) { newsList.find { it.id == newsId } }

    val commentsMap by viewModel.commentsMap.collectAsState()
    val comments = commentsMap[newsId] ?: emptyList()
    val isLoadingComments by viewModel.isLoadingComments.collectAsState()

    var commentText by remember { mutableStateOf("") }
    var isPostingComment by remember { mutableStateOf(false) }

    val currentUserId = remember {
        SupabaseService.getInstance().auth.currentUserOrNull()?.id ?: ""
    }

    var userProfile by remember { mutableStateOf<AppUser?>(null) }

    // Cargar perfil
    LaunchedEffect(Unit) {
        userProfile = SessionManager.getUserProfile(context)
    }

    // Cargar noticias si está vacío
    LaunchedEffect(Unit) {
        Log.d("NewsDetail", "🔍 Verificando noticias... Total: ${newsList.size}")
        if (newsList.isEmpty()) {
            Log.d("NewsDetail", "📥 Lista vacía, cargando noticias...")
            viewModel.loadNews()
        } else {
            Log.d("NewsDetail", "✅ Noticias ya cargadas: ${newsList.size}")
        }
    }

    // Cargar comentarios cuando la noticia esté lista
    LaunchedEffect(newsId, news) {
        if (news != null) {
            Log.d("NewsDetail", "📥 Cargando comentarios para noticia: $newsId")
            viewModel.loadCommentsForNews(newsId)
        } else {
            Log.w("NewsDetail", "⚠️ Noticia no encontrada con ID: $newsId")
        }
    }

    // DEBUG: Ver cambios en comentarios
    LaunchedEffect(comments.size) {
        Log.d("NewsDetail", "🔄 Comentarios actualizados: ${comments.size}")
        if (comments.isNotEmpty()) {
            comments.forEach { c ->
                Log.d("NewsDetail", "  💬 ${c.authorName}: ${c.message.take(30)}...")
            }
        } else {
            Log.d("NewsDetail", "❌ No hay comentarios para mostrar")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Noticia") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            CommentInputBar(
                commentText = commentText,
                onCommentChange = { commentText = it },
                onSendClick = {
                    if (commentText.isNotBlank() && !isPostingComment && currentUserId.isNotBlank()) {
                        isPostingComment = true

                        viewModel.postComment(
                            newsId = newsId,
                            message = commentText,
                            userId = currentUserId,
                            onSuccess = {
                                isPostingComment = false
                                commentText = ""
                                viewModel.loadCommentsForNews(newsId)

                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "✅ Comentario publicado",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onError = { error ->
                                isPostingComment = false

                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "❌ Error: $error",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    } else if (currentUserId.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "❌ Debes iniciar sesión para comentar",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                isPosting = isPostingComment,
                userPhotoUrl = userProfile?.photoProfile
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mostrar la noticia
            if (news != null) {
                item {
                    NewsCard(
                        news = news,
                        onCommentClick = { /* Ya estamos en el detalle */ }
                    )
                }
            }

            // Header de comentarios
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💬 Comentarios",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${comments.size}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            }

            // Loading
            if (isLoadingComments) {
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

            // Lista de comentarios
            if (comments.isEmpty() && !isLoadingComments) {
                item {
                    EmptyCommentsMessage()
                }
            } else {
                items(comments, key = { it.id }) { comment ->
                    CommentItem(
                        comment = comment,
                        navController = navController,
                        onDeleteClick = {
                            scope.launch {
                                viewModel.deleteComment(comment.id)
                                viewModel.loadCommentsForNews(newsId)
                            }
                        },
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
fun CommentInputBar(
    commentText: String,
    onCommentChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isPosting: Boolean,
    userPhotoUrl: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (userPhotoUrl != null) {
                    AsyncImage(
                        model = userPhotoUrl,
                        contentDescription = "Tu foto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un comentario...") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                ),
                maxLines = 3,
                enabled = !isPosting
            )

            IconButton(
                onClick = onSendClick,
                enabled = commentText.isNotBlank() && !isPosting,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (commentText.isNotBlank() && !isPosting) PrimaryColor
                        else Color.Gray.copy(alpha = 0.3f)
                    )
            ) {
                if (isPosting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    navController: NavController,
    onDeleteClick: () -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reportTypes by remember { mutableStateOf<List<ReportType>>(emptyList()) }
    var isLoadingAction by remember { mutableStateOf(false) }

    val reportService = remember { ReportService() }
    val currentUserRoleId = remember { SessionManager.getUserRole(context) }
    val isModerator = currentUserRoleId in 2..4

    val currentUserId = remember {
        SupabaseService.getInstance().auth.currentUserOrNull()?.id ?: ""
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            navController.navigate("profile/${comment.userId}")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (comment.displayPhotoUrl != null) {
                        AsyncImage(
                            model = comment.displayPhotoUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = comment.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor,
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("profile/${comment.userId}")
                                }
                                .weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = formatDate(comment.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = comment.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones",
                            tint = Color.Gray
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Reportar comentario") },
                            onClick = {
                                showMenu = false
                                scope.launch {
                                    val result = reportService.getAllReportTypes()
                                    result.onSuccess { types ->
                                        reportTypes = types
                                        showReportDialog = true
                                    }
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Flag, null, tint = Color(0xFFE53935))
                            }
                        )

                        if (isModerator) {
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Eliminar comentario") },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Delete, null, tint = Color(0xFFD32F2F))
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        ReportCommentDialog(
            commentAuthor = comment.displayName,
            reportTypes = reportTypes,
            isLoading = isLoadingAction,
            onDismiss = { showReportDialog = false },
            onSubmit = { reportTypeId, message ->
                scope.launch {
                    isLoadingAction = true
                    val result = reportService.createCommentReport(
                        reporterId = currentUserId,
                        commentId = comment.id,
                        reportTypeId = reportTypeId,
                        message = message
                    )
                    isLoadingAction = false
                    result.onSuccess {
                        showReportDialog = false
                    }
                }
            }
        )
    }

    if (showDeleteDialog) {
        DeleteCommentDialog(
            commentAuthor = comment.displayName,
            isLoading = isLoadingAction,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                scope.launch {
                    isLoadingAction = true
                    onDeleteClick()
                    isLoadingAction = false
                    showDeleteDialog = false
                }
            }
        )
    }
}

@Composable
fun EmptyCommentsMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.ChatBubbleOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray.copy(alpha = 0.4f)
        )
        Text(
            text = "No hay comentarios aún",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        Text(
            text = "¡Sé el primero en comentar!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun DeleteCommentDialog(
    commentAuthor: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Eliminar comentario",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("¿Estás seguro de que deseas eliminar el comentario de $commentAuthor? Esta acción no se puede deshacer.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Eliminar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM", Locale("es", "ES"))
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: "Fecha"
    } catch (e: Exception) {
        "Fecha"
    }
}