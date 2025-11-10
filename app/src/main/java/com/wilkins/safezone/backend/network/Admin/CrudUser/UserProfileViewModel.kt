package com.wilkins.safezone.backend.network.Admin.CrudUser

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {

    private val profileService = ProfileService()

    // ✅ ID del usuario actualmente seleccionado
    var selectedUserId by mutableStateOf<String?>(null)
        private set

    // ✅ Datos del perfil cargado desde Supabase
    var profile by mutableStateOf<Profile?>(null)
        private set

    // ✅ Estado de carga
    var loading by mutableStateOf(false)
        private set

    /**
     * ✅ Guarda el UUID del usuario que se seleccionó
     */
    fun selectUser(uuid: String) {
        selectedUserId = uuid
    }

    /**
     * ✅ Carga la información del perfil por ID
     */
    fun loadProfile(userId: String) {
        viewModelScope.launch {
            try {
                loading = true
                profile = profileService.getProfileById(userId)
            } catch (e: Exception) {
                println("❌ Error cargando perfil: ${e.message}")
            } finally {
                loading = false
            }
        }
    }

    /**
     * ✅ Carga el perfil seleccionado (si existe)
     */
    fun loadSelectedProfile() {
        selectedUserId?.let { loadProfile(it) }
    }
}
