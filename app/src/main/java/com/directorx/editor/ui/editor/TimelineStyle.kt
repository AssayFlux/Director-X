package com.directorx.editor.ui.editor

import androidx.compose.runtime.Composable
import com.directorx.editor.ui.theme.DirectorXAccents
import androidx.compose.ui.graphics.Color
import com.directorx.editor.model.TrackType

@Composable
internal fun trackAccentColor(trackType: TrackType): Color = when (trackType) {
    TrackType.VIDEO -> DirectorXAccents.Blue
    TrackType.AUDIO -> DirectorXAccents.Green
    TrackType.OVERLAY -> DirectorXAccents.Peach
    TrackType.TEXT -> DirectorXAccents.Mauve
    TrackType.ADJUSTMENT -> DirectorXAccents.Yellow
}
