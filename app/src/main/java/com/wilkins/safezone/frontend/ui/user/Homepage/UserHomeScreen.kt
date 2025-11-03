package com.wilkins.safezone.frontend.ui.user.Homepage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wilkins.safezone.GenericUserUi.SideMenu
import com.wilkins.safezone.frontend.ui.user.Homepage.NewsItem
import com.wilkins.safezone.frontend.ui.user.Homepage.NewsSlider
import com.wilkins.safezone.frontend.ui.user.Homepage.WelcomeBanner

@Composable
fun UserHomeScreen() {
    // Lista de noticias de ejemplo
    val newsItems = listOf(
        NewsItem(
            title = "Nuevo programa de seguridad comunitaria",
            date = "12/10/2024",
            description = "Se implementa nuevo sistema de vigilancia en el sector norte de la ciudad.",
            imageRes = com.wilkins.safezone.R.drawable.personas_recogiendo
        ),
        NewsItem(
            title = "Jornada de limpieza este sábado",
            date = "15/10/2024",
            description = "Participa en la jornada de limpieza comunitaria en el parque central.",
            imageRes = com.wilkins.safezone.R.drawable.bandalismo
        ),
        NewsItem(
            title = "Mejoras en alumbrado público",
            date = "18/10/2024",
            description = "Instalación de nuevas luminarias en zonas estratégicas de la ciudad.",
            imageRes = com.wilkins.safezone.R.drawable.baches
        )
    )

    // Box principal para superponer el menú
    Box(modifier = Modifier.fillMaxSize()) {
        // Contenido principal con scroll
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Espaciado para el header del menú (altura aproximada del header)
            Spacer(modifier = Modifier.height(75.dp))

            Spacer(modifier = Modifier.height(12.dp))

            // Slider de noticias
            NewsSlider(
                newsItems = newsItems,
                onNewsClick = { newsItem ->
                    // Manejar click en la noticia
                }
            )

            Spacer(modifier = Modifier.height(15.dp))

            // Banner de bienvenida animado
            WelcomeBanner()

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Menú superior superpuesto (se dibuja encima del contenido)
        SideMenu()
    }
}