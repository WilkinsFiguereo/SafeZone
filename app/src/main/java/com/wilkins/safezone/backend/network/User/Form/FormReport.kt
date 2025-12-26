package com.wilkins.safezone.backend.network.User.Form

import android.content.Context
import android.net.Uri
import android.util.Log
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import java.util.UUID

suspend fun insertReportBackend(report: Report): Boolean {
    val client = SupabaseService.getInstance()

    val result = client.postgrest
        .from("reports")
        .insert(report)

    return result.data != null
}

suspend fun getAffairs(client: SupabaseClient): List<Affair> {
    Log.d("FormBackend", "🔍 Iniciando consulta a tabla 'affair'...")
    Log.d("FormBackend", "🔌 Supabase URL: ${client.supabaseUrl}")

    try {
        val result = client.postgrest
            .from("affair")
            .select()
            .decodeList<Affair>()

        Log.d("FormBackend", "✅ Consulta exitosa, affairs obtenidos: ${result.size}")

        if (result.isEmpty()) {
            Log.w("FormBackend", "⚠️ ADVERTENCIA: La consulta no devolvió datos")
            Log.w("FormBackend", "⚠️ Posibles causas:")
            Log.w("FormBackend", "   1. Row Level Security (RLS) está bloqueando la lectura")
            Log.w("FormBackend", "   2. La tabla 'affair' está vacía")
            Log.w("FormBackend", "   3. Problemas de permisos en Supabase")
            Log.w("FormBackend", "   4. El modelo Affair no coincide con la estructura de la tabla")
        } else {
            result.forEach { affair ->
                Log.d("FormBackend", "  - ID: ${affair.id}, Type: ${affair.type}")
            }
        }

        return result
    } catch (e: Exception) {
        Log.e("FormBackend", "❌ Error en getAffairs: ${e.message}")
        Log.e("FormBackend", "❌ Tipo de excepción: ${e.javaClass.simpleName}")
        Log.e("FormBackend", "❌ Stack trace completo:", e)
        throw e
    }
}

/**
 * Sube un archivo multimedia (imagen o video) a Supabase Storage
 * @param context Contexto de la aplicación
 * @param fileBytes Bytes del archivo
 * @param fileName Nombre original del archivo (para detectar extensión)
 * @param mediaType Tipo de medio: "image" o "video"
 * @return URL pública del archivo subido o null si falla
 */
suspend fun uploadMediaToSupabase(
    context: Context,
    fileBytes: ByteArray,
    fileName: String,
    mediaType: String
): String? {
    return try {
        Log.d("MediaUpload", "📤 Iniciando subida de $mediaType: $fileName")
        Log.d("MediaUpload", "📊 Tamaño del archivo: ${fileBytes.size} bytes (${fileBytes.size / 1024f / 1024f} MB)")

        val client = SupabaseService.getInstance()
        val bucket = client.storage.from("report")

        // Detectar extensión correcta basada en el nombre del archivo o tipo
        val extension = getFileExtension(fileName, mediaType)

        // Generar nombre único con la extensión correcta
        val uniqueFileName = "${UUID.randomUUID()}.$extension"

        Log.d("MediaUpload", "📝 Nombre original: $fileName")
        Log.d("MediaUpload", "📝 Extensión detectada: $extension")
        Log.d("MediaUpload", "📝 Nombre único generado: $uniqueFileName")
        Log.d("MediaUpload", "📝 Tipo de medio: $mediaType")

        // Subir archivo al bucket
        bucket.upload(uniqueFileName, fileBytes)

        Log.d("MediaUpload", "✅ Archivo subido exitosamente")

        // Obtener URL pública
        val publicUrl = bucket.publicUrl(uniqueFileName)

        Log.d("MediaUpload", "🔗 URL pública generada: $publicUrl")

        publicUrl
    } catch (e: Exception) {
        Log.e("MediaUpload", "❌ Error subiendo archivo: ${e.message}")
        Log.e("MediaUpload", "❌ Tipo de excepción: ${e.javaClass.simpleName}")
        Log.e("MediaUpload", "❌ Stack trace:", e)
        null
    }
}

/**
 * Detecta la extensión correcta del archivo basándose en el nombre y tipo
 */
private fun getFileExtension(fileName: String, mediaType: String): String {
    // Primero intentar obtener la extensión del nombre del archivo
    val extensionFromFileName = fileName.substringAfterLast('.', "").lowercase()

    // Si la extensión es válida, usarla
    if (extensionFromFileName.isNotEmpty() && isValidExtension(extensionFromFileName, mediaType)) {
        Log.d("MediaUpload", "✅ Usando extensión del nombre de archivo: $extensionFromFileName")
        return extensionFromFileName
    }

    // Si no, usar extensión por defecto según el tipo
    val defaultExtension = when (mediaType) {
        "image" -> "jpg"
        "video" -> "mp4"
        else -> "bin" // binario genérico
    }

    Log.d("MediaUpload", "⚠️ Usando extensión por defecto: $defaultExtension (para tipo: $mediaType)")
    return defaultExtension
}

/**
 * Valida si una extensión es apropiada para el tipo de medio
 */
private fun isValidExtension(extension: String, mediaType: String): Boolean {
    val validImageExtensions = listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
    val validVideoExtensions = listOf("mp4", "mov", "avi", "mkv", "webm", "3gp", "flv")

    return when (mediaType) {
        "image" -> extension in validImageExtensions
        "video" -> extension in validVideoExtensions
        else -> false
    }
}

/**
 * Función legacy para mantener compatibilidad
 * @deprecated Usar uploadMediaToSupabase en su lugar
 */
suspend fun uploadImageToSupabase(
    context: Context,
    imageBytes: ByteArray
): String? {
    Log.w("MediaUpload", "⚠️ Usando función legacy uploadImageToSupabase")
    return uploadMediaToSupabase(context, imageBytes, "image.jpg", "image")
}