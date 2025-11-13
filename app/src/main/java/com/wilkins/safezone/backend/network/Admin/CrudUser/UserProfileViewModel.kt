package com.wilkins.safezone.backend.network.Admin.CrudUser

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {
    private val crudUser = CrudUser()

    var profile by mutableStateOf<Profile?>(null)
        private set

    var roles by mutableStateOf<List<Role>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var loadingRoles by mutableStateOf(false)
        private set

    /**
     * Cargar el perfil de un usuario específico
     */
    fun loadProfile(userId: String) {
        viewModelScope.launch {
            loading = true
            try {
                profile = crudUser.getProfileById(userId)
                println("✅ Perfil cargado: ${profile?.name}")
                println("   - ID: ${profile?.id}")
                println("   - Email: ${profile?.email}")
                println("   - Teléfono: ${profile?.phone}")
                println("   - Rol: ${profile?.rol?.name} (ID: ${profile?.roleId})")
                println("   - Estado: ${profile?.statusId}")
            } catch (e: Exception) {
                println("❌ Error cargando perfil: ${e.message}")
                e.printStackTrace()
            } finally {
                loading = false
            }
        }
    }

    /**
     * Cargar todos los roles disponibles desde la base de datos
     */
    fun loadRoles() {
        viewModelScope.launch {
            loadingRoles = true
            try {
                roles = crudUser.getAllRoles()
                println("✅ Roles cargados: ${roles.size} roles encontrados")
                roles.forEach { role ->
                    println("   - ${role.name} (ID: ${role.id})")
                }
            } catch (e: Exception) {
                println("❌ Error cargando roles: ${e.message}")
                e.printStackTrace()
            } finally {
                loadingRoles = false
            }
        }
    }

    /**
     * Recargar el perfil (útil después de actualizar)
     */
    fun refreshProfile(userId: String) {
        loadProfile(userId)
    }

    /**
     * Seleccionar un usuario específico para ver/editar su perfil
     */
    fun selectUser(userId: String) {
        viewModelScope.launch {
            loading = true
            try {
                profile = crudUser.getProfileById(userId)
                println("✅ Usuario seleccionado: ${profile?.name}")
                println("   - UUID: ${profile?.id}")
                println("   - Rol: ${profile?.rol?.name} (ID: ${profile?.roleId})")
            } catch (e: Exception) {
                println("❌ Error seleccionando usuario: ${e.message}")
                e.printStackTrace()
            } finally {
                loading = false
            }
        }
    }
}