package com.wilkins.safezone.backend.network.User.Profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ProfileUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val userProfile: AppUser? = null,
    val isOwnProfile: Boolean = false,
    val currentUserId: String? = null,
    val isUploadingPhoto: Boolean = false
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val supabaseClient = SupabaseService.getInstance()

    fun loadProfile(context: Context, userId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                // Obtener el ID del usuario actual
                val session = SessionManager.loadSession(context)
                val currentUserId = session?.user?.id

                // Cargar el perfil del usuario objetivo
                val userProfile = supabaseClient.postgrest
                    .from("profiles")
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }
                    .decodeSingleOrNull<AppUser>()

                if (userProfile != null) {
                    _uiState.value = ProfileUiState(
                        isLoading = false,
                        userProfile = userProfile,
                        isOwnProfile = currentUserId == userId,
                        currentUserId = currentUserId
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Usuario no encontrado"
                    )
                }

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error cargando perfil: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar el perfil: ${e.message}"
                )
            }
        }
    }

    fun updateProfile(
        context: Context,
        name: String,
        phone: String,
        pronouns: String,
        description: String,
        address: String,
        statusId: Int
    ) {
        viewModelScope.launch {
            try {
                val currentProfile = _uiState.value.userProfile ?: return@launch

                supabaseClient.postgrest
                    .from("profiles")
                    .update({
                        set("name", name)
                        set("phone", phone)
                        set("pronouns", pronouns)
                        set("description", description)
                        set("address", address)
                        set("status_id", statusId)
                    }) {
                        filter {
                            eq("id", currentProfile.id)
                        }
                    }

                // Recargar el perfil actualizado
                loadProfile(context, currentProfile.id)

                Log.i("ProfileViewModel", "✅ Perfil actualizado correctamente")

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "❌ Error actualizando perfil: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    error = "Error al actualizar el perfil: ${e.message}"
                )
            }
        }
    }

    fun uploadProfilePhoto(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            try {
                val currentProfile = _uiState.value.userProfile ?: return@launch

                _uiState.value = _uiState.value.copy(isUploadingPhoto = true)

                // Leer los bytes de la imagen
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(imageUri)
                val imageBytes = inputStream?.readBytes()
                inputStream?.close()

                if (imageBytes == null) {
                    _uiState.value = _uiState.value.copy(
                        isUploadingPhoto = false,
                        error = "No se pudo leer la imagen"
                    )
                    return@launch
                }

                // Generar nombre único para la imagen
                val fileName = "${currentProfile.id}/${UUID.randomUUID()}.jpg"

                // Subir imagen al bucket UserProfile
                val bucket = supabaseClient.storage.from("UserProfile")

                // Si ya tiene una foto anterior, eliminarla
                if (!currentProfile.photoProfile.isNullOrEmpty()) {
                    try {
                        bucket.delete(currentProfile.photoProfile)
                    } catch (e: Exception) {
                        Log.w("ProfileViewModel", "No se pudo eliminar la foto anterior: ${e.message}")
                    }
                }

                // Subir nueva foto
                bucket.upload(fileName, imageBytes)

                // Actualizar el campo photo_profile en la base de datos
                supabaseClient.postgrest
                    .from("profiles")
                    .update({
                        set("photo_profile", fileName)
                    }) {
                        filter {
                            eq("id", currentProfile.id)
                        }
                    }

                // Recargar perfil
                loadProfile(context, currentProfile.id)

                _uiState.value = _uiState.value.copy(isUploadingPhoto = false)

                Log.i("ProfileViewModel", "✅ Foto de perfil actualizada correctamente")

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "❌ Error subiendo foto: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isUploadingPhoto = false,
                    error = "Error al subir la foto: ${e.message}"
                )
            }
        }
    }

    fun getProfilePhotoUrl(photoProfile: String?): String? {
        if (photoProfile.isNullOrEmpty()) return null

        return try {
            val bucket = supabaseClient.storage.from("UserProfile")
            bucket.publicUrl(photoProfile)
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error obteniendo URL de foto: ${e.message}")
            null
        }
    }
}