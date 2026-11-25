package com.directorx.editor.ui.editor

import com.directorx.editor.ai.AutoEditResult
import com.directorx.editor.model.BlendMode
import com.directorx.editor.model.Clip
import java.security.MessageDigest
import java.util.UUID

internal fun autoEditClipFingerprint(clip: Clip): String {
    val canonical = listOf(
        clip.id,
        clip.sourceUri.toString(),
        clip.sourceDurationMs.toString(),
        clip.trimStartMs.toString(),
        clip.trimEndMs.toString()
    ).joinToString("|")
    return MessageDigest.getInstance("SHA-256")
        .digest(canonical.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}

/** Build render-safe excerpts without carrying edits or ownership from source clips. */
internal fun buildAutoEditExcerptClips(
    sourceClips: List<Clip>,
    proposal: AutoEditResult,
    newId: () -> String = { UUID.randomUUID().toString() }
): List<Clip> {
    require(proposal.segments.isNotEmpty()) { "Auto Edit proposal has no segments" }
    val sources = sourceClips.associateBy { it.id }
    return proposal.segments.map { segment ->
        val source = requireNotNull(sources[segment.clipId]) { "Auto Edit source is no longer present" }
        require(autoEditClipFingerprint(source) == segment.clipFingerprint) {
            "Auto Edit source changed after analysis"
        }
        require(segment.trimStartMs >= source.trimStartMs && segment.trimEndMs <= source.trimEndMs) {
            "Auto Edit range is outside the analyzed source"
        }
        require(segment.trimEndMs > segment.trimStartMs && segment.timelineEndMs > segment.timelineStartMs) {
            "Auto Edit segment has an invalid range"
        }
        source.copy(
            id = newId(),
            timelineStartMs = segment.timelineStartMs,
            trimStartMs = segment.trimStartMs,
            trimEndMs = segment.trimEndMs,
            effects = emptyList(),
            headTransition = null,
            tailTransition = null,
            volume = 1f,
            speed = 1f,
            isReversed = false,
            opacity = 1f,
            rotation = 0f,
            scaleX = 1f,
            scaleY = 1f,
            positionX = 0f,
            positionY = 0f,
            anchorX = 0.5f,
            anchorY = 0.5f,
            fadeInMs = 0L,
            fadeOutMs = 0L,
            keyframes = emptyList(),
            blendMode = BlendMode.NORMAL,
            speedCurve = null,
            colorGrade = null,
            masks = emptyList(),
            linkedClipId = null,
            isCompound = false,
            compoundClips = emptyList(),
            audioEffects = emptyList(),
            motionTrackingData = null,
            captions = emptyList(),
            groupId = null
        )
    }
}
