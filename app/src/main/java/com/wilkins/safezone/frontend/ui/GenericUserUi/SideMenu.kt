package com.wilkins.safezone.GenericUserUi

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wilkins.safezone.ui.theme.NameApp
import com.wilkins.safezone.ui.theme.PrimaryColor

@Composable
fun SideMenu(
    navController: NavController,
    userId: String, // 👈 Agregamos el parámetro del ID de usuario
    modifier: Modifier = Modifier
) {
    var isOpen by remember { mutableStateOf(false) }
    val menuItems = listOf(
        MenuItem(Icons.Default.Person, "Perfil"),
        MenuItem(Icons.Default.Home, "Inicio"),
        MenuItem(Icons.Default.Notifications, "Noticias"),
        MenuItem(Icons.Default.Place, "Reportes en tu zona"),
        MenuItem(Icons.Default.Warning, "Alerta una emergencia"),
        MenuItem(Icons.Default.Visibility, "Mis alertas"),
        MenuItem(Icons.Default.Notifications, "Notificaciones"),
        MenuItem(Icons.Default.Settings, "Configuración")
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isOpen = !isOpen }) {
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
                    navController.navigate("NavigationDrawer") {
                        launchSingleTop = true
                    }
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
                        .width(280.dp)
                        .fillMaxHeight()
                        .background(PrimaryColor)
                ) {
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
                            text = NameApp,
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

                    // 🔹 Items del menú
                    menuItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isOpen = false
                                    when (item.label) {
                                        "Inicio" -> navController.navigate("userHome/$userId") {
                                            launchSingleTop = true
                                            popUpTo("userHome/$userId") { inclusive = true }
                                        }
                                        "Perfil" -> navController.navigate("profile") { launchSingleTop = true }
                                        "Noticias" -> navController.navigate("NewsUser") { launchSingleTop = true }
                                        "Reportes en tu zona" -> navController.navigate("ReportsUser") { launchSingleTop = true }
                                        "Alerta una emergencia" -> navController.navigate("AlertUser") { launchSingleTop = true }
                                        "Mis alertas" -> navController.navigate("MyAlertsUser") { launchSingleTop = true }
                                        "Notificaciones" -> navController.navigate("NotificationsUser") { launchSingleTop = true }
                                        "Configuración" -> navController.navigate("settings") { launchSingleTop = true }
                                    }
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
                            text = "Versión 1.0",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}


data class MenuItem(
    val icon: ImageVector,
    val label: String
)

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun AlertaYaMenuPreview() {
//    MaterialTheme {
//        SideMenu()
//    }
//}