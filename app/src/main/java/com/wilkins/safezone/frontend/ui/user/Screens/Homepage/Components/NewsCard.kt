package com.wilkins.safezone.frontend.ui.user.Screens.Homepage.Components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image as ImageIcon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.delay
import com.wilkins.safezone.R

data class NewsItem(
    val title: String,
    val date: String,
    val description: String,
    val imageUrl: String? = null,  // URL desde Supabase
    val imageRes: Int? = null      // Fallback local
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewsSlider(
    newsItems: List<NewsItem> = emptyList(),
    onNewsClick: (NewsItem) -> Unit = {}
) {
    val pagerState = rememberPagerState(
        pageCount = { newsItems.size }
    )

    // Auto-scroll del slider
    LaunchedEffect(pagerState) {
        while (true) {
            delay(5000) // Cambia cada 5 segundos
            if (newsItems.isNotEmpty()) {
                val nextPage = (pagerState.currentPage + 1) % newsItems.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            if (newsItems.isNotEmpty()) {
                val newsItem = newsItems[page]
                NewsCard(
                    title = newsItem.title,
                    date = newsItem.date,
                    description = newsItem.description,
                    imageUrl = newsItem.imageUrl,
                    imageRes = newsItem.imageRes,
                    onCardClick = { onNewsClick(newsItem) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
        }

        // Indicadores de página
        if (newsItems.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(newsItems.size) { index ->
                    val color = if (pagerState.currentPage == index) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Gray.copy(alpha = 0.5f)
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
fun NewsCard(
    title: String,
    date: String,
    description: String,
    imageUrl: String? = null,
    imageRes: Int? = null,
    onCardClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                clip = true
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen de fondo - Prioriza URL sobre recurso local
            if (!imageUrl.isNullOrBlank()) {
                // Cargar desde URL de Supabase
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Imagen de la noticia: $title",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = Color.White
                            )
                        }
                    },
                    error = {
                        // Si falla, intentar cargar recurso local si existe
                        if (imageRes != null) {
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = "Imagen de la noticia: $title",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Mostrar icono de error
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ImageIcon,
                                    contentDescription = "Error al cargar imagen",
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
            } else if (imageRes != null) {
                // Cargar recurso local si no hay URL
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Imagen de la noticia: $title",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Sin imagen disponible
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ImageIcon,
                        contentDescription = "Sin imagen",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Overlay mejorado con gradiente más profesional
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.1f),
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Contenido de la noticia mejorado
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header con fecha y badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Badge de noticia
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Noticia",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Fecha
                    Text(
                        text = date,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Contenido principal
                Column {
                    // Título mejorado
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Línea separadora con estilo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                            .padding(bottom = 10.dp)
                    )

                    // Descripción mejorada
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
            }

            // Efecto de borde sutil
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                Color.Transparent,
                                Color.Transparent,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewsSliderPreview() {
    MaterialTheme {
        val newsItems = listOf(
            NewsItem(
                title = "Nuevo programa de seguridad comunitaria",
                date = "12/10/2024",
                description = "Se implementa nuevo sistema de vigilancia en el sector norte de la ciudad.",
                imageRes = R.drawable.personas_recogiendo
            ),
            NewsItem(
                title = "Jornada de limpieza este sábado",
                date = "15/10/2024",
                description = "Participa en la jornada de limpieza comunitaria en el parque central.",
                imageRes = R.drawable.bandalismo
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(16.dp)
        ) {
            Text(
                text = "Últimas Noticias",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            NewsSlider(
                newsItems = newsItems,
                onNewsClick = { newsItem ->
                    // Manejar click en noticia
                }
            )
        }
    }
}