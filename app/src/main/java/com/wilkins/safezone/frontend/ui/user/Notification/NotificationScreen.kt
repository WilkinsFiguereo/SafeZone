package com.wilkins.safezone.frontend.ui.user.Notification

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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wilkins.safezone.GenericUserUi.SideMenu
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.gotrue.auth

data class Notification(
    val userName: String,
    val time: String,
    val message: String,
    val type: NotificationType = NotificationType.COMMENT,
    val isRead: Boolean = false
)

enum class NotificationType {
    COMMENT, REQUEST, ALERT, SYSTEM
}

enum class FilterTab(val label: String, val icon: ImageVector) {
    ALL("Todas", Icons.Outlined.Inbox),
    UNREAD("No leídas", Icons.Outlined.MarkEmailUnread),
    COMMENTS("Comentarios", Icons.Outlined.Comment),
    REQUESTS("Solicitudes", Icons.Outlined.Assignment)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    val supabase = SupabaseService.getInstance()
    val userId = supabase.auth.currentUserOrNull()?.id ?: ""

    var searchText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(FilterTab.ALL) }
    var isSearchActive by remember { mutableStateOf(false) }

    val notifications = remember {
        listOf(
            Notification(
                userName = "María González",
                time = "5 min",
                message = "Envió un comentario a tu reporte y solicita más información sobre el incidente",
                type = NotificationType.COMMENT,
                isRead = false
            ),
            Notification(
                userName = "Juan Pérez",
                time = "10 min",
                message = "Solicitó acceso al código de registro de tu reporte #1234",
                type = NotificationType.REQUEST,
                isRead = false
            ),
            Notification(
                userName = "Sistema SafeZone",
                time = "1 h",
                message = "Nueva alerta de seguridad en tu zona: Mantente alerta",
                type = NotificationType.ALERT,
                isRead = true
            ),
            Notification(
                userName = "Ana Martínez",
                time = "2 h",
                message = "Te ha enviado un comentario en tu reporte y solicita más detalles",
                type = NotificationType.COMMENT,
                isRead = true
            ),
            Notification(
                userName = "Carlos Rodríguez",
                time = "3 h",
                message = "Ha solicitado una reunión para discutir el caso reportado",
                type = NotificationType.REQUEST,
                isRead = true
            )
        )
    }

    val filteredNotifications = notifications.filter { notification ->
        val matchesSearch = if (searchText.isEmpty()) true else {
            notification.userName.contains(searchText, ignoreCase = true) ||
                    notification.message.contains(searchText, ignoreCase = true)
        }

        val matchesFilter = when (selectedTab) {
            FilterTab.ALL -> true
            FilterTab.UNREAD -> !notification.isRead
            FilterTab.COMMENTS -> notification.type == NotificationType.COMMENT
            FilterTab.REQUESTS -> notification.type == NotificationType.REQUEST
        }

        matchesSearch && matchesFilter
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Top Bar simple
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = if (isSearchActive) 0.dp else 1.dp
            ) {
                Column {
                    if (!isSearchActive) {
                        // Header normal
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Notificaciones",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF202124)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = "Buscar",
                                        tint = Color(0xFF5F6368)
                                    )
                                }
                                IconButton(onClick = { /* Más opciones */ }) {
                                    Icon(
                                        imageVector = Icons.Outlined.MoreVert,
                                        contentDescription = "Más",
                                        tint = Color(0xFF5F6368)
                                    )
                                }
                            }
                        }
                    } else {
                        // Barra de búsqueda
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                isSearchActive = false
                                searchText = ""
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color(0xFF5F6368)
                                )
                            }

                            TextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                placeholder = {
                                    Text(
                                        "Buscar en notificaciones",
                                        color = Color(0xFF80868B)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                singleLine = true
                            )

                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { searchText = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar",
                                        tint = Color(0xFF5F6368)
                                    )
                                }
                            }
                        }
                    }

                    // Tabs de filtros
                    TabRow(
                        selectedTabIndex = FilterTab.values().indexOf(selectedTab),
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A73E8),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[FilterTab.values().indexOf(selectedTab)]),
                                color = Color(0xFF1A73E8)
                            )
                        }
                    ) {
                        FilterTab.values().forEach { tab ->
                            Tab(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                selectedContentColor = Color(0xFF1A73E8),
                                unselectedContentColor = Color(0xFF5F6368)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tab.label,
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedTab == tab) FontWeight.Medium else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Lista de notificaciones
            if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = Color(0xFFDADCE0)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay notificaciones",
                            fontSize = 16.sp,
                            color = Color(0xFF5F6368)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredNotifications) { notification ->
                        NotificationItem(notification = notification)
                        HorizontalDivider(color = Color(0xFFE8EAED), thickness = 1.dp)
                    }
                }
            }
        }

        SideMenu(
            navController = navController,
            modifier = Modifier.align(Alignment.TopCenter),
            userId = userId
        )
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!notification.isRead) Color(0xFFF1F3F4) else Color.White)
            .clickable { /* Marcar como leído */ }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar simple con inicial
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8EAED)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = notification.userName.firstOrNull()?.toString() ?: "?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5F6368)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = notification.userName,
                    fontSize = 14.sp,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
                    color = Color(0xFF202124),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = notification.time,
                    fontSize = 12.sp,
                    color = Color(0xFF5F6368)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = notification.message,
                fontSize = 13.sp,
                color = Color(0xFF5F6368),
                lineHeight = 18.sp,
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Indicador de no leído
        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A73E8))
            )
        }
    }
}