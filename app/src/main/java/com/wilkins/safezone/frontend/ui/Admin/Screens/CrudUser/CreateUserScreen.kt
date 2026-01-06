package com.wilkins.safezone.frontend.ui.Admin.Screens.CrudUser

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.Admin.CrudUser.CreateUserRequest
import com.wilkins.safezone.backend.network.Admin.CrudUser.Role
import com.wilkins.safezone.bridge.Crud.CreateUserViewModel
import com.wilkins.safezone.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserScreen(
    navController: NavController,
    viewModel: CreateUserViewModel = viewModel()
) {
    val roles = viewModel.roles
    val loadingRoles = viewModel.loadingRoles
    val loading = viewModel.loading

    LaunchedEffect(Unit) {
        viewModel.loadRoles()
    }

    // Estados del formulario
    var nombreCompleto by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf("Usuario") }
    var rolIdSeleccionado by remember { mutableStateOf(1) }

    // Estados para validación
    var showPasswordError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Crear Nuevo Usuario",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
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
        if (loadingRoles) {
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
            // Header decorativo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(PrimaryColor, Color(0xFF89FF7B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .size(100.dp)
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
                                    colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Crear Usuario",
                            modifier = Modifier.size(50.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            // Formulario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-30).dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "📝 Información del Usuario",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Nombre completo
                    CreateUserInputField(
                        label = "Nombre completo *",
                        value = nombreCompleto,
                        onValueChange = { nombreCompleto = it },
                        placeholder = "Ej: Juan Pérez"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email
                    CreateUserInputField(
                        label = "Email *",
                        value = email,
                        onValueChange = { email = it.trim() },
                        placeholder = "ejemplo@correo.com",
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contraseña
                    CreateUserPasswordField(
                        label = "Contraseña *",
                        value = password,
                        onValueChange = {
                            password = it
                            showPasswordError = confirmPassword.isNotEmpty() && password != confirmPassword
                        },
                        placeholder = "Mínimo 6 caracteres",
                        passwordVisible = passwordVisible,
                        onVisibilityChange = { passwordVisible = !passwordVisible }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirmar contraseña
                    CreateUserPasswordField(
                        label = "Confirmar contraseña *",
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            showPasswordError = password != confirmPassword
                        },
                        placeholder = "Repite la contraseña",
                        passwordVisible = confirmPasswordVisible,
                        onVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
                        isError = showPasswordError
                    )

                    if (showPasswordError) {
                        Text(
                            text = "❌ Las contraseñas no coinciden",
                            color = Color(0xFFF56565),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Teléfono
                    CreateUserInputField(
                        label = "Teléfono",
                        value = telefono,
                        onValueChange = { telefono = it },
                        placeholder = "809-555-1234",
                        keyboardType = KeyboardType.Phone
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dirección
                    CreateUserInputField(
                        label = "Dirección",
                        value = direccion,
                        onValueChange = { direccion = it },
                        placeholder = "Calle, ciudad, país"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rol
                    CreateUserRolDropdown(
                        label = "Rol *",
                        roles = roles,
                        rolSeleccionado = rolSeleccionado,
                        onRolChange = { nombre, id ->
                            rolSeleccionado = nombre
                            rolIdSeleccionado = id
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "* Campos obligatorios",
                        fontSize = 12.sp,
                        color = Color(0xFF718096),
                        fontStyle = FontStyle.Italic
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón crear usuario
                    Button(
                        onClick = {
                            // Validaciones
                            when {
                                nombreCompleto.isBlank() -> {
                                    Toast.makeText(context, "❌ El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                                }
                                email.isBlank() -> {
                                    Toast.makeText(context, "❌ El email es obligatorio", Toast.LENGTH_SHORT).show()
                                }
                                !email.contains("@") -> {
                                    Toast.makeText(context, "❌ Email inválido", Toast.LENGTH_SHORT).show()
                                }
                                password.isBlank() -> {
                                    Toast.makeText(context, "❌ La contraseña es obligatoria", Toast.LENGTH_SHORT).show()
                                }
                                password.length < 6 -> {
                                    Toast.makeText(context, "❌ La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                                }
                                password != confirmPassword -> {
                                    Toast.makeText(context, "❌ Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    showConfirmDialog = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF48BB78)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Crear Usuario",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Diálogo de confirmación
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = Color(0xFF667EEA),
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = "Confirmar creación",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF2D3748)
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "¿Estás seguro de crear el siguiente usuario?",
                            fontSize = 15.sp,
                            color = Color(0xFF4A5568)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "• Nombre: $nombreCompleto\n• Email: $email\n• Rol: $rolSeleccionado",
                            fontSize = 13.sp,
                            color = Color(0xFF718096),
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "El usuario será creado y verificado automáticamente.",
                            fontSize = 12.sp,
                            color = Color(0xFF48BB78),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            val request = CreateUserRequest(
                                name = nombreCompleto,
                                email = email,
                                password = password,
                                phone = telefono.ifBlank { null },
                                address = direccion.ifBlank { null },
                                roleId = rolIdSeleccionado,
                                statusId = 1
                            )

                            viewModel.createUser(request) { response ->
                                if (response.success) {
                                    Toast.makeText(context, "✅ ${response.message}", Toast.LENGTH_LONG).show()
                                    navController.navigateUp()
                                } else {
                                    Toast.makeText(context, "❌ ${response.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF48BB78)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("✅ Sí, crear", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showConfirmDialog = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("❌ Cancelar", fontWeight = FontWeight.Medium)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun CreateUserInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
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
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFFA0AEC0),
                    fontSize = 14.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667EEA),
                unfocusedBorderColor = Color(0xFFCBD5E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFFAFAFA),
                cursorColor = Color(0xFF667EEA)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}

@Composable
fun CreateUserPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    passwordVisible: Boolean,
    onVisibilityChange: () -> Unit,
    isError: Boolean = false
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
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFFA0AEC0),
                    fontSize = 14.sp
                )
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onVisibilityChange) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = Color(0xFF667EEA)
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) Color(0xFFF56565) else Color(0xFF667EEA),
                unfocusedBorderColor = if (isError) Color(0xFFF56565) else Color(0xFFCBD5E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFFAFAFA),
                cursorColor = Color(0xFF667EEA)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = isError
        )
    }
}

@Composable
fun CreateUserRolDropdown(
    label: String,
    roles: List<Role>,
    rolSeleccionado: String,
    onRolChange: (String, Int) -> Unit
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
                    .clickable { expanded = true },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF667EEA),
                    unfocusedBorderColor = Color(0xFFCBD5E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFFAFAFA),
                    cursorColor = Color.Transparent,
                    disabledContainerColor = Color.White,
                    disabledBorderColor = Color(0xFFCBD5E0),
                    disabledTextColor = Color(0xFF2D3748)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = false,
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Desplegar",
                        tint = Color(0xFF667EEA)
                    )
                }
            )

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
                                text = rol.name,
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