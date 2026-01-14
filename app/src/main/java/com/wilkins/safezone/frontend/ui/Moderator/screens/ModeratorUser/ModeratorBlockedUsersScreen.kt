package com.wilkins.safezone.frontend.ui.Moderator.screens.ModeratorUser


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
import coil.request.ImageRequest
import com.wilkins.safezone.backend.network.Admin.CrudUser.CrudUser
import com.wilkins.safezone.backend.network.Admin.CrudUser.Profile
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.frontend.ui.Moderator.ModeratorSideMenu
import com.wilkins.safezone.navigation.theme.PrimaryColor
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

@Composable
fun ModeratorBlockedUsersScreen(
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

    // Cargar usuarios bloqueados/inactivos al iniciar
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                users = crudUser.getAllProfilesDisabled()
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
                        placeholder = { Text("Buscar usuario bloqueado o inactivo...") },
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

            // Estadísticas de usuarios bloqueados/inactivos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatsCardBlocked(
                    title = "Total",
                    value = users.size.toString(),
                    icon = Icons.Default.Block,
                    color = Color(0xFFE53935),
                    modifier = Modifier.weight(1f)
                )
                StatsCardBlocked(
                    title = "Inactivos",
                    value = users.count { it.statusId == 2 }.toString(),
                    icon = Icons.Default.RemoveCircle,
                    color = Color(0xFFF57C00),
                    modifier = Modifier.weight(1f)
                )
                StatsCardBlocked(
                    title = "Pendientes",
                    value = users.count { it.statusId == 3 }.toString(),
                    icon = Icons.Default.HourglassEmpty,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                StatsCardBlocked(
                    title = "Bloqueados",
                    value = users.count { it.statusId == 4 }.toString(),
                    icon = Icons.Default.Block,
                    color = Color(0xFFD32F2F),
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
                            CircularProgressIndicator(color = Color(0xFFE53935))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cargando usuarios bloqueados/inactivos...",
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
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isEmpty())
                                    "¡Genial! No hay usuarios bloqueados o inactivos"
                                else
                                    "No se encontraron usuarios con ese criterio",
                                color = Color.Gray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
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
                            BlockedUserCard(
                                user = user,
                                onClick = {
                                    navController.navigate("profileUser/${user.id}/${user.name}")
                                }
                            )
                        }
                    }
                }
            }
        }

        // Menú lateral superpuesto
        ModeratorSideMenu(
            navController = navController,
            moderatorId = moderatorId,
            moderatorName = moderatorName,
            currentRoute = "moderatorBlockedUsers",
            isMenuOpen = isMenuOpen,
            onMenuToggle = { isMenuOpen = it },
            context = context,
            supabaseClient = supabaseClient
        )
    }
}

@Composable
fun BlockedUserCard(
    user: Profile,
    onClick: () -> Unit
) {
    val context = LocalContext.current

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

    // Determinar el color según el estado
    val statusColor = when (user.statusId) {
        2 -> Color(0xFFF57C00) // Inactivo - Naranja
        3 -> Color(0xFFFF9800) // Pendiente - Naranja claro
        4 -> Color(0xFFD32F2F) // Bloqueado - Rojo oscuro
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Foto de perfil con overlay de estado
                Box(
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePhotoUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(profilePhotoUrl)
                                    .crossfade(true)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .error(android.R.drawable.ic_menu_report_image)
                                    .build(),
                                contentDescription = "Foto de ${user.name}",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                alpha = 0.6f // Opacidad reducida para usuarios bloqueados
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Badge de estado en la esquina
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(statusColor)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (user.statusId) {
                                2 -> Icons.Default.RemoveCircle
                                3 -> Icons.Default.HourglassEmpty
                                4 -> Icons.Default.Block
                                else -> Icons.Default.Block
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
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
                        color = Color.Black.copy(alpha = 0.7f),
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
                        color = Color.Gray.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = user.rol?.name ?: "Sin rol",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Estado destacado
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = when (user.statusId) {
                                    2 -> Icons.Default.RemoveCircle
                                    3 -> Icons.Default.HourglassEmpty
                                    4 -> Icons.Default.Block
                                    else -> Icons.Default.Block
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = when (user.statusId) {
                                    2 -> "Inactivo"
                                    3 -> "Pendiente"
                                    4 -> "Bloqueado"
                                    else -> "Desconocido"
                                },
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
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

            // Barra inferior con advertencia según el estado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(statusColor)
            )
        }
    }
}

@Composable
fun StatsCardBlocked(
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
                color = color
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}