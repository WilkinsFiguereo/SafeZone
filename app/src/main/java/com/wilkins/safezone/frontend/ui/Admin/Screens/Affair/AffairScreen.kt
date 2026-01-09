package com.wilkins.safezone.frontend.ui.Admin.Screens.Affair

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wilkins.safezone.GenericUserUi.AdminMenu
import com.wilkins.safezone.backend.network.Admin.Affair.Affair
import com.wilkins.safezone.backend.network.Admin.Affair.AffairCategory
import com.wilkins.safezone.backend.network.Admin.Affair.AffairViewModel
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.auth.SessionManager.getUserProfile
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AffairScreen(
    navController: NavController,
    viewModel: AffairViewModel = viewModel(),
    context: Context = LocalContext.current
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingAffair by remember { mutableStateOf<Affair?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Affair?>(null) }
    var isMenuOpen by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<Int?>(null) }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    val userState = produceState<AppUser?>(initialValue = null) {
        value = getUserProfile(context)
    }

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
        viewModel.loadAffairs()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            delay(3000)
        }
    }

    AdminMenu(
        navController = navController,
        isMenuOpen = isMenuOpen,
        onMenuToggle = { isMenuOpen = !isMenuOpen },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Incidencias",
                                fontWeight = FontWeight.Medium,
                                fontSize = 22.sp
                            )
                            Text(
                                text = "${uiState.affairs.size} registros",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    ),
                    actions = {
                        AnimatedVisibility(visible = uiState.affairs.isNotEmpty()) {
                            IconButton(onClick = { viewModel.loadAffairs() }) {
                                Icon(
                                    Icons.Outlined.Refresh,
                                    contentDescription = "Actualizar",
                                    tint = Color.Black
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (uiState.categories.isEmpty()) {
                            viewModel.setError("Primero debes crear categorías")
                        } else {
                            editingAffair = null
                            showDialog = true
                        }
                    },
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, "Agregar")
                }
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color(0xFF1A1A1A),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            containerColor = Color(0xFFFAFAFA)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    uiState.isLoading -> {
                        LoadingState()
                    }
                    uiState.error != null && uiState.affairs.isEmpty() -> {
                        ErrorState(
                            error = uiState.error!!,
                            onRetry = {
                                viewModel.loadCategories()
                                viewModel.loadAffairs()
                            }
                        )
                    }
                    uiState.affairs.isEmpty() -> {
                        EmptyState(onReload = { viewModel.loadAffairs() })
                    }
                    else -> {
                        val filteredAffairs = if (selectedCategoryFilter != null) {
                            uiState.affairs.filter { it.categoriesId == selectedCategoryFilter }
                        } else {
                            uiState.affairs
                        }

                        Column(modifier = Modifier.fillMaxSize()) {
                            AnimatedVisibility(
                                visible = uiState.categories.isNotEmpty(),
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                CategoryFilterRow(
                                    categories = uiState.categories,
                                    selectedCategoryId = selectedCategoryFilter,
                                    totalCount = uiState.affairs.size,
                                    onCategorySelected = { categoryId ->
                                        selectedCategoryFilter = categoryId
                                    },
                                    onClearFilter = {
                                        selectedCategoryFilter = null
                                    }
                                )
                            }

                            AnimatedVisibility(
                                visible = filteredAffairs.isEmpty() && selectedCategoryFilter != null,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                NoResultsState()
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = listState,
                                contentPadding = PaddingValues(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredAffairs, key = { it.id ?: 0 }) { affair ->
                                    AffairItem(
                                        affair = affair,
                                        categoryName = uiState.categories
                                            .find { it.id == affair.categoriesId }
                                            ?.name ?: "Sin categoría",
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
                }
            }
        }
    }

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
            },
            isLoading = uiState.isLoading
        )
    }

    showDeleteDialog?.let { affair ->
        DeleteConfirmationDialog(
            affair = affair,
            onConfirm = {
                viewModel.deleteAffair(affair.id!!)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                strokeWidth = 3.dp,
                color = Color.Black
            )
            Text(
                "Cargando...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = "Error",
                tint = Color.Black,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Algo salió mal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun EmptyState(onReload: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Assignment,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.Gray.copy(alpha = 0.4f)
            )
            Text(
                text = "Sin incidencias",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Agrega tu primera incidencia",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            TextButton(onClick = onReload) {
                Text("Recargar", color = Color.Black)
            }
        }
    }
}

@Composable
private fun NoResultsState() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = null,
            tint = Color.Gray
        )
        Text(
            "No hay incidencias en esta categoría",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<AffairCategory>,
    selectedCategoryId: Int?,
    totalCount: Int,
    onCategorySelected: (Int) -> Unit,
    onClearFilter: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = onClearFilter,
                    label = {
                        Text(
                            "Todas",
                            fontWeight = if (selectedCategoryId == null) FontWeight.Medium else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.Black,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = Color.Gray
                    )
                )

                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { onCategorySelected(category.id!!) },
                        label = {
                            Text(
                                category.name,
                                fontWeight = if (selectedCategoryId == category.id) FontWeight.Medium else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.Black,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color.Gray
                        ),
                    )
                }
            }
        }
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEdit() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Assignment,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = affair.type,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    affair: Affair,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                "¿Eliminar incidencia?",
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Esta acción no se puede deshacer.",
                    color = Color.Gray
                )
                Text(
                    text = affair.type,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
            ) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AffairDialog(
    affair: Affair?,
    categories: List<AffairCategory>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit,
    isLoading: Boolean = false
) {
    var type by remember { mutableStateOf(affair?.type ?: "") }
    var selectedCategoryId by remember {
        mutableStateOf(affair?.categoriesId ?: categories.firstOrNull()?.id ?: 0)
    }
    var expanded by remember { mutableStateOf(false) }
    var isTypeError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (affair == null) Icons.Outlined.Add else Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                if (affair == null) "Nueva Incidencia" else "Editar Incidencia",
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {
                        type = it
                        isTypeError = it.isBlank()
                    },
                    label = { Text("Tipo de incidencia") },
                    placeholder = { Text("Ej: Robo, Daño...") },
                    isError = isTypeError,
                    supportingText = {
                        if (isTypeError) {
                            Text("Campo requerido", color = Color.Black)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name
                            ?: "Seleccionar categoría",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.Gray
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id!!
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (type.isNotBlank() && selectedCategoryId > 0) {
                        onConfirm(type, selectedCategoryId)
                    } else {
                        isTypeError = type.isBlank()
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(if (affair == null) "Crear" else "Guardar")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
            ) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}