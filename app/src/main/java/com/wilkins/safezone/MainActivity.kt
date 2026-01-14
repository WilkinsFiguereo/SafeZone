package com.wilkins.safezone

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.wilkins.safezone.backend.network.AppUser
import com.wilkins.safezone.backend.network.Services.NotificationBackgroundService
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.auth.SessionManager
import com.wilkins.safezone.backend.network.auth.SessionManager.getUserProfile
import com.wilkins.safezone.navigation.adminRoutes
import com.wilkins.safezone.navigation.associationRoutes
import com.wilkins.safezone.navigation.generalRoutes
import com.wilkins.safezone.navigation.moderatorRoutes
import com.wilkins.safezone.navigation.userRoutes
import com.wilkins.safezone.navigation.theme.SafeZoneTheme
import io.github.jan.supabase.gotrue.auth

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

    // ═══════════════════════════════════════════
    // 📲 LAUNCHER PARA PERMISOS DE NOTIFICACIÓN
    // ═══════════════════════════════════════════
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("MainActivity", "✅ Permiso de notificaciones concedido")
            startNotificationServiceIfLoggedIn()
        } else {
            Log.w("MainActivity", "⚠️ Permiso de notificaciones denegado")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar permisos de notificación al iniciar la app
        requestNotificationPermission()

        setContent {
            SafeZoneTheme {
                FullScreenTheme {
                    val navController = rememberNavController()
                    val savedEmail = remember { mutableStateOf("") }
                    val savedPassword = remember { mutableStateOf("") }
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

                    // ═══════════════════════════════════════════
                    // 🚀 INICIAR SERVICIO DE NOTIFICACIONES
                    // Cuando hay sesión activa
                    // ═══════════════════════════════════════════
                    LaunchedEffect(user) {
                        if (user != null) {
                            Log.i("MainActivity", "👤 Usuario detectado: ${user.name}")
                            startNotificationServiceIfLoggedIn()
                        }
                    }

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
                        // ════════════════════════════════════════════
                        userRoutes(
                            navController = navController,
                            context = context,
                            user = user,
                            hasActiveSession = ::hasActiveSession
                        )

                        // ════════════════════════════════════════════
                        // 🛡️ RUTAS DE ADMINISTRADOR (Role ID: 2)
                        // ════════════════════════════════════════════
                        adminRoutes(
                            navController = navController,
                            context = context,
                            hasActiveSession = ::hasActiveSession
                        )

                        // ════════════════════════════════════════════
                        // ⚙️ RUTAS DE MODERADOR (Role ID: 3)
                        // ════════════════════════════════════════════
                        moderatorRoutes(
                            navController = navController,
                            context = context,
                            hasActiveSession = ::hasActiveSession
                        )

                        // ════════════════════════════════════════════
                        // 🏢 RUTAS DE ASOCIACIÓN (Role ID: 4)
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
    // 📲 SOLICITAR PERMISO DE NOTIFICACIONES
    // ═══════════════════════════════════════════
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.i("MainActivity", "✅ Permiso de notificaciones ya concedido")
                    startNotificationServiceIfLoggedIn()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Log.i("MainActivity", "ℹ️ Mostrando razón del permiso de notificaciones")
                    // Aquí podrías mostrar un diálogo explicativo
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    Log.i("MainActivity", "📋 Solicitando permiso de notificaciones")
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // En versiones anteriores a Android 13, el permiso se concede automáticamente
            Log.i("MainActivity", "✅ Permiso de notificaciones no requerido (Android < 13)")
            startNotificationServiceIfLoggedIn()
        }
    }

    // ═══════════════════════════════════════════
    // 🚀 INICIAR SERVICIO DE NOTIFICACIONES
    // ═══════════════════════════════════════════
    private fun startNotificationServiceIfLoggedIn() {
        try {
            val supabase = SupabaseService.getInstance()
            val userId = supabase.auth.currentUserOrNull()?.id

            if (userId != null) {
                Log.i("MainActivity", "🚀 Iniciando servicio de notificaciones para userId=$userId")
                NotificationBackgroundService.startService(this, userId)
            } else {
                Log.i("MainActivity", "ℹ️ No hay sesión activa, servicio no iniciado")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ Error al iniciar servicio de notificaciones: ${e.message}")
            e.printStackTrace()
        }
    }

    // ═══════════════════════════════════════════
    // 🛑 DETENER SERVICIO AL DESTRUIR LA ACTIVIDAD
    // ═══════════════════════════════════════════
    override fun onDestroy() {
        super.onDestroy()
        // Solo detenemos el servicio si el usuario cerró sesión
        // El servicio debe seguir corriendo en background
        Log.i("MainActivity", "🏁 MainActivity destruida")
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