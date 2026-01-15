package com.wilkins.safezone.frontend.ui.Moderator

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.wilkins.safezone.backend.network.Admin.CrudUser.CrudUser
import com.wilkins.safezone.backend.network.Admin.CrudUser.Profile
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.auth.SessionManager.getUserProfile
import com.wilkins.safezone.navigation.theme.PrimaryColor
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

@Composable
fun ModeratorUsersScreen(
    navController: NavController,
    moderatorId: String,
    moderatorName: String = "Moderador"
) {
    val context = LocalContext.current
    val supabaseClient = SupabaseService.getInstance()
    val scope = rememberCoroutineScope()
    val crudUser = remember { CrudUser() }

    var isMenuOpen by remember { mutableStateOf(false) }
    var users by remember { mutableStateOf<List<Profile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Cargar usuarios al iniciar
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                users = crudUser.getAllProfiles()
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Error al cargar usuarios: ${e.message}"
                isLoading = false
            }
        }
    }

    // Filtrar usuarios según búsqueda
    val filteredUsers = users.filter { user ->
        user.name.contains(searchQuery, ignoreCase = true) ||
                user.email?.contains(searchQuery, ignoreCase = true) == true ||
                user.id.contains(searchQuery, ignoreCase = true)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Barra de búsqueda
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar usuario por nombre, email o ID...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar búsqueda",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }

            // Estadísticas rápidas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatsCard(
                    title = "Total",
                    value = users.size.toString(),
                    icon = Icons.Default.People,
                    color = PrimaryColor,
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    title = "Activos",
                    value = users.count { it.statusId == 1 }.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    title = "Inactivos",
                    value = users.count { it.statusId != 1 }.toString(),
                    icon = Icons.Default.Block,
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contenido principal
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = PrimaryColor)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cargando usuarios...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = errorMessage ?: "Error desconocido",
                                color = Color.Red,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                filteredUsers.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonOff,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isEmpty()) "No hay usuarios disponibles"
                                else "No se encontraron usuarios",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredUsers) { user ->
                            UserCard(
                                user = user,
                                onClick = {
                                    navController.navigate("profile/${user.id}/${user.name}")
                                }
                            )
                        }
                    }
                }
            }
        }

        val userState = produceState<AppUser?>(initialValue = null) {
            value = getUserProfile(context)
        }
        val user = userState.value
        if (isMenuOpen) {
            ModeratorSideMenu(
                navController = navController,
                moderatorId = moderatorId,
                moderatorName = user?.name ?: "Moderator",
                currentRoute = "moderatorDashboard",
                isMenuOpen = isMenuOpen,
                onMenuToggle = { isMenuOpen = it },
                context = context,
                supabaseClient = supabaseClient
            )
        }
    }
}

@Composable
fun UserCard(
    user: Profile,
    onClick: () -> Unit
) {
    // Obtener URL de la foto de perfil si existe
    val profilePhotoUrl = remember(user.photoProfile) {
        if (!user.photoProfile.isNullOrEmpty()) {
            try {
                val bucket = SupabaseService.getInstance().storage.from("UserProfile")
                bucket.publicUrl(user.photoProfile!!)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto de perfil
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(PrimaryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (profilePhotoUrl != null) {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(profilePhotoUrl)
                            .crossfade(true)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_report_image)
                            .build(),
                        contentDescription = "Foto de ${user.name}",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del usuario
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email ?: "Sin email",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ID: ${user.id.take(8)}...",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Badge de rol y estado
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Rol
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PrimaryColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = user.rol?.name ?: "Sin rol",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = PrimaryColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Estado
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (user.statusId) {
                        1 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        2 -> Color(0xFFF44336).copy(alpha = 0.1f)
                        3 -> Color(0xFFFF9800).copy(alpha = 0.1f)
                        4 -> Color.Red.copy(alpha = 0.1f)
                        else -> Color.Gray.copy(alpha = 0.1f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    when (user.statusId) {
                                        1 -> Color(0xFF4CAF50)
                                        2 -> Color(0xFFF44336)
                                        3 -> Color(0xFFFF9800)
                                        4 -> Color.Red
                                        else -> Color.Gray
                                    }
                                )
                        )
                        Text(
                            text = when (user.statusId) {
                                1 -> "Activo"
                                2 -> "Inactivo"
                                3 -> "Pendiente"
                                4 -> "Bloqueado"
                                else -> "Desconocido"
                            },
                            fontSize = 11.sp,
                            color = when (user.statusId) {
                                1 -> Color(0xFF4CAF50)
                                2 -> Color(0xFFF44336)
                                3 -> Color(0xFFFF9800)
                                4 -> Color.Red
                                else -> Color.Gray
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Icono de navegación
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver perfil",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}