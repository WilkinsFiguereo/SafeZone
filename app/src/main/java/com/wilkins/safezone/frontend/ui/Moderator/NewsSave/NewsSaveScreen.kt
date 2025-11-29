package com.wilkins.safezone.frontend.ui.Moderator.NewsSave

import android.graphics.Bitmap
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.Moderator.NewsRequest
import com.wilkins.safezone.backend.network.Moderator.NewsRepository
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun NewsSaveScreen(
    navController: NavController,
    userId: String
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var important by remember { mutableStateOf(false) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    // 📌 Seleccionar imagen
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val context = navController.context
            val bitmap = android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            imageBitmap = bitmap
        }
    }

    fun bitmapToBase64(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output)
        val bytes = output.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Crear Noticia", style = MaterialTheme.typography.h5)

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Noticia importante/estelar")
            Spacer(Modifier.width(10.dp))
            Switch(
                checked = important,
                onCheckedChange = { important = it }
            )
        }

        Spacer(Modifier.height(15.dp))

        Button(onClick = { imagePicker.launch("image/*") }) {
            Text("Seleccionar Imagen")
        }

        imageBitmap?.let {
            Spacer(Modifier.height(15.dp))
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            enabled = !loading,
            onClick = {
                loading = true
                message = ""

                scope.launch {
                    try {
                        val repo = NewsRepository()

                        val imgBase64 = bitmapToBase64(imageBitmap)

                        val request = NewsRequest(
                            title = title,
                            description = description,
                            important = important,
                            userId = userId,
                            imageBase64 = imgBase64
                        )

                        repo.save(request)

                        message = "Noticia creada con éxito"
                    } catch (e: Exception) {
                        message = "Error: ${e.message}"
                    } finally {
                        loading = false
                    }
                }
            }
        ) {
            Text(if (loading) "Guardando..." else "Guardar")
        }

        Spacer(Modifier.height(20.dp))
        Text(message)
    }
}
