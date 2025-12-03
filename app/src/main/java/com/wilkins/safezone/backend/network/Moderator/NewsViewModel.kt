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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class NewsViewModel : ViewModel() {

    private val supabase = SupabaseService.getInstance()
    private val TAG = "NewsViewModel"

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

                // 1. Obtener el ID del usuario actual
                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser == null) {
                    Log.e(TAG, "❌ No hay usuario autenticado")
                    withContext(Dispatchers.Main) {
                        onError("No hay usuario autenticado. Por favor inicia sesión.")
                    }
                    return@launch
                }

                val userId = currentUser.id
                Log.d(TAG, "✅ Usuario autenticado: $userId")

                // 2. Subir la imagen a Supabase Storage
                Log.d(TAG, "📤 Subiendo imagen...")
                val imageUrl = uploadImage(context, imageUri)
                if (imageUrl == null) {
                    Log.e(TAG, "❌ Error al subir la imagen")
                    withContext(Dispatchers.Main) {
                        onError("Error al subir la imagen. Verifica que el bucket 'news-images' existe y es público.")
                    }
                    return@launch
                }

                Log.d(TAG, "✅ Imagen subida: $imageUrl")

                // 3. Crear el objeto de noticia
                val news = News(
                    title = title,
                    description = description,
                    imageUrl = imageUrl,
                    isImportant = isImportant,
                    userId = userId
                )

                Log.d(TAG, "💾 Insertando noticia en BD...")

                // 4. Insertar en la base de datos
                supabase.from("news").insert(news)

                Log.d(TAG, "✅ Noticia creada exitosamente")

                // 5. Notificar éxito
                withContext(Dispatchers.Main) {
                    onSuccess()
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en createNews: ${e.message}", e)
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    val errorMsg = when {
                        e.message?.contains("404") == true -> "El bucket 'news-images' no existe. Créalo en Supabase Storage."
                        e.message?.contains("401") == true || e.message?.contains("403") == true ->
                            "No tienes permisos. Configura las políticas del bucket."
                        e.message?.contains("network") == true || e.message?.contains("timeout") == true ->
                            "Error de conexión. Verifica tu internet."
                        e.message?.contains("duplicate") == true ->
                            "Ya existe una noticia con estos datos."
                        else -> "Error: ${e.message ?: "Error desconocido"}"
                    }
                    onError(errorMsg)
                }
            }
        }
    }

    private suspend fun uploadImage(context: Context, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            var file: File? = null
            try {
                Log.d(TAG, "📸 Procesando imagen desde URI: $imageUri")

                // Generar nombre único para la imagen
                val timestamp = System.currentTimeMillis()
                val randomId = UUID.randomUUID().toString().take(8)
                val fileName = "news_${timestamp}_${randomId}.jpg"
                Log.d(TAG, "📝 Nombre de archivo: $fileName")

                // Leer el archivo de la URI
                val inputStream = context.contentResolver.openInputStream(imageUri)
                if (inputStream == null) {
                    Log.e(TAG, "❌ No se pudo abrir el inputStream de la URI")
                    return@withContext null
                }

                file = File(context.cacheDir, fileName)
                val outputStream = FileOutputStream(file)

                var bytesCopied = 0L
                inputStream.use { input ->
                    outputStream.use { output ->
                        bytesCopied = input.copyTo(output)
                        Log.d(TAG, "✅ Bytes copiados: $bytesCopied")
                    }
                }

                // Verificar que el archivo existe y tiene contenido
                if (!file.exists()) {
                    Log.e(TAG, "❌ El archivo no se creó correctamente")
                    return@withContext null
                }

                val fileSize = file.length()
                Log.d(TAG, "📦 Tamaño del archivo: $fileSize bytes (${fileSize / 1024}KB)")

                if (fileSize == 0L) {
                    Log.e(TAG, "❌ El archivo está vacío")
                    file.delete()
                    return@withContext null
                }

                // Verificar tamaño máximo (5MB)
                val maxSize = 5 * 1024 * 1024 // 5MB
                if (fileSize > maxSize) {
                    Log.e(TAG, "❌ El archivo es muy grande: ${fileSize / 1024 / 1024}MB (máximo 5MB)")
                    file.delete()
                    return@withContext null
                }

                // Subir a Supabase Storage
                Log.d(TAG, "☁️ Conectando con Supabase Storage bucket: news-images")
                val bucket = supabase.storage.from("news-images")

                Log.d(TAG, "⬆️ Subiendo archivo al bucket (${fileSize / 1024}KB)...")

                val fileBytes = file.readBytes()
                Log.d(TAG, "📊 Bytes leídos para upload: ${fileBytes.size}")

                bucket.upload(
                    path = fileName,
                    data = fileBytes,
                    upsert = false
                )

                Log.d(TAG, "✅ Archivo subido exitosamente a Supabase")

                // Obtener la URL pública
                val publicUrl = bucket.publicUrl(fileName)
                Log.d(TAG, "🔗 URL pública generada: $publicUrl")

                // Limpiar archivo temporal
                if (file.exists()) {
                    val deleted = file.delete()
                    Log.d(TAG, "🧹 Archivo temporal eliminado: $deleted")
                }

                if (publicUrl.isBlank()) {
                    Log.e(TAG, "❌ La URL pública está vacía")
                    return@withContext null
                }

                publicUrl

            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR DETALLADO al subir imagen:", e)
                Log.e(TAG, "   Tipo de error: ${e.javaClass.simpleName}")
                Log.e(TAG, "   Mensaje: ${e.message}")
                Log.e(TAG, "   Causa: ${e.cause?.message}")
                e.printStackTrace()

                // Limpiar archivo si existe
                file?.let {
                    if (it.exists()) {
                        val deleted = it.delete()
                        Log.d(TAG, "🧹 Archivo temporal eliminado después del error: $deleted")
                    }
                }

                null
            }
        }
    }
}