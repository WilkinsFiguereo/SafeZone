package com.wilkins.safezone.frontend.ui.user.profile

import SessionManager.logout
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.wilkins.safezone.backend.network.User.Profile.ProfileViewModel
import com.wilkins.safezone.ui.theme.NameApp
import com.wilkins.safezone.ui.theme.PrimaryColor
import io.github.jan.supabase.SupabaseClient
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
        // Contenido principal del perfil
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            TopBar(
                userName = userName,
                onMenuToggle = { isMenuOpen = !isMenuOpen },
                onProfileClick = { navController.navigate("navigationDrawer") }
            )

            // Profile Content
            ProfileScreen(
                userId = userId,
                onNavigateToChangePassword = onNavigateToChangePassword,
                onNavigateToChangeEmail = onNavigateToChangeEmail,
                viewModel = viewModel
            )
        }

        // Menú lateral superpuesto
        SideMenuOverlay(
            isOpen = isMenuOpen,
            navController = navController,
            userId = userId,
            userName = userName,
            currentRoute = currentRoute,
            context = context,
            supabaseClient = supabaseClient,
            onMenuToggle = { isMenuOpen = it }
        )
    }
}

@Composable
fun TopBar(
    userName: String,
    onMenuToggle: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuToggle) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = NameApp,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onProfileClick) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SideMenuOverlay(
    isOpen: Boolean,
    navController: NavController,
    userId: String,
    userName: String,
    currentRoute: String,
    context: Context,
    supabaseClient: SupabaseClient,
    onMenuToggle: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()

    val menuSections = listOf(
        MenuSection(
            title = "Principal",
            items = listOf(
                MenuItem(Icons.Default.Home, "Inicio", "userHome/$userId"),
                MenuItem(Icons.Default.Person, "Mi Perfil", "MyProfile")
            )
        ),
        MenuSection(
            title = "Información",
            items = listOf(
                MenuItem(Icons.Default.Newspaper, "Noticias", "NewsUser"),
                MenuItem(Icons.Default.Place, "Reportes en tu zona", "MapReports")
            )
        ),
        MenuSection(
            title = "Alertas",
            items = listOf(
                MenuItem(Icons.Default.Warning, "Alerta una emergencia", "FormUser"),
                MenuItem(Icons.Default.Visibility, "Mis alertas", "MyAlerts"),
                MenuItem(Icons.Default.Notifications, "Notificaciones", "Notification")
            )
        ),
        MenuSection(
            title = "Configuración",
            items = listOf(
                MenuItem(Icons.Default.Settings, "Configuración", "settings"),
                MenuItem(Icons.Default.Logout, "Cerrar Sesión", "logout")
            )
        )
    )

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(300)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(300)
        )
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Menú lateral
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .background(PrimaryColor)
            ) {
                // Header del menú
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = userName,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Secciones del menú
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    menuSections.forEach { section ->
                        MenuSectionComponent(
                            section = section,
                            currentRoute = currentRoute,
                            onItemClick = { route ->
                                onMenuToggle(false)

                                if (route == "logout") {
                                    scope.launch {
                                        logout(context, supabaseClient)
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                } else {
                                    navController.navigate(route)
                                }
                            }
                        )
                    }
                }

                // Footer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SafeZone App",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Versión 1.0.0",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Área de cierre (click fuera del menú)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onMenuToggle(false) }
            )
        }
    }
}

