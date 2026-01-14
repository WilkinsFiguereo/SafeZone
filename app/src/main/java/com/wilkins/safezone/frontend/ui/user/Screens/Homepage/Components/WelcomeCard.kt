package com.wilkins.safezone.frontend.ui.user.Screens.Homepage.Components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.*
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
import androidx.compose.ui.graphics.graphicsLayer
import com.wilkins.safezone.navigation.theme.NameApp
import kotlinx.coroutines.delay

@Composable
fun WelcomeBanner(
    modifier: Modifier = Modifier,
    userName: String? = null,
    variant: BannerVariant = BannerVariant.PRIMARY
) {
    var isVisible by remember { mutableStateOf(false) }

    // Animaciones corregidas
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "alpha_animation"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isVisible) 12f else 2f,
        animationSpec = tween(durationMillis = 800),
        label = "elevation_animation"
    )

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(24.dp),
                clip = true
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
        shape = RoundedCornerShape(24.dp),
        color = Color.White, // Fondo blanco
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Icono principal con fondo blanco
            AnimatedWelcomeIcon(variant = variant)

            // Contenido de texto mejorado
            WelcomeTextContent(userName = userName, variant = variant)

            // Indicador mejorado
            AnimatedPulseIndicator(variant = variant)
        }
    }
}

@Composable
fun AnimatedWelcomeIcon(variant: BannerVariant) {
    val rotation by animateFloatAsState(
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "icon_rotation"
    )

    val pulse by animateFloatAsState(
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2500
                1.0f at 0
                1.15f at 750
                1.0f at 1500
                1.15f at 2250
                1.0f at 2500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_animation"
    )

    val iconColor = when (variant) {
        BannerVariant.PRIMARY -> MaterialTheme.colorScheme.primary
        BannerVariant.SECONDARY -> MaterialTheme.colorScheme.secondary
    }

    Box(
        modifier = Modifier
            .size(70.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                clip = true
            )
            .clip(CircleShape)
            .background(Color.White) // Círculo blanco
            .scale(pulse),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "App Icon",
            tint = iconColor,
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer {
                    rotationZ = rotation
                }
        )
    }
}

@Composable
fun WelcomeTextContent(userName: String?, variant: BannerVariant) {
    val textColor = when (variant) {
        BannerVariant.PRIMARY -> MaterialTheme.colorScheme.primary
        BannerVariant.SECONDARY -> MaterialTheme.colorScheme.secondary
    }

    val secondaryTextColor = Color.DarkGray.copy(alpha = 0.7f)

    Column(
        modifier = Modifier
    ) {
        Text(
            text = buildWelcomeText(userName),
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            letterSpacing = 0.1.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Reporta incidentes en tu comunidad y mantente informado en tiempo real. " +
                    "Tu participación activa crea entornos más seguros para todos.",
            color = secondaryTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Start,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun AnimatedPulseIndicator(variant: BannerVariant) {
    var currentPulse by remember { mutableStateOf(0) }

    val indicatorColor = when (variant) {
        BannerVariant.PRIMARY -> MaterialTheme.colorScheme.primary
        BannerVariant.SECONDARY -> MaterialTheme.colorScheme.secondary
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            currentPulse = (currentPulse + 1) % 3
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(3) { index ->
            val isPulsing = index == currentPulse

            // Animación de tamaño corregida
            val size by animateDpAsState(
                targetValue = if (isPulsing) 12.dp else 8.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "pulse_size"
            )

            // Animación de alpha corregida
            val alpha by animateFloatAsState(
                targetValue = if (isPulsing) 1f else 0.6f,
                animationSpec = tween(durationMillis = 300),
                label = "pulse_alpha"
            )

            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        color = indicatorColor.copy(alpha = alpha)
                    )
            )
        }
    }
}

// Banner minimalista alternativo
@Composable
fun ElegantWelcomeBanner(
    modifier: Modifier = Modifier,
    userName: String? = null
) {
    var isVisible by remember { mutableStateOf(false) }

    val slideIn by animateFloatAsState(
        targetValue = if (isVisible) 0f else -50f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "slide_animation"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "fade_animation"
    )

    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .graphicsLayer {
                translationX = slideIn
                this.alpha = alpha
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White // Fondo blanco
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Icono elegante con fondo blanco
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White) // Círculo blanco
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WavingHand,
                    contentDescription = "Welcome",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = buildWelcomeText(userName),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Reporta incidentes, recibe alertas instantáneas y contribuye a la seguridad colectiva.",
                    color = Color.DarkGray.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.sp
                )
            }

            // Indicador de estado mejorado
            CommunityStatusIndicator()
        }
    }
}

@Composable
fun CommunityStatusIndicator() {
    val bounce by animateFloatAsState(
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1800
                1.0f at 0
                1.1f at 600
                1.0f at 1200
                1.1f at 1500
                1.0f at 1800
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "status_bounce"
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .scale(bounce)
            .clip(CircleShape)
            .background(Color.White) // Fondo blanco
            .shadow(
                elevation = 4.dp,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "Community Status",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
    }
}

private fun buildWelcomeText(userName: String?): String {
    return if (!userName.isNullOrBlank()) {
        "¡Hola, $userName!"
    } else {
        "¡Bienvenido a $NameApp!"
    }
}

enum class BannerVariant {
    PRIMARY, SECONDARY
}

@Preview(showBackground = true)
@Composable
fun WelcomeBannerPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)) // Fondo gris claro para mejor visualización
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WelcomeBanner(userName = "María")
            WelcomeBanner(variant = BannerVariant.SECONDARY)
            ElegantWelcomeBanner(userName = "Carlos")
        }
    }
}