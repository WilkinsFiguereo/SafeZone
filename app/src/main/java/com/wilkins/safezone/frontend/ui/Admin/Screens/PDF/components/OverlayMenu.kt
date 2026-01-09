package com.wilkins.safezone.frontend.ui.Admin.Screens.PDF.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * Menú desplegable superpuesto con opciones
 * NOTA: Este menú se muestra como un DropdownMenu anclado al botón que lo activa
 */
@Composable
fun OverlayMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onConfigClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onScheduleClick: () -> Unit = {},
    onExportAllClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier.width(240.dp),
        offset = DpOffset(x = (-8).dp, y = 8.dp)
    ) {
        // Opción: Configuración
        DropdownMenuItem(
            text = {
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            onClick = {
                onConfigClick()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Opción: Historial de reportes
        DropdownMenuItem(
            text = {
                Text(
                    text = "Historial de Reportes",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            onClick = {
                onHistoryClick()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        )

        // Opción: Programar reportes
        DropdownMenuItem(
            text = {
                Text(
                    text = "Programar Reportes",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            onClick = {
                onScheduleClick()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Opción: Exportar todo
        DropdownMenuItem(
            text = {
                Text(
                    text = "Exportar Todos",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            onClick = {
                onExportAllClick()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Opción: Ayuda
        DropdownMenuItem(
            text = {
                Text(
                    text = "Ayuda",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            onClick = {
                onHelpClick()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Help,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        )

        // Opción: Acerca de
        DropdownMenuItem(
            text = {
                Text(
                    text = "Acerca de",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            onClick = {
                onAboutClick()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        )
    }
}