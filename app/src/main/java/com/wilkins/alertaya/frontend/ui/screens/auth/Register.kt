package com.wilkins.alertaya.frontend.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.wilkins.alertaya.frontend.ui.theme.PrimaryColor
import com.wilkins.alertaya.frontend.ui.theme.SecondaryColor

@Composable
fun RegisterScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryColor), // Fondo verde (PrimaryColor)
        contentAlignment = Alignment.Center
    ) {
        // Caja central
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp) // margen lateral
                .background(
                    color = SecondaryColor, // color interior
                    shape = RoundedCornerShape(10.dp) // bordes redondeados
                )
                .padding(24.dp), // espacio interno
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aquí irá el formulario 👇",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewRegisterScreen() {
    RegisterScreen()
}
