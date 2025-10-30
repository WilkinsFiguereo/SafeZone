package com.wilkins.alertaya.frontend.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wilkins.alertaya.backend.network.AppUser

@Composable
fun UserHomeScreen(userId: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "👋 Hola, usuario con ID: $userId",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Bienvenido a tu panel de usuario 🎉")
    }
}
