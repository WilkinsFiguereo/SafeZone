package com.wilkins.safezone.frontend.ui.user.Screens.RecordResports

import SessionManager.getUserProfile
import android.content.Context
import android.util.Log
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
import androidx.navigation.NavController
import com.wilkins.safezone.GenericUserUi.SideMenu
import com.wilkins.safezone.backend.network.AppUser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

// Modelo de datos para el reporte
@Serializable
data class ReportHistory(
    val id: String,
    val id_affair: Int,
    val description: String,
    val image_url: String?,
    val user_id: String,
    val is_anonymous: Boolean,
    val user_name: String?,
    val report_location: String?,
    val id_reporting_status: Int,
    val created_at: String,
    val affair_type: String? = null
)

// Modelo para los asuntos
@Serializable
data class Affair(
    val id: Int,
    val type: String
)

@Composable
fun ReportHistoryScreen(
    navController: NavController,
    userId: String,
    context: Context,
    supabaseClient: SupabaseClient
) {
    val scope = rememberCoroutineScope()
    var isMenuOpen by remember { mutableStateOf(false) }
    var reports by remember { mutableStateOf<List<ReportHistory>>(emptyList()) }
    var affairs by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val userState = produceState<AppUser?>(initialValue = null) {
        value = getUserProfile(context)
    }
    val user = userState.value

    // Cargar asuntos y reportes al iniciar
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                Log.d("ReportHistory", "🔄 Iniciando carga de datos...")
                Log.d("ReportHistory", "📋 User ID recibido: $userId")

                // Cargar asuntos primero
                val affairsList = getAffairs(supabaseClient)
                affairs = affairsList.associate { it.id to it.type }
                Log.d("ReportHistory", "✅ Asuntos cargados: ${affairs.size}")
                affairs.forEach { (id, type) ->
                    Log.d("ReportHistory", "   - Affair ID: $id, Type: $type")
                }

                // Luego cargar reportes del usuario específico
                loadUserReports(userId, supabaseClient) { result, error ->
                    isLoading = false
                    if (result != null) {
                        Log.d("ReportHistory", "✅ Reportes del usuario obtenidos: ${result.size}")
                        // Mapear los reportes con el tipo de asunto
                        reports = result.map { report ->
                            val affairType = affairs[report.id_affair]
                            Log.d("ReportHistory", "   - Reporte ${report.id.take(8)}: affair_id=${report.id_affair}, type=$affairType")
                            report.copy(affair_type = affairType)
                        }
                    } else {
                        Log.e("ReportHistory", "❌ Error: $error")
                        errorMessage = error
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportHistory", "❌ Error cargando datos: ${e.message}", e)
                isLoading = false
                errorMessage = "Error al cargar datos: ${e.message}"
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Contenido de reportes
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Espaciador para la altura del TopBar
            Spacer(modifier = Modifier.height(75.dp))

            when {
                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Cargando reportes...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Error al cargar reportes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    loadUserReports(userId, supabaseClient) { result, error ->
                                        isLoading = false
                                        if (result != null) {
                                            reports = result.map { report ->
                                                report.copy(affair_type = affairs[report.id_affair])
                                            }
                                        } else {
                                            errorMessage = error
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reintentar")
                        }
                    }
                }

                reports.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Sin reportes",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "No tienes reportes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tus reportes aparecerán aquí una vez que realices tu primera alerta",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5))
                            .padding(16.dp)
                    ) {
                        // Header con estadísticas
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "Mis Reportes",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${reports.size} ${if (reports.size == 1) "reporte registrado" else "reportes registrados"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Lista de reportes
                        reports.forEach { report ->
                            EnhancedReportCard(report = report)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        // Menú superior (último, para que quede por encima)
        SideMenu(
            navController = navController,
            userId = userId,
            userName = user?.name ?: "Usuario",
            currentRoute = "RecordReports/$userId",
            isMenuOpen = isMenuOpen,
            onMenuToggle = { isMenuOpen = it },
            context = context,
            supabaseClient = supabaseClient,
            modifier = Modifier.align(Alignment.TopCenter)
        )

    }
}

@Composable
fun EnhancedReportCard(report: ReportHistory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header con ID y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Reporte #${report.id.take(8).uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDate(report.created_at),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusChip(statusId = report.id_reporting_status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tipo de asunto destacado
            if (!report.affair_type.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tipo de asunto",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                            Text(
                                text = report.affair_type,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Descripción
            Column {
                Text(
                    text = "Descripción",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }

            // Ubicación (si existe)
            if (!report.report_location.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = report.report_location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(statusId: Int) {
    val (backgroundColor, textColor, text, icon) = when (statusId) {
        1 -> Quadruple(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            "Pendiente",
            Icons.Default.Schedule
        )
        2 -> Quadruple(
            Color(0xFFE3F2FD),
            Color(0xFF1565C0),
            "Revisado",
            Icons.Default.Visibility
        )
        3 -> Quadruple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            "Resuelto",
            Icons.Default.CheckCircle
        )
        4 -> Quadruple(
            Color(0xFFFFEBEE),
            Color(0xFFC62828),
            "Rechazado",
            Icons.Default.Cancel
        )
        else -> Quadruple(
            Color(0xFFF5F5F5),
            Color(0xFF616161),
            "Desconocido",
            Icons.Default.Help
        )
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = textColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "ES"))
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

suspend fun getAffairs(client: SupabaseClient): List<Affair> {
    Log.d("ReportHistory", "🔍 Iniciando consulta a tabla 'affair'...")
    try {
        val result = client.postgrest
            .from("affair")
            .select()
            .decodeList<Affair>()

        Log.d("ReportHistory", "✅ Consulta exitosa, affairs obtenidos: ${result.size}")
        result.forEach { affair ->
            Log.d("ReportHistory", "   Affair - ID: ${affair.id}, Type: ${affair.type}")
        }
        return result
    } catch (e: Exception) {
        Log.e("ReportHistory", "❌ Error en getAffairs: ${e.message}", e)
        throw e
    }
}

suspend fun loadUserReports(
    userId: String,
    client: SupabaseClient,
    callback: (List<ReportHistory>?, String?) -> Unit
) {
    try {
        Log.d("ReportHistory", "🔍 Iniciando carga de reportes para userId: $userId")

        if (userId.isBlank()) {
            Log.e("ReportHistory", "❌ userId está vacío")
            callback(null, "ID de usuario inválido")
            return
        }

        // Usar el filtro de Supabase directamente en la consulta
        val reports = client.postgrest
            .from("reports")
            .select()
            .decodeList<ReportHistory>()
            .filter { it.user_id == userId }  // Filtrar por el userId específico
            .sortedByDescending { it.created_at }

        Log.d("ReportHistory", "✅ Reportes obtenidos para el usuario: ${reports.size}")

        if (reports.isEmpty()) {
            Log.w("ReportHistory", "⚠️ No se encontraron reportes para este usuario")
        } else {
            reports.forEach { report ->
                Log.d("ReportHistory", "   - Reporte: ${report.id.take(8)}, User: ${report.user_id.take(8)}, Affair: ${report.id_affair}, Status: ${report.id_reporting_status}")
            }
        }

        callback(reports, null)

    } catch (e: Exception) {
        Log.e("ReportHistory", "❌ Error cargando reportes: ${e.message}", e)
        Log.e("ReportHistory", "❌ Tipo de excepción: ${e.javaClass.simpleName}")
        callback(null, "Error al cargar reportes: ${e.message}")
    }
}