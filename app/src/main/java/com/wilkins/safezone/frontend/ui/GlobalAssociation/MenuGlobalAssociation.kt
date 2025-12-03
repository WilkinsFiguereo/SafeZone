package com.wilkins.safezone.frontend.ui.GlobalAssociation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.ui.theme.NameApp
import com.wilkins.safezone.ui.theme.PrimaryColor
import kotlinx.coroutines.launch



// Data classes
data class GovernmentMenuItem(
    val icon: ImageVector,
    val title: String,
    val route: String,
    val badge: Int? = null
)

data class GovernmentMenuSection(
    val title: String,
    val items: List<GovernmentMenuItem>
)
// Componente de Sección del Menú
@Composable
fun GovernmentMenuSectionComponent(
    section: GovernmentMenuSection,
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
                    .clickable { onItemClick(item.route) }
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.title,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )

            }
        }
    }
}

// Menú Gubernamental
@Composable
fun GovernmentMenu(
    navController: NavController,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
    isMenuOpen: Boolean,
    onMenuToggle: (Boolean) -> Unit,
    currentRoute: String = "",
    content: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val supabaseClient = SupabaseService.getInstance()

    var isOpen by remember { mutableStateOf(isMenuOpen) }
    LaunchedEffect(isMenuOpen) {
        isOpen = isMenuOpen
    }

    // Menú específico para entidades gubernamentales
    val menuSections = listOf(
        GovernmentMenuSection(
            title = "DASHBOARD",
            items = listOf(
                GovernmentMenuItem(Icons.Default.Dashboard, "Dashboard", "government_dashboard")
            )
        ),
        GovernmentMenuSection(
            title = "REPORTES",
            items = listOf(
                GovernmentMenuItem(Icons.Default.Send, "Reportes Enviados", "ReportSentList", badge = 25),
                GovernmentMenuItem(Icons.Default.Pending, "Reportes Pendientes", "PendingReports", badge = 12),
                GovernmentMenuItem(Icons.Default.Update, "Reportes en Proceso", "ReportsProgress", badge = 8),
                GovernmentMenuItem(Icons.Default.CheckCircle, "Reportes Finalizados", "ReportsCompleted"),
                GovernmentMenuItem(Icons.Default.Cancel, "Reportes Cancelados", "ReportsCancelled")
            )
        ),
        GovernmentMenuSection(
            title = "ANÁLISIS",
            items = listOf(
                GovernmentMenuItem(Icons.Default.Analytics, "Estadísticas", "government_analytics"),
                GovernmentMenuItem(Icons.Default.Map, "Mapa de Incidentes", "government_map"),
                GovernmentMenuItem(Icons.Default.TrendingUp, "Tendencias", "government_trends")
            )
        ),
        GovernmentMenuSection(
            title = "GESTIÓN",
            items = listOf(
                GovernmentMenuItem(Icons.Default.Category, "Categorías", "government_categories"),
                GovernmentMenuItem(Icons.Default.Notifications, "Notificaciones", "government_notifications", badge = 5),
                GovernmentMenuItem(Icons.Default.Settings, "Configuración", "government_settings")
            )
        ),
        GovernmentMenuSection(
            title = "CUENTA",
            items = listOf(
                GovernmentMenuItem(Icons.Default.AccountCircle, "Mi Perfil", "government_profile"),
                GovernmentMenuItem(Icons.Default.Logout, "Cerrar Sesión", "logout")
            )
        )
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (showHeader) {
                // Header Principal
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
                        navController.navigate("government_profile")
                    }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "Perfil",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            content()
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
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Panel Gubernamental",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Entidad Pública",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp
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
                            GovernmentMenuSectionComponent(
                                section = section,
                                currentRoute = currentRoute,
                                onItemClick = { route ->
                                    isOpen = false
                                    onMenuToggle(false)

                                    if (route == "logout") {
                                        scope.launch {
                                            try {
                                                // SessionManager.logout(context, supabaseClient)
                                                navController.navigate("login") {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            } catch (e: Exception) {
                                                // Manejar error de logout
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
                                    text = "SafeZone Gov",
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
                                imageVector = Icons.Default.Gavel,
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