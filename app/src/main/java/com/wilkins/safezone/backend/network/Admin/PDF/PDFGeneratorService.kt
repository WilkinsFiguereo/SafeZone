package com.wilkins.safezone.backend.network.Admin.PDF

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.wilkins.safezone.backend.network.Admin.CrudUser.CrudUser
import com.wilkins.safezone.backend.network.Admin.CrudUser.Profile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PDFGeneratorService(private val context: Context) {

    private val crudUser = CrudUser()
    private val reportRepository = ReportRepository()
    private val pageWidth = 595 // A4 width in points
    private val pageHeight = 842 // A4 height in points
    private val margin = 40
    private val contentWidth = pageWidth - (margin * 2)

    /**
     * Genera un reporte PDF de usuarios según el filtro especificado
     */
    suspend fun generateUserReport(reportType: String): Result<File> {
        return try {
            val profiles = when (reportType) {
                "Usuarios en General" -> crudUser.getAllProfiles() + crudUser.getAllProfilesDisabled()
                "Usuarios Regulares" -> crudUser.getProfilesByRole(1)
                "Usuarios Admin" -> crudUser.getProfilesByRole(2)
                "Usuarios Moderadores" -> crudUser.getProfilesByRole(3)
                "Usuarios Asociación Gubernamental" -> crudUser.getProfilesByRole(4)
                else -> emptyList()
            }

            if (profiles.isEmpty()) {
                return Result.failure(Exception("No se encontraron usuarios para este reporte"))
            }

            val pdfFile = createUserPDF(reportType, profiles)
            Result.success(pdfFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Genera un reporte PDF de incidencias según el filtro especificado
     */
    suspend fun generateReportReport(reportType: String): Result<File> {
        return try {
            android.util.Log.d("PDFGenerator", "🔄 Generando reporte: $reportType")

            // Primero hacer diagnóstico si es necesario
            reportRepository.debugDatabaseInfo()

            val reports = when (reportType) {
                "Reportes en General" -> {
                    android.util.Log.d("PDFGenerator", "📊 Obteniendo todos los reportes...")
                    reportRepository.getAllReports()
                }
                "Reportes Pendientes" -> {
                    android.util.Log.d("PDFGenerator", "📊 Obteniendo reportes pendientes (status=1)...")
                    reportRepository.getReportsByStatus(1)
                }
                "Reportes en Proceso" -> {
                    android.util.Log.d("PDFGenerator", "📊 Obteniendo reportes en proceso (status=2)...")
                    reportRepository.getReportsByStatus(2)
                }
                "Reportes Completados" -> {
                    android.util.Log.d("PDFGenerator", "📊 Obteniendo reportes completados (status=3)...")
                    reportRepository.getReportsByStatus(3)
                }
                "Reportes Cancelados" -> {
                    android.util.Log.d("PDFGenerator", "📊 Obteniendo reportes cancelados (status=4)...")
                    reportRepository.getReportsByStatus(4)
                }
                else -> {
                    android.util.Log.w("PDFGenerator", "⚠️ Tipo de reporte desconocido: $reportType")
                    emptyList()
                }
            }

            android.util.Log.d("PDFGenerator", "✅ Reportes obtenidos: ${reports.size}")

            if (reports.isEmpty()) {
                android.util.Log.w("PDFGenerator", "⚠️ No se encontraron reportes para: $reportType")
                return Result.failure(Exception("No se encontraron reportes de tipo '$reportType'.\n\nVerifica que existan reportes en la base de datos."))
            }

            android.util.Log.d("PDFGenerator", "📝 Creando PDF con ${reports.size} reportes...")
            val pdfFile = createReportPDF(reportType, reports)
            android.util.Log.d("PDFGenerator", "✅ PDF creado exitosamente: ${pdfFile.name}")

            Result.success(pdfFile)
        } catch (e: Exception) {
            android.util.Log.e("PDFGenerator", "❌ Error generando reporte: ${e.message}", e)
            e.printStackTrace()
            Result.failure(Exception("Error al generar el reporte: ${e.message}"))
        }
    }

    /**
     * Crea el documento PDF con los datos de usuarios
     */
    private fun createUserPDF(reportTitle: String, profiles: List<Profile>): File {
        val document = PdfDocument()
        var pageNumber = 1
        var yPosition = margin + 60

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        drawHeader(canvas, reportTitle, pageNumber, profiles.size)
        yPosition += 40

        profiles.forEachIndexed { index, profile ->
            if (yPosition > pageHeight - 100) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                drawHeader(canvas, reportTitle, pageNumber, profiles.size)
                yPosition = margin + 100
            }

            drawUserInfo(canvas, profile, index + 1, yPosition)
            yPosition += 120
        }

        document.finishPage(page)

        val fileName = "Reporte_${reportTitle.replace(" ", "_")}_${getTimestamp()}.pdf"
        val file = createFile(fileName)

        FileOutputStream(file).use { outputStream ->
            document.writeTo(outputStream)
        }

        document.close()
        return file
    }

    /**
     * Crea el documento PDF con los datos de reportes de incidencias
     */
    private fun createReportPDF(reportTitle: String, reports: List<ReportData>): File {
        val document = PdfDocument()
        var pageNumber = 1
        var yPosition = margin + 60

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        drawHeader(canvas, reportTitle, pageNumber, reports.size)
        yPosition += 40

        reports.forEachIndexed { index, report ->
            if (yPosition > pageHeight - 150) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                drawHeader(canvas, reportTitle, pageNumber, reports.size)
                yPosition = margin + 100
            }

            drawReportInfo(canvas, report, index + 1, yPosition)
            yPosition += 150
        }

        document.finishPage(page)

        val fileName = "Reporte_${reportTitle.replace(" ", "_")}_${getTimestamp()}.pdf"
        val file = createFile(fileName)

        FileOutputStream(file).use { outputStream ->
            document.writeTo(outputStream)
        }

        document.close()
        return file
    }

    /**
     * Dibuja el encabezado de cada página
     */
    private fun drawHeader(
        canvas: android.graphics.Canvas,
        title: String,
        pageNumber: Int,
        totalItems: Int = 0
    ) {
        val paint = Paint().apply {
            textSize = 24f
            isFakeBoldText = true
            color = android.graphics.Color.rgb(33, 150, 243)
        }

        canvas.drawText("SafeZone - Reporte", margin.toFloat(), (margin + 30).toFloat(), paint)

        paint.textSize = 18f
        paint.isFakeBoldText = false
        canvas.drawText(title, margin.toFloat(), (margin + 55).toFloat(), paint)

        paint.textSize = 12f
        paint.color = android.graphics.Color.GRAY
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generado: $dateStr", margin.toFloat(), (margin + 75).toFloat(), paint)

        // Total de items
        if (totalItems > 0) {
            canvas.drawText(
                "Total: $totalItems",
                (margin + 200).toFloat(),
                (margin + 75).toFloat(),
                paint
            )
        }

        val pageText = "Página $pageNumber"
        val pageTextWidth = paint.measureText(pageText)
        canvas.drawText(
            pageText,
            (pageWidth - margin - pageTextWidth),
            (margin + 75).toFloat(),
            paint
        )

        paint.strokeWidth = 2f
        canvas.drawLine(
            margin.toFloat(),
            (margin + 85).toFloat(),
            (pageWidth - margin).toFloat(),
            (margin + 85).toFloat(),
            paint
        )
    }

    /**
     * Dibuja la información de un usuario
     */
    private fun drawUserInfo(
        canvas: android.graphics.Canvas,
        profile: Profile,
        index: Int,
        yPos: Int
    ) {
        val paint = Paint()
        var currentY = yPos

        if (index % 2 == 0) {
            paint.color = android.graphics.Color.rgb(245, 245, 245)
            paint.style = Paint.Style.FILL
            canvas.drawRect(
                margin.toFloat(),
                currentY.toFloat(),
                (pageWidth - margin).toFloat(),
                (currentY + 110).toFloat(),
                paint
            )
        }

        paint.color = android.graphics.Color.rgb(200, 200, 200)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(
            margin.toFloat(),
            currentY.toFloat(),
            (pageWidth - margin).toFloat(),
            (currentY + 110).toFloat(),
            paint
        )

        currentY += 20
        paint.style = Paint.Style.FILL

        paint.color = android.graphics.Color.BLACK
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText(
            "$index. ${profile.name}",
            (margin + 10).toFloat(),
            currentY.toFloat(),
            paint
        )

        currentY += 20
        paint.isFakeBoldText = false
        paint.textSize = 12f
        paint.color = android.graphics.Color.DKGRAY

        canvas.drawText(
            "Email: ${profile.email ?: "No disponible"}",
            (margin + 10).toFloat(),
            currentY.toFloat(),
            paint
        )

        currentY += 18

        canvas.drawText(
            "Teléfono: ${profile.phone ?: "No disponible"}",
            (margin + 10).toFloat(),
            currentY.toFloat(),
            paint
        )

        currentY += 18

        val roleName = profile.rol?.name ?: "No asignado"
        canvas.drawText(
            "Rol: $roleName",
            (margin + 10).toFloat(),
            currentY.toFloat(),
            paint
        )

        val statusText = when (profile.statusId) {
            1 -> "Activo"
            2 -> "Inactivo"
            3 -> "Baneado"
            else -> "Desconocido"
        }

        paint.color = when (profile.statusId) {
            1 -> android.graphics.Color.rgb(76, 175, 80)
            2 -> android.graphics.Color.rgb(158, 158, 158)
            3 -> android.graphics.Color.rgb(255, 152, 0)
            4 -> android.graphics.Color.rgb(244, 67, 54)
            else -> android.graphics.Color.BLACK
        }

        paint.isFakeBoldText = true
        val statusWidth = paint.measureText("Estado: $statusText")
        canvas.drawText(
            "Estado: $statusText",
            (pageWidth - margin - statusWidth - 10),
            currentY.toFloat(),
            paint
        )

        currentY += 18
        paint.color = android.graphics.Color.DKGRAY
        paint.isFakeBoldText = false

        if (!profile.address.isNullOrEmpty()) {
            val addressText = "Dirección: ${profile.address}"
            if (addressText.length > 60) {
                canvas.drawText(
                    addressText.substring(0, 57) + "...",
                    (margin + 10).toFloat(),
                    currentY.toFloat(),
                    paint
                )
            } else {
                canvas.drawText(
                    addressText,
                    (margin + 10).toFloat(),
                    currentY.toFloat(),
                    paint
                )
            }
        }
    }

    /**
     * Dibuja la información de un reporte de incidencia (ACTUALIZADO)
     */
    private fun drawReportInfo(
        canvas: android.graphics.Canvas,
        report: ReportData,
        index: Int,
        yPos: Int
    ) {
        val paint = Paint()
        var currentY = yPos

        // Fondo alternado
        if (index % 2 == 0) {
            paint.color = android.graphics.Color.rgb(245, 245, 245)
            paint.style = Paint.Style.FILL
            canvas.drawRect(
                margin.toFloat(),
                currentY.toFloat(),
                (pageWidth - margin).toFloat(),
                (currentY + 140).toFloat(),
                paint
            )
        }

        // Borde
        paint.color = android.graphics.Color.rgb(200, 200, 200)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(
            margin.toFloat(),
            currentY.toFloat(),
            (pageWidth - margin).toFloat(),
            (currentY + 140).toFloat(),
            paint
        )

        currentY += 20
        paint.style = Paint.Style.FILL

        // Título (ubicación del reporte)
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 14f
        paint.isFakeBoldText = true
        val titleText = report.reportLocation ?: report.description?.take(40) ?: "Sin título"
        canvas.drawText(
            "$index. $titleText",
            (margin + 10).toFloat(),
            currentY.toFloat(),
            paint
        )

        currentY += 20
        paint.isFakeBoldText = false
        paint.textSize = 11f
        paint.color = android.graphics.Color.DKGRAY

        // Usuario (nombre o ID)
        val userDisplay = if (report.isAnonymous) {
            "Usuario: Anónimo"
        } else {
            "Usuario: ${report.userName ?: report.userId.take(8)}"
        }
        canvas.drawText(
            userDisplay,
            (margin + 10).toFloat(),
            currentY.toFloat(),
            paint
        )

        currentY += 16

        // Categoría (Affair)
        canvas.drawText(
            "Categoría ID: ${report.idAffair ?: "N/A"}",
            (margin + 10).toFloat(),
            currentY.toFloat(),
            paint
        )

        currentY += 16

        // Fecha de creación
        val dateStr = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val cleanDate = report.createdAt.replace("+00", "").replace("T", " ").trim()
            val date = inputFormat.parse(cleanDate)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            report.createdAt.take(16).replace("T", " ")
        }
        canvas.drawText(
            "Fecha: $dateStr",
            (margin + 10).toFloat(),
            currentY.toFloat(),
            paint
        )

        currentY += 16

        // Última actualización
        if (!report.lastUpdate.isNullOrEmpty()) {
            val updateStr = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val cleanDate = report.lastUpdate.replace("+00", "").replace("T", " ").trim()
                val date = inputFormat.parse(cleanDate)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                report.lastUpdate.take(16).replace("T", " ")
            }
            canvas.drawText(
                "Actualizado: $updateStr",
                (margin + 10).toFloat(),
                currentY.toFloat(),
                paint
            )
            currentY += 16
        }

        // Descripción (truncada)
        if (!report.description.isNullOrEmpty()) {
            val descText = if (report.description.length > 60) {
                report.description.substring(0, 57) + "..."
            } else {
                report.description
            }
            canvas.drawText(
                "Desc: $descText",
                (margin + 10).toFloat(),
                currentY.toFloat(),
                paint
            )
        }

        // Estado (arriba a la derecha)
        currentY = yPos + 20
        val statusText = when (report.idReportingStatus) {
            1 -> "Pendiente"
            2 -> "En Proceso"
            3 -> "Completado"
            4 -> "Cancelado"
            5 -> "En revision"
            else -> "Estado ${report.idReportingStatus}"
        }

        paint.color = when (report.idReportingStatus) {
            1 -> android.graphics.Color.rgb(255, 152, 0)  // Naranja
            2 -> android.graphics.Color.rgb(33, 150, 243)  // Azul
            3 -> android.graphics.Color.rgb(76, 175, 80)   // Verde
            4 -> android.graphics.Color.rgb(244, 67, 54)   // Rojo
            else -> android.graphics.Color.BLACK
        }

        paint.isFakeBoldText = true
        paint.textSize = 12f
        val statusWidth = paint.measureText(statusText)
        canvas.drawText(
            statusText,
            (pageWidth - margin - statusWidth - 10),
            currentY.toFloat(),
            paint
        )

        // ID del reporte
        paint.color = android.graphics.Color.GRAY
        paint.textSize = 9f
        paint.isFakeBoldText = false
        currentY += 16
        val reportIdText = "ID: ${report.id.take(13)}..."
        val idWidth = paint.measureText(reportIdText)
        canvas.drawText(
            reportIdText,
            (pageWidth - margin - idWidth - 10),
            currentY.toFloat(),
            paint
        )

        // Indicador de imagen si existe
        if (!report.imageUrl.isNullOrEmpty()) {
            currentY += 14
            paint.color = android.graphics.Color.rgb(33, 150, 243)
            paint.textSize = 9f
            val imgText = "📷 Con imagen"
            val imgWidth = paint.measureText(imgText)
            canvas.drawText(
                imgText,
                (pageWidth - margin - imgWidth - 10),
                currentY.toFloat(),
                paint
            )
        }
    }

    /**
     * Crea el archivo en el directorio de descargas
     */
    private fun createFile(fileName: String): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appDir = File(downloadsDir, "SafeZone_Reports")

        if (!appDir.exists()) {
            appDir.mkdirs()
        }

        return File(appDir, fileName)
    }

    /**
     * Obtiene un timestamp formateado para nombres de archivo
     */
    private fun getTimestamp(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }
}