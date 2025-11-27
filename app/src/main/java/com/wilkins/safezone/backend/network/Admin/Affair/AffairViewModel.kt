package com.wilkins.safezone.backend.network.Admin.Affair

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AffairUiState(
    val affairs: List<Affair> = emptyList(),
    val categories: List<AffairCategory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AffairViewModel : ViewModel() {
    private val repository = CategoryRepository()

    private val _uiState = MutableStateFlow(AffairUiState())
    val uiState: StateFlow<AffairUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadAffairs()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getAffairCategories()
                .onSuccess { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al cargar categorías"
                    )
                }
        }
    }

    fun loadAffairs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getAffairs()
                .onSuccess { affairs ->
                    _uiState.value = _uiState.value.copy(
                        affairs = affairs,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al cargar affairs"
                    )
                }
        }
    }

    fun createAffair(type: String, categoryId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.createAffair(type, categoryId)
                .onSuccess {
                    loadAffairs() // Recargar la lista
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al crear affair"
                    )
                }
        }
    }

    fun updateAffair(id: Int, type: String, categoryId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.updateAffair(id, type, categoryId)
                .onSuccess {
                    loadAffairs() // Recargar la lista
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al actualizar affair"
                    )
                }
        }
    }

    fun deleteAffair(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.deleteAffair(id)
                .onSuccess {
                    loadAffairs() // Recargar la lista
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al eliminar affair"
                    )
                }
        }
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }
}