package com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.ReportSent.ReportList

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.GlobalAssociation.DateUtils
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportDto
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportUtils
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportsRepository
import com.wilkins.safezone.frontend.ui.GlobalAssociation.GovernmentMenu
import com.wilkins.safezone.ui.theme.PrimaryColor
import kotlinx.coroutines.launch
import kotlin.collections.get

// ============================================
// MODELOS UI
// ============================================

data class ReportItem(
    val id: String,
    val title: String,
    val incidentType: String,
    val location: String,
    val date: String,
    val time: String,
    val status: ReportStatus,
    val priority: ReportPriority,
    val reporterName: String,
    val description: String,
    val imageUrl: String?,
    val createdAt: String // Para ordenamiento
)

enum class ReportStatus(val id: Int, val label: String, val color: Color, val icon: ImageVector) {
    PENDING(1, "Pendiente", Color(0xFFFFA726), Icons.Default.Pending),
    IN_PROGRESS(2, "En Proceso", Color(0xFF2196F3), Icons.Default.Update),
    COMPLETED(3, "Finalizado", Color(0xFF4CAF50), Icons.Default.CheckCircle),
    CANCELLED(4, "Cancelado", Color(0xFFE53935), Icons.Default.Cancel);

    companion object {
        fun fromId(id: Int): ReportStatus {
            return entries.find { it.id == id } ?: PENDING
        }
    }
}

enum class ReportPriority(val label: String, val color: Color) {
    LOW("Baja", Color(0xFF66BB6A)),
    MEDIUM("Media", Color(0xFFFFA726)),
    HIGH("Alta", Color(0xFFEF5350)),
    CRITICAL("Crítica", Color(0xFFB71C1C));

    companion object {
        fun fromAffairId(affairId: Int?): ReportPriority {
            return when (affairId) {
                1 -> CRITICAL  // Emergencia
                2 -> HIGH      // Seguridad
                3 -> MEDIUM    // Infraestructura
                4 -> MEDIUM    // Servicios
                else -> LOW
            }
        }
    }
}

// ============================================
// FILTROS Y PAGINACIÓN
// ============================================

data class ReportFilters(
    val status: ReportStatus? = null,
    val priority: ReportPriority? = null,
    val dateRange: Pair<Long, Long>? = null,
    val sortBy: SortOption = SortOption.DATE_DESC,
    val searchQuery: String = ""
)

enum class SortOption(
    val label: String,
    val icon: ImageVector,
    val comparator: Comparator<ReportItem>
) {
    DATE_DESC(
        "Más recientes",
        Icons.Default.ArrowDownward,
        Comparator { a, b -> b.createdAt.compareTo(a.createdAt) }
    ),
    DATE_ASC(
        "Más antiguos",
        Icons.Default.ArrowUpward,
        Comparator { a, b -> a.createdAt.compareTo(b.createdAt) }
    ),
    PRIORITY_HIGH(
        "Prioridad alta",
        Icons.Default.Warning,
        Comparator { a, b -> b.priority.ordinal.compareTo(a.priority.ordinal) }
    ),
    PRIORITY_LOW(
        "Prioridad baja",
        Icons.Default.LowPriority,
        Comparator { a, b -> a.priority.ordinal.compareTo(b.priority.ordinal) }
    ),
    STATUS(
        "Por estado",
        Icons.Default.FilterList,
        Comparator { a, b -> a.status.ordinal.compareTo(b.status.ordinal) }
    );
}

// ============================================
// CONVERTIDOR DE DATOS
// ============================================

fun convertToReportItem(dto: ReportDto, affairName: String?): ReportItem {
    return ReportItem(
        id = dto.id,
        title = ReportUtils.generateTitle(affairName, dto.description),
        incidentType = affairName ?: "Sin categoría",
        location = dto.reportLocation ?: "Ubicación no especificada",
        date = DateUtils.formatDate(dto.createdAt),
        time = DateUtils.formatTime(dto.createdAt),
        status = ReportStatus.fromId(dto.idReportingStatus),
        priority = ReportPriority.fromAffairId(dto.idAffair),
        reporterName = ReportUtils.getReporterName(dto.isAnonymous, dto.userName),
        description = dto.description ?: "Sin descripción",
        imageUrl = dto.imageUrl,
        createdAt = dto.createdAt
    )
}

// ============================================
// PANTALLA PRINCIPAL
// ============================================

