package com.wilkins.safezone.frontend.ui.user.Form

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.safezone.GenericUserUi.SideMenu
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.wilkins.safezone.ui.theme.PrimaryColor
import io.github.jan.supabase.SupabaseClient
import com.wilkins.safezone.backend.network.User.Form.Affair
import com.wilkins.safezone.backend.network.User.Form.getAffairs
import com.wilkins.safezone.bridge.User.Form.ReportRepository
import com.wilkins.safezone.frontend.ui.Map.GoogleMapPicker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    navController: NavController,
    userId: String,
    userName: String,
    supabaseClient: SupabaseClient,
    modifier: Modifier = Modifier
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reportRepository = remember { ReportRepository(context) }

    // Estados del formulario
    var affairSeleccionado by remember { mutableStateOf<Affair?>(null) }
    var descripcion by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var evidenciaSubida by remember { mutableStateOf(false) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var showMapPicker by remember { mutableStateOf(false) }
    var isAnonymous by remember { mutableStateOf(false) }
    var mediaType by remember { mutableStateOf<String?>(null) } // "image" o "video"
    var mediaFileName by remember { mutableStateOf<String?>(null) }
    var showMediaOptions by remember { mutableStateOf(false) }

    // Lista de affairs desde la BD
    var affairsList by remember { mutableStateOf<List<Affair>>(emptyList()) }
    var isLoadingAffairs by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estado del snackbar
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    // Estado para el dropdown de asunto
    var affairExpandido by remember { mutableStateOf(false) }

    // Launcher para imágenes
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        if (inputStream == null) {
                            snackbarMessage = "No se pudo leer la imagen"
                            showSnackbar = true
                            return@launch
                        }

                        val bytes = inputStream.readBytes()
                        inputStream.close()

                        // Obtener nombre del archivo
                        val cursor = context.contentResolver.query(uri, null, null, null, null)
                        val fileName = cursor?.use {
                            if (it.moveToFirst()) {
                                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                                if (nameIndex != -1) it.getString(nameIndex) else "imagen.jpg"
                            } else "imagen.jpg"
                        } ?: "imagen.jpg"

                        imageBytes = bytes
                        mediaType = "image"
                        mediaFileName = fileName
                        evidenciaSubida = true

                        snackbarMessage = "Imagen seleccionada: $fileName"
                        showSnackbar = true

                    } catch (e: Exception) {
                        snackbarMessage = "Error al seleccionar imagen: ${e.message}"
                        showSnackbar = true
                    }
                }
            }
        }
    )

    // Launcher para videos
    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        if (inputStream == null) {
                            snackbarMessage = "No se pudo leer el video"
                            showSnackbar = true
                            return@launch
                        }

                        val bytes = inputStream.readBytes()
                        inputStream.close()

                        // Verificar tamaño del video (límite: 50MB)
                        val maxSize = 50 * 1024 * 1024 // 50MB en bytes
                        if (bytes.size > maxSize) {
                            snackbarMessage = "El video es demasiado grande. Máximo 50MB"
                            showSnackbar = true
                            return@launch
                        }

                        // Obtener nombre del archivo
                        val cursor = context.contentResolver.query(uri, null, null, null, null)
                        val fileName = cursor?.use {
                            if (it.moveToFirst()) {
                                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                                if (nameIndex != -1) it.getString(nameIndex) else "video.mp4"
                            } else "video.mp4"
                        } ?: "video.mp4"

                        imageBytes = bytes
                        mediaType = "video"
                        mediaFileName = fileName
                        evidenciaSubida = true

                        val sizeMB = bytes.size / (1024f * 1024f)
                        snackbarMessage = "Video seleccionado: $fileName (${String.format("%.2f", sizeMB)} MB)"
                        showSnackbar = true

                    } catch (e: Exception) {
                        snackbarMessage = "Error al seleccionar video: ${e.message}"
                        showSnackbar = true
                    }
                }
            }
        }
    )

    // Cargar affairs al iniciar
    LaunchedEffect(Unit) {
        try {
            android.util.Log.d("FormScreen", "🔄 Iniciando carga de affairs...")
            android.util.Log.d("FormScreen", "🔌 Supabase Client: ${supabaseClient.supabaseUrl}")
            affairsList = getAffairs(supabaseClient)
            android.util.Log.d("FormScreen", "✅ Affairs cargados exitosamente: ${affairsList.size} items")
            if (affairsList.isEmpty()) {
                android.util.Log.w("FormScreen", "⚠️ La lista de affairs está vacía - posible problema de RLS o permisos")
            }
            affairsList.forEach { affair ->
                android.util.Log.d("FormScreen", "📋 Affair ID: ${affair.id}, Type: ${affair.type}")
            }
            isLoadingAffairs = false
        } catch (e: Exception) {
            android.util.Log.e("FormScreen", "❌ Error al cargar affairs: ${e.message}")
            android.util.Log.e("FormScreen", "❌ Stack trace: ", e)
            errorMessage = "Error al cargar tipos de incidencia: ${e.message}"
            isLoadingAffairs = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Reportar Incidencia",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { isMenuOpen = !isMenuOpen }
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menú",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = PrimaryColor
                    )
                )
            },
            snackbarHost = {
                if (showSnackbar) {
                    Snackbar(
                        action = {
                            TextButton(onClick = { showSnackbar = false }) {
                                Text("OK")
                            }
                        },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(snackbarMessage)
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
            ) {

                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color(0xFF1B5E20), PrimaryColor)
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(24.dp)
                    ) {
                        Text(
                            "Reporta una incidencia",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Tu reporte ayuda a mejorar la comunidad",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier
                            .size(90.dp)
                            .align(Alignment.CenterEnd)
                            .padding(end = 24.dp)
                    )
                }

                // Tarjeta del formulario
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {

                        // Tipo de incidencia - Dropdown dinámico
                        Text(
                            "Tipo de incidencia *",
                            color = PrimaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        if (isLoadingAffairs) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = PrimaryColor
                                )
                            }
                        } else if (errorMessage != null) {
                            Text(
                                errorMessage!!,
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { affairExpandido = true }
                                    .background(
                                        color = if (affairSeleccionado != null) PrimaryColor.copy(alpha = 0.1f)
                                        else Color(0xFFF5F5F5),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (affairSeleccionado != null) PrimaryColor
                                        else Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 18.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = affairSeleccionado?.type ?: "Selecciona el tipo de incidencia",
                                        color = if (affairSeleccionado != null) PrimaryColor
                                        else Color(0xFF9E9E9E),
                                        fontSize = 15.sp
                                    )
                                    Icon(
                                        imageVector = if (affairExpandido) Icons.Default.ArrowDropUp
                                        else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = PrimaryColor
                                    )
                                }
                            }

                            if (showMapPicker) {
                                GoogleMapPicker(
                                    onLocationSelected = { direccionSeleccionada ->
                                        direccion = direccionSeleccionada
                                    },
                                    onDismiss = {
                                        showMapPicker = false
                                    }
                                )
                            }

                            if (affairExpandido) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        affairsList.forEach { affair ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        affairSeleccionado = affair
                                                        affairExpandido = false
                                                    }
                                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Circle,
                                                    contentDescription = null,
                                                    tint = if (affairSeleccionado?.id == affair.id) PrimaryColor
                                                    else Color(0xFFE0E0E0),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = affair.type,
                                                    color = if (affairSeleccionado?.id == affair.id) PrimaryColor
                                                    else Color(0xFF424242),
                                                    fontWeight = if (affairSeleccionado?.id == affair.id) FontWeight.Medium
                                                    else FontWeight.Normal,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            if (affair != affairsList.last()) {
                                                Divider(
                                                    modifier = Modifier.padding(horizontal = 16.dp),
                                                    thickness = 0.5.dp,
                                                    color = Color(0xFFEEEEEE)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Ubicación
                        Text(
                            "Ubicación *",
                            color = PrimaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showMapPicker = true },
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(18.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Mapa",
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Seleccionar en el mapa",
                                        color = PrimaryColor,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (direccion.isEmpty()) "Toca para elegir la ubicación"
                                        else direccion,
                                        color = PrimaryColor,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }



                        Spacer(modifier = Modifier.height(20.dp))

                        // Descripción
                        Text(
                            "Descripción detallada *",
                            color = PrimaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            placeholder = {
                                Text("Describe la situación con todos los detalles relevantes.")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            leadingIcon = {
                                Icon(Icons.Default.Description, null, tint = PrimaryColor)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        // Checkbox para reporte anónimo
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isAnonymous = !isAnonymous }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isAnonymous,
                                onCheckedChange = { isAnonymous = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = PrimaryColor
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Enviar reporte de forma anónima",
                                color = Color(0xFF424242),
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // Sección de evidencia multimedia (ACTUALIZADA)
                        // Reemplaza solo la sección de evidencia multimedia y botones de acción

// Dentro de Column (línea ~520 aprox), reemplaza desde "Sección de evidencia multimedia" hasta el final de los botones:

// Sección de evidencia multimedia - BOTÓN UNIFICADO
                        Text(
                            "Evidencia multimedia",
                            color = PrimaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        if (!evidenciaSubida) {
                            // Botón unificado con opciones
                            var showMediaDialog by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showMediaDialog = true },
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Agregar multimedia",
                                        tint = PrimaryColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            "Agregar foto o video",
                                            color = PrimaryColor,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            "Opcional - Ayuda a validar tu reporte",
                                            color = Color(0xFF757575),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            // Diálogo para elegir tipo de archivo
                            if (showMediaDialog) {
                                AlertDialog(
                                    onDismissRequest = { showMediaDialog = false },
                                    title = {
                                        Text(
                                            "Seleccionar evidencia",
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    text = {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Opción: Foto
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        showMediaDialog = false
                                                        imageLauncher.launch("image/*")
                                                    },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = PrimaryColor.copy(alpha = 0.1f)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(20.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Photo,
                                                        contentDescription = null,
                                                        tint = PrimaryColor,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(16.dp))
                                                    Column {
                                                        Text(
                                                            "Seleccionar foto",
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 16.sp
                                                        )
                                                        Text(
                                                            "Formatos: JPG, PNG, etc.",
                                                            fontSize = 13.sp,
                                                            color = Color(0xFF757575)
                                                        )
                                                    }
                                                }
                                            }

                                            // Opción: Video
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        showMediaDialog = false
                                                        videoLauncher.launch("video/*")
                                                    },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = PrimaryColor.copy(alpha = 0.1f)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(20.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.VideoLibrary,
                                                        contentDescription = null,
                                                        tint = PrimaryColor,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(16.dp))
                                                    Column {
                                                        Text(
                                                            "Seleccionar video",
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 16.sp
                                                        )
                                                        Text(
                                                            "Máximo 50MB",
                                                            fontSize = 13.sp,
                                                            color = Color(0xFF757575)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {},
                                    dismissButton = {
                                        TextButton(onClick = { showMediaDialog = false }) {
                                            Text("Cancelar")
                                        }
                                    }
                                )
                            }
                        } else {
                            // Card mostrando archivo subido
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(20.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (mediaType == "image") Icons.Default.Photo
                                            else Icons.Default.VideoLibrary,
                                            contentDescription = null,
                                            tint = PrimaryColor,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (mediaType == "image") "Imagen adjunta"
                                                else "Video adjunto",
                                                color = PrimaryColor,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = mediaFileName ?: "Archivo",
                                                color = PrimaryColor.copy(alpha = 0.7f),
                                                fontSize = 12.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    // Botón para eliminar
                                    IconButton(
                                        onClick = {
                                            evidenciaSubida = false
                                            imageBytes = null
                                            mediaType = null
                                            mediaFileName = null
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

// Botones de acción
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    affairSeleccionado = null
                                    descripcion = ""
                                    direccion = ""
                                    evidenciaSubida = false
                                    imageBytes = null
                                    isAnonymous = false
                                    mediaType = null
                                    mediaFileName = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Limpiar",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }

                            Button(
                                onClick = {
                                    if (affairSeleccionado == null) {
                                        snackbarMessage = "Selecciona un tipo de incidencia"
                                        showSnackbar = true
                                        return@Button
                                    }
                                    if (descripcion.isBlank()) {
                                        snackbarMessage = "La descripción es obligatoria"
                                        showSnackbar = true
                                        return@Button
                                    }
                                    if (direccion.isBlank()) {
                                        snackbarMessage = "La ubicación es obligatoria"
                                        showSnackbar = true
                                        return@Button
                                    }

                                    scope.launch {
                                        try {
                                            val result = reportRepository.createReportBridge(
                                                description = descripcion,
                                                imageBytes = imageBytes,
                                                isAnonymous = isAnonymous,
                                                reportLocation = direccion,
                                                affairId = affairSeleccionado!!.id,
                                                mediaType = mediaType,
                                                mediaFileName = mediaFileName
                                            )

                                            result.fold(
                                                onSuccess = {
                                                    // Navegar a pantalla de éxito
                                                    navController.navigate("reportResult/$userId/true/success") {
                                                        popUpTo("formScreen") { inclusive = true }
                                                    }
                                                },
                                                onFailure = { error ->
                                                    // Navegar a pantalla de error
                                                    val errorMsg = error.message ?: "Error desconocido"
                                                    navController.navigate("reportResult/$userId/false/$errorMsg") {
                                                        popUpTo("formScreen") { inclusive = true }
                                                    }
                                                }
                                            )
                                        } catch (e: Exception) {
                                            // Navegar a pantalla de error
                                            val errorMsg = e.message ?: "Error desconocido"
                                            navController.navigate("reportResult/$userId/false/$errorMsg") {
                                                popUpTo("formScreen") { inclusive = true }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryColor
                                ),
                                shape = RoundedCornerShape(14.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 2.dp
                                )
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Enviar Reporte",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        // Mensaje informativo
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    "Tu reporte será revisado y atendido por las autoridades correspondientes en un plazo máximo de 48 horas.",
                                    color = Color(0xFF1976D2),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Side Menu
        SideMenu(
            navController = navController,
            userId = userId,
            userName = userName,
            currentRoute = "formScreen",
            modifier = Modifier.matchParentSize(),
            isMenuOpen = isMenuOpen,
            onMenuToggle = { isOpen ->
                isMenuOpen = isOpen
            },
            context = context,
            supabaseClient = supabaseClient
        )
    }
}