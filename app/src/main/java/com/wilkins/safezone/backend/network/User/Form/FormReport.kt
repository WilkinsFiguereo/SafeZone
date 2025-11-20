package com.wilkins.safezone.backend.network.User.Form

import android.util.Log
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

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