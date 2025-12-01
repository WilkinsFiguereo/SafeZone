//package com.wilkins.safezone.frontend.ui.user.Homepage.Components
//
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.google.accompanist.pager.ExperimentalPagerApi
//import com.google.accompanist.pager.HorizontalPager
//import com.google.accompanist.pager.rememberPagerState
//import kotlinx.coroutines.delay
//import com.wilkins.safezone.R
//import com.wilkins.safezone.ui.theme.PrimaryColor
//
//@OptIn(ExperimentalPagerApi::class)
//@Composable
//fun WelcomeScreen(
//    onReportarClick: () -> Unit = {},
//    onBoton2Click: () -> Unit = {}
//) {
//    val images = listOf(
//        R.drawable.baches,
//        R.drawable.bandalismo,
//        R.drawable.personas_recogiendo
//    )
//
//    val pagerState = rememberPagerState()
//
//    // Auto-scroll del slider
//    LaunchedEffect(pagerState) {
//        while (true) {
//            delay(4000)
//            val nextPage = (pagerState.currentPage + 1) % images.size
//            pagerState.animateScrollToPage(nextPage)
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF5F5F5))
//    ) {
//        // Slider de imágenes con contenido (40% de altura)
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .fillMaxHeight(0.4f)
//                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
//        ) {
//            HorizontalPager(
//                count = images.size,
//                state = pagerState,
//                modifier = Modifier.fillMaxSize()
//            ) { page ->
//                Box(modifier = Modifier.fillMaxSize()) {
//                    Image(
//                        painter = painterResource(id = images[page]),
//                        contentDescription = "Imagen ${page + 1}",
//                        modifier = Modifier.fillMaxSize(),
//                        contentScale = ContentScale.Crop
//                    )
//
//                    // Overlay con gradiente más suave
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(
//                                Brush.verticalGradient(
//                                    colors = listOf(
//                                        PrimaryColor
//                                    ),
//                                    startY = 0f,
//                                    endY = 1200f
//                                )
//                            )
//                    )
//                }
//            }
//
//            // Contenido sobre el slider
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(28.dp),
//                verticalArrangement = Arrangement.SpaceBetween
//            ) {
//                // Header superior
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.Top
//                ) {
//                    Column {
//                        Text(
//                            text = "Bienvenido Wilkins 👋",
//                            color = Color.White.copy(alpha = 0.9f),
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Normal,
//                            letterSpacing = 0.5.sp
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            text = "Alerta Ya!",
//                            color = Color.White,
//                            fontSize = 28.sp,
//                            fontWeight = FontWeight.Bold,
//                            letterSpacing = 0.5.sp
//                        )
//                    }
//
//                    // Logo con sombra
//                    Box(
//                        modifier = Modifier
//                            .size(75.dp)
//                            .shadow(8.dp, RoundedCornerShape(16.dp))
//                            .clip(RoundedCornerShape(16.dp))
//                            .background(Color.White),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "Logo",
//                            color = MaterialTheme.colorScheme.primary,
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
//
//                // Texto y botones en la parte inferior
//                Column(
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    // Indicadores del pager arriba del texto
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(bottom = 16.dp),
//                        horizontalArrangement = Arrangement.Center,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        repeat(images.size) { index ->
//                            Box(
//                                modifier = Modifier
//                                    .width(if (pagerState.currentPage == index) 24.dp else 8.dp)
//                                    .height(8.dp)
//                                    .clip(RoundedCornerShape(4.dp))
//                                    .background(
//                                        if (pagerState.currentPage == index)
//                                            MaterialTheme.colorScheme.primary
//                                        else
//                                            Color.White.copy(alpha = 0.4f)
//                                    )
//                            )
//                            if (index < images.size - 1) {
//                                Spacer(modifier = Modifier.width(6.dp))
//                            }
//                        }
//                    }
//
//                    Text(
//                        text = "Mantén tu comunidad segura",
//                        color = Color.White,
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        modifier = Modifier.padding(bottom = 6.dp)
//                    )
//                    Text(
//                        text = "Reporta incidentes y contribuye al bienestar de todos",
//                        color = Color.White.copy(alpha = 0.85f),
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Normal,
//                        lineHeight = 20.sp,
//                        modifier = Modifier.padding(bottom = 24.dp)
//                    )
//
//                    // Botones con mejor estilo
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        Button(
//                            onClick = onReportarClick,
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = MaterialTheme.colorScheme.primary
//                            ),
//                            shape = RoundedCornerShape(16.dp),
//                            elevation = ButtonDefaults.buttonElevation(
//                                defaultElevation = 4.dp,
//                                pressedElevation = 8.dp
//                            ),
//                            modifier = Modifier
//                                .weight(1f)
//                                .height(56.dp)
//                        ) {
//                            Text(
//                                text = "Reportar",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.Bold,
//                                letterSpacing = 0.5.sp
//                            )
//                        }
//
//                        OutlinedButton(
//                            onClick = onBoton2Click,
//                            colors = ButtonDefaults.outlinedButtonColors(
//                                containerColor = Color.White.copy(alpha = 0.15f),
//                                contentColor = Color.White
//                            ),
//                            shape = RoundedCornerShape(16.dp),
//                            border = BorderStroke(
//                                2.dp,
//                                Color.White.copy(alpha = 0.5f)
//                            ),
//                            modifier = Modifier
//                                .weight(1f)
//                                .height(56.dp)
//                        ) {
//                            Text(
//                                text = "Ver Mapa",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.Bold,
//                                letterSpacing = 0.5.sp
//                            )
//                        }
//                    }
//                }
//            }
//        }
//
//        // Contenido debajo del slider (60% restante)
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .fillMaxHeight()
//                .padding(28.dp)
//        ) {
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text(
//                text = "Actividad Reciente",
//                fontSize = 22.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color(0xFF1A1A1A)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Ejemplo de tarjeta de actividad
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .shadow(4.dp, RoundedCornerShape(16.dp)),
//                shape = RoundedCornerShape(16.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = Color.White
//                )
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(20.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column(modifier = Modifier.weight(1f)) {
//                        Text(
//                            text = "Reportes Activos",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color(0xFF1A1A1A)
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            text = "En tu área",
//                            fontSize = 13.sp,
//                            color = Color.Gray
//                        )
//                    }
//                    Box(
//                        modifier = Modifier
//                            .size(50.dp)
//                            .clip(CircleShape)
//                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "12",
//                            fontSize = 20.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//// Vista previa
//@Preview(showBackground = true)
//@Composable
//fun WelcomeScreenPreview() {
//    MaterialTheme {
//        WelcomeScreen()
//    }
//}