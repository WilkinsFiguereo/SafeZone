package com.wilkins.safezone.backend.network.User.Profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FollowViewModel(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _followState = MutableStateFlow<FollowState>(FollowState.Idle)
    val followState: StateFlow<FollowState> = _followState.asStateFlow()

    private val _followStats = MutableStateFlow<FollowStats?>(null)
    val followStats: StateFlow<FollowStats?> = _followStats.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    /**
     * Verifica si el usuario actual sigue a otro usuario
     */
    fun checkIfFollowing(currentUserId: String, targetUserId: String) {
        viewModelScope.launch {
            try {
                Log.d("FollowViewModel", "🔍 Verificando si $currentUserId sigue a $targetUserId")

                val follows = supabaseClient.postgrest
                    .from("user_follows")
                    .select {
                        filter {
                            eq("follower_id", currentUserId)
                            eq("following_id", targetUserId)
                        }
                    }
                    .decodeList<UserFollow>()

                _isFollowing.value = follows.isNotEmpty()
                Log.d("FollowViewModel", "✅ Resultado: ${if (follows.isNotEmpty()) "SIGUIENDO" else "NO SIGUIENDO"}")

            } catch (e: Exception) {
                Log.e("FollowViewModel", "❌ Error verificando seguimiento: ${e.message}", e)
                _isFollowing.value = false
            }
        }
    }

    /**
     * Seguir a un usuario
     */
    fun followUser(currentUserId: String, targetUserId: String) {
        viewModelScope.launch {
            try {
                _followState.value = FollowState.Loading
                Log.d("FollowViewModel", "👤 Intentando seguir a $targetUserId")

                // Insertar en la tabla user_follows
                supabaseClient.postgrest
                    .from("user_follows")
                    .insert(
                        FollowRequest(
                            follower_id = currentUserId,
                            following_id = targetUserId
                        )
                    )

                _isFollowing.value = true
                _followState.value = FollowState.Success("Ahora sigues a este usuario")
                Log.d("FollowViewModel", "✅ Seguimiento exitoso")

                // Actualizar estadísticas
                loadFollowStats(targetUserId)

            } catch (e: Exception) {
                Log.e("FollowViewModel", "❌ Error al seguir: ${e.message}", e)
                _followState.value = FollowState.Error(e.message ?: "Error al seguir al usuario")
            }
        }
    }

    /**
     * Dejar de seguir a un usuario
     */
    fun unfollowUser(currentUserId: String, targetUserId: String) {
        viewModelScope.launch {
            try {
                _followState.value = FollowState.Loading
                Log.d("FollowViewModel", "👤 Dejando de seguir a $targetUserId")

                // Eliminar de la tabla user_follows
                supabaseClient.postgrest
                    .from("user_follows")
                    .delete {
                        filter {
                            eq("follower_id", currentUserId)
                            eq("following_id", targetUserId)
                        }
                    }

                _isFollowing.value = false
                _followState.value = FollowState.Success("Dejaste de seguir a este usuario")
                Log.d("FollowViewModel", "✅ Dejó de seguir exitosamente")

                // Actualizar estadísticas
                loadFollowStats(targetUserId)

            } catch (e: Exception) {
                Log.e("FollowViewModel", "❌ Error al dejar de seguir: ${e.message}", e)
                _followState.value = FollowState.Error(e.message ?: "Error al dejar de seguir")
            }
        }
    }

    /**
     * Alternar seguimiento (seguir/dejar de seguir)
     */
    fun toggleFollow(currentUserId: String, targetUserId: String) {
        if (_isFollowing.value) {
            unfollowUser(currentUserId, targetUserId)
        } else {
            followUser(currentUserId, targetUserId)
        }
    }

    /**
     * Cargar estadísticas de seguimiento de un usuario
     */
    fun loadFollowStats(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("FollowViewModel", "📊 Cargando estadísticas de $userId")

                // Obtener conteo de seguidores
                val followersCount = supabaseClient.postgrest
                    .from("user_follows")
                    .select(columns = Columns.raw("count")) {
                        filter {
                            eq("following_id", userId)
                        }
                        count(Count.EXACT)

                    }
                    .countOrNull() ?: 0

                // Obtener conteo de seguidos
                val followingCount = supabaseClient.postgrest
                    .from("user_follows")
                    .select(columns = Columns.raw("count")) {
                        filter {
                            eq("follower_id", userId)
                        }
                        count(Count.EXACT)

                    }
                    .countOrNull() ?: 0

                val stats = FollowStats(
                    user_id = userId,
                    followers_count = followersCount.toInt(),
                    following_count = followingCount.toInt()
                )

                _followStats.value = stats
                Log.d("FollowViewModel", "✅ Estadísticas: ${stats.followers_count} seguidores, ${stats.following_count} seguidos")

            } catch (e: Exception) {
                Log.e("FollowViewModel", "❌ Error cargando estadísticas: ${e.message}", e)
            }
        }
    }

    /**
     * Obtener lista de seguidores
     */
    suspend fun getFollowers(userId: String): List<String> {
        return try {
            val follows = supabaseClient.postgrest
                .from("user_follows")
                .select {
                    filter {
                        eq("following_id", userId)
                    }
                }
                .decodeList<UserFollow>()

            follows.map { it.follower_id }
        } catch (e: Exception) {
            Log.e("FollowViewModel", "❌ Error obteniendo seguidores: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Obtener lista de seguidos
     */
    suspend fun getFollowing(userId: String): List<String> {
        return try {
            val follows = supabaseClient.postgrest
                .from("user_follows")
                .select {
                    filter {
                        eq("follower_id", userId)
                    }
                }
                .decodeList<UserFollow>()

            follows.map { it.following_id }
        } catch (e: Exception) {
            Log.e("FollowViewModel", "❌ Error obteniendo seguidos: ${e.message}", e)
            emptyList()
        }
    }

    fun resetState() {
        _followState.value = FollowState.Idle
    }
}

sealed class FollowState {
    object Idle : FollowState()
    object Loading : FollowState()
    data class Success(val message: String) : FollowState()
    data class Error(val message: String) : FollowState()
}