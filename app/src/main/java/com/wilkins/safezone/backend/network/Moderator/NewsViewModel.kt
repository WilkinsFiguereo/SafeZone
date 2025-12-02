package com.wilkins.safezone.backend.network.Moderator


import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class NewsViewModel : ViewModel() {

    private val supabase = SupabaseService.getInstance()

    fun createNews(
        context: Context,
        title: String,
        description: String,
        isImportant: Boolean,
        imageUri: Uri,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Obtener el ID del usuario actual
                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser == null) {
                    onError("No hay usuario autenticado")
                    return@launch
                }

                val userId = currentUser.id

                // 2. Subir la imagen a Supabase Storage
                val imageUrl = uploadImage(context, imageUri)
                if (imageUrl == null) {
                    onError("Error al subir la imagen")
                    return@launch
                }

                // 3. Crear el objeto de noticia
                val news = News(
                    title = title,
                    description = description,
                    imageUrl = imageUrl,
                    isImportant = isImportant,
                    userId = userId
                )

                // 4. Insertar en la base de datos
                supabase.from("news").insert(news)

                // 5. Notificar éxito
                withContext(Dispatchers.Main) {
                    onSuccess()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Error: ${e.message}")
                }
            }
        }
    }

    private suspend fun uploadImage(context: Context, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Generar nombre único para la imagen
                val fileName = "news_${UUID.randomUUID()}.jpg"

                // Leer el archivo de la URI
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val file = File(context.cacheDir, fileName)
                val outputStream = FileOutputStream(file)

                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                // Subir a Supabase Storage
                val bucket = supabase.storage.from("news-images")
                bucket.upload(fileName, file.readBytes())

                // Obtener la URL pública
                val publicUrl = bucket.publicUrl(fileName)

                // Limpiar archivo temporal
                file.delete()

                publicUrl
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}