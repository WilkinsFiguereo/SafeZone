package com.wilkins.safezone.frontend.ui.GlobalAssociation.Screens.ReportSent.ReportList

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.GlobalAssociation.ReportsRepository
import com.wilkins.safezone.frontend.ui.GlobalAssociation.GovernmentMenu
import com.wilkins.safezone.navigation.theme.PrimaryColor
import kotlinx.coroutines.launch
import kotlin.collections.get

@Composable
fun ReportsCompletedScreen(
    navController: NavController,
    initialStatusId: Int?
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    val statusToFilter = remember(initialStatusId) {
        initialStatusId?.let { ReportStatus.fromId(it) }
    }
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

                val affairsResult = repository.getAllAffairs()
                val affairs = affairsResult.getOrNull()?.associateBy { it.id } ?: emptyMap()

                // 3. Lógica Clave: Usar el initialStatusId para llamar al repositorio
                val reportsResult = if (initialStatusId != null) {
                    // Si se pasó un ID (ej: 1), llama a la función filtrada
                    repository.getReportsByStatus(initialStatusId)
                } else {
                    // Si no se pasó un ID (o es nulo), trae todos los reportes
                    repository.getAllReports()
                }

                val reports = reportsResult.getOrNull() ?: emptyList()

                allReports = reports.map { dto ->
                    convertToReportItem(dto, affairs[dto.idAffair]?.affairName)
                }

                isLoading = false
            } catch (e: Exception) {
                // Mensaje de error ajustado para el estado
                val statusLabel = statusToFilter?.label ?: "todos los estados"
                errorMessage = "Error al cargar reportes de $statusLabel: ${e.message}"
                isLoading = false
            }
        }
    }
    // Cargar datos al iniciar
    LaunchedEffect(initialStatusId) { // Recargar si cambia el ID de estado
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


// Función auxiliar para contar filtros activos
private fun countActiveFilters(filters: ReportFilters): Int {
    var count = 0
    if (filters.status != null) count++
    if (filters.priority != null) count++
    if (filters.dateRange != null) count++
    if (filters.searchQuery.isNotEmpty()) count++
    return count
}