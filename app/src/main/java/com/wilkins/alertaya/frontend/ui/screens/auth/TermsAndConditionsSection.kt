package com.wilkins.alertaya.frontend.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.wilkins.alertaya.ui.theme.NameApp
import com.wilkins.alertaya.ui.theme.PrimaryColor

@Composable
fun TermsAndConditionsSection(
    onTermsClicked: (url: String) -> Unit,
    onPrivacyPolicyClicked: (url: String) -> Unit,
    screenHeight: Dp,
) {
    val fontSize = getResponsiveFontSizeTerms(screenHeight, 10.sp, 11.sp, 14.sp)
    val smallFontSize = getResponsiveFontSizeTerms(screenHeight, 8.sp, 10.sp, 12.sp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Texto de derechos reservados
        Text(
            text = "© 2025 $NameApp. Todos los derechos reservados. La información proporcionada será tratada de manera confidencial según nuestra Política de Privacidad.",
            fontSize = smallFontSize,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = smallFontSize * 1.2,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Enlaces de Términos y Política
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Términos de Uso",
                fontSize = fontSize,
                color = PrimaryColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    onTermsClicked("http://localhost:5000/static/Document/terminos_y_condiciones_gesde.pdf")
                }
            )

            Text(
                text = " | ",
                fontSize = fontSize,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Text(
                text = "Política de Privacidad",
                fontSize = fontSize,
                color = PrimaryColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    onPrivacyPolicyClicked("http://localhost:5000/static/Document/politica_privacidad_gesde_3_paginas.pdf")
                }
            )
        }
    }
}

// Versión compacta para pantallas pequeñas
@Composable
fun CompactTermsAndConditionsSection(
    onTermsClicked: (url: String) -> Unit,
    onPrivacyPolicyClicked: (url: String) -> Unit,
    screenHeight: Dp
) {
    val fontSize = getResponsiveFontSizeTerms(screenHeight, 9.sp, 10.sp, 11.sp)
    val smallFontSize = getResponsiveFontSizeTerms(screenHeight, 7.sp, 8.sp, 9.sp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Texto de derechos reservados
        Text(
            text = "© 2025 AlertaYa. Todos los derechos reservados.",
            fontSize = smallFontSize,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = smallFontSize * 1.2
        )

        Text(
            text = "La información se tratará de manera confidencial según nuestra Política.",
            fontSize = smallFontSize,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = smallFontSize * 1.2,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Enlaces de Términos y Política
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Términos de Uso",
                fontSize = fontSize,
                color = PrimaryColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    onTermsClicked("http://localhost:5000/static/Document/terminos_y_condiciones_gesde.pdf")
                }
            )

            Text(
                text = "|",
                fontSize = fontSize,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            Text(
                text = "Política de Privacidad",
                fontSize = fontSize,
                color = PrimaryColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    onPrivacyPolicyClicked("http://localhost:5000/static/Document/politica_privacidad_gesde_3_paginas.pdf")
                }
            )
        }
    }
}

// Función auxiliar específica para este archivo
@Composable
fun getResponsiveFontSizeTerms(
    screenHeight: Dp,
    small: TextUnit,
    medium: TextUnit,
    large: TextUnit
): TextUnit {
    return when {
        screenHeight < 600.dp -> small
        screenHeight < 800.dp -> medium
        else -> large
    }
}

// Función para tamaños de fuente pequeños (texto secundario)
@Composable
fun getResponsiveSmallFontSize(screenHeight: Dp): TextUnit {
    return when {
        screenHeight < 600.dp -> 7.sp
        screenHeight < 800.dp -> 8.sp
        else -> 9.sp
    }
}

// Función para tamaños de fuente normales (texto principal)
@Composable
fun getResponsiveNormalFontSize(screenHeight: Dp): TextUnit {
    return when {
        screenHeight < 600.dp -> 9.sp
        screenHeight < 800.dp -> 10.sp
        else -> 11.sp
    }
}

// Función para tamaños de fuente grandes (enlaces y elementos importantes)
@Composable
fun getResponsiveLargeFontSize(screenHeight: Dp): TextUnit {
    return when {
        screenHeight < 600.dp -> 10.sp
        screenHeight < 800.dp -> 12.sp
        else -> 14.sp
    }
}