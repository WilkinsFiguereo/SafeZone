package com.wilkins.alertaya.frontend.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
)

@Composable
fun AlertaYaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography(), // usa las predeterminadas por ahora
        shapes = Shapes(),         // formas predeterminadas
        content = content
    )
}
