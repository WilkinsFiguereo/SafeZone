package com.wilkins.safezone.frontend.ui.user.NavigationDrawer

import SessionManager.getUserProfile
import SessionManager.logout
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.AppUser
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(navController: NavController, context: Context, supabaseClient: SupabaseClient) {
    val scope = rememberCoroutineScope()
    val backgroundColor = Color(0xFFF5F5F5)
    val cardColor = Color.White
    val textPrimary = Color(0xFF1F1F1F)
    val textSecondary = Color(0xFF6B7280)
    // 🔥 Cargar perfil de usuario de forma segura
    val userState = produceState<AppUser?>(initialValue = null) {
        value = getUserProfile(context)
    }

    val user = userState.value


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Perfil",
                        color = textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Tarjeta de perfil del usuario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Navegar a la pantalla Profile
                        navController.navigate("profile")
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar circular
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E7EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFF6B7280)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Nombre y texto "ver perfil"
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.name ?: "Usuario",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Ver perfil",
                            fontSize = 13.sp,
                            color = textSecondary
                        )
                    }

                    // Flecha derecha
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Ir",
                        tint = textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

        }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de opciones
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileMenuItem(
                        icon = Icons.Default.List,
                        title = "Historial de reportes",
                        onClick = { /* Navegar */ }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFE5E7EB),
                        thickness = 0.5.dp
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.DateRange,
                        title = "Historial de actividad",
                        onClick = { /* Navegar */ }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFE5E7EB),
                        thickness = 0.5.dp
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.Info,
                        title = "Terminos y condiciones",
                        onClick = { /* Navegar */ }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFE5E7EB),
                        thickness = 0.5.dp
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.Settings,
                        title = "Ajustes y privacidad",
                        onClick = {
                            navController.navigate("settings")
                        }
                    )



                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFE5E7EB),
                        thickness = 0.5.dp
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.ExitToApp,
                        title = "Cerrar sesión",
                        showDivider = false,
                        onClick = {
                            scope.launch {
                                logout(context, supabaseClient)

                                navController.navigate("login") {
                                    popUpTo("profile") { inclusive = true }
                                }
                            }
                        }
                    )

                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = Color(0xFF1F1F1F)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 15.sp,
            color = Color(0xFF1F1F1F),
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Ir",
            tint = Color(0xFF1F1F1F),
            modifier = Modifier.size(24.dp)
        )
    }
}