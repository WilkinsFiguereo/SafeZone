package com.wilkins.safezone.frontend.ui.user.Homepage

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import com.wilkins.safezone.ui.theme.NameApp
import kotlinx.coroutines.delay

@Composable
fun WelcomeBanner() {
    var isVisible by remember { mutableStateOf(false) }

    // Animaciones principales
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "alpha_animation"
    )

    // Animación de elevación
    val elevation by animateFloatAsState(
        targetValue = if (isVisible) 8f else 2f,
        animationSpec = tween(durationMillis = 600),
        label = "elevation_animation"
    )

    LaunchedEffect(Unit) {
        delay(500)
        isVisible = true
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(20.dp),
                clip = true
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icono principal animado
            AnimatedWelcomeIcon()

            // Contenido de texto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "¡Bienvenido a $NameApp!",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    letterSpacing = 0.2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Reporta incidentes en tu comunidad y mantente informado en tiempo real. Tu participación hace la diferencia.",
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Start,
                    lineHeight = 18.sp
                )
            }

            // Indicador lateral animado
            AnimatedIndicator()
        }
    }
}

@Composable
fun AnimatedWelcomeIcon() {
    val rotation by animateFloatAsState(
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "icon_rotation"
    )

    val pulse by animateFloatAsState(
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                1.0f at 0
                1.1f at 1000
                1.0f at 2000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_animation"
    )

    Box(
        modifier = Modifier
            .size(60.dp)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                clip = true
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .scale(pulse),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🚨",
            fontSize = 28.sp,
            modifier = Modifier.graphicsLayer {
                rotationZ = rotation
            }
        )
    }
}

@Composable
fun AnimatedIndicator() {
    var currentState by remember { mutableStateOf(0) }

    val dotSize by animateFloatAsState(
        targetValue = when (currentState) {
            0 -> 6f
            1 -> 8f
            else -> 6f
        },
        animationSpec = tween(durationMillis = 600),
        label = "dot_animation"
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(600)
            currentState = (currentState + 1) % 3
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == currentState) dotSize.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        color = if (index == currentState)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
            )
        }
    }
}

// Versión alternativa más minimalista
@Composable
fun ElegantWelcomeBanner() {
    var isVisible by remember { mutableStateOf(false) }

    val slideIn by animateFloatAsState(
        targetValue = if (isVisible) 0f else -100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "slide_animation"
    )

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                clip = true
            )
            .graphicsLayer {
                translationX = slideIn
            },
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Icono elegante
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👋",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$NameApp - Comunidad Segura",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Reporta incidentes, recibe alertas y protege tu comunidad.",
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.sp
                )
            }

            // Flecha animada
            AnimatedArrow()
        }
    }
}

@Composable
fun AnimatedArrow() {
    val bounce by animateFloatAsState(
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1500
                1.0f at 0
                1.2f at 750
                1.0f at 1500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "arrow_bounce"
    )

    Box(
        modifier = Modifier
            .size(24.dp)
            .scale(bounce),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "➡️",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeBannerPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            WelcomeBanner()
            ElegantWelcomeBanner()
        }
    }
}