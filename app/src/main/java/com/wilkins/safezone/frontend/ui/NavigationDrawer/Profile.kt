package com.wilkins.safezone.frontend.ui.NavigationDrawer//package com.wilkins.safezone.frontend.ui.NavigationDrawer
//
//import SessionManager.getUserProfile
//import android.content.Context
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.produceState
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import com.wilkins.safezone.GenericUserUi.BottomNavigationMenu
//import com.wilkins.safezone.GenericUserUi.SideMenu
//import com.wilkins.safezone.backend.network.AppUser
//import com.wilkins.safezone.backend.network.SupabaseService
//import io.github.jan.supabase.SupabaseClient
//import io.github.jan.supabase.gotrue.auth
//
//@Composable
//fun Profile(navController: NavController, context: Context, supabaseClient: SupabaseClient){
//    val supabase = SupabaseService.getInstance()
//    val userId = supabase.auth.currentUserOrNull()?.id ?: ""
//    val userState = produceState<AppUser?>(initialValue = null) {
//        value = getUserProfile(context)
//    }
//
//    val user = userState.value
//    Box(modifier = Modifier.fillMaxSize()) {
//
//        // 🔸 Contenido principal con scroll
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .verticalScroll(rememberScrollState())
//        ) {
//
//            Spacer(modifier = Modifier.height(75.dp))
//
//            Spacer(modifier = Modifier.height(12.dp))
//            ProfileSection(isFollowing = true)
//        }
//
//        // 🔸 Menú lateral superior (fijo arriba)
//        SideMenu(
//            navController = navController,
//            modifier = Modifier.align(Alignment.TopCenter),
//            userId = userId,
//            userName = user?.name ?: "Usuario",
//            context = context,
//            supabaseClient = supabaseClient
//        )
//
//
//        // 🔸 Menú inferior (fijo y siempre visible abajo)
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .align(Alignment.BottomCenter)
//        ) {
//            BottomNavigationMenu(
//                onNewsClick = { /* Navegar a noticias */ },
//                onAlertClick = { /* Crear nueva alerta */ },
//                onMyAlertsClick = { /* Ver mis alertas */ }
//            )
//        }
//    }
//}