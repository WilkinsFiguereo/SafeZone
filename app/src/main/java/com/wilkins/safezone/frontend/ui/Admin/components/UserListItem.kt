package com.wilkins.safezone.frontend.ui.Admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.safezone.backend.network.Admin.CrudUser.UserProfileViewModel
import com.wilkins.safezone.backend.network.Admin.CrudUser.Usuario

@Composable
fun UserListItem(
    usuario: Usuario,
    viewModel: UserProfileViewModel, // ✅ se pasa el ViewModel
    onClick: (String) -> Unit,        // ✅ ahora recibe el uuid del usuario
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Columna Usuario
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = usuario.nombre,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "ID: ${usuario.id}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Columna Rol
        Box(
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.CenterHorizontally)
        ) {
            val roleColor = when (usuario.roleId) {
                1 -> Color(0xFFFF6B6B)
                2 -> Color(0xFF4ECDC4)
                3 -> Color(0xFF45B7D1)
                else -> MaterialTheme.colorScheme.primary
            }

            Text(
                text = usuario.rol,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier
                    .background(roleColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }

        // Columna Email
        Text(
            text = usuario.email ?: "Sin email",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1.5f)
                .wrapContentWidth(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )

        // Columna Acciones - Botón Ver
        Box(
            modifier = Modifier
                .weight(0.8f)
                .wrapContentWidth(Alignment.CenterHorizontally)
        ) {
            IconButton(
                onClick = {
                    viewModel.selectUser(usuario.idCompleto)  // ✅ Guardamos el UUID en el ViewModel
                    onClick(usuario.idCompleto)               // ✅ Navegamos pasando el UUID
                },
                modifier = Modifier.size(36.dp)
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
