//package com.wilkins.safezone.bridge.Crud
//
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.wilkins.safezone.backend.network.Admin.CrudUser.CreateUserRequest
//import com.wilkins.safezone.backend.network.Admin.CrudUser.CreateUserResponse
//import com.wilkins.safezone.backend.network.Admin.CrudUser.CreateUserService
//import com.wilkins.safezone.backend.network.Admin.CrudUser.CrudUser
//import com.wilkins.safezone.backend.network.Admin.CrudUser.Role
//import kotlinx.coroutines.launch
//
///**
// * Estados para la UI
// */
//sealed class CreateUserUiState {
//    object Idle : CreateUserUiState()
//    object Loading : CreateUserUiState()
//    data class Success(val response: CreateUserResponse) : CreateUserUiState()
//    data class Error(val message: String) : CreateUserUiState()
//}
//
//class CreateUserViewModel : ViewModel() {
//
//    private val createUserService = CreateUserService()
//    private val crudUser = CrudUser()
//
//    var roles by mutableStateOf<List<Role>>(emptyList())
//        private set
//
//    var loadingRoles by mutableStateOf(false)
//        private set
//
//    var uiState by mutableStateOf<CreateUserUiState>(CreateUserUiState.Idle)
//        private set
//
//    // Mantener compatibilidad con código anterior
//    val loading: Boolean
//        get() = uiState is CreateUserUiState.Loading
//
//    val createUserResponse: CreateUserResponse?
//        get() = when (val state = uiState) {
//            is CreateUserUiState.Success -> state.response
//            is CreateUserUiState.Error -> CreateUserResponse(
//                success = false,
//                message = state.message
//            )
//            else -> null
//        }
//
//    /**
//     * Cargar roles disponibles al inicializar
//     */
//    init {
//        loadRoles()
//    }
//
//    /**
//     * Cargar roles disponibles
//     */
//    fun loadRoles() {
//        viewModelScope.launch {
//            loadingRoles = true
//            try {
//                roles = crudUser.getAllRoles()
//                println("✅ Roles cargados: ${roles.size} roles")
//            } catch (e: Exception) {
//                println("❌ Error cargando roles: ${e.message}")
//                e.printStackTrace()
//            } finally {
//                loadingRoles = false
//            }
//        }
//    }
//
//    /**
//     * Crear un nuevo usuario verificado
//     */
//    fun createUser(request: CreateUserRequest, onResult: (CreateUserResponse) -> Unit) {
//        viewModelScope.launch {
//            uiState = CreateUserUiState.Loading
//
//            try {
//                println("🔄 Creando usuario: ${request.email}")
//
//                // Llamar directamente a la Edge Function
//                // La Edge Function se encarga de:
//                // 1. Verificar si el email existe
//                // 2. Verificar permisos de admin
//                // 3. Crear el usuario
//                val response = createUserService.createVerifiedUser(request)
//
//                if (response.success) {
//                    println("✅ Usuario creado exitosamente: ${response.userId}")
//                    uiState = CreateUserUiState.Success(response)
//                } else {
//                    println("❌ Error en la creación: ${response.message}")
//                    uiState = CreateUserUiState.Error(response.message)
//                }
//
//                onResult(response)
//
//            } catch (e: Exception) {
//                println("❌ Excepción al crear usuario: ${e.message}")
//                e.printStackTrace()
//
//                val response = CreateUserResponse(
//                    success = false,
//                    message = "Error: ${e.message ?: "Error desconocido"}"
//                )
//                uiState = CreateUserUiState.Error(response.message)
//                onResult(response)
//            }
//        }
//    }
//
//    /**
//     * Validar datos antes de crear usuario
//     */
//    fun validateUserData(request: CreateUserRequest): String? {
//        return when {
//            request.name.isBlank() -> "El nombre es requerido"
//            request.name.length < 3 -> "El nombre debe tener al menos 3 caracteres"
//            request.email.isBlank() -> "El email es requerido"
//            !android.util.Patterns.EMAIL_ADDRESS.matcher(request.email).matches() ->
//                "Email inválido"
//            request.password.isBlank() -> "La contraseña es requerida"
//            request.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
//            request.phone?.isNotBlank() == true && request.phone.length < 10 ->
//                "Teléfono inválido"
//            else -> null // Sin errores
//        }
//    }
//
//    /**
//     * Crear usuario con validación previa
//     */
//    fun createUserWithValidation(
//        request: CreateUserRequest,
//        onResult: (CreateUserResponse) -> Unit
//    ) {
//        // Validar datos primero
//        val validationError = validateUserData(request)
//        if (validationError != null) {
//            val response = CreateUserResponse(
//                success = false,
//                message = validationError
//            )
//            uiState = CreateUserUiState.Error(validationError)
//            onResult(response)
//            return
//        }
//
//        // Si pasa la validación, crear el usuario
//        createUser(request, onResult)
//    }
//
//    /**
//     * Limpiar el estado y respuesta
//     */
//    fun clearResponse() {
//        uiState = CreateUserUiState.Idle
//    }
//
//    /**
//     * Reintentar operación
//     */
//    fun retry(request: CreateUserRequest, onResult: (CreateUserResponse) -> Unit) {
//        clearResponse()
//        createUser(request, onResult)
//    }
//
//    /**
//     * Limpiar recursos al destruir el ViewModel
//     */
//    override fun onCleared() {
//        super.onCleared()
//        createUserService.close()
//        println("🧹 CreateUserViewModel limpiado")
//    }
//}