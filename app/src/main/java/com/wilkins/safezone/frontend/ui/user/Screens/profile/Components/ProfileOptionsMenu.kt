package com.wilkins.safezone.frontend.ui.user.Screens.profile.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileOptionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    isModerator: Boolean,
    onReportClick: () -> Unit,
    onBanClick: () -> Unit,
    onBlockClick: () -> Unit,
    onShareClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(Color.White)
            .width(220.dp)
    ) {
        // Compartir perfil
        DropdownMenuItem(
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Compartir perfil",
                        fontSize = 14.sp,
                        color = Color(0xFF1F1F1F)
                    )
                }
            },
            onClick = onShareClick
        )

        Divider(color = Color(0xFFE0E0E0))

        // Bloquear
        DropdownMenuItem(
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Block,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Bloquear",
                        fontSize = 14.sp,
                        color = Color(0xFF1F1F1F)
                    )
                }
            },
            onClick = onBlockClick
        )

        // Reportar
        DropdownMenuItem(
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Flag,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Reportar usuario",
                        fontSize = 14.sp,
                        color = Color(0xFF1F1F1F)
                    )
                }
            },
            onClick = onReportClick
        )

        // Opción de moderador
        if (isModerator) {
            Divider(color = Color(0xFFE0E0E0))

            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Gavel,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Banear usuario",
                                fontSize = 14.sp,
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Solo moderadores",
                                fontSize = 10.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                },
                onClick = onBanClick
            )
        }
    }
}