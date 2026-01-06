package com.wilkins.safezone.frontend.ui.user.Screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.wilkins.safezone.GenericUserUi.SideMenu
import com.wilkins.safezone.backend.network.User.Form.Affair
import com.wilkins.safezone.backend.network.User.Profile.ProfileViewModel
import com.wilkins.safezone.ui.theme.PrimaryColor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

// Data classes para los reportes
@Serializable
data class UserReport(
    val id: String,
    val id_affair: Int,
    val description: String,
    val image_url: String?,
    val user_id: String,
    val is_anonymous: Boolean,
    val user_name: String?,
    val report_location: String?,
    val id_reporting_status: Int,
    val created_at: String
)

@Serializable
data class ReportWithAffair(
    val id: String,
    val description: String,
    val id_reporting_status: Int,
    val created_at: String,
    val affairType: String,
    val report_location: String?
)


@Composable
fun ProfileScreenWithMenu(
    userId: String,
    userName: String,
    navController: NavController,
    supabaseClient: SupabaseClient,
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToChangeEmail: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    var isMenuOpen by remember { mutableStateOf(false) }
    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""

    Box(modifier = modifier.fillMaxSize()) {
        // Contenido principal del perfil
        Column(modifier = Modifier.fillMaxSize()) {
            // Espaciador para la altura del TopBar del SideMenu
            Spacer(modifier = Modifier.height(56.dp))

            // Profile Content
            ProfileScreen(
                userId = userId,
                supabaseClient = supabaseClient,
                onNavigateToChangePassword = onNavigateToChangePassword,
                onNavigateToChangeEmail = onNavigateToChangeEmail,
                viewModel = viewModel
            )
        }

        // SideMenu genérico (último, para que quede por encima)
        SideMenu(
            navController = navController,
            userId = userId,
            userName = userName,
            currentRoute = currentRoute,
            isMenuOpen = isMenuOpen,
            onMenuToggle = { isMenuOpen = it },
            context = context,
            supabaseClient = supabaseClient,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

// Componentes originales del perfil
@Composable
fun ProfileScreen(
    userId: String,
    supabaseClient: SupabaseClient,
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToChangeEmail: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var userReports by remember { mutableStateOf<List<ReportWithAffair>>(emptyList()) }
    var isLoadingReports by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        Log.d("ProfileScreen", "🔄 LaunchedEffect disparado para userId: ${userId.take(8)}")
        viewModel.loadProfile(context, userId)

        Log.d("ProfileScreen", "⏳ Esperando a que se cargue el perfil...")
    }

    // Cargar reportes cuando el perfil esté cargado y sea el perfil propio
    LaunchedEffect(uiState.isOwnProfile, userId) {
        if (uiState.isOwnProfile) {
            Log.d("ProfileScreen", "✅ Es el perfil propio, cargando reportes...")
            isLoadingReports = true
            scope.launch {
                try {
                    Log.d("ProfileScreen", "📡 Llamando a getUserReports...")
                    userReports = getUserReports(supabaseClient, userId)
                    Log.d("ProfileScreen", "✅ Reportes cargados en UI: ${userReports.size}")
                } catch (e: Exception) {
                    Log.e("ProfileScreen", "❌ Error cargando reportes en UI: ${e.message}", e)
                } finally {
                    isLoadingReports = false
                    Log.d("ProfileScreen", "🏁 Carga de reportes finalizada")
                }
            }
        } else {
            Log.d("ProfileScreen", "ℹ️ No es el perfil propio, omitiendo carga de reportes")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryColor
                )
            }
            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = uiState.error ?: "Error desconocido",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
            }
            uiState.userProfile != null -> {
                if (isEditing && uiState.isOwnProfile) {
                    EditProfileContent(
                        userProfile = uiState.userProfile!!,
                        viewModel = viewModel,
                        onSave = { name, phone, pronouns, description, address, statusId ->
                            viewModel.updateProfile(context, name, phone, pronouns, description, address, statusId)
                            isEditing = false
                        },
                        onCancel = { isEditing = false },
                        onNavigateToChangePassword = onNavigateToChangePassword,
                        onNavigateToChangeEmail = onNavigateToChangeEmail
                    )
                } else {
                    ProfileContent(
                        userProfile = uiState.userProfile!!,
                        isOwnProfile = uiState.isOwnProfile,
                        viewModel = viewModel,
                        userReports = userReports,
                        isLoadingReports = isLoadingReports,
                        onEditClick = { isEditing = true },
                        onFollowClick = { /* TODO: Implementar seguir */ }
                    )
                }
            }
        }
    }
}

