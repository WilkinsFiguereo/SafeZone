package com.wilkins.safezone.frontend.ui.Admin.Screens.PDF

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.Admin.PDF.PDFGeneratorService
import com.wilkins.safezone.backend.network.SupabaseService
import com.wilkins.safezone.backend.network.auth.SessionManager
import com.wilkins.safezone.frontend.ui.Admin.Screens.PDF.components.OverlayMenu
import com.wilkins.safezone.frontend.ui.Admin.Screens.PDF.components.ReportItem
import com.wilkins.safezone.frontend.ui.Admin.Screens.PDF.components.ReportSection
import com.wilkins.safezone.ui.theme.NameApp
import com.wilkins.safezone.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

// Data classes para el menú
data class MenuItem(
    val icon: ImageVector,
    val title: String,
    val route: String
)

data class MenuSection(
    val title: String,
    val items: List<MenuItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PDFScreen(navController: NavController) {
    var showMenu by remember { mutableStateOf(false) }
    var showSideMenu by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val supabaseClient = SupabaseService.getInstance()
    val pdfGenerator = remember { PDFGeneratorService(context) }

    // Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            snackbarMessage = "Se requiere permiso de almacenamiento para generar el PDF"
            showSnackbar = true
        }
    }

    // Verificar y solicitar permisos si es necesario
    fun checkAndRequestPermission(onGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ no necesita permiso para Downloads
            onGranted()
        } else {
            // Android 9 y anteriores
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            onGranted()
        }
    }

    // Función para generar reporte de usuarios
    fun generateUserReport(reportType: String) {
        checkAndRequestPermission {
            scope.launch {
                isGenerating = true
                try {
                    val result = pdfGenerator.generateUserReport(reportType)
                    result.onSuccess { file ->
                        snackbarMessage = "PDF generado: ${file.name}"
                        showSnackbar = true

                        // Abrir el PDF automáticamente
                        try {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            snackbarMessage = "PDF generado en: ${file.absolutePath}"
                            showSnackbar = true
                        }
                    }.onFailure { error ->
                        snackbarMessage = "Error: ${error.message}"
                        showSnackbar = true
                    }
                } finally {
                    isGenerating = false
                }
            }
        }
    }

    // Función para generar reporte de incidencias
    fun generateIncidentReport(reportType: String) {
        checkAndRequestPermission {
            scope.launch {
                isGenerating = true
                try {
                    val result = pdfGenerator.generateReportReport(reportType)
                    result.onSuccess { file ->
                        snackbarMessage = "PDF generado: ${file.name}"
                        showSnackbar = true

                        // Abrir el PDF automáticamente
                        try {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            snackbarMessage = "PDF generado en: ${file.absolutePath}"
                            showSnackbar = true
                        }
                    }.onFailure { error ->
                        snackbarMessage = "Error: ${error.message}"
                        showSnackbar = true
                    }
                } finally {
                    isGenerating = false
                }
            }
        }
    }

    // Función para generar reporte de categorías
    fun generateCategoryReport(reportType: String) {
        checkAndRequestPermission {
            scope.launch {
                isGenerating = true
                try {
                    val result = pdfGenerator.generateCategoryReport(reportType)
                    result.onSuccess { file ->
                        snackbarMessage = "PDF generado: ${file.name}"
                        showSnackbar = true

                        // Abrir el PDF automáticamente
                        try {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            snackbarMessage = "PDF generado en: ${file.absolutePath}"
                            showSnackbar = true
                        }
                    }.onFailure { error ->
                        snackbarMessage = "Error: ${error.message}"
                        showSnackbar = true
                    }
                } finally {
                    isGenerating = false
                }
            }
        }
    }

    // Definir las secciones del menú lateral
    val menuSections = listOf(
        MenuSection(
            title = "Dashboard",
            items = listOf(
                MenuItem(Icons.Default.Dashboard, "Dashboard", "DashboardAdmin")
            )
        ),
        MenuSection(
            title = "Gestión de Usuarios",
            items = listOf(
                MenuItem(Icons.Default.People, "Lista de Usuarios", "crudUsuarios"),
                MenuItem(Icons.Default.PersonOff, "Usuarios Deshabilitados", "crudUsuariosDisabled")
            )
        ),
        MenuSection(
            title = "Reportes",
            items = listOf(
                MenuItem(Icons.Default.PictureAsPdf, "Generar Reportes PDF", "PDF"),
                MenuItem(Icons.Default.Pending, "Reportes Pendientes", "pending_reports"),
                MenuItem(Icons.Default.Update, "Reportes en Proceso", "in_progress_reports"),
                MenuItem(Icons.Default.CheckCircle, "Reportes Completados", "completed_reports"),
                MenuItem(Icons.Default.Cancel, "Reportes Cancelados", "cancelled_reports")
            )
        ),
        MenuSection(
            title = "Categorías",
            items = listOf(
                MenuItem(Icons.Default.Category, "Categorías de Incidencias", "incident_categories"),
                MenuItem(Icons.Default.BusinessCenter, "Incidencias", "affair_categories")
            )
        ),
        MenuSection(
            title = "Configuración",
            items = listOf(
                MenuItem(Icons.Default.Settings, "Configuración", "admin_settings"),
                MenuItem(Icons.Default.Logout, "Cerrar Sesión", "logout")
            )
        )
    )

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar(
                message = snackbarMessage,
                duration = SnackbarDuration.Long
            )
            showSnackbar = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Surface(
                    shadowElevation = 4.dp,
                    color = PrimaryColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showSideMenu = !showSideMenu }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Generador de Reportes",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "PDF",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }

                        Box {
                            IconButton(onClick = { showMenu = !showMenu }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menú de opciones",
                                    tint = Color.White
                                )
                            }

                            OverlayMenu(
                                expanded = showMenu,
                                onDismiss = { showMenu = false }
                            )
                        }
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Header informativo
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Selecciona el tipo de reporte",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Los reportes se generarán en formato PDF",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Contenido scrolleable
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Sección de Usuarios
                        ReportSection(
                            title = "Reportes de Usuarios",
                            icon = Icons.Default.Person,
                            reports = listOf(
                                ReportItem("Usuarios en General", Icons.Default.People),
                                ReportItem("Usuarios Regulares", Icons.Default.Person),
                                ReportItem("Usuarios Admin", Icons.Default.AdminPanelSettings),
                                ReportItem("Usuarios Moderadores", Icons.Default.Verified),
                                ReportItem("Usuarios Asociación Gubernamental", Icons.Default.AccountBalance)
                            ),
                            onReportClick = { reportName ->
                                generateUserReport(reportName)
                            }
                        )

                        // Sección de Incidencias
                        ReportSection(
                            title = "Reportes de Incidencias",
                            icon = Icons.Default.Report,
                            reports = listOf(
                                ReportItem("Reportes en General", Icons.Default.Assignment),
                                ReportItem("Reportes Pendientes", Icons.Default.PendingActions),
                                ReportItem("Reportes en Proceso", Icons.Default.Autorenew),
                                ReportItem("Reportes Completados", Icons.Default.CheckCircle),
                                ReportItem("Reportes Cancelados", Icons.Default.Cancel),
                                ReportItem("Reportes Anónimos", Icons.Default.VisibilityOff)
                            ),
                            onReportClick = { reportName ->
                                generateIncidentReport(reportName)
                            }
                        )

                        // Sección de Categorías (AHORA FUNCIONAL)
                        ReportSection(
                            title = "Reportes de Categorías",
                            icon = Icons.Default.Category,
                            reports = listOf(
                                ReportItem("Categorías de Incidencia", Icons.Default.Label),
                                ReportItem("Incidencias por Categoría", Icons.Default.Inventory)
                            ),
                            onReportClick = { reportName ->
                                generateCategoryReport(reportName)
                            }
                        )

                        // Resto de secciones (placeholder por ahora)
                        ReportSection(
                            title = "Reportes de Noticias",
                            icon = Icons.Default.Article,
                            reports = listOf(
                                ReportItem("Todas las Noticias", Icons.Default.Newspaper),
                                ReportItem("Noticias Publicadas", Icons.Default.Publish),
                                ReportItem("Noticias por Categoría", Icons.Default.Category)
                            ),
                            onReportClick = { reportName ->
                                snackbarMessage = "Funcionalidad en desarrollo: $reportName"
                                showSnackbar = true
                            }
                        )

                        ReportSection(
                            title = "Reportes de Encuestas",
                            icon = Icons.Default.Poll,
                            reports = listOf(
                                ReportItem("Todas las Encuestas", Icons.Default.BarChart),
                                ReportItem("Encuestas Activas", Icons.Default.TrendingUp),
                                ReportItem("Resultados de Encuestas", Icons.Default.Assessment)
                            ),
                            onReportClick = { reportName ->
                                snackbarMessage = "Funcionalidad en desarrollo: $reportName"
                                showSnackbar = true
                            }
                        )

                        ReportSection(
                            title = "Estadísticas de la App",
                            icon = Icons.Default.Analytics,
                            reports = listOf(
                                ReportItem("Estadísticas Generales", Icons.Default.Timeline),
                                ReportItem("Uso de la Aplicación", Icons.Default.PhoneAndroid),
                                ReportItem("Métricas de Rendimiento", Icons.Default.Speed)
                            ),
                            onReportClick = { reportName ->
                                snackbarMessage = "Funcionalidad en desarrollo: $reportName"
                                showSnackbar = true
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Indicador de carga
                if (isGenerating) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.padding(32.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(color = PrimaryColor)
                                Text(
                                    text = "Generando PDF...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Por favor espere",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Menú lateral superpuesto
        AnimatedVisibility(
            visible = showSideMenu,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            )
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight(),
                    color = PrimaryColor,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = PrimaryColor.copy(alpha = 0.9f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AdminPanelSettings,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "Panel Admin",
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Administrador",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.2f),
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 8.dp)
                        ) {
                            menuSections.forEach { section ->
                                AdminMenuSection(
                                    section = section,
                                    currentRoute = "PDF",
                                    onItemClick = { route ->
                                        showSideMenu = false

                                        if (route == "logout") {
                                            scope.launch {
                                                try {
                                                    SessionManager.logout(context, supabaseClient)
                                                    navController.navigate("login") {
                                                        popUpTo(0) { inclusive = true }
                                                    }
                                                } catch (e: Exception) {
                                                    snackbarMessage = "Error al cerrar sesión"
                                                    showSnackbar = true
                                                }
                                            }
                                        } else {
                                            navController.navigate(route)
                                        }
                                    }
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = PrimaryColor.copy(alpha = 0.9f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.2f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "SafeZone Admin",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Versión 1.0.0",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 10.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showSideMenu = false }
                )
            }
        }
    }
}

@Composable
fun AdminMenuSection(
    section: MenuSection,
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = section.title,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        section.items.forEach { item ->
            val isSelected = currentRoute == item.route

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                color = if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(item.route) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item.title,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}