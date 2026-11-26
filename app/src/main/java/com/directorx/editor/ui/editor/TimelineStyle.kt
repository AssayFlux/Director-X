package com.directorx.editor.ui.editor

import androidx.compose.ui.graphics.Color
import com.directorx.editor.model.TrackType
import com.directorx.editor.ui.theme.DirectorXSemanticColors

internal fun trackAccentColor(
    trackType: TrackType,
    colors: DirectorXSemanticColors,
): Color = when (trackType) {
    TrackType.VIDEO -> colors.accentSecondary
    TrackType.AUDIO -> colors.success
    TrackType.OVERLAY -> colors.accentTertiary
    TrackType.TEXT -> colors.accentTertiary
    TrackType.ADJUSTMENT -> colors.warning
}
