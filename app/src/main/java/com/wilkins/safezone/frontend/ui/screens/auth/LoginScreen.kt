//package com.wilkins.safezone.frontend.ui.screens.auth
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.platform.LocalConfiguration
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.wilkins.safezone.ui.theme.PrimaryColor
//import kotlinx.coroutines.launch
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.navigation.NavController
//import androidx.navigation.compose.rememberNavController
//import com.wilkins.safezone.backend.network.AppUser
//import com.wilkins.safezone.bridge.auth.LoginBridge
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.ui.platform.LocalContext
//import com.wilkins.safezone.ui.theme.NameApp
//
//@Composable
//fun LoginScreen(
//    navController: NavController,
//    onLoginSuccess: (user: AppUser) -> Unit = {},
//    onNavigateToRegister: () -> Unit = {},
//    onGoogleSignIn: () -> Unit = {},
//    onTermsClicked: (url: String) -> Unit = {},
//    onPrivacyPolicyClicked: (url: String) -> Unit = {}
//) {
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var isLoading by remember { mutableStateOf(false) } // Estado para controlar la carga
//    val scope = rememberCoroutineScope()
//    val snackbarHostState = remember { SnackbarHostState() }
//
//    val configuration = LocalConfiguration.current
//    val screenHeight = configuration.screenHeightDp.dp
//    val screenWidth = configuration.screenWidthDp.dp
//
//    // Tamaños más compactos
//    val logoSize = getResponsiveSize(screenHeight, 80.dp, 90.dp, 100.dp)
//    val iconSize = getResponsiveSize(screenHeight, 24.dp, 28.dp, 32.dp)
//    val titleFontSize = getResponsiveFontSize(screenHeight, 20.sp, 24.sp, 28.sp)
//    val horizontalPadding = getResponsivePadding(screenWidth, 16.dp, 24.dp, 32.dp)
//    val verticalSpacing = getResponsiveSize(screenHeight, 8.dp, 12.dp, 16.dp)
//
//    // Si está cargando, mostrar overlay de carga
//    if (isLoading) {
//        LoadingOverlay()
//    }
//
//    Scaffold(
//        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.White)
//                .padding(padding)
//                .verticalScroll(rememberScrollState()),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = horizontalPadding),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // Header compacto
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier.padding(vertical = verticalSpacing)
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(logoSize)
//                            .background(
//                                PrimaryColor.copy(alpha = 0.1f),
//                                shape = RoundedCornerShape(logoSize * 0.25f)
//                            ),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Security,
//                            contentDescription = "Logo",
//                            tint = PrimaryColor,
//                            modifier = Modifier.size(iconSize)
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.height(verticalSpacing * 0.5f))
//
//                    Text(
//                        text = NameApp,
//                        color = PrimaryColor,
//                        fontWeight = FontWeight.Bold,
//                        fontSize = titleFontSize,
//                        letterSpacing = 0.5.sp
//                    )
//
//                    Spacer(modifier = Modifier.height(verticalSpacing * 0.3f))
//
//                    Text(
//                        text = "Iniciar sesión en tu cuenta",
//                        color = Color.Gray,
//                        fontSize = getResponsiveFontSize(screenHeight, 12.sp, 14.sp, 16.sp),
//                        textAlign = TextAlign.Center
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(verticalSpacing * 1.5f))
//
//                // Campos de texto compactos
//                CompactTextField(
//                    value = email,
//                    onValueChange = { email = it },
//                    placeholder = "correo@ejemplo.com",
//                    label = "Correo electrónico",
//                    leadingIcon = Icons.Default.Email,
//                    keyboardType = KeyboardType.Email,
//                    modifier = Modifier.fillMaxWidth(),
//                    screenHeight = screenHeight,
//                    enabled = !isLoading // Deshabilitar campo cuando está cargando
//                )
//
//                Spacer(modifier = Modifier.height(verticalSpacing))
//
//                CompactTextField(
//                    value = password,
//                    onValueChange = { password = it },
//                    placeholder = "********",
//                    label = "Contraseña",
//                    leadingIcon = Icons.Default.Lock,
//                    isPassword = true,
//                    modifier = Modifier.fillMaxWidth(),
//                    screenHeight = screenHeight,
//                    enabled = !isLoading // Deshabilitar campo cuando está cargando
//                )
//
//                Spacer(modifier = Modifier.height(verticalSpacing * 0.5f))
//
//                TextButton(
//                    onClick = { /* TODO */ },
//                    modifier = Modifier.align(Alignment.End),
//                    enabled = !isLoading // Deshabilitar cuando está cargando
//                ) {
//                    Text(
//                        text = "¿Olvidaste tu contraseña?",
//                        color = if (isLoading) Color.Gray else PrimaryColor,
//                        fontSize = getResponsiveFontSize(screenHeight, 10.sp, 12.sp, 14.sp),
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(verticalSpacing * 1.5f))
//
//                val context = LocalContext.current
//                // Botón principal compacto
//                // En el Button del LoginScreen, cambia esto:
//
//                Button(
//                    onClick = {
//                        scope.launch {
//                            if (email.isNotEmpty() && password.isNotEmpty()) {
//                                isLoading = true
//                                try {
//                                    val result = LoginBridge.performLogin(context, email, password)
//                                    result.onSuccess { user ->
//                                        // ✅ NO desactivar isLoading aquí, dejar que la navegación se complete
//                                        // El overlay de carga desaparecerá automáticamente al cambiar de pantalla
//                                        onLoginSuccess(user)
//                                    }.onFailure { e ->
//                                        // ❌ Solo desactivar si hay error
//                                        isLoading = false
//                                        snackbarHostState.showSnackbar(e.message ?: "Error en login")
//                                    }
//                                } catch (e: Exception) {
//                                    // ❌ Solo desactivar si hay excepción
//                                    isLoading = false
//                                    snackbarHostState.showSnackbar("Error inesperado: ${e.message}")
//                                }
//                                // ⚠️ REMOVIDO: No poner isLoading = false aquí en finally
//                            } else {
//                                snackbarHostState.showSnackbar("Credenciales vacías")
//                            }
//                        }
//                    },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = PrimaryColor
//                    ),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(getResponsiveSize(screenHeight, 40.dp, 48.dp, 52.dp)),
//                    shape = RoundedCornerShape(12.dp),
//                    enabled = !isLoading
//                ) {
//                    if (isLoading) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(getResponsiveSize(screenHeight, 16.dp, 18.dp, 20.dp)),
//                            color = Color.White,
//                            strokeWidth = 2.dp
//                        )
//                    } else {
//                        Icon(
//                            Icons.Default.Login,
//                            contentDescription = null,
//                            modifier = Modifier.size(getResponsiveSize(screenHeight, 16.dp, 18.dp, 20.dp))
//                        )
//                    }
//                    Spacer(modifier = Modifier.width(verticalSpacing * 0.5f))
//                    Text(
//                        if (isLoading) "Iniciando sesión..." else "Iniciar sesión",
//                        fontSize = getResponsiveFontSize(screenHeight, 13.sp, 15.sp, 17.sp),
//                        fontWeight = FontWeight.SemiBold
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(verticalSpacing * 1.5f))
//
//                // Separador compacto
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Divider(
//                        color = Color.Gray.copy(alpha = 0.2f),
//                        modifier = Modifier.weight(1f),
//                        thickness = 1.dp
//                    )
//                    Text(
//                        "o continúa con",
//                        color = Color.Gray,
//                        fontSize = getResponsiveFontSize(screenHeight, 10.sp, 12.sp, 14.sp),
//                        modifier = Modifier.padding(horizontal = verticalSpacing * 0.5f)
//                    )
//                    Divider(
//                        color = Color.Gray.copy(alpha = 0.2f),
//                        modifier = Modifier.weight(1f),
//                        thickness = 1.dp
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(verticalSpacing * 1.5f))
//
//                // Botones sociales compactos
//                Column(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalArrangement = Arrangement.spacedBy(verticalSpacing * 0.8f)
//                ) {
//                    OutlinedButton(
//                        onClick = onGoogleSignIn,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(getResponsiveSize(screenHeight, 36.dp, 40.dp, 44.dp)),
//                        shape = RoundedCornerShape(10.dp),
//                        enabled = !isLoading // Deshabilitar cuando está cargando
//                    ) {
//                        Box(
//                            modifier = Modifier.size(getResponsiveSize(screenHeight, 16.dp, 18.dp, 20.dp)),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                "G",
//                                color = Color(0xFF4285F4),
//                                fontWeight = FontWeight.Bold,
//                                fontSize = getResponsiveFontSize(screenHeight, 12.sp, 14.sp, 16.sp)
//                            )
//                        }
//                        Spacer(modifier = Modifier.width(verticalSpacing * 0.4f))
//                        Text(
//                            "Google",
//                            fontSize = getResponsiveFontSize(screenHeight, 12.sp, 14.sp, 16.sp),
//                            fontWeight = FontWeight.Medium
//                        )
//                    }
//
//                    OutlinedButton(
//                        onClick = onNavigateToRegister,
//                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor),
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(getResponsiveSize(screenHeight, 36.dp, 40.dp, 44.dp)),
//                        shape = RoundedCornerShape(10.dp),
//                        enabled = !isLoading // Deshabilitar cuando está cargando
//                    ) {
//                        Icon(
//                            Icons.Default.PersonAdd,
//                            null,
//                            modifier = Modifier.size(getResponsiveSize(screenHeight, 14.dp, 16.dp, 18.dp))
//                        )
//                        Spacer(modifier = Modifier.width(verticalSpacing * 0.4f))
//                        Text(
//                            "Crear cuenta",
//                            fontSize = getResponsiveFontSize(screenHeight, 12.sp, 14.sp, 16.sp)
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(verticalSpacing))
//
//                // Términos y condiciones compactos
//                TermsAndConditionsSection(
//                    onTermsClicked = onTermsClicked,
//                    onPrivacyPolicyClicked = onPrivacyPolicyClicked,
//                    screenHeight = screenHeight
//                )
//            }
//        }
//    }
//}
//
//// Componente para el overlay de carga
//@Composable
//fun LoadingOverlay() {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Black.copy(alpha = 0.5f))
//            .clickable(enabled = false) {}, // Evitar clics mientras carga
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            CircularProgressIndicator(
//                modifier = Modifier.size(64.dp),
//                color = PrimaryColor,
//                strokeWidth = 4.dp
//            )
//            Text(
//                text = "Iniciando sesión...",
//                color = Color.White,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Medium
//            )
//        }
//    }
//}
//
//// Campo de texto más compacto
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CompactTextField(
//    value: String,
//    onValueChange: (String) -> Unit,
//    placeholder: String,
//    label: String,
//    leadingIcon: ImageVector,
//    modifier: Modifier = Modifier,
//    keyboardType: KeyboardType = KeyboardType.Text,
//    isPassword: Boolean = false,
//    screenHeight: Dp,
//    enabled: Boolean = true // Nuevo parámetro para habilitar/deshabilitar
//) {
//    var passwordVisible by remember { mutableStateOf(false) }
//
//    val fieldHeight = getResponsiveSize(screenHeight, 44.dp, 48.dp, 52.dp)
//    val iconSize = getResponsiveSize(screenHeight, 16.dp, 18.dp, 20.dp)
//    val fontSize = getResponsiveFontSize(screenHeight, 12.sp, 14.sp, 16.sp)
//    val labelFontSize = getResponsiveFontSize(screenHeight, 12.sp, 14.sp, 16.sp)
//
//    Column(modifier = modifier) {
//        Text(
//            text = label,
//            color = if (enabled) Color.Gray else Color.Gray.copy(alpha = 0.5f),
//            fontWeight = FontWeight.Medium,
//            fontSize = labelFontSize,
//            modifier = Modifier.padding(bottom = 4.dp)
//        )
//
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(fieldHeight)
//                .background(
//                    color = if (enabled) Color(0xFFF8F9FA) else Color(0xFFF0F0F0),
//                    shape = RoundedCornerShape(10.dp)
//                )
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 12.dp)
//            ) {
//                Icon(
//                    leadingIcon,
//                    null,
//                    tint = if (enabled) PrimaryColor.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.4f),
//                    modifier = Modifier.size(iconSize)
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                TextField(
//                    value = value,
//                    onValueChange = onValueChange,
//                    placeholder = {
//                        Text(
//                            placeholder,
//                            color = Color.Gray.copy(alpha = 0.6f),
//                            fontSize = fontSize
//                        )
//                    },
//                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
//                    visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
//                    colors = TextFieldDefaults.colors(
//                        focusedContainerColor = Color.Transparent,
//                        unfocusedContainerColor = Color.Transparent,
//                        focusedIndicatorColor = Color.Transparent,
//                        unfocusedIndicatorColor = Color.Transparent,
//                        cursorColor = if (enabled) PrimaryColor else Color.Gray,
//                        focusedTextColor = if (enabled) Color.Black else Color.Gray,
//                        unfocusedTextColor = if (enabled) Color.Black else Color.Gray
//                    ),
//                    modifier = Modifier.weight(1f),
//                    singleLine = true,
//                    textStyle = LocalTextStyle.current.copy(fontSize = fontSize),
//                    enabled = enabled // Pasar el estado de habilitado
//                )
//                if (isPassword) {
//                    IconButton(
//                        onClick = { passwordVisible = !passwordVisible },
//                        modifier = Modifier.size(iconSize),
//                        enabled = enabled // Deshabilitar botón de visibilidad cuando está cargando
//                    ) {
//                        Icon(
//                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
//                            null,
//                            tint = if (enabled) PrimaryColor.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.4f),
//                            modifier = Modifier.size(iconSize * 0.8f)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//
//// Funciones auxiliares para responsive design (mantenidas igual)
//@Composable
//fun getResponsiveSize(screenHeight: Dp, small: Dp, medium: Dp, large: Dp): Dp {
//    return when {
//        screenHeight < 600.dp -> small
//        screenHeight < 800.dp -> medium
//        else -> large
//    }
//}
//
//@Composable
//fun getResponsiveFontSize(screenHeight: Dp, small: androidx.compose.ui.unit.TextUnit, medium: androidx.compose.ui.unit.TextUnit, large: androidx.compose.ui.unit.TextUnit): androidx.compose.ui.unit.TextUnit {
//    return when {
//        screenHeight < 600.dp -> small
//        screenHeight < 800.dp -> medium
//        else -> large
//    }
//}
//
//@Composable
//fun getResponsivePadding(screenWidth: Dp, small: Dp, medium: Dp, large: Dp): Dp {
//    return when {
//        screenWidth < 360.dp -> small
//        screenWidth < 600.dp -> medium
//        else -> large
//    }
//}
//
//@Preview(showBackground = true, showSystemUi = true, widthDp = 411, heightDp = 820)
//@Composable
//fun PreviewLoginScreenMedium() {
//    val navController = rememberNavController()
//    LoginScreen(
//        navController = navController,
//        onLoginSuccess = { },
//        onNavigateToRegister = { },
//        onGoogleSignIn = { },
//        onTermsClicked = { println("Términos: $it") },
//        onPrivacyPolicyClicked = { println("Política: $it") }
//    )
//}
//
//@Preview(showBackground = true, showSystemUi = true, widthDp = 768, heightDp = 1024)
//@Composable
//fun PreviewLoginScreenTablet() {
//    val navController = rememberNavController()
//    LoginScreen(
//        navController = navController,
//        onLoginSuccess = { },
//        onNavigateToRegister = { },
//        onGoogleSignIn = { },
//        onTermsClicked = { println("Términos: $it") },
//        onPrivacyPolicyClicked = { println("Política: $it") }
//    )
//}
//
//@Preview(showBackground = true, showSystemUi = true, widthDp = 360, heightDp = 640)
//@Composable
//fun PreviewLoginScreenSmall() {
//    val navController = rememberNavController()
//    LoginScreen(
//        navController = navController,
//        onLoginSuccess = { },
//        onNavigateToRegister = { },
//        onGoogleSignIn = { },
//        onTermsClicked = { println("Términos: $it") },
//        onPrivacyPolicyClicked = { println("Política: $it") }
//    )
//}