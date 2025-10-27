package com.wilkins.alertaya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import com.wilkins.alertaya.GenericUserUi.AlertaYaMenu
import com.wilkins.alertaya.GenericUserUi.SplashScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    var showSplash by remember { mutableStateOf(true) }

                    // Controla cuánto tiempo se muestra el SplashScreen
                    LaunchedEffect(Unit) {
                        delay(2500) // 2.5 segundos
                        showSplash = false
                    }

                    if (showSplash) {
                        SplashScreen()
                    } else {
                        AlertaYaMenu()
                    }
                }
            }
        }
    }
}
