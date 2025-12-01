//package com.wilkins.safezone.frontend.ui.Moderator.Dasbhoard.Components
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Newspaper
//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//
//data class NewsItem(
//    val id: String,
//    val title: String,
//    val summary: String,
//    val author: String,
//    val timestamp: String,
//    val views: Int,
//    val isPublished: Boolean
//)
//
//@Composable
//fun LatestNewsCard(
//    newsList: List<NewsItem>,
//    onNewsClick: (NewsItem) -> Unit,
//    onViewAllClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    primaryColor: Color
//) {
//    Card(
//        modifier = modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(16.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(
//                        imageVector = Icons.Default.Newspaper,
//                        contentDescription = null,
//                        tint = primaryColor,
//                        modifier = Modifier.size(24.dp)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = "Últimas Noticias",
//                        style = MaterialTheme.typography.titleLarge,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//
//                TextButton(onClick = onViewAllClick) {
//                    Text(
//                        text = "Ver todas",
//                        color = primaryColor
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            if (newsList.isEmpty()) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(100.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = "No hay noticias recientes",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            } else {
//                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                    newsList.take(5).forEach { news ->
//                        NewsItemRow(
//                            news = news,
//                            onClick = { onNewsClick(news) },
//                            primaryColor = primaryColor
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun NewsItemRow(
//    news: NewsItem,
//    onClick: () -> Unit,
//    primaryColor: Color
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onClick),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
//        ),
//        shape = RoundedCornerShape(8.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = news.title,
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//
//                Text(
//                    text = news.summary,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis,
//                    modifier = Modifier.padding(top = 4.dp)
//                )
//
//                Row(
//                    modifier = Modifier.padding(top = 8.dp),
//                    horizontalArrangement = Arrangement.spacedBy(16.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = news.author,
//                        style = MaterialTheme.typography.labelSmall,
//                        color = primaryColor,
//                        fontWeight = FontWeight.Medium
//                    )
//
//                    Text(
//                        text = news.timestamp,
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            imageVector = Icons.Default.Visibility,
//                            contentDescription = null,
//                            modifier = Modifier.size(14.dp),
//                            tint = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = "${news.views}",
//                            style = MaterialTheme.typography.labelSmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            Surface(
//                color = if (news.isPublished) Color(0xFF4CAF50) else Color(0xFFFFA726),
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                Text(
//                    text = if (news.isPublished) "Publicada" else "Borrador",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = Color.White,
//                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
//    }
//}