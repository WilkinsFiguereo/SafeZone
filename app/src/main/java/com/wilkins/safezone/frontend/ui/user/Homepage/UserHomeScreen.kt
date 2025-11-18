package com.wilkins.safezone.frontend.ui.user.Homepage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wilkins.safezone.GenericUserUi.BottomNavigationMenu
import com.wilkins.safezone.GenericUserUi.SideMenu
import com.wilkins.safezone.R
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.gotrue.auth

@Composable
fun UserHomeScreen(navController: NavController) {
    val supabase = SupabaseService.getInstance()
    val userId = supabase.auth.currentUserOrNull()?.id ?: ""
    val newsItems = listOf(
        NewsItem(
            title = "Nuevo programa de seguridad comunitaria",
            date = "12/10/2024",
            description = "Se implementa nuevo sistema de vigilancia en el sector norte de la ciudad.",
            imageRes = R.drawable.personas_recogiendo
        ),
        NewsItem(
            title = "Jornada de limpieza este sábado",
            date = "15/10/2024",
            description = "Participa en la jornada de limpieza comunitaria en el parque central.",
            imageRes = R.drawable.bandalismo
        ),
        NewsItem(
            title = "Mejoras en alumbrado público",
            date = "18/10/2024",
            description = "Instalación de nuevas luminarias en zonas estratégicas de la ciudad.",
            imageRes = R.drawable.baches
        )
    )

    // 🔹 Box principal que permite superposición de componentes
    Box(modifier = Modifier.fillMaxSize()) {

        // 🔸 Contenido principal con scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Espacio para el SideMenu fijo
            Spacer(modifier = Modifier.height(75.dp))

            Spacer(modifier = Modifier.height(12.dp))

            NewsSlider(
                newsItems = newsItems,
                onNewsClick = { /* Acción al hacer clic en noticia */ }
            )

            Spacer(modifier = Modifier.height(15.dp))

            WelcomeBanner()


            Spacer(modifier = Modifier.height(16.dp))

            RecentReportsSection()

            // Espacio extra para asegurar que el contenido no queda detrás del BottomNavigation
            Spacer(modifier = Modifier.height(100.dp))
        }

        // 🔸 Menú lateral superior (fijo arriba)
        SideMenu(
            navController = navController,
            modifier = Modifier.align(Alignment.TopCenter),
            userId = userId
        )


        // 🔸 Menú inferior (fijo y siempre visible abajo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            BottomNavigationMenu(
                onNewsClick = { /* Navegar a noticias */ },
                onAlertClick = { /* Crear nueva alerta */ },
                onMyAlertsClick = { /* Ver mis alertas */ }
            )
        }
    }
}