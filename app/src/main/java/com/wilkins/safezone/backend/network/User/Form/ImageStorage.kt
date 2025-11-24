package com.wilkins.safezone.backend.network.User.Form

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import java.util.UUID

suspend fun uploadImageToSupabase(
    supabase: SupabaseClient,
    fileBytes: ByteArray,
    userId: String
): String {
    val TAG = "SupabaseUpload"

    Log.d(TAG, "🚀 Iniciando subida de imagen...")
    Log.d(TAG, "📌 Tamaño del archivo: ${fileBytes.size} bytes")
    Log.d(TAG, "📌 UserID: $userId")

    val bucket = supabase.storage.from("report")
    Log.d(TAG, "📦 Accediendo al bucket 'reports'...")

    val fileName = "$userId/${UUID.randomUUID()}.jpg"
    Log.d(TAG, "📝 Nombre generado para el archivo: $fileName")

    try {
        Log.d(TAG, "⬆️ Subiendo archivo a Supabase Storage...")

        val result = bucket.upload(
            path = fileName,
            data = fileBytes,
            upsert = false
        )

        Log.d(TAG, "✅ Upload completado: $result")

        // Obtener URL pública
        val publicUrl = bucket.publicUrl(fileName)

        Log.d(TAG, "🌐 URL pública generada:")
        Log.d(TAG, publicUrl)

        return publicUrl

    } catch (e: Exception) {
        Log.e(TAG, "❌ Error subiendo archivo: ${e.message}", e)
        throw e
    }
}
