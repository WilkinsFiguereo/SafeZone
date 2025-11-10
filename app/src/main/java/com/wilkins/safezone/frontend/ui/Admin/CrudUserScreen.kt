package com.wilkins.safezone.frontend.ui.Admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.Admin.CrudUser.ProfileService
import com.wilkins.safezone.backend.network.Admin.CrudUser.UserProfileViewModel
import com.wilkins.safezone.backend.network.Admin.CrudUser.Usuario
import com.wilkins.safezone.frontend.ui.Admin.components.*
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

    val coroutineScope = rememberCoroutineScope()
    val profileService = remember { ProfileService() }

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
                    .padding(16.dp)
            )

            // Cabecera de la tabla
            TableHeader()

            // Contenido de la tabla
            val filteredUsers = filterUsers()

            if (filteredUsers.isEmpty()) {
                EmptyState(
                    isLoading = isLoading,
                    searchText = searchText,
                    errorMessage = errorMessage,
                    selectedFilter = selectedFilter,
                    filterOptions = filterOptions
                )
            } else {
                UsersTable(
                    users = filteredUsers,
                    onUserClick = { usuario ->
                        navController?.navigate("userDetails/${usuario.idCompleto}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    navController = navController!!
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
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = "Sin resultados",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = if (searchText.isNotEmpty()) {
                        "No se encontraron usuarios que coincidan con \"$searchText\""
                    } else {
                        errorMessage ?: "No se encontraron usuarios"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                if (searchText.isNotEmpty()) {
                    Text(
                        text = "Filtro activo: ${filterOptions.find { it.first == selectedFilter }?.second ?: "Todos"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun UsersTable(
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
            UserListItem(
                usuario = usuario,
                onClick = { uuid ->
                    userProfileViewModel.selectUser(uuid) // ✅ guarda el ID en el ViewModel
                    navController.navigate("userProfileCrud/$uuid") // ✅ navega al detalle
                },
                viewModel = userProfileViewModel,
            )

            // Separador entre filas
            if (users.last() != usuario) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
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