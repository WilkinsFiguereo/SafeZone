package com.wilkins.safezone.frontend.ui.Admin.Screens.CrudUser

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
import com.wilkins.safezone.frontend.ui.Admin.Screens.CrudUser.components.SearchBar
import com.wilkins.safezone.ui.theme.PrimaryColor
import kotlinx.coroutines.delay
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

// ✅ NUEVO COMPONENTE: Footer con botón Crear Usuario
@Composable
fun CreateUserFooter(
    totalUsers: Int,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        label = "buttonScale"
    )
    val shadowElevation by animateFloatAsState(
        targetValue = if (isHovered) 8.dp.value else 4.dp.value,
        label = "shadowElevation"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Información de estadísticas
            Column {
                Text(
                    text = "Total de usuarios",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$totalUsers registrados",
                    fontSize = 18.sp,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // Botón Crear Usuario con diseño mejorado
            Button(
                onClick = onNavigateToCreate,
                modifier = Modifier
                    .scale(scale)
                    .height(50.dp)
                    .width(180.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                ),
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Crear usuario",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Crear Usuario",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ✅ NUEVA TABLA MODERNA CON CARDS
@Composable
fun ModernUsersTable(
    users: List<Usuario>,
    onUserClick: (Usuario) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users) { usuario ->
            val userProfileViewModel: UserProfileViewModel = viewModel()
            ModernUserListItem(
                usuario = usuario,
                viewModel = userProfileViewModel,
                onClick = { uuid ->
                    userProfileViewModel.selectUser(uuid)
                    navController.navigate("userProfileCrud/$uuid")
                }
            )
        }
    }
}

// ✅ NUEVO COMPONENTE: Item de Usuario Moderno con Card
@Composable
fun ModernUserListItem(
    usuario: Usuario,
    viewModel: UserProfileViewModel,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "scale")

    val roleColor = when (usuario.roleId) {
        1 -> Color(0xFFFF6B6B) // Admin - Rojo
        2 -> Color(0xFF4ECDC4) // Moderador - Turquesa
        3 -> Color(0xFF45B7D1) // Usuario - Azul
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                viewModel.selectUser(usuario.idCompleto)
                onClick(usuario.idCompleto)
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Avatar y info principal
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar con gradiente
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    roleColor.copy(alpha = 0.8f),
                                    roleColor.copy(alpha = 0.5f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = usuario.nombre.firstOrNull()?.uppercase() ?: "U",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Información del usuario
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = usuario.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Email con icono
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = usuario.email ?: "Sin email",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ID con estilo sutil
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "ID: ${usuario.id}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Rol y botón de acción
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                // Badge de rol mejorado
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = roleColor,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = usuario.rol,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón de ver detalles
                FilledTonalIconButton(
                    onClick = {
                        viewModel.selectUser(usuario.idCompleto)
                        onClick(usuario.idCompleto)
                    },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Ver detalles",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

// Controles de Paginación
@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    totalItems: Int,
    itemsPerPage: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val startItem = ((currentPage - 1) * itemsPerPage) + 1
    val endItem = minOf(currentPage * itemsPerPage, totalItems)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Mostrando $startItem-$endItem de $totalItems",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { onPageChange(currentPage - 1) },
                enabled = currentPage > 1,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Página anterior",
                    tint = if (currentPage > 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = "Página $currentPage de $totalPages",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(
                onClick = { onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Página siguiente",
                    tint = if (currentPage < totalPages) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Estado vacío
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
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = "Sin resultados",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = if (searchText.isNotEmpty()) {
                        "No se encontraron usuarios que coincidan con \"$searchText\""
                    } else {
                        errorMessage ?: "No se encontraron usuarios"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (searchText.isNotEmpty()) {
                    Text(
                        text = "Filtro: ${filterOptions.find { it.first == selectedFilter }?.second ?: "Todos"}",
                        fontSize = 10.sp,
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