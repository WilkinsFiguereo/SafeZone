package com.wilkins.safezone.frontend.ui.user.Screens.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wilkins.safezone.GenericUserUi.AdminMenu
import com.wilkins.safezone.GenericUserUi.SideMenu
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.User.Profile.FollowState
import com.wilkins.safezone.backend.network.User.Profile.FollowViewModel
import com.wilkins.safezone.backend.network.User.Profile.ProfileViewModel
import com.wilkins.safezone.backend.network.User.Profile.ReportWithAffair
import com.wilkins.safezone.backend.network.User.Profile.getUserReports
import com.wilkins.safezone.backend.network.auth.SessionManager
import com.wilkins.safezone.backend.network.auth.SessionManager.getUserProfile
import com.wilkins.safezone.frontend.ui.GlobalAssociation.GovernmentMenu
import com.wilkins.safezone.frontend.ui.Moderator.ModeratorSideMenu
import com.wilkins.safezone.frontend.ui.user.Screens.profile.Components.EditProfileContent
import com.wilkins.safezone.frontend.ui.user.Screens.profile.Components.ProfileContent
import com.wilkins.safezone.navigation.theme.PrimaryColor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@Composable
fun ProfileScreenWithMenu(
    userId: String,
    userName: String,
    navController: NavController,
    supabaseClient: SupabaseClient,
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToChangeEmail: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    var isMenuOpen by remember { mutableStateOf(false) }
    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""

    Box(modifier = modifier.fillMaxSize()) {

        // 🔹 CONTENIDO PRINCIPAL
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(56.dp))

            ProfileScreen(
                userId = userId,
                supabaseClient = supabaseClient,
                onNavigateToChangePassword = onNavigateToChangePassword,
                onNavigateToChangeEmail = onNavigateToChangeEmail,
                viewModel = viewModel
            )
        }

        // 🔹 MENÚ SEGÚN ROL (COMO QUERÍAS)
        RoleBasedMenu(
            navController = navController,
            currentRoute = currentRoute,
            isMenuOpen = isMenuOpen,
            onMenuToggle = { isMenuOpen = it },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}


@Composable
fun ProfileScreen(
    userId: String,
    supabaseClient: SupabaseClient,
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToChangeEmail: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
    followViewModel: FollowViewModel = remember { FollowViewModel(supabaseClient) }
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val followState by followViewModel.followState.collectAsState()
    val isFollowing by followViewModel.isFollowing.collectAsState()
    val followStats by followViewModel.followStats.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var userReports by remember { mutableStateOf<List<ReportWithAffair>>(emptyList()) }
    var isLoadingReports by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar perfil y estadísticas
    LaunchedEffect(userId) {
        Log.d("ProfileScreen", "🔄 Cargando perfil para userId: ${userId.take(8)}")
        viewModel.loadProfile(context, userId)
        followViewModel.loadFollowStats(userId)

        if (!uiState.isOwnProfile) {
            val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
            if (currentUserId != null && currentUserId != userId) {
                followViewModel.checkIfFollowing(currentUserId, userId)
            }
        }
    }

    // Cargar reportes
    LaunchedEffect(userId) {
        Log.d("ProfileScreen", "✅ Cargando reportes del usuario...")
        isLoadingReports = true
        scope.launch {
            try {
                userReports = getUserReports(supabaseClient, userId)
                Log.d("ProfileScreen", "✅ Reportes cargados: ${userReports.size}")
            } catch (e: Exception) {
                Log.e("ProfileScreen", "❌ Error cargando reportes: ${e.message}", e)
            } finally {
                isLoadingReports = false
            }
        }
    }

    // Manejar mensajes de seguimiento
    LaunchedEffect(followState) {
        when (val state = followState) {
            is FollowState.Success -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                followViewModel.resetState()
            }
            is FollowState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
                followViewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryColor
                )
            }
            uiState.error != null -> {
                ErrorState(
                    error = uiState.error ?: "Error desconocido",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.userProfile != null -> {
                if (isEditing && uiState.isOwnProfile) {
                    EditProfileContent(
                        userProfile = uiState.userProfile!!,
                        viewModel = viewModel,
                        onSave = { name, phone, pronouns, description, address, statusId ->
                            viewModel.updateProfile(context, name, phone, pronouns, description, address, statusId)
                            isEditing = false
                        },
                        onCancel = { isEditing = false },
                        onNavigateToChangePassword = onNavigateToChangePassword,
                        onNavigateToChangeEmail = onNavigateToChangeEmail
                    )
                } else {
                    ProfileContent(
                        userProfile = uiState.userProfile!!,
                        isOwnProfile = uiState.isOwnProfile,
                        viewModel = viewModel,
                        followViewModel = followViewModel,
                        userReports = userReports,
                        isLoadingReports = isLoadingReports,
                        isFollowing = isFollowing,
                        followStats = followStats,
                        onEditClick = { isEditing = true },
                        onFollowClick = {
                            val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
                            if (currentUserId != null) {
                                followViewModel.toggleFollow(currentUserId, userId)
                            }
                        }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun ErrorState(
    error: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = Color.Red,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = error,
            color = Color.Red,
            fontSize = 16.sp
        )
    }
}
@Composable
fun RoleBasedMenu(
    navController: NavController,
    currentRoute: String,
    isMenuOpen: Boolean,
    onMenuToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val supabaseClient = SupabaseService.getInstance()

    // 🔥 Rol desde SessionManager (fuente oficial)
    val roleId = remember {
        SessionManager.getUserRole(context)
    }

    when (roleId) {

        // 🧍 USUARIO NORMAL
        1 -> {
            SideMenu(
                navController = navController,
                userId = SessionManager.loadSession(context)?.user?.id ?: "",
                userName = "Usuario",
                currentRoute = currentRoute,
                isMenuOpen = isMenuOpen,
                onMenuToggle = onMenuToggle,
                context = context,
                supabaseClient = supabaseClient,
                modifier = modifier
            )
        }

        // 👑 ADMIN
        2 -> {
            AdminMenu(
                navController = navController,
                currentRoute = currentRoute,
                isMenuOpen = isMenuOpen,
                onMenuToggle = onMenuToggle,
                modifier = modifier,
                content = content
            )
        }

        // 🛡️ MODERADOR
        3 -> {
            val userState = produceState<AppUser?>(initialValue = null) {
                value = getUserProfile(context)
            }
            val user = userState.value
            ModeratorSideMenu(
                navController = navController,
                moderatorId = SessionManager.loadSession(context)?.user?.id ?: "",
                moderatorName = user?.name ?: "Moderator",
                currentRoute = currentRoute,
                modifier = modifier,
                isMenuOpen = isMenuOpen,
                onMenuToggle = onMenuToggle,
                context = context,
                supabaseClient = supabaseClient
            )
        }

        // 🏛️ GOBIERNO
        4 -> {
            GovernmentMenu(
                navController = navController,
                currentRoute = currentRoute,
                isMenuOpen = isMenuOpen,
                onMenuToggle = onMenuToggle,
                modifier = modifier,
                content = content
            )
        }

        // 🚨 SIN ROL
        else -> {
            LaunchedEffect(Unit) {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
}
