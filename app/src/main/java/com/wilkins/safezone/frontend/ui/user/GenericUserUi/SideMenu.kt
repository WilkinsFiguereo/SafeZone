package com.wilkins.safezone.GenericUserUi

import SessionManager.getUserProfile
import SessionManager.logout
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.ui.theme.NameApp
import com.wilkins.safezone.ui.theme.PrimaryColor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

@Composable
fun SideMenu(
    navController: NavController,
    userId: String,
    userName: String, // Nombre del usuario
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

    val userState = produceState<AppUser?>(initialValue = null) {
        value = getUserProfile(context)
    }

    val user = userState.value

    // Obtener URL de la foto de perfil
    val profilePhotoUrl = remember(user?.photoProfile) {
        if (!user?.photoProfile.isNullOrEmpty()) {
            try {
                val bucket = SupabaseService.getInstance().storage.from("UserProfile")
                bucket.publicUrl(user!!.photoProfile!!)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    val menuSections = listOf(
        MenuSection(
            title = "Principal",
            items = listOf(
                MenuItem(Icons.Default.Home, "Inicio", "userHome/$userId"),
                MenuItem(Icons.Default.Person, "Mi Perfil", "MyProfile")
            )
        ),
        MenuSection(
            title = "Información",
            items = listOf(
                MenuItem(Icons.Default.Newspaper, "Noticias", "NewsUser"),
                MenuItem(Icons.Default.Place, "Reportes en tu zona", "MapReports")
            )
        ),
        MenuSection(
            title = "Alertas",
            items = listOf(
                MenuItem(Icons.Default.Warning, "Alerta una emergencia", "FormUser"),
                MenuItem(Icons.Default.Visibility, "Mis alertas", "RecordReports/${user?.id ?: ""}"),
                MenuItem(Icons.Default.Notifications, "Notificaciones", "Notification")
            )
        ),
        MenuSection(
            title = "Configuración",
            items = listOf(
                MenuItem(Icons.Default.Settings, "Configuración", "settings"),
                MenuItem(Icons.Default.Logout, "Cerrar Sesión", "logout")
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

                Text(
                    text = NameApp,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = {
                    navController.navigate("navigationDrawer")
                }) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePhotoUrl != null) {
                            AsyncImage(
                                model = profilePhotoUrl,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
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
                    // Header del menú con foto de perfil
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Foto de perfil en el menú
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (profilePhotoUrl != null) {
                                    AsyncImage(
                                        model = profilePhotoUrl,
                                        contentDescription = "Foto de perfil",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // Ícono por defecto con gradiente
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        Color.White.copy(alpha = 0.3f),
                                                        Color.White.copy(alpha = 0.1f)
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = userName,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ver perfil",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.clickable {
                                        isOpen = false
                                        onMenuToggle(false)
                                        navController.navigate("MyProfile")
                                    }
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
                                        scope.launch {
                                            logout(context, supabaseClient)
                                            navController.navigate("login") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    } else {
                                        navController.navigate(route)
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
                            Column {
                                Text(
                                    text = "SafeZone App",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Versión 1.0.0",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 10.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
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

data class MenuItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

data class MenuSection(
    val title: String,
    val items: List<MenuItem>
)