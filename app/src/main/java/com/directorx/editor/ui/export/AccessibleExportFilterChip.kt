package com.directorx.editor.ui.export

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.directorx.editor.ui.theme.TouchTarget

@Composable
internal fun ExportFilterChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    selected: Boolean,
    enabled: Boolean = true,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(),
) {
    FilterChip(
        onClick = onClick,
        enabled = enabled,
        label = {
            // FilterChip's modifier wraps its internal click semantics. Give the
            // label a real 48dp footprint so the clickable surface itself meets
            // the accessibility minimum at large font scales.
            Box(
                modifier = Modifier.requiredSizeIn(
                    minWidth = TouchTarget.minimum,
                    minHeight = TouchTarget.minimum,
                ),
                contentAlignment = Alignment.Center,
            ) {
                label()
            }
        },
        selected = selected,
        colors = colors,
    )
}
