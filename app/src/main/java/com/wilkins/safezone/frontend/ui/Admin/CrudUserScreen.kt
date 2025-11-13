package com.wilkins.safezone.frontend.ui.Admin

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wilkins.safezone.GenericUserUi.AdminMenu
import com.wilkins.safezone.backend.network.Admin.CrudUser.CrudUser
import com.wilkins.safezone.backend.network.Admin.CrudUser.UserProfileViewModel
import com.wilkins.safezone.backend.network.Admin.CrudUser.Usuario
import com.wilkins.safezone.frontend.ui.Admin.components.*
import com.wilkins.safezone.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CrudUsuarios(navController: NavController? = null) {
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var searchText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("nombre") }
    var showFilters by remember { mutableStateOf(false) }

    // ✅ Estado para controlar la apertura del menú
    var isMenuOpen by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val profileService = remember { CrudUser() }

    // Opciones de filtro
    val filterOptions = listOf(
        "nombre" to "Nombre",
        "rol" to "Rol",
        "email" to "Email",
        "todos" to "Todos los campos"
    )

    fun loadUsers() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val profiles = profileService.getAllProfiles()
                if (profiles.isNotEmpty()) {
                    usuarios = profiles.map { profile ->
                        Usuario(
                            id = if (profile.id.length > 12) profile.id.take(12) + "..." else profile.id,
                            idCompleto = profile.id,
                            nombre = profile.name.ifEmpty { "Sin nombre" },
                            rol = profile.rol?.name ?: "Sin rol",
                            roleId = profile.roleId,
                            email = profile.email ?: "Sin email",
                            photoProfile = profile.photoProfile,
                            createdAt = profile.createdAt
                        )
                    }
                } else {
                    usuarios = emptyList()
                    errorMessage = "No hay usuarios registrados en la base de datos"
                }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "Timeout de conexión. Verifica tu internet."
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                        "Error de conexión. Verifica tu red."
                    else -> "Error al cargar usuarios: ${e.message}"
                }
                errorMessage = errorMsg
                usuarios = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    // Función para filtrar usuarios
    fun filterUsers(): List<Usuario> {
        if (searchText.isEmpty()) {
            return usuarios
        }

        return usuarios.filter { usuario ->
            when (selectedFilter) {
                "nombre" -> usuario.nombre.contains(searchText, ignoreCase = true)
                "rol" -> usuario.rol.contains(searchText, ignoreCase = true)
                "email" -> usuario.email?.contains(searchText, ignoreCase = true) == true
                "todos" -> usuario.nombre.contains(searchText, ignoreCase = true) ||
                        usuario.rol.contains(searchText, ignoreCase = true) ||
                        usuario.email?.contains(searchText, ignoreCase = true) == true
                else -> false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadUsers()
    }

    // Usar AdminMenu como contenedor principal
    if (navController != null) {
        AdminMenu(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
            showHeader = false,
            isMenuOpen = isMenuOpen, // ✅ Pasar el estado
            onMenuToggle = { isMenuOpen = !isMenuOpen } // ✅ Pasar el callback
        ) {
            // Contenido del CRUD con SU PROPIO header personalizado
            Column(modifier = Modifier.fillMaxSize()) {
                // ✅ Header personalizado del CRUD con botón de menú
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryColor)
                        .padding(horizontal = 16.dp, vertical = 10.dp), // 🔽 Más compacto
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de menú (3 líneas) - ✅ Ahora funciona!
                    IconButton(
                        onClick = { isMenuOpen = !isMenuOpen },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Título del CRUD
                    Text(
                        text = "Gestión de Usuarios",
                        color = Color.White,
                        fontSize = 18.sp, // 🔽 Texto más compacto
                        fontWeight = FontWeight.Bold
                    )
                    // Botón de perfil
                    IconButton(
                        onClick = { navController.navigate("admin_profile") },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Contenido del CRUD
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF8FAFC))
                ) {
                    // Barra de búsqueda con filtros
                    SearchBar(
                        searchText = searchText,
                        onSearchTextChange = { searchText = it },
                        selectedFilter = selectedFilter,
                        showFilters = showFilters,
                        onToggleFilters = { showFilters = !showFilters },
                        isLoading = isLoading,
                        onRefresh = { loadUsers() },
                        filterOptions = filterOptions,
                        onFilterSelected = { selectedFilter = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp) // 🔽 Padding más compacto
                    )

                    // Cabecera de la tabla COMPACTA
                    CompactTableHeader()

                    // Contenido de la tabla
                    val filteredUsers = filterUsers()

                    if (filteredUsers.isEmpty()) {
                        EmptyState(
                            isLoading = isLoading,
                            searchText = searchText,
                            errorMessage = errorMessage,
                            selectedFilter = selectedFilter,
                            filterOptions = filterOptions,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        CompactUsersTable(
                            users = filteredUsers,
                            onUserClick = { usuario ->
                                navController.navigate("userDetails/${usuario.idCompleto}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            navController = navController
                        )
                    }
                }
            }
        }
    } else {
        // Fallback si no hay navController (para previews)
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Gestión de Usuarios",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Barra de búsqueda con filtros
                SearchBar(
                    searchText = searchText,
                    onSearchTextChange = { searchText = it },
                    selectedFilter = selectedFilter,
                    showFilters = showFilters,
                    onToggleFilters = { showFilters = !showFilters },
                    isLoading = isLoading,
                    onRefresh = { loadUsers() },
                    filterOptions = filterOptions,
                    onFilterSelected = { selectedFilter = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )

                // Cabecera de la tabla COMPACTA
                CompactTableHeader()

                // Contenido de la tabla
                val filteredUsers = filterUsers()

                if (filteredUsers.isEmpty()) {
                    EmptyState(
                        isLoading = isLoading,
                        searchText = searchText,
                        errorMessage = errorMessage,
                        selectedFilter = selectedFilter,
                        filterOptions = filterOptions,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Text(
                            "NavController no disponible",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

// 🔽 NUEVO: Cabecera de tabla compacta
@Composable
fun CompactTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 12.dp, vertical = 10.dp), // 🔽 Más compacto
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "USUARIO",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 12.sp, // 🔽 Texto más pequeño
            modifier = Modifier.weight(1.8f)
        )
        Text(
            "ROL",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            "EMAIL",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.Center
        )
        Text(
            "ACCIONES",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.weight(0.7f), // 🔽 Menos espacio para acciones
            textAlign = TextAlign.Center
        )
    }
}

// 🔽 NUEVO: Tabla de usuarios compacta
@Composable
fun CompactUsersTable(
    users: List<Usuario>,
    onUserClick: (Usuario) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        items(users) { usuario ->
            val userProfileViewModel: UserProfileViewModel = viewModel()
            CompactUserListItem(
                usuario = usuario,
                onClick = { uuid ->
                    userProfileViewModel.selectUser(uuid)
                    navController.navigate("userProfileCrud/$uuid")
                },
                viewModel = userProfileViewModel,
            )

            // Separador entre filas más delgado
            if (users.last() != usuario) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    thickness = 0.3.dp, // 🔽 Línea más delgada
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
        }
    }
}

// 🔽 NUEVO: Item de usuario compacto
@Composable
fun CompactUserListItem(
    usuario: Usuario,
    onClick: (String) -> Unit,
    viewModel: UserProfileViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(usuario.idCompleto) }
            .padding(horizontal = 12.dp, vertical = 8.dp), // 🔽 Padding más compacto
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Columna Usuario - MÁS COMPACTO
        Column(
            modifier = Modifier.weight(1.8f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = usuario.nombre,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp, // 🔽 Texto más pequeño
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "ID: ${usuario.id}",
                fontSize = 10.sp, // 🔽 Texto más pequeño
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Columna Rol - MÁS COMPACTO
        Box(
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.CenterHorizontally)
        ) {
            val roleColor = when (usuario.roleId) {
                1 -> Color(0xFFFF6B6B) // Rojo para admin
                2 -> Color(0xFF4ECDC4) // Verde azulado para guardia
                3 -> Color(0xFF45B7D1) // Azul para residente
                else -> MaterialTheme.colorScheme.primary
            }

            Text(
                text = usuario.rol,
                fontSize = 10.sp, // 🔽 Texto más pequeño
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier
                    .background(roleColor, RoundedCornerShape(4.dp)) // 🔽 Bordes más pequeños
                    .padding(horizontal = 4.dp, vertical = 2.dp) // 🔽 Padding más pequeño
            )
        }

        // Columna Email - MÁS COMPACTO
        Text(
            text = usuario.email ?: "Sin email",
            fontSize = 11.sp, // 🔽 Texto más pequeño
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1.5f)
                .wrapContentWidth(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )

        // Columna Acciones - MÁS COMPACTO
        Box(
            modifier = Modifier
                .weight(0.7f)
                .wrapContentWidth(Alignment.CenterHorizontally)
        ) {
            IconButton(
                onClick = { onClick(usuario.idCompleto) },
                modifier = Modifier.size(32.dp) // 🔽 Botón más pequeño
            ) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = "Ver detalles",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp) // 🔽 Icono más pequeño
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    isLoading: Boolean,
    searchText: String,
    errorMessage: String?,
    selectedFilter: String,
    filterOptions: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp), // 🔽 Padding más compacto
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp) // 🔽 Spinner más pequeño
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp) // 🔽 Espaciado más compacto
            ) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = "Sin resultados",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp) // 🔽 Icono más pequeño
                )
                Text(
                    text = if (searchText.isNotEmpty()) {
                        "No se encontraron usuarios que coincidan con \"$searchText\""
                    } else {
                        errorMessage ?: "No se encontraron usuarios"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp, // 🔽 Texto más pequeño
                    textAlign = TextAlign.Center
                )
                if (searchText.isNotEmpty()) {
                    Text(
                        text = "Filtro activo: ${filterOptions.find { it.first == selectedFilter }?.second ?: "Todos"}",
                        fontSize = 11.sp, // 🔽 Texto más pequeño
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CrudUsuariosPreview() {
    MaterialTheme {
        CrudUsuarios()
    }
}