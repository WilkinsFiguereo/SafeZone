@file:OptIn(ExperimentalMaterial3Api::class)

package com.wilkins.alertaya.frontend.ui.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FormScreen() {

    var asunto by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enviar reporte usuarios") },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {

            // ---------------- ENCABEZADO ----------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(0xFF28A745))
            ) {

                // Fondo simulado
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2E7D32))
                )

                // Icono de perfil
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(38.dp)
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(16.dp)
                ) {
                    Text("Bienvenido Wilkins a",
                        color = Color.White,
                        fontSize = 18.sp
                    )

                    Text(
                        "AlertaYa",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Row {
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00C853)
                            )
                        ) { Text("Boton 1") }

                        Spacer(Modifier.width(10.dp))

                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00C853)
                            )
                        ) { Text("Boton 2") }
                    }
                }

                // Logo transparente
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color.White.copy(alpha = 0.65f), shape = RoundedCornerShape(8.dp))
                        .align(Alignment.CenterEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        "Logo",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ---------------- FORMULARIO ----------------
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {

                // Título verde
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF28A745))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Comunica una problemática",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    "Un mensaje algo así como, reporta una problemática etc.",
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(12.dp))

                // Asunto
                OutlinedTextField(
                    value = asunto,
                    onValueChange = { asunto = it },
                    label = { Text("Asunto") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Tipo
                OutlinedTextField(
                    value = tipo,
                    onValueChange = { tipo = it },
                    label = { Text("Tipo") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción del mensaje") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )

                Spacer(Modifier.height(20.dp))

                // Botón imagen + ícono
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                    ) {
                        Text("Sube tu imagen")
                    }

                    Spacer(Modifier.width(12.dp))

                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF2E7D32)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Botones finales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        modifier = Modifier.weight(1f)
                    ) { Text("Publicar") }

                    Spacer(Modifier.width(16.dp))

                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        modifier = Modifier.weight(1f)
                    ) { Text("Borrador") }
                }

                Spacer(Modifier.height(24.dp))

                // Mensaje azul inferior
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E88E5))
                        .padding(10.dp)
                ) {
                    Text(
                        "Un mensaje de notificación que le diga algo al usuario",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewForm() {
    MaterialTheme {
        FormScreen()
    }
}
