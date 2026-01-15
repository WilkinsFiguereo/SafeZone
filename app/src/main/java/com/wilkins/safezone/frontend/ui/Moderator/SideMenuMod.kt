package com.wilkins.safezone.frontend.ui.Moderator

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.wilkins.safezone.backend.network.auth.SessionManager.logout
import com.wilkins.safezone.navigation.theme.PrimaryColor
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.launch

@Composable
fun ModeratorSideMenu(
    navController: NavController,
    moderatorId: String,
    moderatorName: String = "Moderador",
    currentRoute: String = "",
    modifier: Modifier = Modifier,
    isMenuOpen: Boolean = false,
    onMenuToggle: (Boolean) -> Unit = {},
    context: Context,
    supabaseClient: SupabaseClient
) {
    var isOpen by remember { mutableStateOf(isMenuOpen) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isMenuOpen) {
        isOpen = isMenuOpen
    }

    val menuSections = listOf(
        MenuSection(
            title = "Panel Principal",
            items = listOf(
                ModeratorMenuItem(Icons.Default.Dashboard, "Dashboard", "DashboardMod"),
                ModeratorMenuItem(Icons.Default.BarChart, "Estadísticas", "moderatorStatistics")
            )
        ),
        MenuSection(
            title = "Gestión de Noticias",
            items = listOf(
                ModeratorMenuItem(Icons.Default.Newspaper, "Ver Noticias", "NewsUser"),
                ModeratorMenuItem(Icons.Default.Add, "Subir Noticia", "newsSave"),
                ModeratorMenuItem(Icons.Default.Edit, "Editar Noticias", "newsList")
            )
        ),
        MenuSection(
            title = "Gestión de Encuestas",
            items = listOf(
                ModeratorMenuItem(Icons.Default.Newspaper, "Ver Encuestas", "SurveyUser"),
                ModeratorMenuItem(Icons.Default.Add, "Subir Encuestas", "surveyCreate"),
                ModeratorMenuItem(Icons.Default.Edit, "Editar Encuestas", "surveyList")
            )
        ),
        MenuSection(
            title = "Reportes",
            items = listOf(
                ModeratorMenuItem(Icons.Default.Report, "Ver Reportes", "ReportReviewList"),
                ModeratorMenuItem(Icons.Default.PendingActions, "Reportes en tu sona", "MapReports")
            )
        ),
        MenuSection(
            title = "Usuarios",
            items = listOf(
                ModeratorMenuItem(Icons.Default.People, "Gestionar Usuarios", "moderatorUser"),
                ModeratorMenuItem(Icons.Default.Block, "Usuarios Bloqueados", "moderatorUserDisable")
            )
        ),
        MenuSection(
            title = "Configuración",
            items = listOf(
                ModeratorMenuItem(Icons.Default.Person, "Mi Perfil", "MyProfile"),
                ModeratorMenuItem(Icons.Default.Notifications, "Notificaciones", "Notification"),
                ModeratorMenuItem(Icons.Default.Settings, "Configuración", "moderatorSettings"),
                ModeratorMenuItem(Icons.Default.Logout, "Cerrar Sesión", "logout")
            )
        )
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    isOpen = !isOpen
                    onMenuToggle(isOpen)
                }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Panel Moderador",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Badge de notificaciones
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ) {
                                Text("5", fontSize = 10.sp)
                            }
                        }
                    ) {
                        IconButton(onClick = {
                            navController.navigate("moderatorNotifications")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    IconButton(onClick = {
                        navController.navigate("moderatorProfile")
                    }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Perfil",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Menú lateral animado
        AnimatedVisibility(
            visible = isOpen,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            )
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight()
                        .background(PrimaryColor)
                ) {
                    // Header del menú
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = moderatorName,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ID: $moderatorId",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secciones del menú con scroll
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        menuSections.forEach { section ->
                            MenuSectionComponent(
                                section = section,
                                currentRoute = currentRoute,
                                onItemClick = { route ->
                                    isOpen = false
                                    onMenuToggle(false)

                                    if (route == "logout") {
                                        // Implementar lógica de cerrar sesión
                                        scope.launch {
                                            logout(context, supabaseClient)
                                            navController.navigate("login") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    } else {
                                        navController.navigate(route) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Footer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                        }
                    }
                }

                // Área de cierre (click fuera del menú)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable {
                            isOpen = false
                            onMenuToggle(false)
                        }
                )
            }
        }
    }
}

@Composable
fun MenuSectionComponent(
    section: MenuSection,
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Título de la sección
        Text(
            text = section.title,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Items de la sección
        section.items.forEach { item ->
            val isSelected = currentRoute == item.route

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
                    .clickable { onItemClick(item.route) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.label,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

data class ModeratorMenuItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

data class MenuSection(
    val title: String,
    val items: List<ModeratorMenuItem>
)