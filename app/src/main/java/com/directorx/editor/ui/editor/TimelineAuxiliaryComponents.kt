package com.directorx.editor.ui.editor

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.directorx.editor.R
import com.directorx.editor.model.Clip
import com.directorx.editor.model.Track
import com.directorx.editor.ui.theme.LocalClearCutColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TimelineThumbnailStrip(
    clipId: String,
    quantizedZoom: Float,
    clipWidthPx: Float,
    thumbnails: List<Bitmap>,
    cacheWindow: LazyLayoutCacheWindow,
) {
    val density = LocalDensity.current
    val listState = rememberLazyListState(cacheWindow = cacheWindow)
    val thumbnailWidth = with(density) {
        (clipWidthPx / thumbnails.size.coerceAtLeast(1)).coerceAtLeast(1f).toDp()
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false,
    ) {
        itemsIndexed(
            items = thumbnails,
            key = { index, _ -> "${clipId}_${quantizedZoom}_$index" },
        ) { _, bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.cd_clip_thumbnail),
                modifier = Modifier
                    .width(thumbnailWidth)
                    .fillMaxHeight(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
        }
    }
}

internal fun timelineTrackHeight(track: Track, compact: Boolean): Dp = when {
    track.isCollapsed -> if (compact) 56.dp else 64.dp
    compact -> track.trackHeight.coerceAtLeast(108).dp
    else -> track.trackHeight.coerceAtLeast(120).dp
}

// Allocation-free clip lookup for drag handlers — they fire per pointer event
// (~60-120Hz), where the previous flatMap built a throwaway list each event.
internal fun formatTrackOffsetLabel(offsetMs: Long): String =
    if (offsetMs > 0L) "+$offsetMs ms" else "$offsetMs ms"


internal fun findClipInTracks(tracks: List<Track>, clipId: String): Clip? {
    for (track in tracks) {
        for (candidate in track.clips) {
            if (candidate.id == clipId) return candidate
        }
    }
    return null
}

@Composable
internal fun TimelineHeaderSummary(
    playheadMs: Long,
    totalDurationMs: Long,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalClearCutColors.current
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.timeline_title),
            color = semanticColors.text,
            style = if (compact) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.titleLarge
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(
                R.string.timeline_playhead_total,
                formatTimelineTime(playheadMs),
                formatTimelineTime(totalDurationMs),
            ),
            color = semanticColors.subtext,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

