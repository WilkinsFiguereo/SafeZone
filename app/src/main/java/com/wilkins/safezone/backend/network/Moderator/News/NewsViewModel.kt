package com.wilkins.safezone.backend.network.Moderator.News


import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Locale.filter
import java.util.Objects.isNull
import java.util.UUID

class NewsViewModel : ViewModel() {

    private val supabase = SupabaseService.getInstance()
    private val TAG = "NewsViewModel"

    private val _newsList = MutableStateFlow<List<News>>(emptyList())
    val newsList: StateFlow<List<News>> = _newsList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _commentsMap = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val commentsMap: StateFlow<Map<String, List<Comment>>> = _commentsMap.asStateFlow()

    private val _isLoadingComments = MutableStateFlow(false)
    val isLoadingComments: StateFlow<Boolean> = _isLoadingComments.asStateFlow()

    // 🔥 Cargar todas las noticias
    fun loadNews() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                Log.d(TAG, "📥 Cargando noticias desde Supabase...")

                val news = supabase.from("news")
                    .select()
                    .decodeList<News>()

                val sortedNews = news.sortedByDescending { it.createdAt ?: "" }
                _newsList.value = sortedNews

                Log.d(TAG, "✅ ${news.size} noticias cargadas exitosamente")
                Log.d(TAG, "📊 Destacadas: ${news.count { it.isImportant }}, Normales: ${news.count { !it.isImportant }}")
                Log.d(TAG, "🎥 Con video: ${news.count { !it.videoUrl.isNullOrBlank() }}")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al cargar noticias: ${e.message}", e)
                e.printStackTrace()
                _newsList.value = emptyList()
                _errorMessage.value = "Error al cargar noticias: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔥 Crear noticia con imagen Y/O video
    fun createNews(
        context: Context,
        title: String,
        description: String,
        isImportant: Boolean,
        imageUri: Uri?,
        videoUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "📝 Iniciando creación de noticia...")
                _isLoading.value = true

                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser == null) {
                    withContext(Dispatchers.Main) {
                        onError("No hay usuario autenticado.")
                    }
                    return@launch
                }

                // Validar que tenga al menos imagen o video
                if (imageUri == null && videoUri == null) {
                    withContext(Dispatchers.Main) {
                        onError("Debes seleccionar al menos una imagen o un video")
                    }
                    return@launch
                }

                // Subir imagen (si existe)
                var imageUrl: String? = null
                if (imageUri != null) {
                    Log.d(TAG, "📤 Subiendo imagen...")
                    imageUrl = uploadImage(context, imageUri)
                    if (imageUrl == null) {
                        withContext(Dispatchers.Main) {
                            onError("Error al subir la imagen.")
                        }
                        return@launch
                    }
                }

                // Subir video (si existe)
                var videoUrl: String? = null
                if (videoUri != null) {
                    Log.d(TAG, "🎥 Subiendo video...")

                    // Validar duración del video
                    val duration = getVideoDuration(context, videoUri)
                    if (duration > 120000) { // 120 segundos = 2 minutos
                        withContext(Dispatchers.Main) {
                            onError("El video no debe superar los 2 minutos de duración")
                        }
                        return@launch
                    }

                    videoUrl = uploadVideo(context, videoUri)
                    if (videoUrl == null) {
                        withContext(Dispatchers.Main) {
                            onError("Error al subir el video.")
                        }
                        return@launch
                    }
                }

                // Crear noticia
                val news = News(
                    title = title,
                    description = description,
                    imageUrl = imageUrl ?: "",
                    videoUrl = videoUrl,
                    isImportant = isImportant,
                    userId = currentUser.id
                )

                supabase.from("news").insert(news)
                Log.d(TAG, "✅ Noticia creada exitosamente")
                Log.d(TAG, "📊 Título: $title | Destacada: $isImportant | Tiene video: ${videoUrl != null}")

                // Recargar noticias
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
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔥 Actualizar noticia
    fun updateNews(
        context: Context,
        newsId: String,
        title: String,
        description: String,
        isImportant: Boolean,
        newImageUri: Uri?,
        newVideoUri: Uri?,
        currentImageUrl: String,
        currentVideoUrl: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "📝 Actualizando noticia ID: $newsId")
                _isLoading.value = true

                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser == null) {
                    withContext(Dispatchers.Main) {
                        onError("No hay usuario autenticado.")
                    }
                    return@launch
                }

                // Actualizar imagen si hay una nueva
                val finalImageUrl = if (newImageUri != null) {
                    Log.d(TAG, "📤 Subiendo nueva imagen...")
                    uploadImage(context, newImageUri) ?: currentImageUrl
                } else {
                    currentImageUrl
                }

                // Actualizar video si hay uno nuevo
                val finalVideoUrl = if (newVideoUri != null) {
                    Log.d(TAG, "🎥 Subiendo nuevo video...")

                    val duration = getVideoDuration(context, newVideoUri)
                    if (duration > 120000) {
                        withContext(Dispatchers.Main) {
                            onError("El video no debe superar los 2 minutos de duración")
                        }
                        return@launch
                    }

                    uploadVideo(context, newVideoUri)
                } else {
                    currentVideoUrl
                }

                val updatedNews = News(
                    id = newsId,
                    title = title,
                    description = description,
                    imageUrl = finalImageUrl,
                    videoUrl = finalVideoUrl,
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
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔥 Eliminar noticia
    fun deleteNews(
        newsId: String,
        imageUrl: String,
        videoUrl: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🗑️ Eliminando noticia ID: $newsId")
                _isLoading.value = true

                supabase.from("news").delete {
                    filter {
                        eq("id", newsId)
                    }
                }

                // Eliminar imagen
                try {
                    val fileName = imageUrl.substringAfterLast("/")
                    if (fileName.isNotBlank()) {
                        supabase.storage.from("news-images").delete(fileName)
                        Log.d(TAG, "🗑️ Imagen eliminada: $fileName")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ No se pudo eliminar imagen: ${e.message}")
                }

                // Eliminar video
                if (!videoUrl.isNullOrBlank()) {
                    try {
                        val fileName = videoUrl.substringAfterLast("/")
                        if (fileName.isNotBlank()) {
                            supabase.storage.from("news-videos").delete(fileName)
                            Log.d(TAG, "🗑️ Video eliminado: $fileName")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "⚠️ No se pudo eliminar video: ${e.message}")
                    }
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
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔥 Subir imagen
    private suspend fun uploadImage(context: Context, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            var file: File? = null
            try {
                val timestamp = System.currentTimeMillis()
                val randomId = UUID.randomUUID().toString().take(8)
                val fileName = "news_${timestamp}_${randomId}.jpg"

                Log.d(TAG, "📁 Preparando imagen: $fileName")

                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: run {
                        Log.e(TAG, "❌ No se pudo abrir el InputStream")
                        return@withContext null
                    }

                file = File(context.cacheDir, fileName)
                val outputStream = FileOutputStream(file)

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                if (!file.exists() || file.length() == 0L) {
                    Log.e(TAG, "❌ Archivo vacío o no existe")
                    file?.delete()
                    return@withContext null
                }

                val maxSize = 5 * 1024 * 1024 // 5MB
                if (file.length() > maxSize) {
                    Log.e(TAG, "❌ Imagen muy grande: ${file.length()} bytes")
                    file.delete()
                    return@withContext null
                }

                Log.d(TAG, "📤 Subiendo imagen a Supabase Storage...")
                val bucket = supabase.storage.from("news-images")
                val fileBytes = file.readBytes()

                bucket.upload(
                    path = fileName,
                    data = fileBytes,
                    upsert = false
                )

                val publicUrl = bucket.publicUrl(fileName)
                Log.d(TAG, "✅ Imagen subida exitosamente")

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

    // 🔥 Subir video
    private suspend fun uploadVideo(context: Context, videoUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            var file: File? = null
            try {
                val timestamp = System.currentTimeMillis()
                val randomId = UUID.randomUUID().toString().take(8)
                val fileName = "news_video_${timestamp}_${randomId}.mp4"

                Log.d(TAG, "📁 Preparando video: $fileName")

                val inputStream = context.contentResolver.openInputStream(videoUri)
                    ?: run {
                        Log.e(TAG, "❌ No se pudo abrir el InputStream del video")
                        return@withContext null
                    }

                file = File(context.cacheDir, fileName)
                val outputStream = FileOutputStream(file)

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                if (!file.exists() || file.length() == 0L) {
                    Log.e(TAG, "❌ Video vacío o no existe")
                    file?.delete()
                    return@withContext null
                }

                val maxSize = 50 * 1024 * 1024 // 50MB (2 minutos aproximado)
                if (file.length() > maxSize) {
                    Log.e(TAG, "❌ Video muy grande: ${file.length()} bytes")
                    file.delete()
                    return@withContext null
                }

                Log.d(TAG, "📤 Subiendo video a Supabase Storage...")
                val bucket = supabase.storage.from("news-videos")
                val fileBytes = file.readBytes()

                bucket.upload(
                    path = fileName,
                    data = fileBytes,
                    upsert = false
                )

                val publicUrl = bucket.publicUrl(fileName)
                Log.d(TAG, "✅ Video subido exitosamente")

                file.delete()

                publicUrl.takeIf { it.isNotBlank() }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al subir video: ${e.message}", e)
                e.printStackTrace()
                file?.delete()
                null
            }
        }
    }

    // 🔥 Obtener duración del video en milisegundos
    private fun getVideoDuration(context: Context, videoUri: Uri): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoUri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            duration?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener duración del video: ${e.message}")
            0L
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Reemplazar la función loadCommentsForNews en NewsViewModel con esta versión:

    // REEMPLAZAR loadCommentsForNews en NewsViewModel con esta versión:

    // REEMPLAZAR loadCommentsForNews en NewsViewModel con esta versión SIMPLE:

    // REEMPLAZAR loadCommentsForNews en NewsViewModel:

    // REEMPLAZAR loadCommentsForNews en NewsViewModel:

    // REEMPLAZAR loadCommentsForNews en NewsViewModel:

    fun loadCommentsForNews(newsId: String) {
        viewModelScope.launch {
            _isLoadingComments.value = true

            try {
                Log.d(TAG, "📥 Cargando comentarios para noticia: $newsId")

                // Obtener comentarios básicos
                val rawComments = supabase
                    .from("comments")
                    .select {
                        filter {
                            eq("news_id", newsId)
                            isNull("parent_comment_id")
                        }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<Comment>()

                Log.d(TAG, "📦 ${rawComments.size} comentarios RAW encontrados")

                if (rawComments.isEmpty()) {
                    Log.w(TAG, "⚠️ No se encontraron comentarios para news_id: $newsId")
                    val currentMap = _commentsMap.value.toMutableMap()
                    currentMap[newsId] = emptyList()
                    _commentsMap.value = currentMap
                    return@launch
                }

                // Para cada comentario, obtener datos del usuario
                val commentsWithUserData = rawComments.mapIndexed { index, comment ->
                    try {
                        Log.d(TAG, "  🔍 [$index] Buscando usuario: ${comment.userId}")

                        val userData = supabase
                            .from("users")
                            .select {
                                filter {
                                    eq("id", comment.userId)
                                }
                            }
                            .decodeSingleOrNull<CommentUser>()

                        if (userData != null) {
                            Log.d(TAG, "  ✅ [$index] Usuario encontrado: ${userData.name}")
                            Log.d(TAG, "  📸 [$index] Foto: ${userData.photoUrl ?: "sin foto"}")
                        } else {
                            Log.w(TAG, "  ⚠️ [$index] Usuario NO encontrado para: ${comment.userId}")
                        }

                        // Crear nuevo comentario con los datos del usuario
                        Comment(
                            id = comment.id,
                            userId = comment.userId,
                            message = comment.message,
                            newsId = comment.newsId,
                            createdAt = comment.createdAt,
                            parentCommentId = comment.parentCommentId,
                            authorName = userData?.name, // Asignar nombre del usuario
                            authorPhotoUrl = userData?.photoUrl, // ✅ Asignar foto del usuario
                            users = userData // Asignar objeto completo del usuario
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠️ [$index] Error obteniendo usuario ${comment.userId}: ${e.message}")
                        comment // Retornar comentario original si falla
                    }
                }

                // Actualizar el mapa
                val currentMap = _commentsMap.value.toMutableMap()
                currentMap[newsId] = commentsWithUserData
                _commentsMap.value = currentMap

                Log.d(TAG, "✅ ${commentsWithUserData.size} comentarios procesados")
                commentsWithUserData.forEach { comment ->
                    Log.d(TAG, "  💬 ${comment.displayName}: ${comment.message.take(30)}... | Foto: ${comment.displayPhotoUrl != null}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al cargar comentarios: ${e.message}", e)
                e.printStackTrace()
            } finally {
                _isLoadingComments.value = false
            }
        }
    }


    // Agregar estas funciones al NewsViewModel existente

    /**
     * Publicar un comentario en una noticia
     */
    // REEMPLAZAR la función postComment en NewsViewModel con esta versión:

    // REEMPLAZAR la función postComment en NewsViewModel con esta versión:

    /**
     * Publicar un comentario en una noticia
     */
    fun postComment(
        newsId: String,
        message: String,
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "📝 Publicando comentario en noticia $newsId")
                Log.d(TAG, "🔹 Usuario: $userId")
                Log.d(TAG, "🔹 Mensaje: $message")

                // Crear el comentario
                val comment = mapOf(
                    "news_id" to newsId,
                    "user_id" to userId,
                    "message" to message
                )

                supabase.from("comments")
                    .insert(comment)

                Log.d(TAG, "✅ Comentario publicado exitosamente")

                withContext(Dispatchers.Main) {
                    onSuccess()
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al publicar comentario: ${e.message}", e)
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Error al publicar comentario")
                }
            }
        }
    }

    /**
     * Eliminar un comentario (solo moderadores)
     */
    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🗑️ Eliminando comentario ID: $commentId")

                supabase.from("comments")
                    .delete {
                        filter {
                            eq("id", commentId)
                        }
                    }

                Log.d(TAG, "✅ Comentario eliminado exitosamente")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al eliminar comentario: ${e.message}", e)
            }
        }
    }

    /**
     * Cargar comentarios para todas las noticias visibles
     */
    fun loadAllComments(newsIds: List<String>) {
        newsIds.forEach { newsId ->
            loadCommentsForNews(newsId)
        }
    }

    /**
     * Obtener comentarios de una noticia específica
     */
    fun getCommentsForNews(newsId: String): List<Comment> {
        return _commentsMap.value[newsId] ?: emptyList()
    }

    fun formatNewsDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSXXX", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: "Fecha"
        } catch (e: Exception) {
            try {
                // Intentar con formato ISO 8601 alternativo
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
                val date = inputFormat.parse(dateString)
                date?.let { outputFormat.format(it) } ?: "Fecha"
            } catch (e: Exception) {
                "Fecha"
            }
        }
    }
}