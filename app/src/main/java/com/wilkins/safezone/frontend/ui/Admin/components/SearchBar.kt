package com.wilkins.safezone.frontend.ui.Admin.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    selectedFilter: String,
    showFilters: Boolean,
    onToggleFilters: () -> Unit,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    filterOptions: List<Pair<String, String>>,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Fila de búsqueda
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Campo de búsqueda
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier
                    .weight(1f),
                placeholder = {
                    Text(
                        when (selectedFilter) {
                            "nombre" -> "Buscar por nombre..."
                            "rol" -> "Buscar por rol..."
                            "email" -> "Buscar por email..."
                            else -> "Buscar en todos los campos..."
                        }
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, "Buscar", tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { onSearchTextChange("") }) {
                            Icon(Icons.Default.Close, "Limpiar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            // Botón de filtros
            IconButton(
                onClick = onToggleFilters,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    Icons.Default.FilterList,
                    "Filtros",
                    tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Botón de recargar
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp)
                        .padding(start = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(Icons.Default.Refresh, "Recargar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Selector de filtros
        if (showFilters) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text(
                    text = "Filtrar por:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterOptions.forEach { (key, value) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = {
                                onFilterSelected(key)
                                onToggleFilters()
                            },
                            label = value,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        )
                    }
                }
            }
        }

        // Mostrar filtro activo
        if (searchText.isNotEmpty()) {
            Text(
                text = "Filtrando por: ${filterOptions.find { it.first == selectedFilter }?.second ?: "Todos"} - \"$searchText\"",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            )
        }
    }
}