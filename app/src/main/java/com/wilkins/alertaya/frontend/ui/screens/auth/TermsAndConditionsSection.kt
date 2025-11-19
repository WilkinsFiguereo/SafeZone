package com.wilkins.alertaya.frontend.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.wilkins.alertaya.ui.theme.PrimaryColor


@Composable
fun TermsAndConditionsSection(
    onTermsClicked: (url: String) -> Unit,
    onPrivacyPolicyClicked: (url: String) -> Unit,
    screenHeight: Dp
) {
    val fontSize = getResponsiveFontSize(screenHeight, 10.sp, 12.sp, 14.sp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Al continuar, aceptas nuestros ",
            fontSize = fontSize,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Términos de servicio",
            fontSize = fontSize,
            color = PrimaryColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onTermsClicked("https://ejemplo.com/terminos") }
        )

        Text(
            text = " y ",
            fontSize = fontSize,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Política de privacidad",
            fontSize = fontSize,
            color = PrimaryColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onPrivacyPolicyClicked("https://ejemplo.com/privacidad") }
        )
    }
}