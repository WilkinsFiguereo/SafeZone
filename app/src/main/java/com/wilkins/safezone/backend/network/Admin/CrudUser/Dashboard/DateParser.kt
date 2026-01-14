package com.wilkins.safezone.backend.network.Admin.CrudUser.Dashboard


import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object DateParser {
    private const val TAG = "DateParser"

    // Formatos de fecha soportados
    private val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    )

    /**
     * Parsea una fecha en formato ISO 8601 con microsegundos y zona horaria
     * Ejemplos:
     * - 2025-11-24T18:22:23.098968+00:00
     * - 2025-11-24T18:22:23+00:00
     * - 2025-11-24 18:22:23
     */
    fun parseIsoDate(dateString: String): Date? {
        if (dateString.isBlank()) {
            Log.w(TAG, "Fecha vacía")
            return null
        }

        try {
            // Limpiar la fecha: remover zona horaria y normalizar microsegundos
            var cleanDate = dateString
                .replace("+00:00", "")
                .replace("+00", "")
                .replace("Z", "")
                .trim()

            // Si tiene microsegundos (más de 3 dígitos después del punto), truncar a milisegundos
            val dotIndex = cleanDate.indexOf('.')
            if (dotIndex != -1) {
                val beforeDot = cleanDate.substring(0, dotIndex)
                val afterDot = cleanDate.substring(dotIndex + 1)

                // Tomar solo los primeros 3 dígitos (milisegundos) o 6 (microsegundos)
                val milliseconds = when {
                    afterDot.length >= 6 -> afterDot.substring(0, 6) // Microsegundos
                    afterDot.length >= 3 -> afterDot.substring(0, 3) // Milisegundos
                    else -> afterDot.padEnd(3, '0') // Rellenar con ceros
                }

                // Truncar a 3 dígitos para milisegundos
                cleanDate = "$beforeDot.${milliseconds.take(3)}"
            }

            // Intentar parsear con cada formato
            for (format in dateFormats) {
                try {
                    return format.parse(cleanDate)
                } catch (e: Exception) {
                    // Intentar con el siguiente formato
                    continue
                }
            }

            Log.e(TAG, "No se pudo parsear la fecha con ningún formato: $dateString")
            return null

        } catch (e: Exception) {
            Log.e(TAG, "Error parseando fecha: $dateString", e)
            return null
        }
    }

    /**
     * Formatea una fecha a string legible
     */
    fun formatDate(date: Date, pattern: String = "dd/MM/yyyy HH:mm"): String {
        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).format(date)
        } catch (e: Exception) {
            Log.e(TAG, "Error formateando fecha", e)
            ""
        }
    }

    /**
     * Parsea y formatea una fecha ISO directamente a string legible
     */
    fun parseAndFormat(dateString: String, pattern: String = "dd/MM/yyyy HH:mm"): String {
        val date = parseIsoDate(dateString)
        return if (date != null) {
            formatDate(date, pattern)
        } else {
            "Fecha no disponible"
        }
    }
}