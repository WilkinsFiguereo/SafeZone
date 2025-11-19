package com.wilkins.alertaya.frontend.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.alertaya.ui.theme.PrimaryColor
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.wilkins.alertaya.frontend.ui.network.AppUser
import com.wilkins.alertaya.bridge.auth.LoginBridge
import android.content.Context
import androidx.compose.ui.platform.LocalContext

@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: (user: AppUser) -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    onTermsClicked: (url: String) -> Unit = {},
    onPrivacyPolicyClicked: (url: String) -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // Tamaños responsivos basados en la pantalla
    val logoSize = getResponsiveSize(screenHeight, 100.dp, 120.dp, 140.dp)
    val iconSize = getResponsiveSize(screenHeight, 36.dp, 48.dp, 56.dp)
    val titleFontSize = getResponsiveFontSize(screenHeight, 28.sp, 36.sp, 40.sp)
    val horizontalPadding = getResponsivePadding(screenWidth, 24.dp, 32.dp, 48.dp)
    val verticalSpacing = getResponsiveSize(screenHeight, 16.dp, 24.dp, 32.dp)

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
                // Espacio flexible en la parte superior para centrar el contenido
                if (screenHeight > 600.dp) {
                    Spacer(modifier = Modifier.weight(0.2f))
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
                        text = "Iniciar sesión en tu cuenta",
                        color = Color.Gray,
                        fontSize = getResponsiveFontSize(screenHeight, 14.sp, 16.sp, 18.sp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(verticalSpacing))

                // Campos de texto responsivos
                CustomTextFieldLogin(
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

                CustomTextFieldLogin(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "********",
                    label = "Contraseña",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth(),
                    screenHeight = screenHeight
                )

                Spacer(modifier = Modifier.height(verticalSpacing * 0.6f))

                TextButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = PrimaryColor,
                        fontSize = getResponsiveFontSize(screenHeight, 12.sp, 14.sp, 15.sp),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(verticalSpacing))

                val context = LocalContext.current
                // Botón principal responsivo
                Button(
                    onClick = {
                        scope.launch {
                            if (email.isNotEmpty() && password.isNotEmpty()) {
                                val result = LoginBridge.performLogin(context,email, password)
                                result.onSuccess { user ->
                                    onLoginSuccess(user)
                                }.onFailure { e ->
                                    snackbarHostState.showSnackbar(e.message ?: "Error en login")
                                }
                            } else {
                                snackbarHostState.showSnackbar("Credenciales vacías")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(getResponsiveSize(screenHeight, 48.dp, 58.dp, 64.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Login,
                        contentDescription = null,
                        modifier = Modifier.size(getResponsiveSize(screenHeight, 20.dp, 24.dp, 28.dp))
                    )
                    Spacer(modifier = Modifier.width(verticalSpacing * 0.6f))
                    Text(
                        "Iniciar sesión",
                        fontSize = getResponsiveFontSize(screenHeight, 15.sp, 18.sp, 20.sp),
                        fontWeight = FontWeight.SemiBold
                    )
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

                // Botones sociales responsivos - en columna para pantallas pequeñas
                if (screenWidth < 600.dp) {
                    // Diseño vertical para pantallas pequeñas
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(verticalSpacing * 0.8f)
                    ) {
                        OutlinedButton(
                            onClick = onGoogleSignIn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(getResponsiveSize(screenHeight, 48.dp, 56.dp, 60.dp)),
                            shape = RoundedCornerShape(16.dp)
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
                                "Google",
                                fontSize = getResponsiveFontSize(screenHeight, 14.sp, 16.sp, 18.sp),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                navController.navigate("register") {
                                    launchSingleTop = true
                                }
                            },
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

                    }
                } else {
                    // Diseño horizontal para tablets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(verticalSpacing * 0.8f)
                    ) {
                        OutlinedButton(
                            onClick = onGoogleSignIn,
                            modifier = Modifier
                                .weight(1f)
                                .height(getResponsiveSize(screenHeight, 48.dp, 56.dp, 60.dp)),
                            shape = RoundedCornerShape(16.dp)
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
                                "Google",
                                fontSize = getResponsiveFontSize(screenHeight, 14.sp, 16.sp, 18.sp),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        OutlinedButton(
                            onClick = onNavigateToRegister,
                            modifier = Modifier
                                .weight(1f)
                                .height(getResponsiveSize(screenHeight, 48.dp, 56.dp, 60.dp)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                null,
                                modifier = Modifier.size(getResponsiveSize(screenHeight, 18.dp, 22.dp, 24.dp))
                            )
                            Spacer(modifier = Modifier.width(verticalSpacing * 0.5f))
                            Text(
                                "Crear cuenta",
                                fontSize = getResponsiveFontSize(screenHeight, 14.sp, 16.sp, 18.sp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(verticalSpacing))

                // Términos y condiciones responsivos
                TermsAndConditionsSection(
                    onTermsClicked = onTermsClicked,
                    onPrivacyPolicyClicked = onPrivacyPolicyClicked,
                    screenHeight = screenHeight
                )

                // Espacio flexible en la parte inferior
                if (screenHeight > 600.dp) {
                    Spacer(modifier = Modifier.weight(0.2f))
                } else {
                    Spacer(modifier = Modifier.height(verticalSpacing))
                }
            }
        }
    }
}

// Funciones auxiliares para responsive design
@Composable
fun getResponsiveSize(screenHeight: Dp, small: Dp, medium: Dp, large: Dp): Dp {
    return when {
        screenHeight < 600.dp -> small
        screenHeight < 800.dp -> medium
        else -> large
    }
}

@Composable
fun getResponsiveFontSize(screenHeight: Dp, small: androidx.compose.ui.unit.TextUnit, medium: androidx.compose.ui.unit.TextUnit, large: androidx.compose.ui.unit.TextUnit): androidx.compose.ui.unit.TextUnit {
    return when {
        screenHeight < 600.dp -> small
        screenHeight < 800.dp -> medium
        else -> large
    }
}

@Composable
fun getResponsivePadding(screenWidth: Dp, small: Dp, medium: Dp, large: Dp): Dp {
    return when {
        screenWidth < 360.dp -> small
        screenWidth < 600.dp -> medium
        else -> large
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextFieldLogin(
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


@Preview(showBackground = true, showSystemUi = true, widthDp = 411, heightDp = 820)
@Composable
fun PreviewLoginScreenMedium() {
    val navController = rememberNavController()
    LoginScreen(
        navController = navController,
        onLoginSuccess = { },
        onNavigateToRegister = { },
        onGoogleSignIn = { },
        onTermsClicked = { println("Términos: $it") },
        onPrivacyPolicyClicked = { println("Política: $it") }
    )
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 768, heightDp = 1024)
@Composable
fun PreviewLoginScreenTablet() {
    val navController = rememberNavController()
    LoginScreen(
        navController = navController,
        onLoginSuccess = { },
        onNavigateToRegister = { },
        onGoogleSignIn = { },
        onTermsClicked = { println("Términos: $it") },
        onPrivacyPolicyClicked = { println("Política: $it") }
    )
}
