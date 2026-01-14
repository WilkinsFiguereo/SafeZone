package com.wilkins.safezone.frontend.ui.user.Screens.profile.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.safezone.ui.theme.PrimaryColor

@Composable
fun ProfileNameSection(
    name: String?,
    pronouns: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = name ?: "Usuario sin nombre",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F1F1F),
            lineHeight = 30.sp
        )

        if (!pronouns.isNullOrEmpty()) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = PrimaryColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = pronouns,
                    fontSize = 12.sp,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileStatusSection(
    statusId: Int?,
    followStats: com.wilkins.safezone.backend.network.User.Profile.FollowStats?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Estado (Online/Offline)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Indicador de estado
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        when (statusId) {
                            1 -> Color(0xFF4CAF50) // Online - Verde
                            2 -> Color(0xFFFFC107) // Ausente - Amarillo
                            3 -> Color(0xFFE53935) // Ocupado - Rojo
                            else -> Color(0xFF9E9E9E) // Offline - Gris
                        }
                    )
            )

            Text(
                text = when (statusId) {
                    1 -> "En línea"
                    2 -> "Ausente"
                    3 -> "Ocupado"
                    else -> "Desconectado"
                },
                fontSize = 14.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )
        }

        // Estadísticas de seguimiento
        if (followStats != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seguidores
                FollowStatItem(
                    count = followStats.followers_count,
                    label = "Seguidores"
                )

                // Separador
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(Color(0xFFE0E0E0))
                )

                // Siguiendo
                FollowStatItem(
                    count = followStats.following_count,
                    label = "Siguiendo"
                )
            }
        }
    }
}

@Composable
private fun FollowStatItem(
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = count.toString(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF757575)
        )
    }
}

@Composable
fun ProfileDescriptionCard(
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8F9FA)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Color(0xFF757575),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color(0xFF616161),
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ProfileContactInfo(
    email: String?,
    phone: String?,
    address: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Información de contacto",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF757575)
        )

        ContactInfoRow(
            icon = Icons.Outlined.Email,
            text = email ?: "No especificado"
        )

        if (!phone.isNullOrEmpty()) {
            ContactInfoRow(
                icon = Icons.Outlined.Phone,
                text = phone
            )
        }

        if (!address.isNullOrEmpty()) {
            ContactInfoRow(
                icon = Icons.Outlined.LocationOn,
                text = address
            )
        }
    }
}

@Composable
fun ContactInfoRow(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF8F9FA)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF9E9E9E),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                fontSize = 14.sp,
                color = Color(0xFF424242),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

fun getStatusColor(statusId: Int?): Color {
    return when (statusId) {
        1 -> Color(0xFF4CAF50) // Online - Verde
        2 -> Color(0xFF9E9E9E) // Offline - Gris
        3 -> Color(0xFFF44336) // No disponible - Rojo
        else -> Color(0xFF9E9E9E)
    }
}

fun getStatusText(statusId: Int?): String {
    return when (statusId) {
        1 -> "Online"
        2 -> "Offline"
        3 -> "No disponible"
        else -> "Desconocido"
    }
}