@Composable
fun ReportsSentScreen(
    navController: NavController
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    // Estados de datos
    var allReports by remember { mutableStateOf<List<ReportItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estados de filtros
    var filters by remember { mutableStateOf(ReportFilters()) }
    var showFiltersDialog by remember { mutableStateOf(false) }

    // Estados de paginación
    val pageSize = 10
    var currentPage by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(1) }

    val scope = rememberCoroutineScope()
    val repository = remember { ReportsRepository() }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth >= 600.dp

    // Función para cargar datos
    fun loadReports() {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null

                // Cargar affairs
                val affairsResult = repository.getAllAffairs()
                val affairs = affairsResult.getOrNull()?.associateBy { it.id } ?: emptyMap()

                // Cargar reportes
                val reportsResult = repository.getAllReports()
                val reports = reportsResult.getOrNull() ?: emptyList()

                // Convertir a UI
                allReports = reports.map { dto ->
                    convertToReportItem(dto, affairs[dto.idAffair]?.affairName)
                }

                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Error al cargar reportes: ${e.message}"
                isLoading = false
            }
        }
    }

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        loadReports()
    }

    // Filtrar y paginar reportes
    val paginatedReports = remember(allReports, filters, currentPage) {
        // Aplicar filtros
        val filtered = allReports.filter { report ->
            val matchesSearch = filters.searchQuery.isEmpty() ||
                    report.title.contains(filters.searchQuery, ignoreCase = true) ||
                    report.incidentType.contains(filters.searchQuery, ignoreCase = true) ||
                    report.location.contains(filters.searchQuery, ignoreCase = true) ||
                    report.description.contains(filters.searchQuery, ignoreCase = true) ||
                    report.reporterName.contains(filters.searchQuery, ignoreCase = true)

            val matchesStatus = filters.status == null || report.status == filters.status
            val matchesPriority = filters.priority == null || report.priority == filters.priority

            matchesSearch && matchesStatus && matchesPriority
        }.sortedWith(filters.sortBy.comparator)

        // Calcular paginación
        totalPages = maxOf(1, (filtered.size + pageSize - 1) / pageSize)

        // Paginar resultados
        val startIndex = (currentPage - 1) * pageSize
        val endIndex = minOf(startIndex + pageSize, filtered.size)

        if (startIndex < filtered.size) {
            filtered.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }

    // Estadísticas
    val stats = remember(allReports) {
        mapOf(
            ReportStatus.PENDING to allReports.count { it.status == ReportStatus.PENDING },
            ReportStatus.IN_PROGRESS to allReports.count { it.status == ReportStatus.IN_PROGRESS },
            ReportStatus.COMPLETED to allReports.count { it.status == ReportStatus.COMPLETED },
            ReportStatus.CANCELLED to allReports.count { it.status == ReportStatus.CANCELLED }
        )
    }

    GovernmentMenu(
        navController = navController,
        isMenuOpen = isMenuOpen,
        onMenuToggle = { isMenuOpen = it },
        currentRoute = "reports_sent"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(if (isTablet) 8.dp else 0.dp)
        ) {
            // Header con título y estadísticas
            HeaderSection(
                totalReports = allReports.size,
                stats = stats,
                selectedFilter = filters.status,
                onFilterClick = { status ->
                    filters = filters.copy(status = if (filters.status == status) null else status)
                    currentPage = 1 // Resetear a primera página
                },
                onRefreshClick = { loadReports() },
                onOpenFilters = { showFiltersDialog = true },
                isTablet = isTablet,
                activeFiltersCount = countActiveFilters(filters)
            )

            // Barra de búsqueda
            SearchBar(
                searchQuery = filters.searchQuery,
                onSearchChange = { query ->
                    filters = filters.copy(searchQuery = query)
                    currentPage = 1
                },
                selectedFilter = filters.status,
                onClearFilter = {
                    filters = filters.copy(status = null)
                    currentPage = 1
                },
                isTablet = isTablet
            )

            Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))

            // Controles de filtro y ordenamiento
            FilterControlsRow(
                filters = filters,
                onSortChange = { sortOption ->
                    filters = filters.copy(sortBy = sortOption)
                    currentPage = 1
                },
                onOpenFilters = { showFiltersDialog = true },
                isTablet = isTablet
            )

            Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))

            // Contenido principal
            ContentSection(
                isLoading = isLoading,
                errorMessage = errorMessage,
                reports = paginatedReports,
                totalResults = allReports.filter { report ->
                    val matchesSearch = filters.searchQuery.isEmpty() ||
                            report.title.contains(filters.searchQuery, ignoreCase = true) ||
                            report.incidentType.contains(filters.searchQuery, ignoreCase = true) ||
                            report.location.contains(filters.searchQuery, ignoreCase = true) ||
                            report.description.contains(filters.searchQuery, ignoreCase = true) ||
                            report.reporterName.contains(filters.searchQuery, ignoreCase = true)

                    val matchesStatus = filters.status == null || report.status == filters.status
                    val matchesPriority = filters.priority == null || report.priority == filters.priority

                    matchesSearch && matchesStatus && matchesPriority
                }.size,
                currentPage = currentPage,
                totalPages = totalPages,
                onReportClick = { reportId ->
                    navController.navigate("report_detail/$reportId")
                },
                onNextPage = {
                    if (currentPage < totalPages) {
                        currentPage++
                    }
                },
                onPrevPage = {
                    if (currentPage > 1) {
                        currentPage--
                    }
                },
                onGoToPage = { page ->
                    if (page in 1..totalPages) {
                        currentPage = page
                    }
                },
                isTablet = isTablet
            )
        }
    }

    // Diálogo de filtros avanzados
    if (showFiltersDialog) {
        AdvancedFiltersDialog(
            filters = filters,
            onFiltersChange = { newFilters ->
                filters = newFilters
                currentPage = 1
            },
            onDismiss = { showFiltersDialog = false },
            isTablet = isTablet
        )
    }
}

