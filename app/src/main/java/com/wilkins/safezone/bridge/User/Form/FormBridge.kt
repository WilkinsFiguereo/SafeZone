package com.wilkins.safezone.bridge.User.Form

import SessionManager
import SessionManager.getUserProfile
import android.content.Context
import android.util.Log
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.User.Form.Report
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
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
    // 🔥 BRIDGE: Frontend → valida → crea el modelo → envía al backend (Supabase)
    // ============================================================================
    suspend fun createReportBridge(
        description: String,
        imageUrl: String?,
        isAnonymous: Boolean,
        reportLocation: String?,
        affairId: Int       // 👈 YA RECIBE EL ID SELECCIONADO
    ): Result<Boolean> = withContext(Dispatchers.IO) {

        Log.d("ReportRepository", "🔄 Iniciando creación de reporte...")
        Log.d("ReportRepository", "📋 Datos recibidos:")
        Log.d("ReportRepository", "  - Affair ID: $affairId")
        Log.d("ReportRepository", "  - Description: $description")
        Log.d("ReportRepository", "  - Location: $reportLocation")
        Log.d("ReportRepository", "  - Is Anonymous: $isAnonymous")
        Log.d("ReportRepository", "  - Image URL: $imageUrl")

        // ---- Validación ----
        if (description.isBlank()) {
            Log.e("ReportRepository", "❌ Validación fallida: descripción vacía")
            return@withContext Result.failure(Exception("La descripción es obligatoria"))
        }

        val userId = getUserId()
        if (userId == null) {
            Log.e("ReportRepository", "❌ No hay sesión activa")
            return@withContext Result.failure(Exception("No hay sesión activa"))
        }

        val userName = if (isAnonymous) {
            Log.d("ReportRepository", "🕵️ Reporte anónimo, userName será null")
            null
        } else {
            getUserName()
        }

        // ---- Crear objeto Report EXACTO ----
        val report = Report(
            id_affair = affairId,
            description = description,
            image_url = imageUrl,
            user_id = userId,
            is_anonymous = isAnonymous,
            user_name = userName,
            report_location = reportLocation,
            id_reporting_status = 1 // estado inicial
        )

        Log.d("ReportRepository", "📦 Objeto Report creado:")
        Log.d("ReportRepository", "  - id_affair: ${report.id_affair}")
        Log.d("ReportRepository", "  - user_id: ${report.user_id}")
        Log.d("ReportRepository", "  - is_anonymous: ${report.is_anonymous}")
        Log.d("ReportRepository", "  - user_name: ${report.user_name}")
        Log.d("ReportRepository", "  - report_location: ${report.report_location}")
        Log.d("ReportRepository", "  - id_reporting_status: ${report.id_reporting_status}")

        try {
            Log.d("ReportRepository", "🚀 Enviando reporte a Supabase...")
            val result = client.postgrest
                .from("reports")
                .insert(report)

            Log.d("ReportRepository", "✅ Reporte insertado exitosamente")
            Log.d("ReportRepository", "📊 Resultado: ${result.data}")

            Result.success(true)

        } catch (e: Exception) {
            Log.e("ReportRepository", "❌ Error al insertar reporte: ${e.message}")
            Log.e("ReportRepository", "❌ Tipo de error: ${e.javaClass.simpleName}")
            Log.e("ReportRepository", "❌ Stack trace completo:", e)
            Result.failure(e)
        }
    }
}