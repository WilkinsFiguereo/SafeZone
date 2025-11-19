package com.wilkins.safezone.GenericUserUi

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.ui.theme.NameApp
import com.wilkins.safezone.ui.theme.PrimaryColor
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember { Animatable(0f) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        // Animación
        scale.animateTo(1f, animationSpec = tween(1200))
        delay(800)

        val supabase = SupabaseService.getInstance()
        val session = SessionManager.loadSession(context)

        if (session != null) {
            try {
                supabase.auth.importSession(session)
                val user = supabase.auth.currentUserOrNull()

                if (user != null) {
                    // Obtener el role_id desde SharedPreferences
                    val roleId = SessionManager.getUserRole(context)

                    Log.i("SplashScreen", "🔍 Usuario: id=${user.id}, role=$roleId")

                    when (roleId) {
                        1 -> {
                            Log.i("SplashScreen", "✅ Rol 1 → UserHome")
                            navController.navigate("userHome/${user.id}") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                        2 -> {
                            Log.i("SplashScreen", "✅ Rol 2 → DashboardAdmin")
                            navController.navigate("DashboardAdmin") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                        else -> {
                            Log.e("SplashScreen", "❌ Rol no encontrado en SharedPreferences, consultando base de datos...")
                            // Si no hay rol guardado, consultar la base de datos
                            val userProfile = SessionManager.getUserProfile(context)
                            if (userProfile != null) {
                                SessionManager.saveUserData(context, userProfile)

                                when (userProfile.role_id) {
                                    1 -> {
                                        Log.i("SplashScreen", "✅ Rol 1 → UserHome (desde DB)")
                                        navController.navigate("userHome/${user.id}") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                    2 -> {
                                        Log.i("SplashScreen", "✅ Rol 2 → DashboardAdmin (desde DB)")
                                        navController.navigate("DashboardAdmin") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                    else -> {
                                        Log.e("SplashScreen", "❌ Rol desconocido, redirigiendo a login")
                                        navController.navigate("login") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                }
                            } else {
                                Log.e("SplashScreen", "❌ No se pudo obtener el perfil del usuario")
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        }
                    }
                } else {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            } catch (e: Exception) {
                Log.e("SplashScreen", "Error al restaurar sesión: ${e.message}")
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    // --- UI del Splash ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale.value)
                    .background(Color.White.copy(alpha = 0.2f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = NameApp,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cuidándote en todo momento",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}