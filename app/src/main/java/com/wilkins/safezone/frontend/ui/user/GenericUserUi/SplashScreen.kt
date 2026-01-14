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
import com.wilkins.safezone.backend.network.auth.SessionManager
import com.wilkins.safezone.navigation.theme.NameApp
import com.wilkins.safezone.navigation.theme.PrimaryColor
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember { Animatable(0f) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Log.i("SplashScreen", "🚀 ═══════════════════════════════════")
        Log.i("SplashScreen", "🚀 INICIANDO SPLASH SCREEN")
        Log.i("SplashScreen", "🚀 ═══════════════════════════════════")

        // Animación
        scale.animateTo(1f, animationSpec = tween(1200))
        delay(800)

        val supabase = SupabaseService.getInstance()
        val session = SessionManager.loadSession(context)

        Log.i("SplashScreen", "📦 Sesión cargada: ${session != null}")

        if (session != null) {
            try {
                supabase.auth.importSession(session)
                val user = supabase.auth.currentUserOrNull()

                Log.i("SplashScreen", "👤 Usuario actual: ${user?.id}")

                if (user != null) {
                    // 🔥 PRIMERO: Obtener el perfil del usuario desde la BD
                    Log.i("SplashScreen", "🔍 Obteniendo perfil del usuario desde la base de datos...")
                    val userProfile = SessionManager.getUserProfile(context)

                    if (userProfile != null) {
                        Log.i("SplashScreen", "✅ Perfil obtenido correctamente:")
                        Log.i("SplashScreen", "   - User ID: ${userProfile.id}")
                        Log.i("SplashScreen", "   - Role ID: ${userProfile.role_id}")
                        Log.i("SplashScreen", "   - Status ID: ${userProfile.status_id}")

                        // 🔥 Guardar los datos actualizados del usuario
                        SessionManager.saveUserData(context, userProfile)

                        // 🔥 SEGUNDO: Verificar el status_id
                        val statusId = userProfile.status_id ?: 0

                        Log.i("SplashScreen", "═══════════════════════════════════")
                        Log.i("SplashScreen", "🔍 VERIFICACIÓN DE STATUS")
                        Log.i("SplashScreen", "   - Status ID del perfil: $statusId")
                        Log.i("SplashScreen", "   - ¿Está deshabilitado/baneado? ${statusId in 2..3}")
                        Log.i("SplashScreen", "═══════════════════════════════════")

                        // 🔥 TERCERO: Verificar si la cuenta está deshabilitada o baneada
                        if (statusId in 2..3) {
                            Log.w("SplashScreen", "⛔⛔⛔ CUENTA DESHABILITADA/BANEADA ⛔⛔⛔")
                            Log.w("SplashScreen", "   Status ID: $statusId")
                            Log.w("SplashScreen", "   Estado: ${if (statusId == 2) "DESHABILITADO" else "BANEADO"}")
                            Log.w("SplashScreen", "   Redirigiendo a pantalla de disable...")

                            navController.navigate("disable") {
                                popUpTo("splash") { inclusive = true }
                            }
                            return@LaunchedEffect
                        }

                        // 🔥 CUARTO: Si el status es válido, navegar según el rol
                        val roleId = userProfile.role_id ?: -1

                        Log.i("SplashScreen", "✅ Status válido, navegando según rol...")
                        Log.i("SplashScreen", "   Role ID: $roleId")

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
                            3 -> {
                                Log.i("SplashScreen", "✅ Rol 3 → DashboardMod")
                                navController.navigate("DashboardMod") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                            4 -> {
                                Log.i("SplashScreen", "✅ Rol 4 → DashboardAssociation")
                                navController.navigate("DashboardAssociation") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                            else -> {
                                Log.e("SplashScreen", "❌ Rol desconocido: $roleId")
                                Log.e("SplashScreen", "   Redirigiendo a login...")
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        }
                    } else {
                        Log.e("SplashScreen", "❌ No se pudo obtener el perfil del usuario")
                        Log.e("SplashScreen", "   Redirigiendo a login...")
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                } else {
                    Log.w("SplashScreen", "⚠️ Usuario es null, redirigiendo a login")
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            } catch (e: Exception) {
                Log.e("SplashScreen", "❌ Error al restaurar sesión: ${e.message}", e)
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        } else {
            Log.i("SplashScreen", "📭 No hay sesión guardada, redirigiendo a login")
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }

        Log.i("SplashScreen", "🏁 ═══════════════════════════════════")
        Log.i("SplashScreen", "🏁 SPLASH SCREEN FINALIZADO")
        Log.i("SplashScreen", "🏁 ═══════════════════════════════════")
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