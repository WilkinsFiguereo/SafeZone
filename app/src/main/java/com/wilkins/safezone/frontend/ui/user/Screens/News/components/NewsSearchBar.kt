package com.wilkins.safezone.frontend.ui.user.News

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class NewsFilter(val displayName: String) {
    ALL("Todas"),
    SECURITY("Seguridad"),
    ALERTS("Alertas"),
    COMMUNITY("Comunidad"),
    EVENTS("Eventos"),
    UPDATES("Actualizaciones")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: NewsFilter,
    onFilterChange: (NewsFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Barra de búsqueda principal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Campo de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "Buscar noticias...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar búsqueda",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                // Botón de filtros
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (showFilters || selectedFilter != NewsFilter.ALL)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    IconButton(
                        onClick = { showFilters = !showFilters }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtros",
                            tint = if (showFilters || selectedFilter != NewsFilter.ALL)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Chips de filtros
        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Filtrar por categoría:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NewsFilter.values().forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { onFilterChange(filter) },
                            label = {
                                Text(
                                    text = filter.displayName,
                                    fontWeight = if (selectedFilter == filter)
                                        FontWeight.Bold
                                    else
                                        FontWeight.Normal
                                )
                            },
                            leadingIcon = if (selectedFilter == filter) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Seleccionado",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            ),

                        )
                    }
                }
            }
        }

        // Indicador de filtro activo (cuando los filtros están ocultos)
        if (!showFilters && selectedFilter != NewsFilter.ALL) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filtro: ${selectedFilter.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Quitar filtro",
                            modifier = Modifier
                                .size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}