package com.wilkins.safezone.frontend.ui.user.Screens.Survey

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.Moderator.Survery.Survey
import com.wilkins.safezone.backend.network.Moderator.Survery.SurveyViewModel
import com.wilkins.safezone.backend.network.User.Interaction.EntityType
import com.wilkins.safezone.backend.network.User.Interaction.InteractionsRepository
import com.wilkins.safezone.backend.network.auth.SessionManager
import com.wilkins.safezone.frontend.ui.user.Screens.News.RoleBasedMenu
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveysScreen(
    navController: NavController,
    currentRoute: String,
    viewModel: SurveyViewModel = viewModel()
) {
    val context = LocalContext.current
    val surveys by viewModel.surveys.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var isMenuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchSurveys()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú",
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { isMenuOpen = !isMenuOpen }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                "Encuestas Comunitarias",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                when {
                    isLoading -> LoadingState()
                    error != null -> ErrorState(error = error ?: "Error desconocido")
                    surveys.isEmpty() -> EmptyState()
                    else -> SurveysList(
                        surveys = surveys,
                        onSurveyClick = { survey ->
                            navController.navigate("SurveyDetail/${survey.id}")
                        }
                    )
                }
            }
        }

        RoleBasedMenu(
            navController = navController,
            currentRoute = currentRoute,
            isMenuOpen = isMenuOpen,
            onMenuToggle = { isMenuOpen = it },
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            Text(
                "Cargando encuestas...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorState(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    "Error",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Poll,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            Text(
                "No hay encuestas disponibles",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Las encuestas aparecerán aquí cuando se creen",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun SurveysList(
    surveys: List<Survey>,
    onSurveyClick: (Survey) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(surveys, key = { it.id }) { survey ->
            YoutubeSurveyCard(
                survey = survey,
                onClick = { onSurveyClick(survey) }
            )
        }
    }
}

@Composable
private fun YoutubeSurveyCard(
    survey: Survey,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val interactionsRepo = remember { InteractionsRepository() }
    val userId = SessionManager.loadSession(context)?.user?.id ?: ""

    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(0) }
    var isLoadingLike by remember { mutableStateOf(false) }

    // Cargar estado inicial de likes
    LaunchedEffect(survey.id) {
        interactionsRepo.hasUserLiked(survey.id, EntityType.SURVEY).onSuccess {
            isLiked = it
        }
        interactionsRepo.getLikesCount(survey.id, EntityType.SURVEY).onSuccess {
            likesCount = it
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Avatar + Nombre + Fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Poll,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        "Encuesta Comunitaria",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        formatTimeAgo(survey.created_at),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón de más opciones
                IconButton(onClick = { /* TODO: Opciones */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Más opciones",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Título de la encuesta
            Text(
                text = survey.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (!survey.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = survey.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de "Responder encuesta" estilo YouTube
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HowToVote,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Responder encuesta",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de acciones: Like y Compartir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de Like
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(enabled = !isLoadingLike) {
                            isLoadingLike = true
                            scope.launch {
                                interactionsRepo
                                    .toggleLike(survey.id, EntityType.SURVEY)
                                    .onSuccess { nowLiked ->
                                        isLiked = nowLiked
                                        likesCount = if (nowLiked) likesCount + 1 else likesCount - 1
                                        isLoadingLike = false
                                    }
                                    .onFailure {
                                        Log.e("SurveyCard", "Error al dar like", it)
                                        isLoadingLike = false
                                    }
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Me gusta",
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.6f
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    if (likesCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatCount(likesCount),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.6f
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón de Compartir
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            shareContent(
                                context = context,
                                title = survey.title,
                                description = survey.description ?: "",
                                surveyId = survey.id
                            )
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Compartir",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Compartir",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    // Divider entre encuestas
    HorizontalDivider(
        thickness = 8.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )
}

private fun formatTimeAgo(isoDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(isoDate) ?: return "Hace un momento"

        val now = Date()
        val diffMillis = now.time - date.time
        val diffHours = diffMillis / (1000 * 60 * 60)
        val diffDays = diffHours / 24

        when {
            diffHours < 1 -> "Hace un momento"
            diffHours < 24 -> "Hace ${diffHours}h"
            diffDays < 7 -> "Hace ${diffDays}d"
            diffDays < 30 -> "Hace ${diffDays / 7} semanas"
            else -> {
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("es"))
                outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        "Hace un momento"
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1000000 -> "${count / 1000000}M"
        count >= 1000 -> "${count / 1000}K"
        else -> count.toString()
    }
}

/**
 * Función para compartir contenido de encuesta usando Intent nativo de Android
 */
private fun shareContent(
    context: Context,
    title: String,
    description: String,
    surveyId: String
) {
    try {
        val shareText = buildString {
            append("📊 *Encuesta Comunitaria*\n\n")
            append("*$title*\n\n")
            if (description.isNotBlank()) {
                append("$description\n\n")
            }
            append("¡Participa y da tu opinión!\n")
            append("ID: $surveyId")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_TITLE, "Encuesta: $title")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Compartir encuesta mediante")
        context.startActivity(shareIntent)

        Log.d("ShareSurvey", "✅ Compartir encuesta: $surveyId")
    } catch (e: Exception) {
        Log.e("ShareSurvey", "❌ Error al compartir", e)
    }
}