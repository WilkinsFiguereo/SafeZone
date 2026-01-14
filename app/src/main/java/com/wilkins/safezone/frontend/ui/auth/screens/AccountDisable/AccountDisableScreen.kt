package com.wilkins.safezone.frontend.ui.auth.screens.AccountDisable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.auth.SessionManager
import kotlinx.coroutines.launch

@Composable
fun AccountStatusScreen(
    onNavigateBack: () -> Unit,
    onLogoutComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var user by remember { mutableStateOf<AppUser?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoggingOut by remember { mutableStateOf(false) }

    // Cargar datos del usuario al iniciar
    LaunchedEffect(Unit) {
        user = SessionManager.getUserProfile(context)
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val statusId = user?.status_id ?: 1
    val isDisabled = statusId == 2
    val isBanned = statusId == 3

    // Configuración según el estado
    val statusConfig = when {
        isBanned -> StatusConfig(
            title = "Cuenta Baneada",
            message = "Tu cuenta ha sido baneada y no puedes acceder a la aplicación.\n\nSi crees que esto es un error, por favor contacta al soporte.",
            icon = Icons.Default.Block,
            iconColor = Color(0xFFD32F2F),
            backgroundColor = Color(0xFFFFEBEE)
        )
        isDisabled -> StatusConfig(
            title = "Cuenta Deshabilitada",
            message = "Tu cuenta está temporalmente deshabilitada.\n\nPuedes solicitar la habilitación de tu cuenta o contactar al soporte para más información.",
            icon = Icons.Default.Warning,
            iconColor = Color(0xFFF57C00),
            backgroundColor = Color(0xFFFFF3E0)
        )
        else -> null
    }

    if (statusConfig == null) {
        // Si el estado es válido (1), volver atrás
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Card con la información del estado
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icono de estado con fondo
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                color = statusConfig.backgroundColor,
                                shape = RoundedCornerShape(50.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusConfig.icon,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = statusConfig.iconColor
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Título
                    Text(
                        text = statusConfig.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusConfig.iconColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mensaje
                    Text(
                        text = statusConfig.message,
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botones
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botón de Habilitar (solo si está deshabilitado)
                        if (isDisabled) {
                            Button(
                                onClick = {
                                    // TODO: Implementar lógica para solicitar habilitación
                                    // Esto podría enviar una notificación al admin o crear un ticket
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Text(
                                    text = "Solicitar Habilitación",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Botón de Volver/Cerrar Sesión
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isLoggingOut = true
                                    try {
                                        SessionManager.logout(
                                            context = context,
                                            supabaseClient = SupabaseService.getInstance()
                                        )
                                        onLogoutComplete()
                                    } catch (e: Exception) {
                                        // Manejar error
                                        isLoggingOut = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoggingOut,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF6B7280)
                            )
                        ) {
                            if (isLoggingOut) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Cerrar Sesión",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Información de contacto
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¿Necesitas ayuda?\nContacta a soporte@safezone.com",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

// Data class para configuración de estado
private data class StatusConfig(
    val title: String,
    val message: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color,
    val backgroundColor: Color
)