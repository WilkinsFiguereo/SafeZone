package com.wilkins.safezone.frontend.ui.screens.auth

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
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.wilkins.safezone.bridge.auth.RegisterBridge
import com.wilkins.safezone.ui.theme.NameApp
import com.wilkins.safezone.ui.theme.PrimaryColor

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
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // Tamaños más compactos
    val logoSize = getResponsiveSize(screenHeight, 70.dp, 80.dp, 90.dp)
    val iconSize = getResponsiveSize(screenHeight, 24.dp, 28.dp, 32.dp)
    val titleFontSize = getResponsiveFontSize(screenHeight, 20.sp, 24.sp, 28.sp)
    val horizontalPadding = getResponsivePadding(screenWidth, 16.dp, 20.dp, 24.dp)
    val verticalSpacing = getResponsiveSize(screenHeight, 6.dp, 8.dp, 12.dp)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header compacto
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = verticalSpacing * 1.5f)
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

                    Spacer(modifier = Modifier.height(verticalSpacing * 0.5f))

                    Text(
                        text = NameApp,
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = titleFontSize,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(verticalSpacing * 0.3f))

                    Text(
                        text = "Crear nueva cuenta",
                        color = Color.Gray,
                        fontSize = getResponsiveFontSize(screenHeight, 12.sp, 14.sp, 16.sp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(verticalSpacing * 1.5f))

                // Campos de registro compactos
                CompactTextFieldRegister(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Nombre completo",
                    label = "Nombre completo",
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.fillMaxWidth(),
                    screenHeight = screenHeight
                )

                Spacer(modifier = Modifier.height(verticalSpacing))

                CompactTextFieldRegister(
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

                CompactTextFieldRegister(
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

                CompactTextFieldRegister(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "********",
                    label = "Confirmar contraseña",
                    leadingIcon = Icons.Default.VerifiedUser,
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth(),
                    screenHeight = screenHeight
                )

                Spacer(modifier = Modifier.height(verticalSpacing * 1.5f))

                val context = LocalContext.current

                // Botón de registro compacto
                Button(
                    onClick = {
                        if (!isLoading) {
                            isLoading = true
                            scope.launch {
                                val result = RegisterBridge.handleRegister(context, name, email, password, confirmPassword)
                                result.onSuccess {
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
                        .height(getResponsiveSize(screenHeight, 40.dp, 44.dp, 48.dp)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(getResponsiveSize(screenHeight, 16.dp, 18.dp, 20.dp))
                        )
                    } else {
                        Icon(
                            Icons.Default.HowToReg,
                            null,
                            modifier = Modifier.size(getResponsiveSize(screenHeight, 16.dp, 18.dp, 20.dp))
                        )
                        Spacer(modifier = Modifier.width(verticalSpacing * 0.5f))
                        Text(
                            "Registrarse",
                            fontSize = getResponsiveFontSize(screenHeight, 13.sp, 15.sp, 17.sp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(verticalSpacing * 1.5f))

                // Separador compacto
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
                        fontSize = getResponsiveFontSize(screenHeight, 10.sp, 12.sp, 14.sp),
                        modifier = Modifier.padding(horizontal = verticalSpacing * 0.5f)
                    )
                    Divider(
                        color = Color.Gray.copy(alpha = 0.2f),
                        modifier = Modifier.weight(1f),
                        thickness = 1.dp
                    )
                }

                Spacer(modifier = Modifier.height(verticalSpacing * 1.5f))

                // Botones sociales compactos
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(verticalSpacing * 0.8f)
                ) {
                    OutlinedButton(
                        onClick = onGoogleSignIn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(getResponsiveSize(screenHeight, 36.dp, 40.dp, 44.dp)),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isLoading
                    ) {
                        Box(
                            modifier = Modifier.size(getResponsiveSize(screenHeight, 16.dp, 18.dp, 20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "G",
                                color = Color(0xFF4285F4),
                                fontWeight = FontWeight.Bold,
                                fontSize = getResponsiveFontSize(screenHeight, 12.sp, 14.sp, 16.sp)
                            )
                        }
                        Spacer(modifier = Modifier.width(verticalSpacing * 0.4f))
                        Text(
                            "Continuar con Google",
                            fontSize = getResponsiveFontSize(screenHeight, 12.sp, 14.sp, 16.sp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (!isLoading) {
                                onNavigateToLogin()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(getResponsiveSize(screenHeight, 36.dp, 40.dp, 44.dp)),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Login,
                            null,
                            modifier = Modifier.size(getResponsiveSize(screenHeight, 14.dp, 16.dp, 18.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "¿Ya tienes cuenta? Inicia sesión",
                            fontSize = getResponsiveFontSize(screenHeight, 12.sp, 14.sp, 16.sp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(verticalSpacing))

                // Términos y condiciones compactos
                TermsAndConditionsSection(
                    onTermsClicked = onTermsClicked,
                    onPrivacyPolicyClicked = onPrivacyPolicyClicked,
                    screenHeight = screenHeight
                )

                Spacer(modifier = Modifier.height(verticalSpacing))
            }
        }
    }
}

// Campo de texto compacto
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactTextFieldRegister(
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

    val fieldHeight = getResponsiveSize(screenHeight, 39.dp, 40.dp, 50.dp)
    val iconSize = getResponsiveSize(screenHeight, 16.dp, 18.dp, 20.dp)
    val fontSize = getResponsiveFontSize(screenHeight, 12.sp, 14.sp, 16.sp)
    val labelFontSize = getResponsiveFontSize(screenHeight, 12.sp, 14.sp, 16.sp)

    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            fontSize = labelFontSize,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .background(
                    color = Color(0xFFF8F9FA),
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                Icon(
                    leadingIcon,
                    null,
                    tint = PrimaryColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(8.dp))
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
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.size(iconSize)
                    ) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null,
                            tint = PrimaryColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(iconSize * 0.8f)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

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