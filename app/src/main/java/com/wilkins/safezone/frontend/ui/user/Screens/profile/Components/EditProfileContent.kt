package com.wilkins.safezone.frontend.ui.user.Screens.profile.Components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wilkins.safezone.backend.network.User.Profile.ProfileViewModel
import com.wilkins.safezone.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(
    userProfile: com.wilkins.safezone.backend.network.AppUser,
    viewModel: ProfileViewModel,
    onSave: (String, String, String, String, String, Int) -> Unit,
    onCancel: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToChangeEmail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf(userProfile.name ?: "") }
    var phone by remember { mutableStateOf(userProfile.phone ?: "") }
    var pronouns by remember { mutableStateOf(userProfile.pronouns ?: "") }
    var description by remember { mutableStateOf(userProfile.description ?: "") }
    var address by remember { mutableStateOf(userProfile.address ?: "") }
    var selectedStatus by remember { mutableStateOf(userProfile.status_id ?: 1) }
    var showPhone by remember { mutableStateOf(true) }
    var showAddress by remember { mutableStateOf(true) }
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedPronouns by remember { mutableStateOf(false) }

    val photoUrl = remember(userProfile.photoProfile) {
        viewModel.getProfilePhotoUrl(userProfile.photoProfile)
    }

    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadProfilePhoto(context, it)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editar perfil",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F)
                    )
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF757575)
                        )
                    }
                }

                Divider(color = Color(0xFFE0E0E0))

                // Profile Photo Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, PrimaryColor.copy(alpha = 0.3f), CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isUploadingPhoto) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = PrimaryColor
                            )
                        } else if (photoUrl != null) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Cambiar foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Agregar foto",
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Toca para cambiar foto",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }

                Divider(color = Color(0xFFE0E0E0))

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre completo") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = PrimaryColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    )
                )

                // Phone Field with Privacy Toggle
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Phone,
                                contentDescription = null,
                                tint = PrimaryColor
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (showPhone) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (showPhone) "Visible para todos" else "Oculto",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        Switch(
                            checked = showPhone,
                            onCheckedChange = { showPhone = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryColor,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                }

                // Pronouns Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedPronouns,
                    onExpandedChange = { expandedPronouns = it }
                ) {
                    OutlinedTextField(
                        value = pronouns.ifEmpty { "Seleccionar pronombres" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pronombres") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Badge,
                                contentDescription = null,
                                tint = PrimaryColor
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPronouns)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedPronouns,
                        onDismissRequest = { expandedPronouns = false }
                    ) {
                        listOf(
                            "Él",
                            "Ella",
                            "Elle",
                            "Él/Ella",
                            "Ella/Él",
                            "Elle/Él",
                            "Elle/Ella",
                            "Prefiero no decir"
                        ).forEach { pronoun ->
                            DropdownMenuItem(
                                text = { Text(pronoun) },
                                onClick = {
                                    pronouns = pronoun
                                    expandedPronouns = false
                                }
                            )
                        }
                    }
                }

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = PrimaryColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    ),
                    minLines = 3,
                    maxLines = 5
                )

                // Address Field with Privacy Toggle
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Dirección") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = PrimaryColor
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { /* TODO: Abrir mapa */ }) {
                                Icon(
                                    imageVector = Icons.Outlined.Map,
                                    contentDescription = "Seleccionar en mapa",
                                    tint = PrimaryColor
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (showAddress) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (showAddress) "Visible para todos" else "Oculto",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        Switch(
                            checked = showAddress,
                            onCheckedChange = { showAddress = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryColor,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                }

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = it }
                ) {
                    OutlinedTextField(
                        value = getStatusText(selectedStatus),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(getStatusColor(selectedStatus))
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        listOf(
                            1 to "Online",
                            2 to "Offline",
                            3 to "No disponible"
                        ).forEach { (id, status) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(getStatusColor(id))
                                        )
                                        Text(status)
                                    }
                                },
                                onClick = {
                                    selectedStatus = id
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }

                // Email Field (Read-only)
                OutlinedTextField(
                    value = userProfile.email ?: "",
                    onValueChange = {},
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E)
                        )
                    },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color(0xFFE0E0E0),
                        disabledTextColor = Color(0xFF757575)
                    )
                )

                Divider(color = Color(0xFFE0E0E0))

                // Security Buttons
                Text(
                    text = "Seguridad",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )

                OutlinedButton(
                    onClick = onNavigateToChangeEmail,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiar email", fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = onNavigateToChangePassword,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiar contraseña", fontSize = 14.sp)
                }

                Divider(color = Color(0xFFE0E0E0))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            onSave(name, phone, pronouns, description, address, selectedStatus)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor
                        )
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}