package com.wilkins.safezone.frontend.ui.Admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.Admin.ProfileService
import kotlinx.coroutines.launch

data class Usuario(
    val id: String,
    val idCompleto: String,
    val nombre: String,
    val rol: String,
    val roleId: Int,
    val email: String?, // 🔹 cambiado de phone a email
    val photoProfile: String?,
    val createdAt: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CrudUsuarios(navController: NavController? = null) {
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var searchText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val profileService = remember { ProfileService() }

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
                            email = profile.auth?.email ?: "Sin email", // 🔹 cambiado aquí
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
                .padding(20.dp)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Buscar por nombre o rol...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, "Buscar", tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        IconButton(onClick = { loadUsers() }, enabled = !isLoading) {
                            Icon(Icons.Default.Refresh, "Recargar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column {
                    if (usuarios.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "USUARIO",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1.8f)
                            )
                            Text(
                                "ROL",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(0.8f)
                            )
                            Text(
                                "EMAIL", // 🔹 cambiado de TELÉFONO a EMAIL
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }

                    if (usuarios.isNotEmpty()) {
                        LazyColumn {
                            items(usuarios.filter {
                                it.nombre.contains(searchText, ignoreCase = true) ||
                                        it.rol.contains(searchText, ignoreCase = true)
                            }) { usuario ->
                                UserListItem(usuario = usuario) {
                                    navController?.navigate("userDetails/${usuario.idCompleto}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserListItem(
    usuario: Usuario,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.8f)) {
                Text(
                    text = usuario.nombre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "ID: ${usuario.id}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(modifier = Modifier.weight(0.8f)) {
                val roleColor = when (usuario.roleId) {
                    1 -> Color(0xFFFF6B6B)
                    2 -> Color(0xFF4ECDC4)
                    3 -> Color(0xFF45B7D1)
                    else -> MaterialTheme.colorScheme.primary
                }

                Text(
                    text = usuario.rol,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier
                        .background(roleColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // 🔹 Mostrar email en lugar del teléfono
            Text(
                text = usuario.email ?: "Sin email",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1.2f)
            )

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ver detalles",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
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