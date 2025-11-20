// --------------------- IMPORTS ---------------------
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --------------------- COLOR PRINCIPAL ---------------------
val GreenMain = Color(0xFF2ECC71)
val LightGreen = Color(0xFFB1F5C8)
val SoftGray = Color(0xFFF5F5F5)
val TextGray = Color(0xFF5A5A5A)

// --------------------- SCREEN ---------------------
@Composable
fun MyReportsScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftGray)
            .padding(16.dp)
    ) {

        // --------------------- HEADER ---------------------
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.ChatBubble,
                contentDescription = null,
                tint = GreenMain,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Mis Reportes",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = GreenMain
            )
        }

        Spacer(Modifier.height(20.dp))

        // --------------------- BUSCADOR ---------------------
        var search by remember { mutableStateOf("") }

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar reportes…", color = TextGray) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = GreenMain)
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = GreenMain.copy(alpha = 0.3f),
                focusedBorderColor = GreenMain,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(Modifier.height(24.dp))

        // --------------------- LISTA ---------------------
        val reports = List(4) { "Reporte $it" }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(reports) { ReportCardNew() }
        }
    }
}

// --------------------- NUEVA TARJETA ---------------------
@Composable
fun ReportCardNew() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Column(modifier = Modifier.padding(18.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                // ICONO CIRCULAR
                Box(
                    modifier = Modifier
                        .size(55.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(GreenMain, LightGreen)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Tipo de reporte",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )

                    var tipo by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = tipo,
                        onValueChange = { tipo = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej: Basura, accidente…", color = TextGray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = GreenMain.copy(alpha = 0.25f),
                            focusedBorderColor = GreenMain,
                            unfocusedTextColor = Color.Black,
                            focusedTextColor = Color.Black
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "Descripción",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )

            var desc by remember { mutableStateOf("") }

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Explica el problema con detalles…", color = TextGray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = GreenMain.copy(alpha = 0.25f),
                    focusedBorderColor = GreenMain,
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
            )

            Spacer(Modifier.height(20.dp))

            // ESTADO BADGE
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .background(GreenMain, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "En progreso",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// --------------------- PREVIEWS ---------------------
@Preview(showBackground = true)
@Composable
fun PreviewCard() {
    ReportCardNew()
}

@Preview(showBackground = true)
@Composable
fun PreviewScreen() {
    MyReportsScreen()
}
