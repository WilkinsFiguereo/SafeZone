package com.wilkins.safezone.frontend.Moderator.News

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
import com.wilkins.safezone.backend.network.Moderator.News
import com.wilkins.safezone.backend.network.Moderator.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsEditScreen(
    news: News,
    onNavigateBack: () -> Unit,
    viewModel: NewsViewModel = viewModel()
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(news.title) }
    var description by remember { mutableStateOf(news.description) }
    var isImportant by remember { mutableStateOf(news.isImportant) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentImageUrl by remember { mutableStateOf(news.imageUrl) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Auto-cerrar después de éxito
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(1500)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Noticia") },
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

            // Imagen actual o nueva
            Text(
                text = "Imagen actual:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                AsyncImage(
                    model = selectedImageUri ?: currentImageUrl,
                    contentDescription = "Imagen de la noticia",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Botón para cambiar imagen
            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedImageUri != null) "Cambiar Imagen (Nueva seleccionada)" else "Cambiar Imagen (Opcional)")
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

            // Botón de actualizar
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
                        else -> {
                            isLoading = true
                            viewModel.updateNews(
                                context = context,
                                newsId = news.id!!,
                                title = title,
                                description = description,
                                isImportant = isImportant,
                                newImageUri = selectedImageUri,
                                currentImageUrl = currentImageUrl,
                                onSuccess = {
                                    isLoading = false
                                    successMessage = "Noticia actualizada exitosamente"
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
                    Text("Actualizar Noticia", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}