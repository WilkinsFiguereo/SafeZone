package com.wilkins.safezone.frontend.ui.user.Notification

import SessionManager.getUserProfile
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.User.Notification.NotificationData
import com.wilkins.safezone.backend.network.User.Notification.NotificationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class FilterTab(val label: String, val icon: ImageVector) {
    ALL("Todas", Icons.Outlined.Inbox),
    UNREAD("No leídas", Icons.Outlined.MarkEmailUnread),
    SYSTEM("Sistema", Icons.Outlined.Notifications),
    IMPORTANT("Importantes", Icons.Outlined.PriorityHigh)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController, context: Context, subaseClient: SupabaseClient) {
    val supabase = SupabaseService.getInstance()
    val userId = supabase.auth.currentUserOrNull()?.id ?: ""
    val notificationRepository = remember { NotificationRepository(supabase) }
    val scope = rememberCoroutineScope()

    var searchText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(FilterTab.ALL) }
    var isSearchActive by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf<List<NotificationData>>(emptyList()) }
    var showMenu by remember { mutableStateOf(false) }

    val userState = produceState<AppUser?>(initialValue = null) {
        value = getUserProfile(context)
    }
    val user = userState.value

    // Cargar notificaciones
    LaunchedEffect(selectedTab) {
        isLoading = true
        notifications = when (selectedTab) {
            FilterTab.ALL -> notificationRepository.getUserNotifications(userId)
            FilterTab.UNREAD -> notificationRepository.getUnreadNotifications(userId)
            FilterTab.SYSTEM -> notificationRepository.getNotificationsByType(userId, "SYSTEM")
            FilterTab.IMPORTANT -> notificationRepository.getNotificationsByType(userId, "IMPORTANT")
        }
        isLoading = false
    }

    // Filtrar por búsqueda
    val filteredNotifications = notifications.filter { notification ->
        if (searchText.isEmpty()) {
            true
        } else {
            val senderName = notification.senderName ?: "Sistema SafeZone"
            senderName.contains(searchText, ignoreCase = true) ||
                    notification.message.contains(searchText, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Top Bar
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

                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Outlined.MoreVert,
                                            contentDescription = "Más",
                                            tint = Color(0xFF5F6368)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Marcar todas como leídas") },
                                            onClick = {
                                                scope.launch {
                                                    notificationRepository.markAllAsRead(userId)
                                                    // Recargar notificaciones
                                                    notifications = when (selectedTab) {
                                                        FilterTab.ALL -> notificationRepository.getUserNotifications(userId)
                                                        FilterTab.UNREAD -> notificationRepository.getUnreadNotifications(userId)
                                                        FilterTab.SYSTEM -> notificationRepository.getNotificationsByType(userId, "SYSTEM")
                                                        FilterTab.IMPORTANT -> notificationRepository.getNotificationsByType(userId, "IMPORTANT")
                                                    }
                                                }
                                                showMenu = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.DoneAll,
                                                    contentDescription = null
                                                )
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = { Text("Eliminar leídas") },
                                            onClick = {
                                                scope.launch {
                                                    notificationRepository.deleteAllRead(userId)
                                                    // Recargar notificaciones
                                                    notifications = when (selectedTab) {
                                                        FilterTab.ALL -> notificationRepository.getUserNotifications(userId)
                                                        FilterTab.UNREAD -> notificationRepository.getUnreadNotifications(userId)
                                                        FilterTab.SYSTEM -> notificationRepository.getNotificationsByType(userId, "SYSTEM")
                                                        FilterTab.IMPORTANT -> notificationRepository.getNotificationsByType(userId, "IMPORTANT")
                                                    }
                                                }
                                                showMenu = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.Delete,
                                                    contentDescription = null
                                                )
                                            }
                                        )
                                    }
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

            // Contenido
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1A73E8))
                }
            } else if (filteredNotifications.isEmpty()) {
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
                            text = if (searchText.isNotEmpty()) "No hay resultados" else "No hay notificaciones",
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
                        NotificationItem(
                            notification = notification,
                            onMarkAsRead = {
                                scope.launch {
                                    if (notificationRepository.markAsRead(notification.id)) {
                                        // Recargar notificaciones
                                        notifications = when (selectedTab) {
                                            FilterTab.ALL -> notificationRepository.getUserNotifications(userId)
                                            FilterTab.UNREAD -> notificationRepository.getUnreadNotifications(userId)
                                            FilterTab.SYSTEM -> notificationRepository.getNotificationsByType(userId, "SYSTEM")
                                            FilterTab.IMPORTANT -> notificationRepository.getNotificationsByType(userId, "IMPORTANT")
                                        }
                                    }
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    if (notificationRepository.deleteNotification(notification.id)) {
                                        // Recargar notificaciones
                                        notifications = when (selectedTab) {
                                            FilterTab.ALL -> notificationRepository.getUserNotifications(userId)
                                            FilterTab.UNREAD -> notificationRepository.getUnreadNotifications(userId)
                                            FilterTab.SYSTEM -> notificationRepository.getNotificationsByType(userId, "SYSTEM")
                                            FilterTab.IMPORTANT -> notificationRepository.getNotificationsByType(userId, "IMPORTANT")
                                        }
                                    }
                                }
                            }
                        )
                        HorizontalDivider(color = Color(0xFFE8EAED), thickness = 1.dp)
                    }
                }
            }
        }

        SideMenu(
            navController = navController,
            modifier = Modifier.align(Alignment.TopCenter),
            userId = userId,
            userName = user?.name ?: "Usuario",
            context = context,
            supabaseClient = subaseClient
        )
    }
}

