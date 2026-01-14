package com.wilkins.safezone.navigation.theme


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
)

@Composable
fun SafeZoneTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography(), // usa las predeterminadas por ahora
        shapes = Shapes(),         // formas predeterminadas
        content = content
    )
}
