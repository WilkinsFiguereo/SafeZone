package com.wilkins.safezone.frontend.ui.Admin.Screens.CrudUser

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wilkins.safezone.GenericUserUi.AdminMenu
import com.wilkins.safezone.backend.network.Admin.CrudUser.CrudUser
import com.wilkins.safezone.backend.network.Admin.CrudUser.Usuario
import com.wilkins.safezone.frontend.ui.Admin.Screens.CrudUser.components.SearchBar
import com.wilkins.safezone.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CrudUsuariosDisabled(navController: NavController? = null) {
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var searchText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("nombre") }
    var showFilters by remember { mutableStateOf(false) }

    // ✅ Estado para controlar la apertura del menú
    var isMenuOpen by remember { mutableStateOf(false) }

    // ✅ Estados para paginación
    var currentPage by remember { mutableStateOf(1) }
    val usersPerPage = 10

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
                val profiles = profileService.getAllProfilesDisabled()
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
                currentPage = 1
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

    // ✅ Función para obtener usuarios paginados
    fun getPaginatedUsers(): List<Usuario> {
        val filteredUsers = filterUsers()
        val startIndex = (currentPage - 1) * usersPerPage
        val endIndex = minOf(startIndex + usersPerPage, filteredUsers.size)

        return if (startIndex < filteredUsers.size) {
            filteredUsers.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }

    // ✅ Calcular total de páginas
    val totalPages = remember(usuarios, searchText) {
        val filteredCount = filterUsers().size
        if (filteredCount == 0) 1 else (filteredCount + usersPerPage - 1) / usersPerPage
    }

    // ✅ Función para cambiar de página
    fun goToPage(page: Int) {
        if (page in 1..totalPages) {
            currentPage = page
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
            isMenuOpen = isMenuOpen,
            onMenuToggle = { isMenuOpen = !isMenuOpen }
        ) {
            // Contenido del CRUD con SU PROPIO header personalizado
            Column(modifier = Modifier.fillMaxSize()) {
                // ✅ Header personalizado del CRUD
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryColor)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = { isMenuOpen = !isMenuOpen },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Gestión de Usuarios",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Contenido del CRUD
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF8FAFC))
                ) {
                    // ✅ REMOVIDO: Sección de botón crear usuario de arriba

                    // Barra de búsqueda con filtros - NUEVO DISEÑO MEJORADO
                    SearchBar(
                        searchText = searchText,
                        onSearchTextChange = {
                            searchText = it
                            currentPage = 1
                        },
                        selectedFilter = selectedFilter,
                        showFilters = showFilters,
                        onToggleFilters = { showFilters = !showFilters },
                        isLoading = isLoading,
                        onRefresh = {
                            loadUsers()
                            currentPage = 1
                        },
                        filterOptions = filterOptions,
                        onFilterSelected = {
                            selectedFilter = it
                            currentPage = 1
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Contenido de la tabla con nuevo diseño
                    val paginatedUsers = getPaginatedUsers()
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
                        Column(modifier = Modifier.weight(1f)) {
                            // ✅ NUEVA TABLA CON CARDS MODERNAS
                            ModernUsersTable(
                                users = paginatedUsers,
                                onUserClick = { usuario ->
                                    navController.navigate("userProfileCrud/${usuario.idCompleto}")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                navController = navController
                            )

                            // Paginación
                            PaginationControls(
                                currentPage = currentPage,
                                totalPages = totalPages,
                                totalItems = filteredUsers.size,
                                itemsPerPage = usersPerPage,
                                onPageChange = { page -> goToPage(page) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            )
                        }
                    }

                    // ✅ NUEVO FOOTER CON BOTÓN CREAR USUARIO MEJORADO
                    CreateUserFooter(
                        totalUsers = filteredUsers.size,
                        onNavigateToCreate = {
                            navController.navigate("CreateUserCrud")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    } else {
        // Fallback para previews
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Gestión de Usuarios",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("NavController no disponible")
            }
        }
    }
}