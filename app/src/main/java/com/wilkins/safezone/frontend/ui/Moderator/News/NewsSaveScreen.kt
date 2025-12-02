package com.wilkins.safezone.frontend.Moderator.NewsSave

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.wilkins.safezone.backend.network.Moderator.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsSaveScreen(
    onNavigateBack: () -> Unit,
    viewModel: NewsViewModel = viewModel()
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isImportant by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Noticia") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = title.isBlank() && errorMessage != null
            )

            // Descripción
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 6,
                isError = description.isBlank() && errorMessage != null
            )

            // Switch para noticia importante
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Noticia Importante/Estelar",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = isImportant,
                    onCheckedChange = { isImportant = it }
                )
            }

            // Botón para seleccionar imagen
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Seleccionar Imagen")
            }

            // Vista previa de la imagen
            selectedImageUri?.let { uri ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Imagen seleccionada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Mensajes de error o éxito
            errorMessage?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            successMessage?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Botón de guardar
            Button(
                onClick = {
                    errorMessage = null
                    successMessage = null

                    when {
                        title.isBlank() -> {
                            errorMessage = "El título es obligatorio"
                        }
                        description.isBlank() -> {
                            errorMessage = "La descripción es obligatoria"
                        }
                        selectedImageUri == null -> {
                            errorMessage = "Debes seleccionar una imagen"
                        }
                        else -> {
                            isLoading = true
                            viewModel.createNews(
                                context = context,
                                title = title,
                                description = description,
                                isImportant = isImportant,
                                imageUri = selectedImageUri!!,
                                onSuccess = {
                                    isLoading = false
                                    successMessage = "Noticia creada exitosamente"
                                    // Limpiar formulario
                                    title = ""
                                    description = ""
                                    isImportant = false
                                    selectedImageUri = null
                                },
                                onError = { error ->
                                    isLoading = false
                                    errorMessage = error
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Publicar Noticia", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}