package com.wilkins.safezone.frontend.ui.Admin.Screens.Affair

import SessionManager.getUserProfile
import android.content.Context
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wilkins.safezone.GenericUserUi.AdminMenu
import com.wilkins.safezone.backend.network.Admin.Affair.Affair
import com.wilkins.safezone.backend.network.Admin.Affair.AffairCategory
import com.wilkins.safezone.backend.network.Admin.Affair.AffairViewModel
import com.wilkins.safezone.backend.network.AppUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AffairScreen(
    navController: NavController,
    viewModel: AffairViewModel = viewModel(),
    context: Context
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingAffair by remember { mutableStateOf<Affair?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Affair?>(null) }
    var isMenuOpen by remember { mutableStateOf(false) }
    val userState = produceState<AppUser?>(initialValue = null) {
        value = getUserProfile(context)
    }

    val user = userState.value
    // Cargar categorías y affairs al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadCategories()
        viewModel.loadAffairs()
    }

    AdminMenu(
        navController = navController,
        isMenuOpen = isMenuOpen,
        onMenuToggle = { isMenuOpen = !isMenuOpen },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.error != null -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error: ${uiState.error}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                viewModel.loadCategories()
                                viewModel.loadAffairs()
                            }) {
                                Text("Reintentar")
                            }
                        }
                    }
                    uiState.affairs.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay affairs registrados",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Presiona + para agregar uno",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.affairs, key = { it.id ?: 0 }) { affair ->
                                AffairItem(
                                    affair = affair,
                                    categoryName = uiState.categories.find { it.id == affair.categoriesId }?.name ?: "Sin categoría",
                                    onEdit = {
                                        editingAffair = affair
                                        showDialog = true
                                    },
                                    onDelete = {
                                        showDeleteDialog = affair
                                    }
                                )
                            }
                        }
                    }
                }

                // Botón flotante
                FloatingActionButton(
                    onClick = {
                        if (uiState.categories.isEmpty()) {
                            // Mostrar mensaje si no hay categorías
                            viewModel.setError("Primero debes crear categorías")
                        } else {
                            editingAffair = null
                            showDialog = true
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, "Agregar incidente")
                }
            }
        }
    }

    // Dialog para crear/editar
    if (showDialog) {
        AffairDialog(
            affair = editingAffair,
            categories = uiState.categories,
            onDismiss = { showDialog = false },
            onConfirm = { type, categoryId ->
                if (editingAffair != null) {
                    viewModel.updateAffair(editingAffair!!.id!!, type, categoryId)
                } else {
                    viewModel.createAffair(type, categoryId)
                }
                showDialog = false
            }
        )
    }

    // Dialog de confirmación de eliminación
    showDeleteDialog?.let { affair ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar incidente") },
            text = { Text("¿Estás seguro de que deseas eliminar '${affair.type}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAffair(affair.id!!)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun AffairItem(
    affair: Affair,
    categoryName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = affair.type,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BusinessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AffairDialog(
    affair: Affair?,
    categories: List<AffairCategory>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var type by remember { mutableStateOf(affair?.type ?: "") }
    var selectedCategoryId by remember { mutableStateOf(affair?.categoriesId ?: categories.firstOrNull()?.id ?: 0) }
    var expanded by remember { mutableStateOf(false) }
    var isTypeError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (affair == null) "Nuevo incidente" else "Editar incidente")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {
                        type = it
                        isTypeError = it.isBlank()
                    },
                    label = { Text("Tipo de incidente") },
                    isError = isTypeError,
                    supportingText = if (isTypeError) {
                        { Text("El tipo no puede estar vacío") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dropdown para categorías
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "Seleccionar categoría",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.BusinessCenter,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(category.name)
                                    }
                                },
                                onClick = {
                                    selectedCategoryId = category.id!!
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (type.isNotBlank()) {
                        onConfirm(type, selectedCategoryId)
                    } else {
                        isTypeError = true
                    }
                }
            ) {
                Text(if (affair == null) "Crear" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}