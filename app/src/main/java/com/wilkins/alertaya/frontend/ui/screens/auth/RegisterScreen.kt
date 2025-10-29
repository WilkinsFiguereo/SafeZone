package com.wilkins.alertaya.frontend.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.alertaya.frontend.ui.theme.PrimaryColor
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.wilkins.alertaya.bridge.auth.RegisterBridge


@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    onTermsClicked: (url: String) -> Unit = {},
    onPrivacyPolicyClicked: (url: String) -> Unit = {},
    onNavigateToVerification: (email: String, password: String) -> Unit

) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // Estado para controlar la carga

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // Tamaños responsivos
    val logoSize = getResponsiveSize(screenHeight, 100.dp, 120.dp, 140.dp)
    val iconSize = getResponsiveSize(screenHeight, 36.dp, 48.dp, 56.dp)
    val titleFontSize = getResponsiveFontSize(screenHeight, 28.sp, 36.sp, 40.sp)
    val horizontalPadding = getResponsivePadding(screenWidth, 20.dp, 32.dp, 48.dp)
    val verticalSpacing = getResponsiveSize(screenHeight, 12.dp, 20.dp, 28.dp)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Espacio flexible superior
                if (screenHeight > 700.dp) {
                    Spacer(modifier = Modifier.weight(0.1f))
                }

                // Header responsivo
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = verticalSpacing)
                ) {
                    Box(
                        modifier = Modifier
                            .size(logoSize)
                            .background(
                                PrimaryColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(logoSize * 0.25f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Logo",
                            tint = PrimaryColor,
                            modifier = Modifier.size(iconSize)
                        )
                    }

                    Spacer(modifier = Modifier.height(verticalSpacing * 0.8f))

                    Text(
                        text = "AlertaYa",
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = titleFontSize,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(verticalSpacing * 0.4f))

                    Text(
                        text = "Crear nueva cuenta",
                        color = Color.Gray,
                        fontSize = getResponsiveFontSize(screenHeight, 14.sp, 16.sp, 18.sp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(verticalSpacing))

                // Campos de registro responsivos
                CustomTextFieldRegister(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Nombre completo",
                    label = "Nombre completo",
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.fillMaxWidth(),
                    screenHeight = screenHeight
                )

                Spacer(modifier = Modifier.height(verticalSpacing))

                CustomTextFieldRegister(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "correo@ejemplo.com",
                    label = "Correo electrónico",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier.fillMaxWidth(),
                    screenHeight = screenHeight
                )

                Spacer(modifier = Modifier.height(verticalSpacing))

                CustomTextFieldRegister(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "********",
                    label = "Contraseña",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth(),
                    screenHeight = screenHeight
                )

                Spacer(modifier = Modifier.height(verticalSpacing))

                CustomTextFieldRegister(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "********",
                    label = "Confirmar contraseña",
                    leadingIcon = Icons.Default.VerifiedUser,
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth(),
                    screenHeight = screenHeight
                )

                Spacer(modifier = Modifier.height(verticalSpacing))

                val context = LocalContext.current

                // Botón de registro responsivo con indicador de carga
                Button(
                    onClick = {
                        if (!isLoading) { // Solo permitir click si no está cargando
                            isLoading = true
                            scope.launch {
                                val result = RegisterBridge.handleRegister(context, name, email, password, confirmPassword)
                                result.onSuccess {
                                    // Limpiar campos
                                    val savedEmail = email
                                    val savedPassword = password
                                    name = ""
                                    email = ""
                                    password = ""
                                    confirmPassword = ""
                                    isLoading = false
                                    onNavigateToVerification(savedEmail, savedPassword)
                                }.onFailure { e ->
                                    snackbarHostState.showSnackbar("❌ ${e.message}")
                                    isLoading = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor,
                        disabledContainerColor = PrimaryColor.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(getResponsiveSize(screenHeight, 48.dp, 58.dp, 64.dp)),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading // Deshabilitar botón mientras carga
                ) {
                    if (isLoading) {
                        // Mostrar círculo de carga
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(getResponsiveSize(screenHeight, 20.dp, 24.dp, 28.dp))
                        )
                    } else {
                        // Mostrar icono y texto normal
                        Icon(
                            Icons.Default.HowToReg,
                            null,
                            modifier = Modifier.size(getResponsiveSize(screenHeight, 20.dp, 24.dp, 28.dp))
                        )
                        Spacer(modifier = Modifier.width(verticalSpacing * 0.6f))
                        Text(
                            "Registrarse",
                            fontSize = getResponsiveFontSize(screenHeight, 15.sp, 18.sp, 20.sp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(verticalSpacing))

                // Separador responsivo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        color = Color.Gray.copy(alpha = 0.2f),
                        modifier = Modifier.weight(1f),
                        thickness = 1.dp
                    )
                    Text(
                        "o continúa con",
                        color = Color.Gray,
                        fontSize = getResponsiveFontSize(screenHeight, 12.sp, 14.sp, 16.sp),
                        modifier = Modifier.padding(horizontal = verticalSpacing * 0.6f)
                    )
                    Divider(
                        color = Color.Gray.copy(alpha = 0.2f),
                        modifier = Modifier.weight(1f),
                        thickness = 1.dp
                    )
                }

                Spacer(modifier = Modifier.height(verticalSpacing))

                // Botones sociales responsivos
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(verticalSpacing * 0.8f)
                ) {
                    OutlinedButton(
                        onClick = onGoogleSignIn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(getResponsiveSize(screenHeight, 48.dp, 56.dp, 60.dp)),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading // Deshabilitar mientras carga el registro
                    ) {
                        Box(
                            modifier = Modifier.size(getResponsiveSize(screenHeight, 20.dp, 24.dp, 26.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "G",
                                color = Color(0xFF4285F4),
                                fontWeight = FontWeight.Bold,
                                fontSize = getResponsiveFontSize(screenHeight, 14.sp, 16.sp, 18.sp)
                            )
                        }
                        Spacer(modifier = Modifier.width(verticalSpacing * 0.5f))
                        Text(
                            "Continuar con Google",
                            fontSize = getResponsiveFontSize(screenHeight, 14.sp, 16.sp, 18.sp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (!isLoading) { // Solo permitir si no está cargando
                                onNavigateToLogin()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(getResponsiveSize(screenHeight, 48.dp, 56.dp, 60.dp)),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading // Deshabilitar mientras carga el registro
                    ) {
                        Icon(
                            Icons.Default.Login,
                            null,
                            modifier = Modifier.size(getResponsiveSize(screenHeight, 18.dp, 22.dp, 24.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "¿Ya tienes cuenta? Inicia sesión",
                            fontSize = getResponsiveFontSize(screenHeight, 14.sp, 16.sp, 18.sp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                }

                Spacer(modifier = Modifier.height(verticalSpacing))

                // Términos y condiciones responsivos
                TermsAndConditionsSection(
                    onTermsClicked = onTermsClicked,
                    onPrivacyPolicyClicked = onPrivacyPolicyClicked,
                    screenHeight = screenHeight
                )

                // Espacio flexible inferior
                if (screenHeight > 700.dp) {
                    Spacer(modifier = Modifier.weight(0.1f))
                } else {
                    Spacer(modifier = Modifier.height(verticalSpacing))
                }
            }
        }
    }
}

// El resto del código permanece igual...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextFieldRegister(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    screenHeight: Dp
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val fieldHeight = getResponsiveSize(screenHeight, 48.dp, 56.dp, 64.dp)
    val iconSize = getResponsiveSize(screenHeight, 20.dp, 24.dp, 28.dp)
    val fontSize = getResponsiveFontSize(screenHeight, 14.sp, 16.sp, 18.sp)
    val labelFontSize = getResponsiveFontSize(screenHeight, 14.sp, 16.sp, 18.sp)

    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            fontSize = labelFontSize,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .background(
                    color = Color(0xFFF8F9FA),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    leadingIcon,
                    null,
                    tint = PrimaryColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(12.dp))
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(
                            placeholder,
                            color = Color.Gray.copy(alpha = 0.6f),
                            fontSize = fontSize
                        )
                    },
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
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = fontSize)
                )
                if (isPassword) {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible }
                    ) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null,
                            tint = PrimaryColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 360, heightDp = 640)
@Composable
fun PreviewRegisterScreenSmall() {
    RegisterScreen(
        onNavigateToLogin = {},
        onGoogleSignIn = {},
        onTermsClicked = { println("Términos: $it") },
        onPrivacyPolicyClicked = { println("Política: $it") },
        onNavigateToVerification = { _, _ -> }
    )
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 411, heightDp = 820)
@Composable
fun PreviewRegisterScreenMedium() {
    RegisterScreen(
        onNavigateToLogin = {},
        onGoogleSignIn = {},
        onTermsClicked = { println("Términos: $it") },
        onPrivacyPolicyClicked = { println("Política: $it") },
        onNavigateToVerification = { _, _ -> }
    )
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 768, heightDp = 1024)
@Composable
fun PreviewRegisterScreenTablet() {
    RegisterScreen(
        onNavigateToLogin = {},
        onGoogleSignIn = {},
        onTermsClicked = { println("Términos: $it") },
        onPrivacyPolicyClicked = { println("Política: $it") },
        onNavigateToVerification = { _, _ -> }
    )
}