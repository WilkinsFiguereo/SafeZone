package com.wilkins.alertaya.frontend.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.alertaya.backend.network.registerUser
import com.wilkins.alertaya.frontend.ui.theme.PrimaryColor
import com.wilkins.alertaya.frontend.ui.theme.SecondaryColor
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen() {
    // Estados para los inputs
    val nameState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val confirmPasswordState = remember { mutableStateOf("") }

    // Coroutine scope
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PrimaryColor)
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .background(
                        color = SecondaryColor,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Logo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(PrimaryColor, shape = RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Logo",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Título
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AlertaYa",
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nombre completo
                Text("Nombre completo", fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = nameState.value,
                    onValueChange = { nameState.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ingresa tu nombre completo") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Correo electrónico
                Text("Correo electrónico", fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = emailState.value,
                    onValueChange = { emailState.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("correo@ejemplo.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Contraseña
                Text("Contraseña", fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = passwordState.value,
                    onValueChange = { passwordState.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("********") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Confirmar contraseña
                Text("Confirmar contraseña", fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = confirmPasswordState.value,
                    onValueChange = { confirmPasswordState.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("********") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Botón de registro
                Button(
                    onClick = {
                        if (passwordState.value != confirmPasswordState.value) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Las contraseñas no coinciden")
                            }
                            return@Button
                        }

                        scope.launch {
                            try {
                                registerUser(
                                    name = nameState.value,
                                    email = emailState.value,
                                    password = passwordState.value
                                )
                                snackbarHostState.showSnackbar("Usuario registrado correctamente")
                                // Limpiar inputs si quieres
                                nameState.value = ""
                                emailState.value = ""
                                passwordState.value = ""
                                confirmPasswordState.value = ""
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error al registrar: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrarse", color = Color.White)
                }
            }
        }
    }
}
