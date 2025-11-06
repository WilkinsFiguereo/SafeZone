package com.wilkins.safezone.frontend.ui.Admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.wilkins.safezone.backend.network.Admin.ProfileService
import kotlinx.coroutines.launch

data class Usuario(
    val id: String,
    val nombre: String,
    val rol: String,
    val email: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrudUsuarios() {
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var mostrarDialog by remember { mutableStateOf(false) }
    var usuarioEditando by remember { mutableStateOf<Usuario?>(null) }
    var searchText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val profileService = remember { ProfileService() }

    // Cargar usuarios al iniciar
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val profiles = profileService.getAllProfiles()
            usuarios = profiles.map { profile ->
                Usuario(
                    id = profile.id.take(10) + "..", // Acortar ID para mostrar
                    nombre = profile.name,
                    rol = when (profile.roleId) {
                        1 -> "Administrador"
                        2 -> "Usuario"
                        3 -> "Moderador"
                        else -> "Usuario"
                    },
                    email = profile.email ?: "No disponible"
                )
            }
        } catch (e: Exception) {
            errorMessage = "Error al cargar usuarios: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crud de usuarios", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    usuarioEditando = null
                    mostrarDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Agregar usuario")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Mostrar error si existe
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE6E6))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message,
                            color = Color.Red,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { errorMessage = null }) {
                            Icon(Icons.Default.Close, "Cerrar")
                        }
                    }
                }
            }

            // Barra de búsqueda
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Buscar por nombre o email") },
                leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        IconButton(onClick = {
                            // Recargar datos
                            coroutineScope.launch {
                                isLoading = true
                                try {
                                    val profiles = profileService.getAllProfiles()
                                    usuarios = profiles.map { profile ->
                                        Usuario(
                                            id = profile.id.take(10) + "..",
                                            nombre = profile.name,
                                            rol = when (profile.roleId) {
                                                1 -> "Administrador"
                                                2 -> "Usuario"
                                                3 -> "Moderador"
                                                else -> "Usuario"
                                            },
                                            email = profile.email ?: "No disponible"
                                        )
                                    }
                                    errorMessage = null
                                } catch (e: Exception) {
                                    errorMessage = "Error al cargar usuarios: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }) {
                            Icon(Icons.Default.Refresh, "Recargar")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            // Tabla de usuarios
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
            ) {
                Column {
                    // Encabezado de la tabla
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5))
                            .border(1.dp, Color.LightGray)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "ID",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.8f)
                        )
                        Text(
                            "Nombre",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.5f)
                        )
                        Text(
                            "Rol",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.8f)
                        )
                        Text(
                            "Email",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.2f)
                        )
                        Spacer(modifier = Modifier.width(60.dp))
                    }

                    // Mostrar loading
                    if (isLoading && usuarios.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    // Filas de usuarios
                    LazyColumn {
                        items(usuarios.filter {
                            it.nombre.contains(searchText, ignoreCase = true) ||
                                    it.email.contains(searchText, ignoreCase = true) ||
                                    it.rol.contains(searchText, ignoreCase = true)
                        }) { usuario ->
                            FilaUsuario(
                                usuario = usuario,
                                onEditar = {
                                    usuarioEditando = usuario
                                    mostrarDialog = true
                                },
                                onEliminar = {
                                    // Eliminar usuario
                                    coroutineScope.launch {

                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog para agregar/editar
    if (mostrarDialog) {
        DialogUsuario(
            usuario = usuarioEditando,
            onDismiss = { mostrarDialog = false },
            onGuardar = { nuevoUsuario ->
                coroutineScope.launch {
                    try {
                        // Aquí puedes implementar la lógica para crear/actualizar en Supabase
                        // Por ahora solo actualiza la lista local
                        if (usuarioEditando != null) {
                            usuarios = usuarios.map {
                                if (it == usuarioEditando) nuevoUsuario else it
                            }
                        } else {
                            usuarios = usuarios + nuevoUsuario
                        }
                        mostrarDialog = false
                    } catch (e: Exception) {
                        errorMessage = "Error al guardar usuario: ${e.message}"
                    }
                }
            }
        )
    }
}

@Composable
fun FilaUsuario(
    usuario: Usuario,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.LightGray)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(usuario.id, modifier = Modifier.weight(0.8f), fontSize = 12.sp)
        Text(usuario.nombre, modifier = Modifier.weight(1.5f), fontSize = 14.sp)
        Text(usuario.rol, modifier = Modifier.weight(0.8f), fontSize = 14.sp)
        Text(usuario.email, modifier = Modifier.weight(1.2f), fontSize = 12.sp)

        // Botones de acción
        Row(
            modifier = Modifier.width(60.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onEditar,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    "Editar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onEliminar,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    "Eliminar",
                    tint = Color.Red,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogUsuario(
    usuario: Usuario?,
    onDismiss: () -> Unit,
    onGuardar: (Usuario) -> Unit
) {
    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    var rol by remember { mutableStateOf(usuario?.rol ?: "Usuario") }
    var email by remember { mutableStateOf(usuario?.email ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (usuario != null) "Editar Usuario" else "Nuevo Usuario",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Selector de rol
                var expanded by remember { mutableStateOf(false) }
                val roles = listOf("Administrador", "Usuario", "Moderador")

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    OutlinedTextField(
                        value = rol,
                        onValueChange = {},
                        label = { Text("Rol") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    rol = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val nuevoUsuario = Usuario(
                                id = usuario?.id ?: "new_${System.currentTimeMillis()}",
                                nombre = nombre,
                                rol = rol,
                                email = email
                            )
                            onGuardar(nuevoUsuario)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Guardar")
                    }
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