@Composable
fun MenuSectionComponent(
    section: MenuSection,
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = section.title,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        section.items.forEach { item ->
            val isSelected = currentRoute == item.route

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
                    .clickable { onItemClick(item.route) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.label,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

data class MenuItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

data class MenuSection(
    val title: String,
    val items: List<MenuItem>
)

// Componentes originales del perfil
@Composable
fun ProfileScreen(
    userId: String,
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToChangeEmail: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadProfile(context, userId)
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
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
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
                        text = uiState.error ?: "Error desconocido",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
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
                        onEditClick = { isEditing = true },
                        onFollowClick = { /* TODO: Implementar seguir */ }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    userProfile: com.wilkins.safezone.backend.network.AppUser,
    isOwnProfile: Boolean,
    viewModel: ProfileViewModel,
    onEditClick: () -> Unit,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photoUrl = remember(userProfile.photoProfile) {
        viewModel.getProfilePhotoUrl(userProfile.photoProfile)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    PrimaryColor.copy(alpha = 0.8f),
                                    PrimaryColor.copy(alpha = 0.5f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-40).dp)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
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
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                        }

                        if (isOwnProfile) {
                            OutlinedButton(
                                onClick = onEditClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
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
                        } else {
                            Button(
                                onClick = onFollowClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryColor,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Seguir",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = userProfile.name ?: "Usuario sin nombre",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F1F1F),
                            lineHeight = 26.sp
                        )

                        if (!userProfile.pronouns.isNullOrEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PrimaryColor.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = userProfile.pronouns,
                                    fontSize = 12.sp,
                                    color = PrimaryColor,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = getStatusColor(userProfile.status_id).copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(getStatusColor(userProfile.status_id))
                            )
                            Text(
                                text = getStatusText(userProfile.status_id),
                                fontSize = 13.sp,
                                color = getStatusColor(userProfile.status_id),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (!userProfile.description.isNullOrEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
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
                                    text = userProfile.description,
                                    fontSize = 13.sp,
                                    color = Color(0xFF616161),
                                    lineHeight = 19.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "Información de contacto",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF757575)
                        )

                        ContactInfoRow(
                            icon = Icons.Outlined.Email,
                            text = userProfile.email ?: "No especificado"
                        )

                        if (!userProfile.phone.isNullOrEmpty()) {
                            ContactInfoRow(
                                icon = Icons.Outlined.Phone,
                                text = userProfile.phone
                            )
                        }

                        if (!userProfile.address.isNullOrEmpty()) {
                            ContactInfoRow(
                                icon = Icons.Outlined.LocationOn,
                                text = userProfile.address
                            )
                        }
                    }
                }
            }
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(
    userProfile: com.wilkins.safezone.backend.network.AppUser,
    viewModel: ProfileViewModel,
    onSave: (String, String, String, String, String, Int) -> Unit,
    onCancel: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToChangeEmail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf(userProfile.name ?: "") }
    var phone by remember { mutableStateOf(userProfile.phone ?: "") }
    var pronouns by remember { mutableStateOf(userProfile.pronouns ?: "") }
    var description by remember { mutableStateOf(userProfile.description ?: "") }
    var address by remember { mutableStateOf(userProfile.address ?: "") }
    var selectedStatus by remember { mutableStateOf(userProfile.status_id ?: 1) }
    var showPhone by remember { mutableStateOf(true) }
    var showAddress by remember { mutableStateOf(true) }
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedPronouns by remember { mutableStateOf(false) }

    val photoUrl = remember(userProfile.photoProfile) {
        viewModel.getProfilePhotoUrl(userProfile.photoProfile)
    }

    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadProfilePhoto(context, it)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editar perfil",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F)
                    )
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF757575)
                        )
                    }
                }

                Divider(color = Color(0xFFE0E0E0))

                // Profile Photo Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, PrimaryColor.copy(alpha = 0.3f), CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isUploadingPhoto) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = PrimaryColor
                            )
                        } else if (photoUrl != null) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Cambiar foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Agregar foto",
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Toca para cambiar foto",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }

                Divider(color = Color(0xFFE0E0E0))

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre completo") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = PrimaryColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    )
                )

                // Phone Field with Privacy Toggle
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Phone,
                                contentDescription = null,
                                tint = PrimaryColor
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (showPhone) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (showPhone) "Visible para todos" else "Oculto",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        Switch(
                            checked = showPhone,
                            onCheckedChange = { showPhone = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryColor,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                }

                // Pronouns Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedPronouns,
                    onExpandedChange = { expandedPronouns = it }
                ) {
                    OutlinedTextField(
                        value = pronouns.ifEmpty { "Seleccionar pronombres" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pronombres") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Badge,
                                contentDescription = null,
                                tint = PrimaryColor
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPronouns)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedPronouns,
                        onDismissRequest = { expandedPronouns = false }
                    ) {
                        listOf(
                            "Él",
                            "Ella",
                            "Elle",
                            "Él/Ella",
                            "Ella/Él",
                            "Elle/Él",
                            "Elle/Ella",
                            "Prefiero no decir"
                        ).forEach { pronoun ->
                            DropdownMenuItem(
                                text = { Text(pronoun) },
                                onClick = {
                                    pronouns = pronoun
                                    expandedPronouns = false
                                }
                            )
                        }
                    }
                }

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = PrimaryColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    ),
                    minLines = 3,
                    maxLines = 5
                )

                // Address Field with Privacy Toggle
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Dirección") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = PrimaryColor
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { /* TODO: Abrir mapa */ }) {
                                Icon(
                                    imageVector = Icons.Outlined.Map,
                                    contentDescription = "Seleccionar en mapa",
                                    tint = PrimaryColor
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (showAddress) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (showAddress) "Visible para todos" else "Oculto",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        Switch(
                            checked = showAddress,
                            onCheckedChange = { showAddress = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryColor,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                }

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = it }
                ) {
                    OutlinedTextField(
                        value = getStatusText(selectedStatus),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(getStatusColor(selectedStatus))
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        listOf(
                            1 to "Online",
                            2 to "Offline",
                            3 to "No disponible"
                        ).forEach { (id, status) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(getStatusColor(id))
                                        )
                                        Text(status)
                                    }
                                },
                                onClick = {
                                    selectedStatus = id
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }

                // Email Field (Read-only)
                OutlinedTextField(
                    value = userProfile.email ?: "",
                    onValueChange = {},
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E)
                        )
                    },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color(0xFFE0E0E0),
                        disabledTextColor = Color(0xFF757575)
                    )
                )

                Divider(color = Color(0xFFE0E0E0))

                // Security Buttons
                Text(
                    text = "Seguridad",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )

                OutlinedButton(
                    onClick = onNavigateToChangeEmail,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiar email", fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = onNavigateToChangePassword,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiar contraseña", fontSize = 14.sp)
                }

                Divider(color = Color(0xFFE0E0E0))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            onSave(name, phone, pronouns, description, address, selectedStatus)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor
                        )
                    ) {
                        Text("Guardar")
                    }
                }
            }
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