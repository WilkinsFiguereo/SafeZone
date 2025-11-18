package com.wilkins.safezone.frontend.ui.Admin.CrudUser

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.Admin.CrudUser.Role
import com.wilkins.safezone.backend.network.Admin.CrudUser.UserProfileViewModel
import com.wilkins.safezone.backend.network.Admin.CrudUser.Usuario
import com.wilkins.safezone.bridge.profile.UpdateProfileBridge
import com.wilkins.safezone.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileCrud(
    userId: String,
    navController: NavController,
    viewModel: UserProfileViewModel = viewModel()
) {
    val profile = viewModel.profile
    val loading = viewModel.loading
    val roles = viewModel.roles
    val loadingRoles = viewModel.loadingRoles

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
        viewModel.loadRoles()
    }

    // Estado para controlar el modo edición
    var isEditing by remember { mutableStateOf(false) }

    // ✅ Estado para controlar el diálogo de confirmación
    var showSaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Perfil de Usuario",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("crudUsuarios") {
                            popUpTo("crudUsuarios") { inclusive = false }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        if (loading || loadingRoles) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryColor)
            }
            return@Scaffold
        }

        if (profile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Usuario no encontrado", color = Color.Gray)
            }
            return@Scaffold
        }

        // ✅ Estados del formulario
        var nombreCompleto by remember { mutableStateOf(profile.name) }
        var uuid by remember { mutableStateOf(profile.id) }
        var telefono by remember { mutableStateOf(profile.phone ?: "") }
        var email by remember { mutableStateOf(profile.email ?: "") }
        var fechaCreacion by remember { mutableStateOf(profile.createdAt) }
        var estado by remember { mutableStateOf(if (profile.statusId == 1) "Activo" else "Inactivo") }
        var direccion by remember { mutableStateOf(profile.address ?: "") }

        // ✅ Estado para el rol seleccionado
        var rolSeleccionado by remember { mutableStateOf(profile.rol?.name ?: "Usuario") }
        var rolIdSeleccionado by remember { mutableStateOf(profile.roleId) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF5F7FA), Color(0xFFE8EEF5))
                    )
                )
                .verticalScroll(rememberScrollState())
        ) {
            // Header con avatar y gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                PrimaryColor,
                                Color(0xFF89FF7B)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(8.dp, CircleShape),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF667EEA),
                                        Color(0xFF764BA2)
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(70.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            // Card con información del usuario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-40).dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = nombreCompleto,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF2D3748)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.wrapContentWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFEDF2F7)
                    ) {
                        Text(
                            text = "👤 $rolSeleccionado",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF667EEA)
                        )
                    }
                }
            }

            // Información de usuario
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 0.dp)
            ) {
                Text(
                    text = "📋 Información de usuario",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Nombre completo
                InputField(
                    label = "Nombre completo",
                    value = nombreCompleto,
                    onValueChange = { nombreCompleto = it },
                    enabled = isEditing
                )

                Spacer(modifier = Modifier.height(16.dp))

                // UUID (siempre deshabilitado)
                InputField(
                    label = "UUID",
                    value = uuid,
                    onValueChange = { },
                    enabled = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email
                InputField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    enabled = isEditing,
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Teléfono
                InputField(
                    label = "Teléfono",
                    value = telefono,
                    onValueChange = { telefono = it },
                    enabled = isEditing,
                    keyboardType = KeyboardType.Phone
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dirección
                InputField(
                    label = "Dirección",
                    value = direccion,
                    onValueChange = { direccion = it },
                    enabled = isEditing
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ Dropdown de Rol (dinámico desde la BD)
                RolDropdown(
                    label = "Rol",
                    roles = roles,
                    rolSeleccionado = rolSeleccionado,
                    onRolChange = { nombreRol, idRol ->
                        rolSeleccionado = nombreRol
                        rolIdSeleccionado = idRol
                    },
                    enabled = isEditing
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fecha de creación (siempre deshabilitado)
                InputField(
                    label = "Fecha de creación",
                    value = fechaCreacion,
                    onValueChange = { },
                    enabled = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Estado
                EstadoDropdown(
                    label = "Estado",
                    estado = estado,
                    onEstadoChange = { estado = it },
                    enabled = isEditing
                )

                Spacer(modifier = Modifier.height(32.dp))

                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()

                // ✅ Diálogo de confirmación para guardar
                if (showSaveDialog) {
                    AlertDialog(
                        onDismissRequest = { showSaveDialog = false },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Guardar",
                                tint = Color(0xFF667EEA),
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        title = {
                            Text(
                                text = "Confirmar cambios",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF2D3748)
                            )
                        },
                        text = {
                            Column {
                                Text(
                                    text = "¿Estás seguro de que deseas guardar los cambios realizados en el perfil de $nombreCompleto?",
                                    fontSize = 15.sp,
                                    color = Color(0xFF4A5568),
                                    lineHeight = 22.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Cambios a guardar:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF667EEA)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• Nombre: $nombreCompleto\n• Email: $email\n• Teléfono: $telefono\n• Rol: $rolSeleccionado\n• Estado: $estado",
                                    fontSize = 13.sp,
                                    color = Color(0xFF718096),
                                    lineHeight = 20.sp
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showSaveDialog = false
                                    coroutineScope.launch {
                                        val usuarioActualizado = Usuario(
                                            id = uuid,
                                            idCompleto = profile.id ?: "",
                                            nombre = nombreCompleto,
                                            rol = rolSeleccionado,
                                            roleId = rolIdSeleccionado,
                                            address = direccion,
                                            email = email,
                                            telefono = telefono,
                                            photoProfile = profile.photoProfile ?: "",
                                            createdAt = fechaCreacion,
                                            estado = estado
                                        )

                                        println("🔄 Enviando actualización para usuario: ${usuarioActualizado.nombre}")
                                        println("📧 Email: ${usuarioActualizado.email}")
                                        println("📞 Teléfono: ${usuarioActualizado.telefono}")
                                        println("📍 Dirección: ${usuarioActualizado.address}")
                                        println("👤 Rol: ${usuarioActualizado.rol} (ID: ${usuarioActualizado.roleId})")
                                        println("🎯 Estado: ${usuarioActualizado.estado}")

                                        val result = UpdateProfileBridge.handleUpdateProfile(context, usuarioActualizado)

                                        result.onSuccess {
                                            Toast.makeText(context, "✅ Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                                            isEditing = false
                                            viewModel.loadProfile(userId)
                                        }.onFailure { error ->
                                            Toast.makeText(context, "❌ Error: ${error.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF48BB78)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("✅ Sí, guardar", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = { showSaveDialog = false },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF718096)
                                )
                            ) {
                                Text("❌ Cancelar", fontWeight = FontWeight.Medium)
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        containerColor = Color.White
                    )
                }

                // Botones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (isEditing) {
                                // ✅ Mostrar diálogo de confirmación antes de guardar
                                showSaveDialog = true
                            } else {
                                // Modo Editar - Activar edición
                                isEditing = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEditing) Color(0xFF48BB78) else Color(0xFF667EEA)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp)),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Text(
                            text = if (isEditing) "💾 Guardar" else "✏️ Editar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Botón Deshabilitar/Cancelar
                    Button(
                        onClick = {
                            if (isEditing) {
                                // Modo Cancelar - Cancelar edición
                                isEditing = false
                                // Restaurar valores originales
                                nombreCompleto = profile.name
                                email = profile.email ?: ""
                                telefono = profile.phone ?: ""
                                direccion = profile.address ?: ""
                                estado = if (profile.statusId == 1) "Activo" else "Inactivo"
                                rolSeleccionado = profile.rol?.name ?: "Usuario"
                                rolIdSeleccionado = profile.roleId
                            } else {
                                // Modo Deshabilitar/Habilitar
                                coroutineScope.launch {
                                    val newStatus = if (estado == "Activo") "Inactivo" else "Activo"
                                    val newStatusId = if (estado == "Activo") 2 else 1

                                    val usuarioActualizado = Usuario(
                                        id = uuid,
                                        idCompleto = profile.id ?: "",
                                        nombre = nombreCompleto,
                                        rol = rolSeleccionado,
                                        roleId = rolIdSeleccionado,
                                        address = direccion,
                                        email = email,
                                        telefono = telefono,
                                        photoProfile = profile.photoProfile ?: "",
                                        createdAt = fechaCreacion,
                                        estado = newStatus
                                    )

                                    val result = UpdateProfileBridge.handleUpdateProfile(context, usuarioActualizado)

                                    result.onSuccess {
                                        estado = newStatus
                                        Toast.makeText(context, "✅ Estado cambiado a: $newStatus", Toast.LENGTH_SHORT).show()
                                        viewModel.loadProfile(userId)
                                    }.onFailure { error ->
                                        Toast.makeText(context, "❌ Error: ${error.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEditing) Color(0xFFA0AEC0) else
                                if (estado == "Activo") Color(0xFFF56565) else Color(0xFF48BB78)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp)),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Text(
                            text = if (isEditing) "❌ Cancelar" else
                                if (estado == "Activo") "🚫 Deshabilitar" else "✅ Habilitar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF4A5568),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (enabled) Color(0xFF667EEA) else Color(0xFFCBD5E0),
                unfocusedBorderColor = Color(0xFFCBD5E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = if (enabled) Color(0xFFFAFAFA) else Color(0xFFF7FAFC),
                cursorColor = if (enabled) Color(0xFF667EEA) else Color.Transparent,
                disabledContainerColor = Color(0xFFF7FAFC),
                disabledBorderColor = Color(0xFFE2E8F0),
                disabledTextColor = Color(0xFF718096)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = enabled,
        )
    }
}

@Composable
fun RolDropdown(
    label: String,
    roles: List<Role>,
    rolSeleccionado: String,
    onRolChange: (String, Int) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF4A5568),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            OutlinedTextField(
                value = rolSeleccionado,
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { if (enabled) expanded = true },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (enabled) Color(0xFF667EEA) else Color(0xFFCBD5E0),
                    unfocusedBorderColor = Color(0xFFCBD5E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = if (enabled) Color(0xFFFAFAFA) else Color(0xFFF7FAFC),
                    cursorColor = Color.Transparent,
                    disabledContainerColor = Color(0xFFF7FAFC),
                    disabledBorderColor = Color(0xFFE2E8F0),
                    disabledTextColor = Color(0xFF718096)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = false,
                trailingIcon = {
                    if (enabled) {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Desplegar",
                            tint = Color(0xFF667EEA)
                        )
                    }
                }
            )

            if (enabled) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    roles.forEach { rol ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${rol.name}",
                                    color = if (rol.name == rolSeleccionado) Color(0xFF667EEA) else Color(0xFF2D3748),
                                    fontWeight = if (rol.name == rolSeleccionado) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onRolChange(rol.name, rol.id)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EstadoDropdown(
    label: String,
    estado: String,
    onEstadoChange: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val estados = listOf("Activo", "Inactivo", "Pendiente", "Bloqueado")

    Column {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF4A5568),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            OutlinedTextField(
                value = estado,
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { if (enabled) expanded = true },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (enabled) Color(0xFF667EEA) else Color(0xFFCBD5E0),
                    unfocusedBorderColor = Color(0xFFCBD5E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = if (enabled) Color(0xFFFAFAFA) else Color(0xFFF7FAFC),
                    cursorColor = Color.Transparent,
                    disabledContainerColor = Color(0xFFF7FAFC),
                    disabledBorderColor = Color(0xFFE2E8F0),
                    disabledTextColor = Color(0xFF718096)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = false,
                trailingIcon = {
                    if (enabled) {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Desplegar",
                            tint = Color(0xFF667EEA)
                        )
                    }
                }
            )

            if (enabled) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    estados.forEach { estadoOption ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = estadoOption,
                                    color = if (estadoOption == estado) Color(0xFF667EEA) else Color(0xFF2D3748),
                                    fontWeight = if (estadoOption == estado) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onEstadoChange(estadoOption)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}