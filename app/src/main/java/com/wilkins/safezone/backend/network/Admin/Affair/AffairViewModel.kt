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

class AffairViewModel(
    private val repository: CategoryRepository = CategoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AffairUiState())
    val uiState: StateFlow<AffairUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        loadAffairs()
        loadCategories()
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
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAffairCategories()
                .onSuccess { categories ->
                    _uiState.value = _uiState.value.copy(categories = categories)
                }
                .onFailure { e ->
                    // Mantenemos el error si no hay affairs cargados
                    if (_uiState.value.affairs.isEmpty()) {
                        _uiState.value = _uiState.value.copy(error = e.message)
                    }
                }
        }
    }

    fun createAffair(type: String, categoryId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.createAffair(type, categoryId)
                .onSuccess {
                    loadAffairs()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
        }
    }

    fun updateAffair(id: Int, type: String, categoryId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.updateAffair(id, type, categoryId)
                .onSuccess {
                    loadAffairs()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
        }
    }

    fun deleteAffair(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.deleteAffair(id)
                .onSuccess {
                    loadAffairs()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
        }
    }

    // Función helper para obtener el nombre de una categoría por su ID
    fun getCategoryName(categoryId: Int): String {
        return _uiState.value.categories.find { it.id == categoryId }?.name ?: "Sin categoría"
    }
}