package com.wilkins.safezone.frontend.ui.Map.Components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.safezone.backend.network.User.Interaction.EntityType
import com.wilkins.safezone.backend.network.User.Interaction.InteractionsRepository
import kotlinx.coroutines.launch

/**
 * Botón de like animado con contador
 */
@Composable
fun LikeButton(
    targetId: String,
    entityType: EntityType,
    modifier: Modifier = Modifier,
    showCount: Boolean = true,
    compactMode: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val repository = remember { InteractionsRepository() }

    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }

    // Animación de escala
    val scale by animateFloatAsState(
        targetValue = if (isLiked) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "likeScale"
    )

    // Cargar estado inicial
    LaunchedEffect(targetId) {
        scope.launch {
            // Verificar si el usuario dio like
            val hasLiked = repository.hasUserLiked(targetId, entityType)
                .getOrNull() ?: false
            isLiked = hasLiked

            // Obtener conteo de likes
            val count = repository.getLikesCount(targetId, entityType)
                .getOrNull() ?: 0
            likeCount = count

            isLoading = false
        }
    }

    if (compactMode) {
        // Modo compacto (para cards pequeñas)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    enabled = !isLoading && !isProcessing
                ) {
                    scope.launch {
                        isProcessing = true
                        val result = repository.toggleLike(targetId, entityType)
                        if (result.isSuccess) {
                            isLiked = result.getOrNull() ?: false
                            likeCount = if (isLiked) likeCount + 1 else (likeCount - 1).coerceAtLeast(0)
                        }
                        isProcessing = false
                    }
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            if (isLoading || isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFFE53935)
                )
            } else {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isLiked) "Quitar like" else "Dar like",
                    tint = if (isLiked) Color(0xFFE53935) else Color(0xFF757575),
                    modifier = Modifier
                        .size(18.dp)
                        .scale(scale)
                )
            }

            if (showCount && likeCount > 0) {
                Text(
                    text = formatLikeCount(likeCount),
                    fontSize = 12.sp,
                    fontWeight = if (isLiked) FontWeight.Bold else FontWeight.Medium,
                    color = if (isLiked) Color(0xFFE53935) else Color(0xFF757575)
                )
            }
        }
    } else {
        // Modo normal (para pantallas de detalle)
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isLiked) Color(0xFFE53935).copy(alpha = 0.1f) else Color(0xFFF5F5F5)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isLiked) 4.dp else 2.dp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        enabled = !isLoading && !isProcessing
                    ) {
                        scope.launch {
                            isProcessing = true
                            val result = repository.toggleLike(targetId, entityType)
                            if (result.isSuccess) {
                                isLiked = result.getOrNull() ?: false
                                likeCount = if (isLiked) likeCount + 1 else (likeCount - 1).coerceAtLeast(0)
                            }
                            isProcessing = false
                        }
                    }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLiked) Color(0xFFE53935) else Color.White
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading || isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = if (isLiked) Color.White else Color(0xFFE53935)
                        )
                    } else {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isLiked) "Quitar like" else "Dar like",
                            tint = if (isLiked) Color.White else Color(0xFFE53935),
                            modifier = Modifier
                                .size(20.dp)
                                .scale(scale)
                        )
                    }
                }

                Column {
                    Text(
                        text = if (isLiked) "Te gusta esto" else "Me gusta",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLiked) Color(0xFFE53935) else Color(0xFF424242)
                    )

                    AnimatedVisibility(
                        visible = showCount && likeCount > 0,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Text(
                            text = "${formatLikeCount(likeCount)} ${if (likeCount == 1) "persona" else "personas"}",
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formatea el contador de likes (1K, 1.5K, 1M, etc.)
 */
private fun formatLikeCount(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 10000 -> String.format("%.1fK", count / 1000.0)
        count < 1000000 -> String.format("%dK", count / 1000)
        else -> String.format("%.1fM", count / 1000000.0)
    }
}