package com.wilkins.safezone.frontend.ui.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.safezone.ui.theme.PrimaryColor

@Composable
fun AccountDisabledScreen(
    statusId: Int,
    onBackToLogin: () -> Unit
) {
    val (icon, title, message, color) = when (statusId) {
        2 -> AccountStatusInfo(
            icon = Icons.Default.Block,
            title = "Cuenta Deshabilitada",
            message = "Tu cuenta ha sido temporalmente deshabilitada por un administrador.\n\n" +
                    "Posibles razones:\n" +
                    "• Violación de las normas de la comunidad\n" +
                    "• Actividad sospechosa detectada\n" +
                    "• Reportes de otros usuarios\n\n" +
                    "Si crees que esto es un error, por favor contacta a soporte técnico.",
            color = Color(0xFFFF9800) // Naranja
        )
        3 -> AccountStatusInfo(
            icon = Icons.Default.Cancel,
            title = "Cuenta Baneada",
            message = "Tu cuenta ha sido permanentemente baneada.\n\n" +
                    "Esta decisión fue tomada debido a violaciones graves de nuestros términos de servicio.\n\n" +
                    "Si deseas apelar esta decisión, contacta a nuestro equipo de soporte.",
            color = Color(0xFFD32F2F) // Rojo
        )
        else -> AccountStatusInfo(
            icon = Icons.Default.Warning,
            title = "Cuenta No Disponible",
            message = "Tu cuenta no está disponible en este momento.\n\n" +
                    "Por favor, contacta a soporte para más información.",
            color = Color.Gray
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono principal
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(60.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Título
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mensaje
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        color = Color(0xFF424242),
                        textAlign = TextAlign.Start,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Información de contacto
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryColor.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Soporte Técnico",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = "soporte@safezone.com",
                            fontSize = 13.sp,
                            color = PrimaryColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón volver al login
            Button(
                onClick = onBackToLogin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Volver al Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Text(
                text = "ID de Estado: $statusId",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Data class para organizar la información de cada estado
private data class AccountStatusInfo(
    val icon: ImageVector,
    val title: String,
    val message: String,
    val color: Color
)

// Preview para estado Deshabilitado
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewAccountDisabledScreen() {
    AccountDisabledScreen(
        statusId = 2,
        onBackToLogin = {}
    )
}

// Preview para estado Baneado
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewAccountBannedScreen() {
    AccountDisabledScreen(
        statusId = 3,
        onBackToLogin = {}
    )
}