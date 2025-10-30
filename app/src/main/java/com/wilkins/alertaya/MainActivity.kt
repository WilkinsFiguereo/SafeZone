package com.wilkins.alertaya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wilkins.alertaya.GenericUserUi.AlertaYaMenu
import com.wilkins.alertaya.GenericUserUi.SplashScreen
import com.wilkins.alertaya.configuration.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            MaterialTheme {
                Surface {
                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        // Pantalla de carga
                        composable("splash") {
                            SplashScreen(navController)
                        }

                        // Menú principal
                        composable("menu") {
                            AlertaYaMenu(navController)
                        }

                        // Pantalla de perfil
                        composable("perfil") {
                            ProfileScreen(navController)
                        }
                    }
                }
            }
        }
    }
}
