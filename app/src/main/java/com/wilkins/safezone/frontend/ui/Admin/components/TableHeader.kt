package com.wilkins.safezone.frontend.ui.Admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TableHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "USUARIO",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(2f)
        )
        Text(
            "ROL",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            "EMAIL",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.Center
        )
        Text(
            "ACCIONES",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(0.8f),
            textAlign = TextAlign.Center
        )
    }
}