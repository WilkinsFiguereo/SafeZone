package com.wilkins.safezone

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.auth.SessionManager
import com.wilkins.safezone.backend.network.auth.SessionManager.getUserProfile
import com.wilkins.safezone.navigation.adminRoutes
import com.wilkins.safezone.navigation.associationRoutes
import com.wilkins.safezone.navigation.generalRoutes
import com.wilkins.safezone.navigation.moderatorRoutes
import com.wilkins.safezone.navigation.userRoutes
import com.wilkins.safezone.ui.theme.SafeZoneTheme

/**
 * 🏠 MainActivity - Actividad Principal
 *
 * Esta actividad coordina toda la navegación de la aplicación
 * utilizando rutas modularizadas por rol:
 *
 * - GeneralRoutes: Rutas públicas (splash, login, register)
 * - UserRoutes: Rutas para usuarios normales (Role ID: 1)
 * - AdminRoutes: Rutas para administradores (Role ID: 2)
 * - ModeratorRoutes: Rutas para moderadores (Role ID: 3)
 * - AssociationRoutes: Rutas para asociaciones (Role ID: 4)
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SafeZoneTheme {
                FullScreenTheme {
                    val navController = rememberNavController()
                    var savedEmail = remember { mutableStateOf("") }
                    var savedPassword = remember { mutableStateOf("") }
                    val context = LocalContext.current
                    val userState = produceState<AppUser?>(initialValue = null) {
                        value = getUserProfile(context)
                    }

                    val user = userState.value

                    // ═══════════════════════════════════════════
                    // 🔐 VERIFICACIÓN DE SESIÓN ACTIVA
                    // ═══════════════════════════════════════════
                    fun hasActiveSession(): Boolean {
                        val session = SessionManager.loadSession(context)
                        val hasSession = session != null
                        Log.i("MainActivity", "🔐 Verificando sesión: ${if (hasSession) "Activa" else "Inactiva"}")
                        return hasSession
                    }

                    // 🔐 Estado compartido


                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        generalRoutes(
                            navController = navController,
                            savedEmail = savedEmail,
                            savedPassword = savedPassword
                        )


                        // ════════════════════════════════════════════
                        // 👤 RUTAS DE USUARIO (Role ID: 1)
                        // Total: 10 rutas
                        // ════════════════════════════════════════════
                        userRoutes(
                            navController = navController,
                            context = context,
                            user = user,
                            hasActiveSession = ::hasActiveSession
                        )

                        // ════════════════════════════════════════════
                        // 🛡️ RUTAS DE ADMINISTRADOR (Role ID: 2)
                        // Total: 8 rutas
                        // ════════════════════════════════════════════
                        adminRoutes(
                            navController = navController,
                            context = context,
                            hasActiveSession = ::hasActiveSession
                        )

                        // ════════════════════════════════════════════
                        // ⚙️ RUTAS DE MODERADOR (Role ID: 3)
                        // Total: 1 ruta + acceso a rutas de asociación
                        // ════════════════════════════════════════════
                        moderatorRoutes(
                            navController = navController,
                            context = context,
                            hasActiveSession = ::hasActiveSession
                        )

                        // ════════════════════════════════════════════
                        // 🏢 RUTAS DE ASOCIACIÓN (Role ID: 4)
                        // Total: 6 rutas (compartidas con moderador)
                        // ════════════════════════════════════════════
                        associationRoutes(
                            navController = navController,
                            hasActiveSession = ::hasActiveSession
                        )
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════
    // 🔹 CONFIGURACIÓN DE PANTALLA COMPLETA
    // ═══════════════════════════════════════════
    private fun enableFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                            android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )
        }
    }
}

/**
 * 🎨 FullScreenTheme
 * Composable que configura el modo de pantalla completa
 * ocultando barras de sistema y navegación
 */
@Composable
fun FullScreenTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    val window = (view.context as? ComponentActivity)?.window

    DisposableEffect(Unit) {
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(
                WindowInsetsCompat.Type.statusBars() or
                        WindowInsetsCompat.Type.navigationBars()
            )
        }
        onDispose {}
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        content()
    }
}