@Composable
fun NotificationItem(
    notification: NotificationData,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // Determinar el nombre del remitente
    val senderName = when {
        notification.type == "SYSTEM" -> "Sistema SafeZone"
        notification.senderName != null -> notification.senderName
        else -> "Usuario"
    }

    // Determinar el icono según el tipo
    val typeIcon = when (notification.type) {
        "SYSTEM" -> Icons.Outlined.Notifications
        "IMPORTANT" -> Icons.Outlined.PriorityHigh
        else -> Icons.Outlined.Person
    }

    val typeColor = when (notification.type) {
        "SYSTEM" -> Color(0xFF1A73E8)
        "IMPORTANT" -> Color(0xFFEA4335)
        else -> Color(0xFF5F6368)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!notification.isRead) Color(0xFFF1F3F4) else Color.White)
            .clickable {
                if (!notification.isRead) {
                    onMarkAsRead()
                }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar o icono según tipo
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (notification.type == "SYSTEM" || notification.type == "IMPORTANT")
                        typeColor.copy(alpha = 0.1f)
                    else
                        Color(0xFFE8EAED)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (notification.type == "SYSTEM" || notification.type == "IMPORTANT") {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = senderName.firstOrNull()?.toString()?.uppercase() ?: "?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5F6368)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = senderName,
                    fontSize = 14.sp,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
                    color = Color(0xFF202124),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = getTimeAgo(notification.createdAt),
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
                maxLines = 3
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Menú de opciones
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "Opciones",
                    tint = Color(0xFF5F6368),
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                if (!notification.isRead) {
                    DropdownMenuItem(
                        text = { Text("Marcar como leída") },
                        onClick = {
                            onMarkAsRead()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.DoneAll,
                                contentDescription = null
                            )
                        }
                    )
                }

                DropdownMenuItem(
                    text = { Text("Eliminar") },
                    onClick = {
                        onDelete()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

/**
 * Convierte un timestamp ISO a formato "hace X tiempo"
 */
fun getTimeAgo(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(timestamp) ?: return "Ahora"

        val now = Date()
        val diff = now.time - date.time

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        when {
            seconds < 60 -> "Ahora"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            days < 7 -> "${days}d"
            else -> {
                val displaySdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                displaySdf.format(date)
            }
        }
    } catch (e: Exception) {
        "Ahora"
    }
}