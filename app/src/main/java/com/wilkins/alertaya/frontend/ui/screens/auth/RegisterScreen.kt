package com.wilkins.alertaya.frontend.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.wilkins.alertaya.frontend.ui.theme.PrimaryColor
import com.wilkins.alertaya.frontend.ui.theme.SecondaryColor
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun RegisterScreen() {
    // Estados para los inputs
    val nameState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val confirmPasswordState = remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryColor),
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
            // ... Logo y "AlertaYa" igual que antes ...

            // Nombre completo
            Text(text = "Nombre completo", color = Color.Black, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = nameState.value,
                onValueChange = { nameState.value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ingresa tu nombre completo") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Correo
            Text(text = "Correo electrónico", color = Color.Black, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("correo@ejemplo.com") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Contraseña
            Text(text = "Contraseña", color = Color.Black, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("********") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Confirmar contraseña
            Text(text = "Confirmar contraseña", color = Color.Black, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = confirmPasswordState.value,
                onValueChange = { confirmPasswordState.value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("********") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botón
            Button(
                onClick = {
                    // Aquí llamaremos a la función para registrar usuario en Supabase
                    if (passwordState.value == confirmPasswordState.value) {
                        registerUser(
                            name = nameState.value,
                            email = emailState.value,
                            password = passwordState.value
                        )
                    } else {
                        // mostrar mensaje de error: contraseñas no coinciden
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Registrarse", color = Color.White)
            }
        }
    }
}
