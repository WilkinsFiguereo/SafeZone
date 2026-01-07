package com.wilkins.safezone.backend.network.User.Interaction


import android.util.Log
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InteractionsRepository {

    private val supabase = SupabaseService.getInstance()
    private val TAG = "InteractionsRepository"

    /**
     * Agregar un like a un elemento
     */
    suspend fun addLike(
        targetId: String,
        entityType: EntityType
    ): Result<InteractionDto> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(Exception("Usuario no autenticado"))

            val interaction = InteractionDto(
                userId = userId,
                targetId = targetId,
                interactionType = InteractionType.LIKE,
                entityType = entityType
            )

            val result = supabase.from("interactions")
                .insert(interaction) {
                    select(Columns.ALL)
                }
                .decodeSingle<InteractionDto>()

            Log.d(TAG, "✅ Like agregado: $targetId (${entityType.name})")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error agregando like", e)
            Result.failure(e)
        }
    }

    /**
     * Remover un like de un elemento
     */
    suspend fun removeLike(
        targetId: String,
        entityType: EntityType
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(Exception("Usuario no autenticado"))

            supabase.from("interactions")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("target_id", targetId)
                        eq("entity_type", entityType.name.lowercase())
                        eq("interaction_type", InteractionType.LIKE.name.lowercase())
                    }
                }

            Log.d(TAG, "✅ Like removido: $targetId (${entityType.name})")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error removiendo like", e)
            Result.failure(e)
        }
    }

    /**
     * Verificar si el usuario actual dio like a un elemento
     */
    suspend fun hasUserLiked(
        targetId: String,
        entityType: EntityType
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return@withContext Result.success(false)

            val interactions = supabase.from("interactions")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("target_id", targetId)
                        eq("entity_type", entityType.name.lowercase())
                        eq("interaction_type", InteractionType.LIKE.name.lowercase())
                    }
                }
                .decodeList<InteractionDto>()

            Result.success(interactions.isNotEmpty())
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verificando like", e)
            Result.success(false)
        }
    }

    /**
     * Obtener el conteo de likes de un elemento
     */
    suspend fun getLikesCount(
        targetId: String,
        entityType: EntityType
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val interactions = supabase.from("interactions")
                .select {
                    filter {
                        eq("target_id", targetId)
                        eq("entity_type", entityType.name.lowercase())
                        eq("interaction_type", InteractionType.LIKE.name.lowercase())
                    }
                }
                .decodeList<InteractionDto>()

            val count = interactions.size
            Log.d(TAG, "📊 Conteo de likes para $targetId: $count")
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo conteo de likes", e)
            Result.success(0)
        }
    }

    /**
     * Toggle like (agregar si no existe, remover si existe)
     */
    suspend fun toggleLike(
        targetId: String,
        entityType: EntityType
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val hasLiked = hasUserLiked(targetId, entityType).getOrNull() ?: false

            if (hasLiked) {
                removeLike(targetId, entityType)
                Result.success(false) // Ahora NO tiene like
            } else {
                addLike(targetId, entityType)
                Result.success(true) // Ahora SÍ tiene like
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en toggle like", e)
            Result.failure(e)
        }
    }

    /**
     * Obtener todos los likes de un usuario
     */
    suspend fun getUserLikes(userId: String): Result<List<InteractionDto>> =
        withContext(Dispatchers.IO) {
            try {
                val interactions = supabase.from("interactions")
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("interaction_type", InteractionType.LIKE.name.lowercase())
                        }
                    }
                    .decodeList<InteractionDto>()

                Result.success(interactions)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error obteniendo likes del usuario", e)
                Result.failure(e)
            }
        }

    /**
     * Obtener estadísticas de interacciones de un elemento
     */
    suspend fun getInteractionStats(
        targetId: String,
        entityType: EntityType
    ): Result<Map<InteractionType, Int>> = withContext(Dispatchers.IO) {
        try {
            val interactions = supabase.from("interactions")
                .select {
                    filter {
                        eq("target_id", targetId)
                        eq("entity_type", entityType.name.lowercase())
                    }
                }
                .decodeList<InteractionDto>()

            val stats = interactions.groupBy { it.interactionType }
                .mapValues { it.value.size }

            Log.d(TAG, "📊 Estadísticas para $targetId: $stats")
            Result.success(stats)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo estadísticas", e)
            Result.failure(e)
        }
    }
}