// ReportCommentService.kt
package com.wilkins.safezone.backend.network.User.ReportComment

import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Objects.isNull
import java.util.UUID

// DTO simple para los comentarios básicos
@Serializable
private data class CommentBasic(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val message: String,
    @SerialName("report_id")
    val reportId: String? = null,
    @SerialName("news_id")
    val newsId: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("parent_comment_id")
    val parentCommentId: String? = null,
    @SerialName("author_name")
    val authorName: String? = null
)

// DTO ligero solo para nombre y foto de perfil
@Serializable
private data class UserProfileLight(
    val id: String,
    val name: String? = null,
    @SerialName("photo_profile")
    val photoProfile: String? = null
)

class ReportCommentService {

    private val supabase = SupabaseService.getInstance()

    suspend fun getCommentsForReport(reportId: String): List<ReportCommentDto> {
        return withContext(Dispatchers.IO) {
            try {
                println("🔄 [ReportCommentService] Cargando comentarios para reporte: $reportId")

                // 1. Obtener los comentarios básicos
                val basicComments = supabase
                    .from("comments")
                    .select(
                        columns = Columns.list(
                            "id",
                            "user_id",
                            "message",
                            "report_id",
                            "news_id",
                            "created_at",
                            "parent_comment_id",
                            "author_name"
                        )
                    ) {
                        filter {
                            eq("report_id", reportId)
                            isNull("news_id")
                        }
                        order("created_at", Order.ASCENDING)
                    }
                    .decodeList<CommentBasic>()

                println("✅ [ReportCommentService] Comentarios básicos obtenidos: ${basicComments.size}")

                if (basicComments.isEmpty()) {
                    return@withContext emptyList()
                }

                // 2. Obtener IDs únicos de usuarios
                val userIds = basicComments.map { it.userId }.distinct()
                println("👥 [ReportCommentService] Usuarios únicos: ${userIds.size}")

                // 3. Obtener perfiles de usuarios en una sola consulta
                val userProfiles = try {
                    supabase
                        .from("profiles")
                        .select(
                            columns = Columns.list(
                                "id",
                                "name",
                                "photo_profile"
                            )
                        ) {
                            filter {
                                isIn("id", userIds)
                            }
                        }
                        .decodeList<UserProfileLight>()
                        .associateBy { it.id }
                } catch (e: Exception) {
                    println("⚠️ [ReportCommentService] Error obteniendo perfiles: ${e.message}")
                    e.printStackTrace()
                    emptyMap()
                }

                println("✅ [ReportCommentService] Perfiles obtenidos: ${userProfiles.size}")

                // 4. Combinar comentarios con perfiles y procesar URLs
                val processedComments = basicComments.map { comment ->
                    val userProfile = userProfiles[comment.userId]

                    val photoUrl = userProfile?.photoProfile?.let { photoPath ->
                        try {
                            if (photoPath.isNotBlank()) {
                                supabase.storage
                                    .from("UserProfile")
                                    .publicUrl(photoPath)
                            } else null
                        } catch (e: Exception) {
                            println("⚠️ [ReportCommentService] Error obteniendo URL de foto: ${e.message}")
                            null
                        }
                    }

                    ReportCommentDto(
                        id = comment.id,
                        userId = comment.userId,
                        message = comment.message,
                        reportId = comment.reportId,
                        newsId = comment.newsId,
                        createdAt = comment.createdAt,
                        parentCommentId = comment.parentCommentId,
                        authorName = comment.authorName,
                        user_name = userProfile?.name,
                        userPhotoUrl = photoUrl
                    )
                }

                // Debug: Mostrar información de cada comentario
                processedComments.forEachIndexed { index, comment ->
                    println("   [${index + 1}] ${comment.getDisplayName()}: ${comment.message}")
                    println("       userId: ${comment.userId}")
                    println("       user_name: ${comment.user_name}")
                    println("       authorName: ${comment.authorName}")
                    println("       photo: ${comment.userPhotoUrl}")
                    println("       created: ${comment.createdAt}")
                }

                processedComments

            } catch (e: Exception) {
                println("❌ [ReportCommentService] Error en getCommentsForReport: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun createCommentForReport(
        reportId: String,
        userId: String,
        message: String
    ): Result<ReportCommentDto> {
        return withContext(Dispatchers.IO) {
            try {
                println("📤 [ReportCommentService] Creando comentario para reporte: $reportId")
                println("🔹 Usuario: $userId")
                println("🔹 Mensaje: $message")

                val commentId = UUID.randomUUID().toString()
                val newComment = mapOf(
                    "id" to commentId,
                    "user_id" to userId,
                    "message" to message.trim(),
                    "report_id" to reportId,
                    "news_id" to null,
                    "parent_comment_id" to null
                )

                println("📝 [ReportCommentService] Datos del comentario: $newComment")

                // Insertar el comentario
                supabase
                    .from("comments")
                    .insert(newComment)

                println("✅ [ReportCommentService] Comentario insertado con ID: $commentId")

                // Obtener el comentario recién creado
                val basicComment = supabase
                    .from("comments")
                    .select(
                        columns = Columns.list(
                            "id",
                            "user_id",
                            "message",
                            "report_id",
                            "news_id",
                            "created_at",
                            "parent_comment_id",
                            "author_name"
                        )
                    ) {
                        filter {
                            eq("id", commentId)
                        }
                    }
                    .decodeSingle<CommentBasic>()

                println("✅ [ReportCommentService] Comentario recuperado")

                // Obtener perfil del usuario
                println("🔍 Buscando perfil del usuario: $userId")
                val userProfile = try {
                    supabase
                        .from("profiles")
                        .select {
                            filter {
                                eq("id", userId)
                            }
                        }
                        .decodeSingleOrNull<UserProfileLight>()
                } catch (e: Exception) {
                    println("⚠️ [ReportCommentService] Error obteniendo perfil: ${e.message}")
                    e.printStackTrace()
                    null
                }

                if (userProfile != null) {
                    println("✅ Usuario encontrado: ${userProfile.name}")
                    println("📸 Foto: ${userProfile.photoProfile ?: "sin foto"}")
                } else {
                    println("⚠️ Usuario NO encontrado")
                }

                // Procesar URL de foto
                val photoUrl = userProfile?.photoProfile?.let { photoPath ->
                    try {
                        if (photoPath.isNotBlank()) {
                            val url = supabase.storage
                                .from("UserProfile")
                                .publicUrl(photoPath)
                            println("🔗 URL foto generada: $url")
                            url
                        } else null
                    } catch (e: Exception) {
                        println("⚠️ [ReportCommentService] Error obteniendo URL de foto: ${e.message}")
                        null
                    }
                }

                val resultDto = ReportCommentDto(
                    id = basicComment.id,
                    userId = basicComment.userId,
                    message = basicComment.message,
                    reportId = basicComment.reportId,
                    newsId = basicComment.newsId,
                    createdAt = basicComment.createdAt,
                    parentCommentId = basicComment.parentCommentId,
                    authorName = basicComment.authorName,
                    user_name = userProfile?.name,
                    userPhotoUrl = photoUrl
                )

                println("✅ [ReportCommentService] Comentario final creado: ${resultDto.getDisplayName()}")
                println("💬 ${resultDto.getDisplayName()}: ${resultDto.message} | Foto: ${resultDto.userPhotoUrl != null}")

                Result.success(resultDto)

            } catch (e: Exception) {
                println("❌ [ReportCommentService] Error creando comentario: ${e.message}")
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
}