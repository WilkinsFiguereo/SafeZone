package com.wilkins.alertaya.frontend.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.alertaya.ui.theme.PrimaryColor
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.wilkins.alertaya.backend.network.login
import com.wilkins.alertaya.backend.network.HomeUserScreen
import com.wilkins.alertaya.backend.network.HomeAdminScreen

@Composable
fun LoginScreen(
    onLoginSuccess: (userId: String) -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    onTermsClicked: (url: String) -> Unit = {},
    onPrivacyPolicyClicked: (url: String) -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo y título compactos
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(PrimaryColor, shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "AlertaYa",
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )

                Text(
                    text = "Iniciar sesión",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Campos de texto
                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "correo@ejemplo.com",
                    label = "Correo electrónico",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "********",
                    label = "Contraseña",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = PrimaryColor.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón principal
                Button(
                    onClick = {
                        scope.launch {
                            if (email.isNotEmpty() && password.isNotEmpty()) {
                                // Llamamos a la función login
                                val user = login(email, password)

                                if (user != null) {
                                    // Mostramos snackbar de éxito
                                    snackbarHostState.showSnackbar("Login exitoso")

                                    // Redirigimos según rol
                                    when (user.role_id) {
                                        1 -> HomeUserScreen(user)   // Usuario normal
                                        2 -> HomeAdminScreen(user)  // Admin
                                        else -> snackbarHostState.showSnackbar("Rol desconocido")
                                    }

                                    // Llamada opcional a tu callback
                                    onLoginSuccess(user.id)
                                } else {
                                    snackbarHostState.showSnackbar("Credenciales incorrectas o usuario no encontrado")
                                }
                            } else {
                                snackbarHostState.showSnackbar("Por favor ingresa email y contraseña")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciar sesión", fontSize = 15.sp)
                }


                Spacer(modifier = Modifier.height(12.dp))

                // Separador
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.weight(1f))
                    Text("o", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
                    Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón Google
                OutlinedButton(
                    onClick = onGoogleSignIn,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF757575)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google", fontSize = 14.sp, color = Color(0xFF757575))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón registro
                OutlinedButton(
                    onClick = onNavigateToRegister,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear cuenta", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Términos compactos
                TermsAndConditionsSection(
                    onTermsClicked = onTermsClicked,
                    onPrivacyPolicyClicked = onPrivacyPolicyClicked
                )
            }
        }
    }
}

@Composable
fun TermsAndConditionsSection(
    onTermsClicked: (String) -> Unit = {},
    onPrivacyPolicyClicked: (String) -> Unit = {}
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier =  Modifier.padding(bottom = 10.dp)) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = PrimaryColor, textDecoration = TextDecoration.Underline, fontSize = 10.sp)) {
                        append("Términos de Uso")
                    }
                },
                modifier = Modifier.clickable { onTermsClicked("http://localhost:5000/static/Document/terminos_y_condiciones_gesde.pdf") }
            )

            Text(" | ", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp))

            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = PrimaryColor, textDecoration = TextDecoration.Underline, fontSize = 10.sp)) {
                        append("Política de Privacidad")
                    }
                },
                modifier = Modifier.clickable { onPrivacyPolicyClicked("http://localhost:5000/static/Document/politica_privacidad_gesde_3_paginas.pdf") }
            )
        }

        Text(
            text = "© 2026 AlertaYA. Todos los derechos reservados. La información proporcionada será tratada de manera confidencial según nuestra Política de Privacidad..",
            color = Color.Gray,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )

        Spacer(modifier = Modifier.height(4.dp))


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(text = label, color = PrimaryColor, fontWeight = FontWeight.Medium, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(4.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(leadingIcon, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = PrimaryColor,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                if (isPassword) {
                    IconButton(onClick = { passwordVisible = !passwordVisible }, modifier = Modifier.size(20.dp)) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null,
                            tint = PrimaryColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            Divider(
                color = PrimaryColor.copy(alpha = 0.6f),
                thickness = 1.dp,
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen(
        onLoginSuccess = { },
        onNavigateToRegister = { },
        onGoogleSignIn = { },
        onTermsClicked = { println("Términos: $it") },
        onPrivacyPolicyClicked = { println("Política: $it") }
    )
}