suspend fun getUserReports(supabaseClient: SupabaseClient, userId: String): List<ReportWithAffair> {
    try {
        Log.d("ProfileReports", "🔍 Iniciando carga de reportes para userId: ${userId.take(8)}...")

        if (userId.isBlank()) {
            Log.e("ProfileReports", "❌ userId está vacío")
            return emptyList()
        }

        // Obtener TODOS los reportes primero para debugging
        val allReports = supabaseClient.postgrest
            .from("reports")
            .select()
            .decodeList<UserReport>()

        Log.d("ProfileReports", "📊 Total de reportes en la tabla: ${allReports.size}")

        // Filtrar reportes del usuario que NO sean anónimos
        val userReports = allReports.filter {
            val isUserReport = it.user_id == userId
            val isNotAnonymous = !it.is_anonymous

            Log.d("ProfileReports", "   Reporte ${it.id.take(8)}: user_match=$isUserReport, not_anon=$isNotAnonymous, user=${it.user_id.take(8)}")

            isUserReport && isNotAnonymous
        }

        Log.d("ProfileReports", "✅ Reportes del usuario (no anónimos): ${userReports.size}")

        if (userReports.isEmpty()) {
            Log.w("ProfileReports", "⚠️ No se encontraron reportes públicos para este usuario")
            Log.w("ProfileReports", "   Verifica que:")
            Log.w("ProfileReports", "   1. El userId es correcto: $userId")
            Log.w("ProfileReports", "   2. Existen reportes con is_anonymous=false")
            Log.w("ProfileReports", "   3. El user_id coincide exactamente")
            return emptyList()
        }

        // Obtener los tipos de asuntos
        Log.d("ProfileReports", "🔍 Cargando tipos de asuntos...")
        val affairs = supabaseClient.postgrest
            .from("affair")
            .select()
            .decodeList<Affair>()

        Log.d("ProfileReports", "✅ Asuntos cargados: ${affairs.size}")
        affairs.forEach { affair ->
            Log.d("ProfileReports", "   - Affair ID: ${affair.id}, Type: ${affair.type}")
        }

        // Combinar reportes con sus asuntos
        val reportsWithAffairs = userReports.map { report ->
            val affair = affairs.find { it.id == report.id_affair }
            val affairType = affair?.type ?: "Desconocido"

            Log.d("ProfileReports", "   Mapeando reporte ${report.id.take(8)}: affair_id=${report.id_affair} -> $affairType")

            ReportWithAffair(
                id = report.id,
                description = report.description,
                id_reporting_status = report.id_reporting_status,
                created_at = report.created_at,
                affairType = affairType,
                report_location = report.report_location
            )
        }.sortedByDescending { it.created_at }

        Log.d("ProfileReports", "✅ Reportes procesados exitosamente: ${reportsWithAffairs.size}")
        reportsWithAffairs.take(3).forEach { report ->
            Log.d("ProfileReports", "   📄 ${report.id.take(8)} - ${report.affairType} - Status: ${report.id_reporting_status}")
        }

        return reportsWithAffairs

    } catch (e: Exception) {
        Log.e("ProfileReports", "❌ Error cargando reportes: ${e.message}", e)
        Log.e("ProfileReports", "❌ Tipo de excepción: ${e.javaClass.simpleName}")
        Log.e("ProfileReports", "❌ Stack trace:", e)
        return emptyList()
    }
}


