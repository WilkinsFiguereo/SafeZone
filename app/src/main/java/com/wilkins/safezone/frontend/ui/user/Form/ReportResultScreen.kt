package com.wilkins.safezone.frontend.ui.user.Form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wilkins.safezone.ui.theme.PrimaryColor
import kotlinx.coroutines.delay

@Composable
fun ReportResultScreen(
    navController: NavController,
    userId: String,
    isSuccess: Boolean,
    message: String = ""
) {
    // Redirigir después de 3 segundos
    LaunchedEffect(Unit) {
        delay(3000)
        navController.navigate("userHome/$userId") {
            popUpTo(0) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isSuccess) Color(0xFFF0F9F0) else Color(0xFFFDEDED)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ícono simple
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle
                else Icons.Default.Error,
                contentDescription = null,
                tint = if (isSuccess) PrimaryColor else Color(0xFFD32F2F),
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Título
            Text(
                text = if (isSuccess) "¡Listo!" else "Error",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSuccess) PrimaryColor else Color(0xFFD32F2F),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtítulo
            Text(
                text = if (isSuccess) "Reporte enviado" else "No se pudo enviar",
                fontSize = 18.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mensaje
            Text(
                text = if (isSuccess) {
                    "Tu reporte ha sido recibido y será procesado en breve."
                } else {
                    message.ifEmpty { "Inténtalo nuevamente más tarde." }
                },
                fontSize = 16.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Indicador simple
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(4.dp)
                    .background(
                        color = Color(0xFFE0E0E0),
                        shape = CircleShape
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            color = if (isSuccess) PrimaryColor else Color(0xFFD32F2F),
                            shape = CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Redirigiendo...",
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center
            )
        }
    }
}