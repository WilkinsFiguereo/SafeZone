package com.wilkins.safezone.backend.GlobalAssociation

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
    private val displayTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun formatDate(dateString: String): String {
        return try {
            val date = inputFormat.parse(dateString.replace("+00", ""))
            displayDateFormat.format(date ?: Date())
        } catch (e: Exception) {
            "Fecha no disponible"
        }
    }

    fun formatTime(dateString: String): String {
        return try {
            val date = inputFormat.parse(dateString.replace("+00", ""))
            displayTimeFormat.format(date ?: Date())
        } catch (e: Exception) {
            "Hora no disponible"
        }
    }

    fun formatDateTime(dateString: String): String {
        return "${formatDate(dateString)} ${formatTime(dateString)}"
    }
}

object ReportUtils {
    fun generateTitle(affairName: String?, description: String?): String {
        return when {
            !affairName.isNullOrBlank() && !description.isNullOrBlank() -> {
                val shortDesc = if (description.length > 30) {
                    description.take(27) + "..."
                } else {
                    description
                }
                "$affairName - $shortDesc"
            }
            !affairName.isNullOrBlank() -> affairName
            !description.isNullOrBlank() -> {
                if (description.length > 40) {
                    description.take(37) + "..."
                } else {
                    description
                }
            }
            else -> "Reporte sin título"
        }
    }

    fun getReporterName(isAnonymous: Boolean, userName: String?): String {
        return if (isAnonymous) {
            "Usuario Anónimo"
        } else {
            userName ?: "Usuario Desconocido"
        }
    }

    fun getShortId(id: String): String {
        return id.take(8)
    }
}