@Composable
fun ProfileContent(
    userProfile: com.wilkins.safezone.backend.network.AppUser,
    isOwnProfile: Boolean,
    viewModel: ProfileViewModel,
    userReports: List<ReportWithAffair>,
    isLoadingReports: Boolean,
    onEditClick: () -> Unit,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photoUrl = remember(userProfile.photoProfile) {
        viewModel.getProfilePhotoUrl(userProfile.photoProfile)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
    ) {
        // Header con gradiente
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryColor.copy(alpha = 0.8f),
                                PrimaryColor.copy(alpha = 0.5f)
                            )
                        )
                    )
                    .zIndex(0f)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 60.dp)
                    .padding(horizontal = 16.dp)
                    .zIndex(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(4.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUrl != null) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                PrimaryColor.copy(alpha = 0.2f),
                                                PrimaryColor.copy(alpha = 0.1f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile Picture",
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                        }
                    }

                    if (isOwnProfile) {
                        OutlinedButton(
                            onClick = onEditClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = PrimaryColor
                            ),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Editar perfil",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Button(
                            onClick = onFollowClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Seguir",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Espacio para compensar el offset
        Spacer(modifier = Modifier.height(80.dp))

        // Contenido del perfil
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = userProfile.name ?: "Usuario sin nombre",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1F1F),
                    lineHeight = 30.sp
                )

                if (!userProfile.pronouns.isNullOrEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = userProfile.pronouns,
                            fontSize = 12.sp,
                            color = PrimaryColor,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = getStatusColor(userProfile.status_id).copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(getStatusColor(userProfile.status_id))
                    )
                    Text(
                        text = getStatusText(userProfile.status_id),
                        fontSize = 13.sp,
                        color = getStatusColor(userProfile.status_id),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!userProfile.description.isNullOrEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8F9FA)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = userProfile.description,
                            fontSize = 14.sp,
                            color = Color(0xFF616161),
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Divider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Información de contacto",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )

                ContactInfoRow(
                    icon = Icons.Outlined.Email,
                    text = userProfile.email ?: "No especificado"
                )

                if (!userProfile.phone.isNullOrEmpty()) {
                    ContactInfoRow(
                        icon = Icons.Outlined.Phone,
                        text = userProfile.phone
                    )
                }

                if (!userProfile.address.isNullOrEmpty()) {
                    ContactInfoRow(
                        icon = Icons.Outlined.LocationOn,
                        text = userProfile.address
                    )
                }
            }

            // NUEVA SECCIÓN: Reportes del Usuario
            if (isOwnProfile) {
                Divider(
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                UserReportsSection(
                    reports = userReports,
                    isLoading = isLoadingReports
                )
            }

            // Espacio final para que se vea todo el contenido
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun UserReportsSection(
    reports: List<ReportWithAffair>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header de la sección
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Assignment,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Mis Reportes Públicos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1F1F)
                )
            }

            if (reports.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${reports.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Contenido
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PrimaryColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            reports.isEmpty() -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8F9FA)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AssignmentLate,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No hay reportes públicos",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Tus reportes públicos aparecerán aquí",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }
            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    reports.take(3).forEach { report ->
                        ReportCard(report = report)
                    }

                    if (reports.size > 3) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* TODO: Navegar a vista completa */ },
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryColor.copy(alpha = 0.05f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ver todos los reportes (${reports.size})",
                                    fontSize = 13.sp,
                                    color = PrimaryColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportCard(
    report: ReportWithAffair,
    modifier: Modifier = Modifier
) {
    val statusColor = when (report.id_reporting_status) {
        1 -> Color(0xFFFFA726) // Pendiente - Naranja
        2 -> Color(0xFF42A5F5) // Revisado - Azul
        3 -> Color(0xFF66BB6A) // Resuelto - Verde
        4 -> Color(0xFFEF5350) // Rechazado - Rojo
        else -> Color(0xFF9E9E9E) // Desconocido - Gris
    }

    val statusText = when (report.id_reporting_status) {
        1 -> "Pendiente"
        2 -> "Revisado"
        3 -> "Resuelto"
        4 -> "Rechazado"
        else -> "Desconocido"
    }

    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "ES"))
    }

    val formattedDate = try {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(report.created_at)
        date?.let { dateFormatter.format(it) } ?: report.created_at
    } catch (e: Exception) {
        report.created_at
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navegar a detalle del reporte */ },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header del reporte
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Reporte #${report.id.take(8).uppercase()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryColor.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = report.affairType,
                                    fontSize = 11.sp,
                                    color = PrimaryColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Descripción
            Text(
                text = report.description,
                fontSize = 13.sp,
                color = Color(0xFF616161),
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Footer con fecha y ubicación
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ver detalle",
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Ubicación si existe
                if (!report.report_location.isNullOrBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = report.report_location,
                            fontSize = 11.sp,
                            color = Color(0xFF9E9E9E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactInfoRow(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF8F9FA)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF9E9E9E),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                fontSize = 14.sp,
                color = Color(0xFF424242),
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(
    userProfile: com.wilkins.safezone.backend.network.AppUser,
    viewModel: ProfileViewModel,
    onSave: (String, String, String, String, String, Int) -> Unit,
    onCancel: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToChangeEmail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf(userProfile.name ?: "") }
    var phone by remember { mutableStateOf(userProfile.phone ?: "") }
    var pronouns by remember { mutableStateOf(userProfile.pronouns ?: "") }
    var description by remember { mutableStateOf(userProfile.description ?: "") }
    var address by remember { mutableStateOf(userProfile.address ?: "") }
    var selectedStatus by remember { mutableStateOf(userProfile.status_id ?: 1) }
    var showPhone by remember { mutableStateOf(true) }
    var showAddress by remember { mutableStateOf(true) }
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedPronouns by remember { mutableStateOf(false) }

    val photoUrl = remember(userProfile.photoProfile) {
        viewModel.getProfilePhotoUrl(userProfile.photoProfile)
    }

    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadProfilePhoto(context, it)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editar perfil",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F)
                    )
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF757575)
                        )
                    }
                }

                Divider(color = Color(0xFFE0E0E0))

                // Profile Photo Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, PrimaryColor.copy(alpha = 0.3f), CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isUploadingPhoto) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = PrimaryColor
                            )
                        } else if (photoUrl != null) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Cambiar foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Agregar foto",
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Toca para cambiar foto",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }

                Divider(color = Color(0xFFE0E0E0))

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre completo") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = PrimaryColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    )
                )

                // Phone Field with Privacy Toggle
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Phone,
                                contentDescription = null,
                                tint = PrimaryColor
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (showPhone) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (showPhone) "Visible para todos" else "Oculto",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        Switch(
                            checked = showPhone,
                            onCheckedChange = { showPhone = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryColor,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                }

                // Pronouns Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedPronouns,
                    onExpandedChange = { expandedPronouns = it }
                ) {
                    OutlinedTextField(
                        value = pronouns.ifEmpty { "Seleccionar pronombres" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pronombres") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Badge,
                                contentDescription = null,
                                tint = PrimaryColor
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPronouns)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedPronouns,
                        onDismissRequest = { expandedPronouns = false }
                    ) {
                        listOf(
                            "Él",
                            "Ella",
                            "Elle",
                            "Él/Ella",
                            "Ella/Él",
                            "Elle/Él",
                            "Elle/Ella",
                            "Prefiero no decir"
                        ).forEach { pronoun ->
                            DropdownMenuItem(
                                text = { Text(pronoun) },
                                onClick = {
                                    pronouns = pronoun
                                    expandedPronouns = false
                                }
                            )
                        }
                    }
                }

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = PrimaryColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    ),
                    minLines = 3,
                    maxLines = 5
                )

                // Address Field with Privacy Toggle
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Dirección") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = PrimaryColor
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { /* TODO: Abrir mapa */ }) {
                                Icon(
                                    imageVector = Icons.Outlined.Map,
                                    contentDescription = "Seleccionar en mapa",
                                    tint = PrimaryColor
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (showAddress) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (showAddress) "Visible para todos" else "Oculto",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        Switch(
                            checked = showAddress,
                            onCheckedChange = { showAddress = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryColor,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                }

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = it }
                ) {
                    OutlinedTextField(
                        value = getStatusText(selectedStatus),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(getStatusColor(selectedStatus))
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        listOf(
                            1 to "Online",
                            2 to "Offline",
                            3 to "No disponible"
                        ).forEach { (id, status) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(getStatusColor(id))
                                        )
                                        Text(status)
                                    }
                                },
                                onClick = {
                                    selectedStatus = id
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }

                // Email Field (Read-only)
                OutlinedTextField(
                    value = userProfile.email ?: "",
                    onValueChange = {},
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E)
                        )
                    },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color(0xFFE0E0E0),
                        disabledTextColor = Color(0xFF757575)
                    )
                )

                Divider(color = Color(0xFFE0E0E0))

                // Security Buttons
                Text(
                    text = "Seguridad",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )

                OutlinedButton(
                    onClick = onNavigateToChangeEmail,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiar email", fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = onNavigateToChangePassword,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiar contraseña", fontSize = 14.sp)
                }

                Divider(color = Color(0xFFE0E0E0))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            onSave(name, phone, pronouns, description, address, selectedStatus)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor
                        )
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}


fun getStatusColor(statusId: Int?): Color {
    return when (statusId) {
        1 -> Color(0xFF4CAF50) // Online - Verde
        2 -> Color(0xFF9E9E9E) // Offline - Gris
        3 -> Color(0xFFF44336) // No disponible - Rojo
        else -> Color(0xFF9E9E9E)
    }
}

fun getStatusText(statusId: Int?): String {
    return when (statusId) {
        1 -> "Online"
        2 -> "Offline"
        3 -> "No disponible"
        else -> "Desconocido"
    }
}