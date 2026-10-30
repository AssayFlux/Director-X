package com.directorx.editor.ui.editor

import com.directorx.editor.ui.theme.ClearCutAccents
import androidx.compose.ui.graphics.Color
import com.directorx.editor.model.TrackType

internal fun trackAccentColor(trackType: TrackType): Color = when (trackType) {
    TrackType.VIDEO -> ClearCutAccents.Blue
    TrackType.AUDIO -> ClearCutAccents.Green
    TrackType.OVERLAY -> ClearCutAccents.Peach
    TrackType.TEXT -> ClearCutAccents.Mauve
    TrackType.ADJUSTMENT -> ClearCutAccents.Yellow
}
