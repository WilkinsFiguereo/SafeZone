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
import com.wilkins.alertaya.backend.network.registerUser
import com.wilkins.alertaya.frontend.ui.theme.PrimaryColor
import com.wilkins.alertaya.frontend.ui.theme.SecondaryColor
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    onTermsClicked: (url: String) -> Unit = {},
    onPrivacyPolicyClicked: (url: String) -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

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
                    .fillMaxWidth(0.85f) // 85% del ancho - balanceado
                    .background(
                        color = SecondaryColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo balanceado
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(PrimaryColor, shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
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
                    text = "Crear cuenta",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Campos de registro
                CustomTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Nombre completo",
                    label = "Nombre completo",
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "********",
                    label = "Confirmar contraseña",
                    leadingIcon = Icons.Default.VerifiedUser,
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Botón de registro
                Button(
                    onClick = {
                        if (password != confirmPassword) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Las contraseñas no coinciden")
                            }
                            return@Button
                        }

                        scope.launch {
                            try {
                                registerUser(name = name, email = email, password = password)
                                snackbarHostState.showSnackbar("Usuario registrado correctamente")
                                name = ""
                                email = ""
                                password = ""
                                confirmPassword = ""
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error al registrar: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.HowToReg, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrarse", fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Separador
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.weight(1f))
                    Text("o", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
                    Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Google
                OutlinedButton(
                    onClick = onGoogleSignIn,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF757575)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continuar con Google", fontSize = 14.sp, color = Color(0xFF757575))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón login
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Default.Login, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("¿Ya tienes cuenta? Inicia sesión", fontSize = 13.sp, color = PrimaryColor)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Términos y condiciones
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
        Text(
            text = "© 2026 AlertaYa. Todos los derechos reservados. La información proporcionada será tratada de manera confidencial según nuestra Política de Privacidad.",
            color = Color.Gray,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 5.dp)
        ) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = PrimaryColor, textDecoration = TextDecoration.Underline, fontSize = 10.sp)) {
                        append("Términos de Uso")
                    }
                },
                modifier = Modifier.clickable { onTermsClicked("http://localhost:5000/static/Document/terminos_y_condiciones_gesde.pdf") }
            )

            Text(" | ", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp))

            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = PrimaryColor, textDecoration = TextDecoration.Underline, fontSize = 10.sp)) {
                        append("Política de Privacidad")
                    }
                },
                modifier = Modifier.clickable { onPrivacyPolicyClicked("http://localhost:5000/static/Document/politica_privacidad_gesde_3_paginas.pdf") }
            )
        }
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
                    placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.7f), fontSize = 13.sp) },
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
fun PreviewRegisterScreen() {
    RegisterScreen(
        onNavigateToLogin = {},
        onGoogleSignIn = {},
        onTermsClicked = { println("Términos: $it") },
        onPrivacyPolicyClicked = { println("Política: $it") }
    )
}