package com.wilkins.safezone.frontend.ui.Admin.Screens.CrudUser.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.safezone.backend.network.Admin.CrudUser.UserProfileViewModel
import com.wilkins.safezone.backend.network.Admin.CrudUser.Usuario

@Composable
fun UserListItem(
    usuario: Usuario,
    viewModel: UserProfileViewModel,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable {
                viewModel.selectUser(usuario.idCompleto)
                onClick(usuario.idCompleto)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar circular con inicial
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (usuario.roleId) {
                            1 -> Color(0xFFFF6B6B).copy(alpha = 0.2f)
                            2 -> Color(0xFF4ECDC4).copy(alpha = 0.2f)
                            3 -> Color(0xFF45B7D1).copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = usuario.nombre.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (usuario.roleId) {
                        1 -> Color(0xFFFF6B6B)
                        2 -> Color(0xFF4ECDC4)
                        3 -> Color(0xFF45B7D1)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }

            // Información del usuario
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Nombre y rol en la misma línea
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = usuario.nombre,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Badge de rol compacto
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (usuario.roleId) {
                            1 -> Color(0xFFFF6B6B)
                            2 -> Color(0xFF4ECDC4)
                            3 -> Color(0xFF45B7D1)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    ) {
                        Text(
                            text = usuario.rol,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Email
                Text(
                    text = usuario.email ?: "Sin email",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // ID
                Text(
                    text = "ID: ${usuario.id}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Botón de acción
            FilledTonalIconButton(
                onClick = {
                    viewModel.selectUser(usuario.idCompleto)
                    onClick(usuario.idCompleto)
                },
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = "Ver detalles",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Alternativa con diseño más compacto
@Composable
fun UserListItemCompact(
    usuario: Usuario,
    viewModel: UserProfileViewModel,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                viewModel.selectUser(usuario.idCompleto)
                onClick(usuario.idCompleto)
            },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar pequeño
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (usuario.roleId) {
                            1 -> Color(0xFFFF6B6B).copy(alpha = 0.15f)
                            2 -> Color(0xFF4ECDC4).copy(alpha = 0.15f)
                            3 -> Color(0xFF45B7D1).copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = usuario.nombre.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when (usuario.roleId) {
                        1 -> Color(0xFFFF6B6B)
                        2 -> Color(0xFF4ECDC4)
                        3 -> Color(0xFF45B7D1)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = usuario.nombre,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "• ${usuario.rol}",
                        fontSize = 12.sp,
                        color = when (usuario.roleId) {
                            1 -> Color(0xFFFF6B6B)
                            2 -> Color(0xFF4ECDC4)
                            3 -> Color(0xFF45B7D1)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = usuario.email ?: "Sin email",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Icono
            IconButton(
                onClick = {
                    viewModel.selectUser(usuario.idCompleto)
                    onClick(usuario.idCompleto)
                }
            ) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = "Ver detalles",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}