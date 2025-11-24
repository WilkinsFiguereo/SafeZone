package com.wilkins.safezone.GenericUserUi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.safezone.ui.theme.PrimaryColor

@Composable
fun BottomNavigationMenu(
    modifier: Modifier = Modifier,
    onNewsClick: () -> Unit = {},
    onAlertClick: () -> Unit = {},
    onMyAlertsClick: () -> Unit = {},
    selectedItem: Int = 0
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        // Barra inferior blanca con forma redondeada en el centro
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .align(Alignment.BottomCenter),
            color = Color.White,
            shadowElevation = 16.dp,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Noticias (Izquierda)
                BottomNavItem(
                    icon = Icons.Outlined.Newspaper,
                    label = "Noticias",
                    isSelected = selectedItem == 0,
                    onClick = onNewsClick
                )

                // Espacio para el botón flotante central
                Spacer(modifier = Modifier.width(100.dp))

                // Botón Mis Alertas (Derecha)
                BottomNavItem(
                    icon = Icons.Outlined.RemoveRedEye,
                    label = "Mis Alertas",
                    isSelected = selectedItem == 2,
                    onClick = onMyAlertsClick
                )
            }
        }

        // Botón flotante central (Enviar Alerta) con anillo decorativo
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 5.dp)
        ) {
            // Anillo exterior decorativo
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .background(
                        color = PrimaryColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
                    .align(Alignment.Center)
            )

            // Botón principal
            FloatingActionButton(
                onClick = onAlertClick,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(68.dp)
                    .shadow(16.dp, CircleShape),
                containerColor = PrimaryColor,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Enviar Alerta",
                        modifier = Modifier.size(32.dp)
                    )

                }
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (isSelected) PrimaryColor.copy(alpha = 0.1f) else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) PrimaryColor else Color.Gray,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) PrimaryColor else Color.Gray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationMenuPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Preview con diferentes estados seleccionados
            BottomNavigationMenu(
                selectedItem = 0,
                onNewsClick = { println("Noticias clicked") },
                onAlertClick = { println("Enviar Alerta clicked") },
                onMyAlertsClick = { println("Mis Alertas clicked") }
            )
        }
    }
}