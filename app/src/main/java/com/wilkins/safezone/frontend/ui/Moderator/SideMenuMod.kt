package com.wilkins.safezone.frontend.ui.Moderator

import SessionManager.logout
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
import com.wilkins.safezone.ui.theme.PrimaryColor
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

    LaunchedEffect(isMenuOpen) { isOpen = isMenuOpen }

    // 🔥 SECCIONES DEL MENÚ (RUTAS CORREGIDAS)
    val menuSections = listOf(
        MenuSection(
            title = "Panel Principal",
            items = listOf(
                ModeratorMenuItem(Icons.Default.Dashboard, "Dashboard", "moderatorDashboard"),
                ModeratorMenuItem(Icons.Default.BarChart, "Estadísticas", "moderatorStats")
            )
        ),
        MenuSection(
            title = "Gestión de Noticias",
            items = listOf(
                ModeratorMenuItem(Icons.Default.Newspaper, "Ver Noticias", "NewsUser"),
                ModeratorMenuItem(Icons.Default.Add, "Subir Noticia", "SaveNews"),
                // ❗ CORREGIDO: Editar debe ir a la lista, no a la pantalla directa
                ModeratorMenuItem(Icons.Default.Edit, "Editar Noticias", "news_list")
            )
        ),
        MenuSection(
            title = "Reportes",
            items = listOf(
                ModeratorMenuItem(Icons.Default.Report, "Ver Reportes", "moderatorReports"),
                ModeratorMenuItem(Icons.Default.PendingActions, "Reportes Pendientes", "moderatorPendingReports"),
                ModeratorMenuItem(Icons.Default.CheckCircle, "Reportes Resueltos", "moderatorResolvedReports")
            )
        ),
        MenuSection(
            title = "Usuarios",
            items = listOf(
                ModeratorMenuItem(Icons.Default.People, "Gestionar Usuarios", "moderatorUsers"),
                ModeratorMenuItem(Icons.Default.Block, "Usuarios Bloqueados", "moderatorBlockedUsers")
            )
        ),
        MenuSection(
            title = "Configuración",
            items = listOf(
                ModeratorMenuItem(Icons.Default.Person, "Mi Perfil", "moderatorProfile"),
                ModeratorMenuItem(Icons.Default.Notifications, "Notificaciones", "moderatorNotifications"),
                ModeratorMenuItem(Icons.Default.Settings, "Configuración", "moderatorSettings"),
                ModeratorMenuItem(Icons.Default.Logout, "Cerrar Sesión", "logout")
            )
        )
    )

    Box(modifier = modifier.fillMaxSize()) {

        // 🔹 CONTENIDO DE TOPBAR DEJADO IGUAL
        Column(modifier = Modifier.fillMaxSize()) {
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

                Text(
                    text = "Panel Moderador",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { navController.navigate("moderatorProfile") }) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // 🔥 MENÚ LATERAL ANIMADO
        AnimatedVisibility(
            visible = isOpen,
            enter = slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)),
            exit = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
        ) {

            Row(modifier = Modifier.fillMaxSize()) {

                // CUERPO DEL MENÚ
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight()
                        .background(PrimaryColor)
                ) {

                    // HEADER DEL MENÚ
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp)
                    ) {
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // 🔹 LISTA DE SECCIONES
                    Column(
                        modifier = Modifier
                            .weight(1f)
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
                                        scope.launch {
                                            logout(context, supabaseClient)
                                            navController.navigate("login") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    } else {
                                        navController.navigate(route) { launchSingleTop = true }
                                    }
                                }
                            )
                        }
                    }
                }

                // SOMBRA PARA CERRAR MENU
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
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {

        Text(
            text = section.title,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        section.items.forEach { item ->
            val isSelected = currentRoute == item.route

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                    .clickable { onItemClick(item.route) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.label,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)
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
