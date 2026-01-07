package com.wilkins.safezone.bridge.User.Form

import android.content.Context
import android.util.Log
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.User.Form.Report
import com.wilkins.safezone.backend.network.User.Form.insertReportBackend
import com.wilkins.safezone.backend.network.User.Form.uploadImageToSupabase
import com.wilkins.safezone.backend.network.User.Form.uploadMediaToSupabase
import com.wilkins.safezone.backend.network.auth.SessionManager
import com.wilkins.safezone.backend.network.auth.SessionManager.getUserProfile
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportRepository(private val context: Context) {

    private val client = SupabaseService.getInstance()

    // =============================
    // 🔹 Obtener ID del usuario
    // =============================
    private suspend fun getUserId(): String? {
        val session = SessionManager.loadSession(context) ?: return null
        Log.d("ReportRepository", "✅ User ID obtenido: ${session.user?.id}")
        return session.user?.id
    }

    // =============================
    // 🔹 Obtener nombre del usuario
    // =============================
    private suspend fun getUserName(): String? {
        val profile = getUserProfile(context)
        val name = profile?.name ?: "Usuario"
        Log.d("ReportRepository", "✅ User name obtenido: $name")
        return name
    }

    // ============================================================================
    // 🔥 BRIDGE: Valida → Subir imagen → Crear modelo → Insertar en Supabase
    // ============================================================================
    suspend fun createReportBridge(
        description: String,
        imageBytes: ByteArray?,
        isAnonymous: Boolean,
        reportLocation: String,
        affairId: Int,
        mediaType: String? = null,  // NUEVO PARÁMETRO
        mediaFileName: String? = null  // NUEVO PARÁMETRO
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("ReportRepository", "🔄 Iniciando creación de reporte...")
            Log.d("ReportRepository", "📝 Descripción: ${description.take(50)}...")
            Log.d("ReportRepository", "🔒 Anónimo: $isAnonymous")
            Log.d("ReportRepository", "📍 Ubicación: $reportLocation")
            Log.d("ReportRepository", "📋 Affair ID: $affairId")
            Log.d("ReportRepository", "🎬 Tipo de media: $mediaType")
            Log.d("ReportRepository", "📁 Nombre de archivo: $mediaFileName")

            // Subir imagen/video si existe
            var mediaUrl: String? = null
            if (imageBytes != null) {
                Log.d("ReportRepository", "📤 Subiendo archivo multimedia...")

                // ACTUALIZACIÓN IMPORTANTE: Usar la nueva función con tipo de archivo
                mediaUrl = if (mediaType != null && mediaFileName != null) {
                    // Usar la nueva función que detecta la extensión correcta
                    uploadMediaToSupabase(
                        context = context,
                        fileBytes = imageBytes,
                        fileName = mediaFileName,
                        mediaType = mediaType
                    )
                } else {
                    // Fallback a la función legacy (solo para compatibilidad)
                    Log.w("ReportRepository", "⚠️ Usando función legacy - el tipo de archivo podría ser incorrecto")
                    uploadImageToSupabase(context, imageBytes)
                }

                if (mediaUrl != null) {
                    Log.d("ReportRepository", "✅ Archivo subido exitosamente: $mediaUrl")
                } else {
                    Log.e("ReportRepository", "❌ Error al subir archivo")
                    return@withContext Result.failure(Exception("Error al subir archivo multimedia"))
                }
            } else {
                Log.d("ReportRepository", "ℹ️ No se proporcionó archivo multimedia")
            }

            // Obtener información del usuario
            val session = SessionManager.loadSession(context)
            val userId = session?.user?.id
            val userName = session?.user?.userMetadata?.get("name")?.toString()

            Log.d("ReportRepository", "👤 Usuario ID: $userId")
            Log.d("ReportRepository", "👤 Usuario Nombre: $userName")

            if (userId == null) {
                Log.e("ReportRepository", "❌ Usuario no autenticado")
                return@withContext Result.failure(Exception("Usuario no autenticado"))
            }

            // Crear objeto Report
            val report = Report(
                description = description,
                image_url = mediaUrl,
                is_anonymous = isAnonymous,
                report_location = reportLocation,
                user_id = userId,
                user_name = if (isAnonymous) null else userName,
                id_affair = affairId,
                id_reporting_status = 5
            )

            Log.d("ReportRepository", "💾 Insertando reporte en la base de datos...")

            // Insertar en Supabase
            val success = insertReportBackend(report)

            if (success) {
                Log.d("ReportRepository", "✅ Reporte creado exitosamente")
                Result.success(true)
            } else {
                Log.e("ReportRepository", "❌ Error al insertar reporte en la base de datos")
                Result.failure(Exception("Error al crear el reporte"))
            }

        } catch (e: Exception) {
            Log.e("ReportRepository", "❌ Error en createReportBridge: ${e.message}")
            Log.e("ReportRepository", "❌ Stack trace:", e)
            Result.failure(e)
        }
    }
}
