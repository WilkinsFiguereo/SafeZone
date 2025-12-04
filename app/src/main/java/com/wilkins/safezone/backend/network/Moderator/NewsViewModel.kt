package com.wilkins.safezone.backend.network.Moderator

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class NewsViewModel : ViewModel() {

    private val supabase = SupabaseService.getInstance()
    private val TAG = "NewsViewModel"

    private val _newsList = MutableStateFlow<List<News>>(emptyList())
    val newsList: StateFlow<List<News>> = _newsList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Cargar todas las noticias
    fun loadNews() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "📥 Cargando noticias...")

                val news = supabase.from("news")
                    .select()
                    .decodeList<News>()

                _newsList.value = news.sortedByDescending { it.createdAt ?: "" }
                Log.d(TAG, "✅ ${news.size} noticias cargadas")

                // Log detallado de cada noticia para debug
                news.forEach {
                    Log.d(TAG, "📰 Noticia: id=${it.id}, title=${it.title}, important=${it.isImportant}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al cargar noticias: ${e.message}", e)
                e.printStackTrace()
                _newsList.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Crear noticia
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
                Log.d(TAG, "🔄 Iniciando creación de noticia...")

                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser == null) {
                    withContext(Dispatchers.Main) {
                        onError("No hay usuario autenticado.")
                    }
                    return@launch
                }

                val imageUrl = uploadImage(context, imageUri)
                if (imageUrl == null) {
                    withContext(Dispatchers.Main) {
                        onError("Error al subir la imagen.")
                    }
                    return@launch
                }

                val news = News(
                    title = title,
                    description = description,
                    imageUrl = imageUrl,
                    isImportant = isImportant,
                    userId = currentUser.id
                )

                supabase.from("news").insert(news)
                Log.d(TAG, "✅ Noticia creada exitosamente")

                loadNews()

                withContext(Dispatchers.Main) {
                    onSuccess()
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en createNews: ${e.message}", e)
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Error: ${e.message ?: "Error desconocido"}")
                }
            }
        }
    }

    // Actualizar noticia
    fun updateNews(
        context: Context,
        newsId: String,  // ← CAMBIADO de Int a String
        title: String,
        description: String,
        isImportant: Boolean,
        newImageUri: Uri?,
        currentImageUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔄 Actualizando noticia ID: $newsId")

                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser == null) {
                    withContext(Dispatchers.Main) {
                        onError("No hay usuario autenticado.")
                    }
                    return@launch
                }

                val finalImageUrl = if (newImageUri != null) {
                    Log.d(TAG, "📤 Subiendo nueva imagen...")
                    uploadImage(context, newImageUri) ?: currentImageUrl
                } else {
                    currentImageUrl
                }

                val updatedNews = News(
                    id = newsId,
                    title = title,
                    description = description,
                    imageUrl = finalImageUrl,
                    isImportant = isImportant,
                    userId = currentUser.id
                )

                supabase.from("news")
                    .update(updatedNews) {
                        filter {
                            eq("id", newsId)
                        }
                    }

                Log.d(TAG, "✅ Noticia actualizada exitosamente")
                loadNews()

                withContext(Dispatchers.Main) {
                    onSuccess()
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al actualizar: ${e.message}", e)
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Error: ${e.message}")
                }
            }
        }
    }

    // Eliminar noticia
    fun deleteNews(
        newsId: String,  // ← CAMBIADO de Int a String
        imageUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🗑️ Eliminando noticia ID: $newsId")

                supabase.from("news").delete {
                    filter {
                        eq("id", newsId)
                    }
                }

                try {
                    val fileName = imageUrl.substringAfterLast("/")
                    if (fileName.isNotBlank()) {
                        supabase.storage.from("news-images").delete(fileName)
                        Log.d(TAG, "🗑️ Imagen eliminada: $fileName")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ No se pudo eliminar imagen: ${e.message}")
                }

                Log.d(TAG, "✅ Noticia eliminada")
                loadNews()

                withContext(Dispatchers.Main) {
                    onSuccess()
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al eliminar: ${e.message}", e)
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Error: ${e.message}")
                }
            }
        }
    }

    private suspend fun uploadImage(context: Context, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            var file: File? = null
            try {
                val timestamp = System.currentTimeMillis()
                val randomId = UUID.randomUUID().toString().take(8)
                val fileName = "news_${timestamp}_${randomId}.jpg"

                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: return@withContext null

                file = File(context.cacheDir, fileName)
                val outputStream = FileOutputStream(file)

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                if (!file.exists() || file.length() == 0L) {
                    file?.delete()
                    return@withContext null
                }

                val maxSize = 5 * 1024 * 1024
                if (file.length() > maxSize) {
                    file.delete()
                    return@withContext null
                }

                val bucket = supabase.storage.from("news-images")
                val fileBytes = file.readBytes()

                bucket.upload(
                    path = fileName,
                    data = fileBytes,
                    upsert = false
                )

                val publicUrl = bucket.publicUrl(fileName)

                file.delete()

                publicUrl.takeIf { it.isNotBlank() }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al subir imagen: ${e.message}", e)
                e.printStackTrace()
                file?.delete()
                null
            }
        }
    }
}