// ============================================
// COMPONENTES
// ============================================

@Composable
private fun HeaderSection(
    totalReports: Int,
    stats: Map<ReportStatus, Int>,
    selectedFilter: ReportStatus?,
    onFilterClick: (ReportStatus) -> Unit,
    onRefreshClick: () -> Unit,
    onOpenFilters: () -> Unit,
    isTablet: Boolean,
    activeFiltersCount: Int
) {
    val paddingHorizontal = if (isTablet) 20.dp else 16.dp
    val paddingVertical = if (isTablet) 24.dp else 16.dp
    val titleSize = if (isTablet) 28.sp else 24.sp
    val subtitleSize = if (isTablet) 16.sp else 14.sp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = paddingHorizontal, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(if (isTablet) 20.dp else 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingHorizontal, paddingVertical)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Reportes Enviados",
                        fontSize = titleSize,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "$totalReports reportes totales",
                        fontSize = subtitleSize,
                        color = Color(0xFF666666)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de filtros avanzados
                    BadgedBox(
                        badge = {
                            if (activeFiltersCount > 0) {
                                Badge(
                                    containerColor = PrimaryColor,
                                    contentColor = Color.White
                                ) {
                                    Text(text = activeFiltersCount.toString())
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onOpenFilters,
                            modifier = Modifier
                                .size(if (isTablet) 48.dp else 40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF0F0F0))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filtros avanzados",
                                tint = PrimaryColor,
                                modifier = Modifier.size(if (isTablet) 24.dp else 20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefreshClick,
                        modifier = Modifier
                            .size(if (isTablet) 48.dp else 40.dp)
                            .clip(CircleShape)
                            .background(PrimaryColor.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refrescar",
                            tint = PrimaryColor,
                            modifier = Modifier.size(if (isTablet) 24.dp else 20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(if (isTablet) 56.dp else 48.dp)
                            .clip(CircleShape)
                            .background(PrimaryColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(if (isTablet) 32.dp else 28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 16.dp))

            // Mini estadísticas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 8.dp)
            ) {
                ReportStatus.entries.forEach { status ->
                    MiniStatCard(
                        status = status,
                        count = stats[status] ?: 0,
                        modifier = Modifier.weight(1f),
                        isSelected = selectedFilter == status,
                        onClick = { onFilterClick(status) },
                        isTablet = isTablet
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: ReportStatus?,
    onClearFilter: () -> Unit,
    isTablet: Boolean
) {
    val horizontalPadding = if (isTablet) 20.dp else 16.dp
    val innerPadding = if (isTablet) 16.dp else 12.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(if (isTablet) 16.dp else 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar reportes...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar búsqueda",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (selectedFilter != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = selectedFilter.color.copy(alpha = 0.1f),
                    modifier = Modifier.clickable(onClick = onClearFilter)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = if (isTablet) 16.dp else 12.dp,
                            vertical = if (isTablet) 10.dp else 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = selectedFilter.icon,
                            contentDescription = null,
                            tint = selectedFilter.color,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = selectedFilter.label,
                            color = selectedFilter.color,
                            fontSize = if (isTablet) 14.sp else 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Quitar filtro",
                            tint = selectedFilter.color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterControlsRow(
    filters: ReportFilters,
    onSortChange: (SortOption) -> Unit,
    onOpenFilters: () -> Unit,
    isTablet: Boolean
) {
    val horizontalPadding = if (isTablet) 20.dp else 16.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botón de ordenamiento
        var showSortMenu by remember { mutableStateOf(false) }

        Box {
            OutlinedButton(
                onClick = { showSortMenu = true },
                modifier = Modifier.fillMaxWidth(0.5f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White
                )
            ) {
                Icon(
                    imageVector = filters.sortBy.icon,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = filters.sortBy.label,
                    fontSize = if (isTablet) 14.sp else 13.sp,
                    color = Color(0xFF333333),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null,
                                    tint = if (filters.sortBy == option) PrimaryColor else Color.Gray
                                )
                                Text(
                                    text = option.label,
                                    color = if (filters.sortBy == option) PrimaryColor else Color.Black
                                )
                            }
                        },
                        onClick = {
                            onSortChange(option)
                            showSortMenu = false
                        }
                    )
                }
            }
        }

        // Botón de filtros
        OutlinedButton(
            onClick = onOpenFilters,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.FilterAlt,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Filtros",
                fontSize = if (isTablet) 14.sp else 13.sp,
                color = Color(0xFF333333)
            )
        }
    }
}

@Composable
private fun ContentSection(
    isLoading: Boolean,
    errorMessage: String?,
    reports: List<ReportItem>,
    totalResults: Int,
    currentPage: Int,
    totalPages: Int,
    onReportClick: (String) -> Unit,
    onNextPage: () -> Unit,
    onPrevPage: () -> Unit,
    onGoToPage: (Int) -> Unit,
    isTablet: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = if (isTablet) 20.dp else 16.dp)
    ) {
        when {
            isLoading -> {
                LoadingState(isTablet)
            }

            errorMessage != null -> {
                ErrorState(message = errorMessage, isTablet = isTablet)
            }

            reports.isEmpty() -> {
                EmptyState(isTablet = isTablet)
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Indicador de resultados
                    ResultsIndicator(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        totalResults = totalResults,
                        resultsCount = reports.size,
                        isTablet = isTablet
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lista de reportes
                    ReportsList(
                        reports = reports,
                        onReportClick = onReportClick,
                        isTablet = isTablet
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Controles de paginación
                    PaginationControls(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onNextPage = onNextPage,
                        onPrevPage = onPrevPage,
                        onGoToPage = onGoToPage,
                        isTablet = isTablet
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultsIndicator(
    currentPage: Int,
    totalPages: Int,
    totalResults: Int,
    resultsCount: Int,
    isTablet: Boolean
) {
    val horizontalPadding = if (isTablet) 20.dp else 16.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Mostrando $resultsCount de $totalResults resultados",
            fontSize = if (isTablet) 14.sp else 12.sp,
            color = Color(0xFF666666)
        )

        Text(
            text = "Página $currentPage de $totalPages",
            fontSize = if (isTablet) 14.sp else 12.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onNextPage: () -> Unit,
    onPrevPage: () -> Unit,
    onGoToPage: (Int) -> Unit,
    isTablet: Boolean
) {
    val horizontalPadding = if (isTablet) 20.dp else 16.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón anterior
            Button(
                onClick = onPrevPage,
                enabled = currentPage > 1,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentPage > 1) PrimaryColor else Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Página anterior",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Anterior",
                    fontSize = if (isTablet) 15.sp else 14.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Indicador de página
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Primera página
                if (currentPage > 3) {
                    TextButton(
                        onClick = { onGoToPage(1) },
                        shape = CircleShape
                    ) {
                        Text("1", color = PrimaryColor)
                    }
                    if (currentPage > 4) {
                        Text("...", color = Color.Gray)
                    }
                }

                // Páginas cercanas
                for (page in maxOf(1, currentPage - 2)..minOf(totalPages, currentPage + 2)) {
                    if (page == currentPage) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryColor,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = page.toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        TextButton(
                            onClick = { onGoToPage(page) },
                            shape = CircleShape
                        ) {
                            Text(
                                text = page.toString(),
                                color = PrimaryColor
                            )
                        }
                    }
                }

                // Última página
                if (currentPage < totalPages - 2) {
                    if (currentPage < totalPages - 3) {
                        Text("...", color = Color.Gray)
                    }
                    TextButton(
                        onClick = { onGoToPage(totalPages) },
                        shape = CircleShape
                    ) {
                        Text(totalPages.toString(), color = PrimaryColor)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Botón siguiente
            Button(
                onClick = onNextPage,
                enabled = currentPage < totalPages,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentPage < totalPages) PrimaryColor else Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Siguiente",
                    fontSize = if (isTablet) 15.sp else 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Página siguiente",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Salto rápido a página
        if (isTablet && totalPages > 5) {
            var quickJumpPage by remember { mutableStateOf(currentPage.toString()) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Ir a página:",
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = quickJumpPage,
                    onValueChange = { quickJumpPage = it },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val page = quickJumpPage.toIntOrNull()
                        if (page != null && page in 1..totalPages) {
                            onGoToPage(page)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Ir")
                }
            }
        }
    }
}

@Composable
private fun LoadingState(isTablet: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = PrimaryColor,
                strokeWidth = if (isTablet) 4.dp else 3.dp,
                modifier = Modifier.size(if (isTablet) 60.dp else 48.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Cargando reportes...",
                fontSize = if (isTablet) 18.sp else 16.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, isTablet: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(if (isTablet) 48.dp else 32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(if (isTablet) 100.dp else 80.dp),
                tint = Color(0xFFDC3545)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Error al cargar",
                fontSize = if (isTablet) 22.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = if (isTablet) 16.sp else 14.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                lineHeight = if (isTablet) 24.sp else 20.sp
            )
        }
    }
}

@Composable
private fun EmptyState(isTablet: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isTablet) 48.dp else 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(if (isTablet) 100.dp else 80.dp),
                tint = Color(0xFF9E9E9E)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "No se encontraron reportes",
                fontSize = if (isTablet) 22.sp else 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF555555)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Intenta cambiar los filtros de búsqueda",
                fontSize = if (isTablet) 16.sp else 14.sp,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReportsList(
    reports: List<ReportItem>,
    onReportClick: (String) -> Unit,
    isTablet: Boolean
) {
    val horizontalPadding = if (isTablet) 20.dp else 16.dp
    val verticalSpacing = if (isTablet) 16.dp else 12.dp
    val columnCount = if (isTablet) 2 else 1

    if (isTablet) {
        // Diseño de cuadrícula para tablets
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = horizontalPadding,
                vertical = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing)
        ) {
            items(reports.chunked(columnCount)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(verticalSpacing)
                ) {
                    for (i in 0 until columnCount) {
                        if (i < rowItems.size) {
                            ReportCard(
                                report = rowItems[i],
                                onClick = { onReportClick(rowItems[i].id) },
                                modifier = Modifier.weight(1f),
                                isTablet = true
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    } else {
        // Diseño de lista para móviles
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = horizontalPadding,
                vertical = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing)
        ) {
            items(reports, key = { it.id }) { report ->
                ReportCard(
                    report = report,
                    onClick = { onReportClick(report.id) },
                    isTablet = false
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedFiltersDialog(
    filters: ReportFilters,
    onFiltersChange: (ReportFilters) -> Unit,
    onDismiss: () -> Unit,
    isTablet: Boolean
) {
    val dialogWidth = if (isTablet) 0.8f else 0.9f

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(dialogWidth)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Filtros Avanzados",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Filtro por prioridad
                var selectedPriority by remember { mutableStateOf(filters.priority) }

                Text(
                    text = "Prioridad",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportPriority.entries.forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = {
                                selectedPriority = if (selectedPriority == priority) null else priority
                            },
                            label = {
                                Text(priority.label, fontSize = 13.sp)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = priority.color.copy(alpha = 0.2f),
                                selectedLabelColor = priority.color
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Filtro por estado
                var selectedStatus by remember { mutableStateOf(filters.status) }

                Text(
                    text = "Estado",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportStatus.entries.forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = {
                                selectedStatus = if (selectedStatus == status) null else status
                            },
                            label = {
                                Text(status.label, fontSize = 13.sp)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = status.color.copy(alpha = 0.2f),
                                selectedLabelColor = status.color
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            // Limpiar filtros
                            onFiltersChange(ReportFilters())
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF666666)
                        )
                    ) {
                        Text("Limpiar filtros")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            onFiltersChange(filters.copy(
                                priority = selectedPriority,
                                status = selectedStatus
                                // Aquí se agregaría el filtro por fecha cuando esté implementado
                            ))
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor
                        )
                    ) {
                        Text("Aplicar filtros")
                    }
                }
            }
        }
    }
}

@Composable
fun MiniStatCard(
    status: ReportStatus,
    count: Int,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    isTablet: Boolean
) {
    val padding = if (isTablet) 16.dp else 12.dp
    val iconSize = if (isTablet) 24.dp else 20.dp
    val countSize = if (isTablet) 22.sp else 18.sp
    val labelSize = if (isTablet) 12.sp else 10.sp

    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) status.color.copy(alpha = 0.15f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        shape = RoundedCornerShape(if (isTablet) 16.dp else 12.dp),
        border = if (isSelected) BorderStroke(
            width = if (isTablet) 2.dp else 1.5.dp,
            color = status.color
        ) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = status.icon,
                contentDescription = null,
                tint = status.color,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.height(if (isTablet) 8.dp else 4.dp))
            Text(
                text = count.toString(),
                fontSize = countSize,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) status.color else Color(0xFF1A1A1A)
            )
            Text(
                text = status.label,
                fontSize = labelSize,
                color = if (isSelected) status.color else Color(0xFF666666),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ReportCard(
    report: ReportItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTablet: Boolean
) {
    val padding = if (isTablet) 20.dp else 16.dp
    val cornerRadius = if (isTablet) 20.dp else 16.dp
    val titleSize = if (isTablet) 18.sp else 16.sp
    val descriptionSize = if (isTablet) 15.sp else 13.sp
    val infoSize = if (isTablet) 13.sp else 11.sp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            // Primera fila: ID, Título y Prioridad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reporte #${ReportUtils.getShortId(report.id)}",
                        fontSize = infoSize,
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(if (isTablet) 8.dp else 4.dp))
                    Text(
                        text = report.title,
                        fontSize = titleSize,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = if (isTablet) 24.sp else 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = report.priority.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = report.priority.label,
                        modifier = Modifier.padding(
                            horizontal = if (isTablet) 12.dp else 8.dp,
                            vertical = if (isTablet) 6.dp else 4.dp
                        ),
                        fontSize = if (isTablet) 13.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = report.priority.color
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))

            // Tipo de incidente
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(if (isTablet) 20.dp else 16.dp)
                )
                Text(
                    text = report.incidentType,
                    fontSize = descriptionSize,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 8.dp))

            // Descripción
            Text(
                text = report.description,
                fontSize = descriptionSize,
                color = Color(0xFF666666),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = if (isTablet) 22.sp else 18.sp
            )

            Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))

            // Ubicación
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF888888),
                    modifier = Modifier.size(if (isTablet) 20.dp else 16.dp)
                )
                Text(
                    text = report.location,
                    fontSize = infoSize,
                    color = Color(0xFF666666),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))

            // Divisor
            HorizontalDivider(
                color = Color(0xFFEEEEEE),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))

            // Fila inferior: Fecha, Reportero y Estado
            if (isTablet) {
                // Layout para tablet: 3 columnas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fecha y hora
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF888888),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = report.date,
                                fontSize = infoSize,
                                color = Color(0xFF666666)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = report.time,
                            fontSize = (infoSize.value - 1).sp,
                            color = Color(0xFF888888)
                        )
                    }

                    // Reportero
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF888888),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = report.reporterName,
                                fontSize = infoSize,
                                color = Color(0xFF666666),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 120.dp)
                            )
                        }
                    }

                    // Estado
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = report.status.color.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 8.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = report.status.icon,
                                contentDescription = null,
                                tint = report.status.color,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = report.status.label,
                                fontSize = infoSize,
                                fontWeight = FontWeight.Bold,
                                color = report.status.color
                            )
                        }
                    }
                }
            } else {
                // Layout para móvil: 2 filas
                Column {
                    // Primera fila: Fecha y Reportero
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF888888),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${report.date} • ${report.time}",
                                fontSize = infoSize,
                                color = Color(0xFF666666)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF888888),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = report.reporterName,
                                fontSize = infoSize,
                                color = Color(0xFF666666),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 100.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Segunda fila: Estado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = report.status.color.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = report.status.icon,
                                    contentDescription = null,
                                    tint = report.status.color,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = report.status.label,
                                    fontSize = infoSize,
                                    fontWeight = FontWeight.Bold,
                                    color = report.status.color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Función auxiliar para contar filtros activos
private fun countActiveFilters(filters: ReportFilters): Int {
    var count = 0
    if (filters.status != null) count++
    if (filters.priority != null) count++
    if (filters.dateRange != null) count++
    if (filters.searchQuery.isNotEmpty()) count++
    return count
}