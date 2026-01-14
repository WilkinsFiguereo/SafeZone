package com.wilkins.safezone.frontend.ui.user.Screens.News.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class ShareUtils(private val context: Context) {

    /**
     * Compartir noticia con opciones de múltiples plataformas
     */
    fun shareNews(
        title: String,
        description: String,
        imageUrl: String,
        videoUrl: String? = null
    ) {
        // Si hay video, priorizar compartir video
        if (!videoUrl.isNullOrBlank()) {
            showShareOptionsWithMedia(
                title = title,
                description = description,
                mediaUrl = videoUrl,
                isVideo = true
            )
        } else if (imageUrl.isNotBlank()) {
            // Si hay imagen, compartir imagen
            showShareOptionsWithMedia(
                title = title,
                description = description,
                mediaUrl = imageUrl,
                isVideo = false
            )
        } else {
            // Solo texto
            shareTextOnly(title, description)
        }
    }

    /**
     * Mostrar opciones de compartir con media (imagen o video)
     */
    private fun showShareOptionsWithMedia(
        title: String,
        description: String,
        mediaUrl: String,
        isVideo: Boolean
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(
                context,
                "Preparando contenido para compartir...",
                Toast.LENGTH_SHORT
            ).show()

            try {
                val mediaFile = withContext(Dispatchers.IO) {
                    downloadMedia(mediaUrl, isVideo)
                }

                if (mediaFile != null && mediaFile.exists()) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        mediaFile
                    )

                    // Crear intent de compartir
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = if (isVideo) "video/*" else "image/*"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, title)
                        putExtra(Intent.EXTRA_TEXT, "$title\n\n$description")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    // Crear chooser con opciones personalizadas
                    val chooser = Intent.createChooser(shareIntent, "Compartir noticia")

                    // Añadir intents específicos para Instagram y Facebook
                    val extraIntents = mutableListOf<Intent>()

                    // Instagram Stories
                    if (isAppInstalled("com.instagram.android")) {
                        val instagramIntent = createInstagramStoryIntent(uri, isVideo)
                        if (instagramIntent != null) {
                            extraIntents.add(instagramIntent)
                        }
                    }

                    // Facebook
                    if (isAppInstalled("com.facebook.katana")) {
                        val facebookIntent = createFacebookIntent(uri, title, description, isVideo)
                        if (facebookIntent != null) {
                            extraIntents.add(facebookIntent)
                        }
                    }

                    if (extraIntents.isNotEmpty()) {
                        chooser.putExtra(
                            Intent.EXTRA_INITIAL_INTENTS,
                            extraIntents.toTypedArray()
                        )
                    }

                    context.startActivity(chooser)
                } else {
                    // Si falla la descarga, compartir solo texto
                    Toast.makeText(
                        context,
                        "No se pudo descargar el contenido, compartiendo texto...",
                        Toast.LENGTH_SHORT
                    ).show()
                    shareTextOnly(title, description)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error al compartir: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                // Fallback a compartir solo texto
                shareTextOnly(title, description)
            }
        }
    }

    /**
     * Descargar media desde URL
     */
    private suspend fun downloadMedia(url: String, isVideo: Boolean): File? {
        return withContext(Dispatchers.IO) {
            try {
                val extension = if (isVideo) "mp4" else "jpg"
                val fileName = "share_${System.currentTimeMillis()}.$extension"
                val file = File(context.cacheDir, fileName)

                URL(url).openStream().use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }

                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Crear intent para Instagram Stories
     */
    private fun createInstagramStoryIntent(uri: Uri, isVideo: Boolean): Intent? {
        return try {
            Intent("com.instagram.share.ADD_TO_STORY").apply {
                setPackage("com.instagram.android")
                type = if (isVideo) "video/*" else "image/*"
                putExtra("interactive_asset_uri", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Crear intent para Facebook
     */
    private fun createFacebookIntent(
        uri: Uri,
        title: String,
        description: String,
        isVideo: Boolean
    ): Intent? {
        return try {
            Intent(Intent.ACTION_SEND).apply {
                setPackage("com.facebook.katana")
                type = if (isVideo) "video/*" else "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "$title\n\n$description")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Compartir solo texto (fallback)
     */
    private fun shareTextOnly(title: String, description: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$title\n\n$description\n\n#SafeZone #Noticias")
        }

        val chooser = Intent.createChooser(shareIntent, "Compartir noticia")
        context.startActivity(chooser)
    }

    /**
     * Verificar si una app está instalada
     */
    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Compartir directamente a WhatsApp
     */
    fun shareToWhatsApp(title: String, description: String, imageUrl: String?) {
        if (!isAppInstalled("com.whatsapp")) {
            Toast.makeText(context, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                if (!imageUrl.isNullOrBlank()) {
                    val imageFile = withContext(Dispatchers.IO) {
                        downloadMedia(imageUrl, false)
                    }

                    if (imageFile != null && imageFile.exists()) {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            imageFile
                        )

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            setPackage("com.whatsapp")
                            type = "image/*"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_TEXT, "$title\n\n$description")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        context.startActivity(intent)
                    } else {
                        shareToWhatsAppTextOnly(title, description)
                    }
                } else {
                    shareToWhatsAppTextOnly(title, description)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error al compartir en WhatsApp",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun shareToWhatsAppTextOnly(title: String, description: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            setPackage("com.whatsapp")
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$title\n\n$description")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No se pudo abrir WhatsApp",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Compartir directamente a Telegram
     */
    fun shareToTelegram(title: String, description: String, imageUrl: String?) {
        if (!isAppInstalled("org.telegram.messenger")) {
            Toast.makeText(context, "Telegram no está instalado", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                if (!imageUrl.isNullOrBlank()) {
                    val imageFile = withContext(Dispatchers.IO) {
                        downloadMedia(imageUrl, false)
                    }

                    if (imageFile != null && imageFile.exists()) {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            imageFile
                        )

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            setPackage("org.telegram.messenger")
                            type = "image/*"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_TEXT, "$title\n\n$description")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        context.startActivity(intent)
                    } else {
                        shareToTelegramTextOnly(title, description)
                    }
                } else {
                    shareToTelegramTextOnly(title, description)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error al compartir en Telegram",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun shareToTelegramTextOnly(title: String, description: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            setPackage("org.telegram.messenger")
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$title\n\n$description")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No se pudo abrir Telegram",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}