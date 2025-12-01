//package com.wilkins.safezone.frontend.ui.screens.auth
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalConfiguration
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.wilkins.safezone.backend.network.SupabaseService
//import com.wilkins.safezone.ui.theme.PrimaryColor
//import androidx.compose.ui.unit.TextUnit
//import com.wilkins.safezone.backend.network.AppUser
//import io.github.jan.supabase.SupabaseClient
//import io.github.jan.supabase.gotrue.auth
//import kotlinx.coroutines.delay
//import io.github.jan.supabase.gotrue.providers.builtin.Email
//import kotlinx.serialization.json.*
//import io.ktor.client.*
//import io.ktor.client.engine.cio.*
//import io.ktor.client.request.*
//import io.ktor.client.statement.*
//
//
//data class ResponsiveDimensions(
//    val horizontalPadding: Dp,
//    val logoSize: Dp,
//    val logoCornerRadius: Dp,
//    val logoTextSize: TextUnit,
//    val largeSpacer: Dp,
//    val mediumSpacer: Dp,
//    val titleTextSize: TextUnit,
//    val bodyTextSize: TextUnit
//)
//
//@Composable
//fun getResponsiveDimensions(): ResponsiveDimensions {
//    val configuration = LocalConfiguration.current
//    val screenWidth = configuration.screenWidthDp
//
//    return when {
//        screenWidth >= 600 -> ResponsiveDimensions(
//            horizontalPadding = 80.dp,
//            logoSize = 200.dp,
//            logoCornerRadius = 24.dp,
//            logoTextSize = 22.sp,
//            largeSpacer = 60.dp,
//            mediumSpacer = 40.dp,
//            titleTextSize = 28.sp,
//            bodyTextSize = 18.sp
//        )
//        screenWidth >= 400 -> ResponsiveDimensions(
//            horizontalPadding = 40.dp,
//            logoSize = 160.dp,
//            logoCornerRadius = 20.dp,
//            logoTextSize = 20.sp,
//            largeSpacer = 50.dp,
//            mediumSpacer = 32.dp,
//            titleTextSize = 24.sp,
//            bodyTextSize = 16.sp
//        )
//        else -> ResponsiveDimensions(
//            horizontalPadding = 24.dp,
//            logoSize = 140.dp,
//            logoCornerRadius = 16.dp,
//            logoTextSize = 18.sp,
//            largeSpacer = 40.dp,
//            mediumSpacer = 24.dp,
//            titleTextSize = 22.sp,
//            bodyTextSize = 14.sp
//        )
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun VerificationScreen(
//    savedEmail: String,
//    savedPassword: String,
//    onBackClick: () -> Unit,
//    primaryColor: Color,
//    onVerified: () -> Unit
//) {
//    val dimensions = getResponsiveDimensions()
//
//    var verificationStatus by remember { mutableStateOf("⏳ Esperando verificación...") }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//    var isLoading by remember { mutableStateOf(false) }
//
//    val coroutineScope = rememberCoroutineScope()
//
//    // Verificación automática cada 10 segundos
//    LaunchedEffect(Unit) {
//        repeat(30) { iteration ->
//            isLoading = true
//            verificationStatus = "Verificando estado del email... (${iteration + 1}/30)"
//
//            try {
//                // Verificar directamente en la base de datos usando el email guardado
//                val isVerified = checkEmailVerificationWithRPC(savedEmail)
//
//                if (isVerified) {
//                    verificationStatus = "✅ Correo verificado exitosamente!"
//                    delay(500)
//
//                    // Login automático
//                    val loginSuccess = loginAfterVerification(savedEmail, savedPassword)
//                    if (loginSuccess) {
//                        verificationStatus = "✅ Inicio de sesión exitoso."
//                        delay(500)
//                        onVerified() // Navegar a UserHomeScreen
//                        return@LaunchedEffect
//                    } else {
//                        errorMessage = "Error iniciando sesión tras la verificación."
//                        isLoading = false
//                    }
//                } else {
//                    verificationStatus = "⏳ Aún esperando verificación del email..."
//                }
//            } catch (e: Exception) {
//                errorMessage = "Error al verificar: ${e.message}"
//                verificationStatus = "❌ Error en verificación"
//                println("❌ Error en ciclo de verificación: ${e.message}")
//                e.printStackTrace()
//            } finally {
//                isLoading = false
//            }
//
//            delay(10_000) // 10 segundos
//        }
//
//        // Tiempo agotado
//        if (verificationStatus.startsWith("⏳")) {
//            verificationStatus = "⛔ Tiempo de verificación agotado. Intenta nuevamente."
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {},
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "Volver",
//                            modifier = Modifier.size(if (dimensions.logoSize > 160.dp) 32.dp else 24.dp)
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.background
//                )
//            )
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .padding(horizontal = dimensions.horizontalPadding),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(dimensions.logoSize)
//                    .clip(RoundedCornerShape(dimensions.logoCornerRadius))
//                    .background(primaryColor.copy(alpha = 0.1f)),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "LOGO",
//                    color = primaryColor,
//                    fontWeight = FontWeight.Bold,
//                    fontSize = dimensions.logoTextSize
//                )
//            }
//
//            Spacer(modifier = Modifier.height(dimensions.largeSpacer))
//
//            Text(
//                text = "Por favor verifica tu correo electrónico",
//                style = MaterialTheme.typography.headlineSmall.copy(
//                    fontSize = dimensions.titleTextSize,
//                    fontWeight = FontWeight.Bold
//                ),
//                textAlign = TextAlign.Center,
//                modifier = Modifier.fillMaxWidth(),
//                lineHeight = dimensions.titleTextSize * 1.2
//            )
//
//            Spacer(modifier = Modifier.height(dimensions.mediumSpacer))
//
//            when {
//                isLoading -> {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        CircularProgressIndicator(color = primaryColor)
//                        Spacer(modifier = Modifier.height(16.dp))
//                        Text(
//                            text = verificationStatus,
//                            color = primaryColor,
//                            fontSize = dimensions.bodyTextSize,
//                            textAlign = TextAlign.Center
//                        )
//                    }
//                }
//                errorMessage != null -> {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        Text(text = "❌", fontSize = 32.sp)
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text(
//                            text = errorMessage!!,
//                            color = MaterialTheme.colorScheme.error,
//                            fontSize = dimensions.bodyTextSize,
//                            textAlign = TextAlign.Center
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text(
//                            text = "Estado: $verificationStatus",
//                            color = MaterialTheme.colorScheme.onSurface,
//                            fontSize = dimensions.bodyTextSize * 0.9f,
//                            textAlign = TextAlign.Center
//                        )
//                    }
//                }
//                else -> {
//                    Text(
//                        text = "Hemos enviado un enlace de verificación a:\n$savedEmail\n\n" +
//                                "Por favor revisa tu bandeja de entrada y sigue las instrucciones.\n\n" +
//                                "Estado: $verificationStatus",
//                        style = MaterialTheme.typography.bodyLarge.copy(
//                            fontSize = dimensions.bodyTextSize
//                        ),
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier.fillMaxWidth(),
//                        lineHeight = dimensions.bodyTextSize * 1.4
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(dimensions.mediumSpacer))
//
//        }
//    }
//}
//
//
///**
// * 🔹 Verificación usando función RPC de Supabase
// * Llama a la función PostgreSQL directamente usando HTTP
// */
//suspend fun checkEmailVerificationWithRPC(email: String): Boolean {
//    return try {
//        val supabase = SupabaseService.getInstance()
//
//        // Obtener configuración de Supabase
//        val supabaseConfig = supabase.supabaseHttpUrl
//        val supabaseKey = supabase.supabaseKey
//
//        // Construir la URL completa
//        val url = "$supabaseConfig/rest/v1/rpc/check_email_verification"
//
//        // Crear un cliente HTTP independiente (no usar el de Supabase)
//        val httpClient = HttpClient(CIO)
//
//        try {
//            // Hacer la petición POST con el parámetro
//            val response: HttpResponse = httpClient.post(url) {
//                header("apikey", supabaseKey)
//                header("Authorization", "Bearer $supabaseKey")
//                header("Content-Type", "application/json")
//                header("Prefer", "return=representation")
//                setBody("""{"user_email": "$email"}""")
//            }
//
//            val jsonString = response.bodyAsText()
//            // Parsear la respuesta JSON
//            val jsonElement = Json.parseToJsonElement(jsonString)
//            val isVerified = jsonElement.jsonObject["is_verified"]?.jsonPrimitive?.boolean ?: false
//            val emailConfirmedAt = jsonElement.jsonObject["email_confirmed_at"]?.jsonPrimitive?.contentOrNull
//
//            isVerified
//        } finally {
//            httpClient.close()
//        }
//    } catch (e: Exception) {
//        println("❌ Error verificando email con RPC: ${e.message}")
//        e.printStackTrace()
//        false
//    }
//}
//
//
///**
// * 🔹 Login automático tras la verificación
// */
//suspend fun loginAfterVerification(email: String, password: String): Boolean {
//    return try {
//        val supabase = SupabaseService.getInstance()
//
//        // Iniciar sesión con el proveedor Email
//        supabase.auth.signInWith(Email) {
//            this.email = email
//            this.password = password
//        }
//
//        // Verificar si el usuario está autenticado
//        val user = supabase.auth.currentUserOrNull()
//        val success = user != null
//
//        if (success) {
//            println("✅ Login exitoso. Usuario: ${user?.email}")
//        } else {
//            println("❌ Login falló. No hay usuario actual.")
//        }
//
//        success
//    } catch (e: Exception) {
//        println("❌ Error loginAfterVerification: ${e.message}")
//        e.printStackTrace()
//        false
//    }
//}
//
//
///**
// * 🔹 Verifica si el usuario tiene una sesión activa
// */
//fun SupabaseClient.isUserLoggedIn(): Boolean {
//    return try {
//        auth.currentUserOrNull() != null
//    } catch (e: Exception) {
//        false
//    }
//}
//
///**
// * 🔹 Obtiene el usuario actual (si está logueado)
// */
//suspend fun SupabaseClient.getCurrentUserSafely(): AppUser? {
//    return try {
//        val current = auth.currentUserOrNull() ?: return null
//
//        AppUser(
//            id = current.id,
//            name = current.userMetadata?.get("name")?.toString(),
//            role_id = current.userMetadata?.get("role_id")?.toString()?.toIntOrNull(),
//            status_id = current.userMetadata?.get("status_id")?.toString()?.toIntOrNull(),
//            email = current.email ?: "",
//            emailConfirmedAt = current.emailConfirmedAt?.toString(),
//            confirmedAt = current.confirmedAt?.toString()
//        )
//    } catch (e: Exception) {
//        println("❌ Error obteniendo usuario: ${e.message}")
//        null
//    }
//}
//
//
//// Previews
//@Preview(showBackground = true, showSystemUi = true, name = "Teléfono Pequeño")
//@Composable
//fun VerificationScreenSmallPreview() {
//    MaterialTheme {
//        VerificationScreen(
//            onBackClick = { },
//            primaryColor = PrimaryColor,
//            onVerified = {},
//            savedEmail = "test@example.com",
//            savedPassword = "password123"
//        )
//    }
//}
//
//@Preview(showBackground = true, showSystemUi = true, widthDp = 360, heightDp = 640, name = "Teléfono Medio")
//@Composable
//fun VerificationScreenMediumPreview() {
//    MaterialTheme {
//        VerificationScreen(
//            onBackClick = { },
//            primaryColor = PrimaryColor,
//            onVerified = {},
//            savedEmail = "test@example.com",
//            savedPassword = "password123"
//        )
//    }
//}
//
//@Preview(showBackground = true, showSystemUi = true, widthDp = 420, heightDp = 800, name = "Teléfono Grande")
//@Composable
//fun VerificationScreenLargePreview() {
//    MaterialTheme {
//        VerificationScreen(
//            onBackClick = { },
//            primaryColor = PrimaryColor,
//            onVerified = {},
//            savedEmail = "test@example.com",
//            savedPassword = "password123"
//        )
//    }
//}
//
//@Preview(showBackground = true, showSystemUi = true, widthDp = 600, heightDp = 1024, name = "Tablet 7\"")
//@Composable
//fun VerificationScreenTablet7Preview() {
//    MaterialTheme {
//        VerificationScreen(
//            onBackClick = { },
//            primaryColor = PrimaryColor,
//            onVerified = {},
//            savedEmail = "test@example.com",
//            savedPassword = "password123"
//        )
//    }
//}
//
//@Preview(showBackground = true, showSystemUi = true, widthDp = 840, heightDp = 1200, name = "Tablet 10\"")
//@Composable
//fun VerificationScreenTablet10Preview() {
//    MaterialTheme {
//        VerificationScreen(
//            onBackClick = { },
//            primaryColor = PrimaryColor,
//            onVerified = {},
//            savedEmail = "test@example.com",
//            savedPassword = "password123"
//        )
//    }
//}
//
//@Preview(showBackground = true, showSystemUi = true, name = "Dark Mode")
//@Composable
//fun VerificationScreenDarkPreview() {
//    MaterialTheme(
//        colorScheme = darkColorScheme()
//    ) {
//        VerificationScreen(
//            onBackClick = { },
//            primaryColor = PrimaryColor,
//            onVerified = {},
//            savedEmail = "test@example.com",
//            savedPassword = "password123"
//        )
//    }
//}