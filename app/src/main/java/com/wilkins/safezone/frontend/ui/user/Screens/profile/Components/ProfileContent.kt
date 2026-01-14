package com.wilkins.safezone.frontend.ui.user.Screens.profile.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wilkins.safezone.backend.network.User.Profile.FollowViewModel
import com.wilkins.safezone.backend.network.User.Profile.ProfileViewModel
import com.wilkins.safezone.backend.network.User.Profile.ReportWithAffair
import kotlinx.coroutines.launch

@Composable
fun ProfileContent(
    userProfile: com.wilkins.safezone.backend.network.AppUser,
    isOwnProfile: Boolean,
    viewModel: ProfileViewModel,
    followViewModel: FollowViewModel,
    userReports: List<ReportWithAffair>,
    isLoadingReports: Boolean,
    isFollowing: Boolean,
    followStats: com.wilkins.safezone.backend.network.User.Profile.FollowStats?,
    onEditClick: () -> Unit,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photoUrl = remember(userProfile.photoProfile) {
        viewModel.getProfilePhotoUrl(userProfile.photoProfile)
    }

    // Estados
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado mutable para el status del usuario (para actualizar cuando se banee/desbanee)
    var currentStatusId by remember { mutableStateOf(userProfile.status_id ?: 1) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            // Header con foto de perfil y botones
            ProfileHeader(
                photoUrl = photoUrl,
                isOwnProfile = isOwnProfile,
                isFollowing = isFollowing,
                followViewModel = followViewModel,
                userId = userProfile.id,
                userName = userProfile.name ?: "Usuario",
                userStatusId = currentStatusId,
                onEditClick = onEditClick,
                onFollowClick = onFollowClick,
                onStatusChanged = { newStatusId ->
                    currentStatusId = newStatusId
                },
                onActionComplete = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(80.dp))

            // Información del perfil
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nombre y pronombres
                ProfileNameSection(
                    name = userProfile.name,
                    pronouns = userProfile.pronouns
                )

                // Estado y estadísticas de seguimiento
                ProfileStatusSection(
                    statusId = currentStatusId, // Usar el estado mutable
                    followStats = followStats
                )

                // Descripción
                if (!userProfile.description.isNullOrEmpty()) {
                    ProfileDescriptionCard(description = userProfile.description)
                }

                Divider(
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Información de contacto
                ProfileContactInfo(
                    email = userProfile.email,
                    phone = userProfile.phone,
                    address = userProfile.address
                )

                Divider(
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Reportes
                UserReportsSection(
                    reports = userReports,
                    isLoading = isLoadingReports
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Snackbar para mostrar mensajes de acciones
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}