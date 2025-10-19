package com.wilkins.alertaya.frontend.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.wilkins.alertaya.backend.network.login
import com.wilkins.alertaya.frontend.ui.screens.user.UserScreen

@Composable
fun LoginScreen(onLoginSuccess: (userId: String) -> Unit) {
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val loginResult = remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(text = "Correo electrónico")
        OutlinedTextField(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            placeholder = { Text("correo@ejemplo.com") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Contraseña")
        OutlinedTextField(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            placeholder = { Text("********") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                scope.launch {
                    val user = login(emailState.value, passwordState.value)
                    println("Usuario obtenido: $user")
                    println("Role del usuario: ${user?.role}")
                    println("Status del usuario: ${user?.status}")
                    println("id del usuario: ${user?.id}")


                    if (user != null) {
                        if (user.role == "user") {
                            loginResult.value = "Login exitoso"
                            onLoginSuccess(user.id) // solo si es usuario
                        } else {
                            loginResult.value = "Acceso denegado: no eres usuario"
                        }
                    } else {
                        loginResult.value = "Credenciales incorrectas"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }


        loginResult.value?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                it,
                color = if (it.contains("exitoso")) Color.Green else Color.Red
            )
        }
    }
}
