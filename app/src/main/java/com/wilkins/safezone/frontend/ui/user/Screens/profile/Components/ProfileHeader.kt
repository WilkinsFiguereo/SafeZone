package com.wilkins.safezone.frontend.ui.user.Screens.profile.Components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.wilkins.safezone.backend.network.User.Profile.FollowState
import com.wilkins.safezone.backend.network.User.Profile.FollowViewModel
import com.wilkins.safezone.backend.network.User.ReportSystem.ReportService
import com.wilkins.safezone.backend.network.User.ReportSystem.ReportType
import com.wilkins.safezone.backend.network.auth.SessionManager
import com.wilkins.safezone.frontend.ui.user.Screens.News.components.BanUserDialog
import com.wilkins.safezone.frontend.ui.user.Screens.News.components.ReportUserDialog
import com.wilkins.safezone.ui.theme.PrimaryColor
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@Composable
fun ProfileHeader(
    photoUrl: String?,
    isOwnProfile: Boolean,
    isFollowing: Boolean,
    followViewModel: FollowViewModel,
    userId: String,
    userName: String,
    onEditClick: () -> Unit,
    onFollowClick: () -> Unit,
    onActionComplete: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val followState by followViewModel.followState.collectAsState()
    var showOptionsMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentUserRoleId = remember {
        SessionManager.getUserRole(context)
    }
    val isModerator = currentUserRoleId in 2..4

    // Estados para diálogos
    var showReportDialog by remember { mutableStateOf(false) }
    var showBanDialog by remember { mutableStateOf(false) }
    var reportTypes by remember { mutableStateOf<List<ReportType>>(emptyList()) }
    var isLoadingAction by remember { mutableStateOf(false) }

    val reportService = remember { ReportService() }
    val currentUserId = remember {
        com.wilkins.safezone.backend.network.SupabaseService.getInstance().auth.currentUserOrNull()?.id ?: ""
    }

    Box(modifier = modifier.fillMaxWidth()) {
        // Fondo degradado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            PrimaryColor.copy(alpha = 0.8f),
                            PrimaryColor.copy(alpha = 0.5f)
                        )
                    )
                )
                .zIndex(0f)
        )

        // Contenido
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 60.dp)
                .padding(horizontal = 16.dp)
                .zIndex(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Foto de perfil
                ProfilePhoto(photoUrl = photoUrl)

                // Botones de acción
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isOwnProfile) {
                        EditProfileButton(onClick = onEditClick)
                    } else {
                        FollowButton(
                            isFollowing = isFollowing,
                            followState = followState,
                            onClick = onFollowClick
                        )
                    }

                    // Botón de opciones disponible para todos (isOwnProfile o no)
                    // Solo cambia el contenido del menú según si es moderador
                    if (!isOwnProfile) {
                        ProfileOptionsButton(
                            showMenu = showOptionsMenu,
                            onShowMenuChange = { showOptionsMenu = it },
                            isModerator = isModerator,
                            onReportClick = {
                                scope.launch {
                                    val result = reportService.getAllReportTypes()
                                    result.onSuccess { types ->
                                        reportTypes = types
                                        showReportDialog = true
                                    }
                                }
                            },
                            onBanClick = {
                                showBanDialog = true
                            },
                            onShareClick = {
                                val shareIntent = reportService.createShareProfileIntent(userName, userId)
                                context.startActivity(Intent.createChooser(shareIntent, "Compartir perfil"))
                            },
                            onBlockClick = {
                                onActionComplete("Función de bloqueo próximamente")
                            }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de reporte
    if (showReportDialog) {
        ReportUserDialog(
            userName = userName,
            reportTypes = reportTypes,
            isLoading = isLoadingAction,
            onDismiss = { showReportDialog = false },
            onSubmit = { reportTypeId, message ->
                scope.launch {
                    isLoadingAction = true
                    val result = reportService.createUserReport(
                        reporterId = currentUserId,
                        reportedUserId = userId,
                        reportTypeId = reportTypeId,
                        message = message
                    )
                    isLoadingAction = false

                    result.onSuccess {
                        showReportDialog = false
                        onActionComplete("Reporte enviado exitosamente")
                    }.onFailure {
                        onActionComplete("Error al enviar el reporte")
                    }
                }
            }
        )
    }

    // Diálogo de baneo
    if (showBanDialog) {
        BanUserDialog(
            userName = userName,
            isLoading = isLoadingAction,
            onDismiss = { showBanDialog = false },
            onConfirm = { reason ->
                scope.launch {
                    isLoadingAction = true
                    val result = reportService.banUser(
                        userId = userId,
                        moderatorId = currentUserId,
                        reason = reason
                    )
                    isLoadingAction = false

                    result.onSuccess {
                        showBanDialog = false
                        onActionComplete("Usuario baneado exitosamente")
                    }.onFailure {
                        onActionComplete("Error al banear usuario")
                    }
                }
            }
        )
    }
}

@Composable
private fun ProfilePhoto(
    photoUrl: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(4.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                PrimaryColor.copy(alpha = 0.2f),
                                PrimaryColor.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    tint = PrimaryColor,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}

@Composable
private fun EditProfileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = PrimaryColor
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Editar perfil",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FollowButton(
    isFollowing: Boolean,
    followState: FollowState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = followState !is FollowState.Loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFollowing) Color(0xFFE0E0E0) else PrimaryColor,
            contentColor = if (isFollowing) Color(0xFF757575) else Color.White,
            disabledContainerColor = if (isFollowing) Color(0xFFE0E0E0) else PrimaryColor.copy(alpha = 0.6f),
            disabledContentColor = if (isFollowing) Color(0xFF757575) else Color.White.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
    ) {
        if (followState is FollowState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = if (isFollowing) Color(0xFF757575) else Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = if (isFollowing) Icons.Default.Check else Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isFollowing) "Siguiendo" else "Seguir",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ProfileOptionsButton(
    showMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit,
    isModerator: Boolean,
    onReportClick: () -> Unit,
    onBanClick: () -> Unit,
    onBlockClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        IconButton(
            onClick = { onShowMenuChange(true) },
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Más opciones",
                tint = Color(0xFF757575)
            )
        }

        // El menú se muestra aquí
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { onShowMenuChange(false) },
            modifier = Modifier
                .background(Color.White)
                .width(220.dp)
        ) {
            // Reportar (disponible para todos)
            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Reportar usuario",
                            fontSize = 14.sp,
                            color = Color(0xFF212121)
                        )
                    }
                },
                onClick = {
                    onShowMenuChange(false)
                    onReportClick()
                }
            )

            // Bloquear (disponible para todos)
            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Block,
                            contentDescription = null,
                            tint = Color(0xFFFF6F00),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Bloquear usuario",
                            fontSize = 14.sp,
                            color = Color(0xFF212121)
                        )
                    }
                },
                onClick = {
                    onShowMenuChange(false)
                    onBlockClick()
                }
            )

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
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Compartir perfil",
                            fontSize = 14.sp,
                            color = Color(0xFF212121)
                        )
                    }
                },
                onClick = {
                    onShowMenuChange(false)
                    onShareClick()
                }
            )

            // Opciones de moderador (solo si isModerator = true)
            if (isModerator) {
                Divider(
                    color = Color(0xFFE0E0E0),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Banear usuario
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
                                    color = Color(0xFF212121),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Acción de moderador",
                                    fontSize = 11.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                    },
                    onClick = {
                        onShowMenuChange(false)
                        onBanClick()
                    }
                )

                // Ver historial de moderación
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Historial de moderación",
                                fontSize = 14.sp,
                                color = Color(0xFF212121)
                            )
                        }
                    },
                    onClick = {
                        onShowMenuChange(false)
                        // TODO: Implementar historial
                    }
                )
            }
        }
    }
}