package com.wilkins.safezone.GenericUserUi

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.wilkins.safezone.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@Composable
fun AdminMenu(
    navController: NavController,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
    isMenuOpen: Boolean,
    onMenuToggle: () -> Unit,
    content: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val supabaseClient = SupabaseService.getInstance()

    // Estado para mostrar diálogo de confirmación
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Menú específico para administrador
    val menuItems = listOf(
        AdminMenuItem(Icons.Default.Dashboard, "Dashboard", "DashboardAdmin"),
        AdminMenuItem(Icons.Default.People, "Lista de Usuarios", "crudUsuarios"),
        AdminMenuItem(Icons.Default.PersonOff, "Usuarios Deshabilitados", "disabled_users"),
        AdminMenuItem(Icons.Default.Assessment, "Generar Reportes", "generate_reports"),
        AdminMenuItem(Icons.Default.Pending, "Reportes Pendientes", "pending_reports"),
        AdminMenuItem(Icons.Default.Update, "Reportes en Proceso", "in_progress_reports"),
        AdminMenuItem(Icons.Default.CheckCircle, "Reportes Completados", "completed_reports"),
        AdminMenuItem(Icons.Default.Cancel, "Reportes Cancelados", "cancelled_reports"),
        AdminMenuItem(Icons.Default.Category, "Categorías de Incidencias", "incident_categories"),
        AdminMenuItem(Icons.Default.BusinessCenter, "Categorías de Affairs", "affair_categories"),
        AdminMenuItem(Icons.Default.Settings, "Configuración", "admin_settings")
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Contenido principal proporcionado por el caller
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
                    // Botón de menú (3 líneas)
                    IconButton(onClick = onMenuToggle) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    // Logo/Nombre
                    Text(
                        text = "Panel Admin",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Botón de perfil
                    IconButton(onClick = {
                        navController.navigate("admin_profile")
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

            // Contenido proporcionado por el caller
            content()
        }

        // Menú lateral animado con overlay
        AnimatedVisibility(
            visible = isMenuOpen,
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
                // Panel del menú lateral
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                        .background(PrimaryColor)
                ) {
                    // Header del menú lateral
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Admin Panel",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Divider(
                        color = Color.White.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Items del menú
                    menuItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onMenuToggle()
                                    navController.navigate(item.route)
                                }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Text(
                                text = item.label,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Botón de cerrar sesión
                    Divider(
                        color = Color.White.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showLogoutDialog = true
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = Color(0xFFFFFFFF),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            text = "Cerrar Sesión",
                            color = Color(0xFFFFFFFF),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Footer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Divider(
                            color = Color.White.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            text = "Admin Versión 1.0",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Overlay para cerrar el menú
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onMenuToggle() }
                        .background(Color.Black.copy(alpha = 0.3f))
                )
            }
        }

        // Diálogo de confirmación de logout
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = PrimaryColor
                    )
                },
                title = {
                    Text(
                        text = "Cerrar Sesión",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text("¿Estás seguro de que deseas cerrar sesión?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            scope.launch {
                                try {
                                    Log.i("AdminMenu", "🚪 Iniciando logout...")
                                    SessionManager.logout(context, supabaseClient)
                                    Log.i("AdminMenu", "✅ Logout exitoso, navegando a login")

                                    onMenuToggle() // Cerrar menú

                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                } catch (e: Exception) {
                                    Log.e("AdminMenu", "❌ Error durante logout: ${e.message}", e)
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFFF6B6B)
                        )
                    ) {
                        Text("Cerrar Sesión")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

data class AdminMenuItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)