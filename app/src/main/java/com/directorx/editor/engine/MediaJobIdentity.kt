package com.directorx.editor.engine

import com.directorx.editor.model.Clip
import com.directorx.editor.model.SpeedCurve

/**
 * Identity captured when a timeline-bound media job starts.
 *
 * Clip IDs are intentionally not enough: relinking can keep an ID while changing
 * the source, and trimming/splitting/reordering can change the timeline position
 * that a timeline-bound result belongs to. Proxy artifacts use a separate
 * source-only identity because timeline edits do not change the encoded media.
 */
data class TimelineMediaJobIdentity(
    val clipId: String,
    val sourceUri: String,
    val sourceDurationMs: Long,
    val trimStartMs: Long,
    val trimEndMs: Long,
    val timelineStartMs: Long,
    val timelineEndMs: Long,
    val audioSyncOffsetMs: Long,
    val speed: Float,
    val speedCurve: SpeedCurve?,
    val isReversed: Boolean,
) {
    /** Compact version token for job stores that cannot retain the full identity. */
    val version: String
        get() = listOf(
            clipId,
            sourceUri,
            sourceDurationMs,
            trimStartMs,
            trimEndMs,
            timelineStartMs,
            timelineEndMs,
            audioSyncOffsetMs,
            speed,
            speedCurve,
            isReversed,
        ).joinToString("\u001f")
}

fun Clip.timelineMediaJobIdentity(): TimelineMediaJobIdentity = TimelineMediaJobIdentity(
    clipId = id,
    sourceUri = sourceUri.toString(),
    sourceDurationMs = sourceDurationMs,
    trimStartMs = trimStartMs,
    trimEndMs = trimEndMs,
    timelineStartMs = timelineStartMs,
    timelineEndMs = timelineEndMs,
    audioSyncOffsetMs = audioSyncOffsetMs,
    speed = speed,
    speedCurve = speedCurve,
    isReversed = isReversed,
)

internal fun shouldApplyMediaJobResult(
    expected: TimelineMediaJobIdentity,
    current: TimelineMediaJobIdentity?,
): Boolean = current == expected

internal fun shouldApplyProxyResult(
    expectedVersion: String,
    currentVersion: String?,
): Boolean = expectedVersion.isNotBlank() && expectedVersion == currentVersion

/**
 * Stable identity for a source-media proxy artifact.
 *
 * Timeline placement, trims, playback speed and audio offsets are deliberately
 * excluded: none of those changes the source pixels that the proxy encodes.
 */
fun Clip.proxyMediaVersion(): String = listOf(
    assetId.orEmpty(),
    sourceUri.toString(),
    sourceDurationMs,
).joinToString("\u001f")
