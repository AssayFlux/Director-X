package com.directorx.editor.ui.editor

import com.directorx.editor.model.Track
import com.directorx.editor.model.TrackType

internal fun ensureEditorTracks(tracks: List<Track>): List<Track> {
    if (tracks.any { it.type == TrackType.TEXT }) return tracks
    val nextIndex = (tracks.maxOfOrNull { it.index } ?: -1) + 1
    return tracks + Track(
        type = TrackType.TEXT,
        index = nextIndex,
        trackHeight = 48,
        isCollapsed = true
    )
}

internal fun orderedTimelineTracks(tracks: List<Track>): List<Track> {
    fun priority(type: TrackType): Int = when (type) {
        TrackType.TEXT -> 0
        TrackType.OVERLAY -> 1
        TrackType.ADJUSTMENT -> 2
        TrackType.VIDEO -> 3
        TrackType.AUDIO -> 4
    }
    return tracks.sortedWith(compareBy<Track>({ priority(it.type) }, { it.index }))
}
