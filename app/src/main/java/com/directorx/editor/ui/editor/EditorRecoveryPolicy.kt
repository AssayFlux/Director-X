package com.directorx.editor.ui.editor

import com.directorx.editor.R
import com.directorx.editor.engine.ProjectAutoSave
import com.directorx.editor.model.VideoCodec

/**
 * Pure editor-facing policy for recovery, media relink feedback, and safe editor defaults.
 *
 * Kept outside EditorViewModel so the ViewModel coordinates lifecycle and state transitions,
 * while these deterministic decisions remain independently testable.
 */
internal fun shouldShowEditingSuggestion(
    suggestionId: String,
    nowMs: Long,
    snoozedUntilMs: Map<String, Long>
): Boolean = nowMs >= snoozedUntilMs.getOrDefault(suggestionId, 0L)
internal data class RecoveryOpenFeedback(
    val messageResId: Int,
    val messageArgs: List<Any> = emptyList(),
    val severity: ToastSeverity,
)

internal data class MediaRelinkOpenToastPart(
    val count: Int,
    val quantityResId: Int,
)

internal fun recoveryOpenFeedbackFor(
    outcome: ProjectAutoSave.LoadOutcome,
    expectedRecovery: Boolean,
    partialRestoreSummaryText: String? = null,
): RecoveryOpenFeedback? = when (outcome) {
    is ProjectAutoSave.LoadOutcome.Loaded -> if (outcome.report.isPartial) {
        RecoveryOpenFeedback(
            messageResId = R.string.vm_recovery_partial_toast,
            messageArgs = listOf(partialRestoreSummaryText ?: outcome.report.summary()),
            severity = ToastSeverity.Error,
        )
    } else {
        null
    }
    is ProjectAutoSave.LoadOutcome.FutureSchema -> RecoveryOpenFeedback(
        messageResId = R.string.vm_recovery_future_schema_toast,
        severity = ToastSeverity.Error,
    )
    is ProjectAutoSave.LoadOutcome.Corrupt -> RecoveryOpenFeedback(
        messageResId = R.string.vm_recovery_corrupt_toast,
        severity = ToastSeverity.Error,
    )
    ProjectAutoSave.LoadOutcome.NotFound -> if (expectedRecovery) {
        RecoveryOpenFeedback(
            messageResId = R.string.vm_recovery_not_found_toast,
            severity = ToastSeverity.Warning,
        )
    } else {
        null
    }
}

/**
 * Map the Settings "default codec" string onto the enum the export config uses.
 * The setting is stored as a bare token ("H264"/"HEVC"/"AV1"/"VP9"); an unrecognised
 * value falls back to H.264, which every Android device can encode.
 */
internal fun videoCodecForSetting(stored: String): VideoCodec =
    VideoCodec.entries.firstOrNull { it.name.equals(stored, ignoreCase = true) } ?: VideoCodec.H264

internal fun shouldBlockAutoSaveForRecoveryOutcome(outcome: ProjectAutoSave.LoadOutcome): Boolean {
    return outcome is ProjectAutoSave.LoadOutcome.FutureSchema ||
        outcome is ProjectAutoSave.LoadOutcome.Corrupt ||
        // A partial load is the dangerous case precisely because it looks fine: the
        // project opens, the user edits, and the first autosave overwrites the file
        // with the truncated version. Hold the write until they decide.
        (outcome is ProjectAutoSave.LoadOutcome.Loaded && outcome.report.isPartial)
}

internal fun mediaRelinkOpenToast(
    missingCount: Int,
    unknownCount: Int,
    healthBlockingCount: Int = 0,
    healthWarningCount: Int = 0
): List<MediaRelinkOpenToastPart> {
    val total = missingCount + unknownCount + healthBlockingCount + healthWarningCount
    if (total <= 0) return emptyList()
    val parts = buildList {
        if (missingCount > 0) add(MediaRelinkOpenToastPart(missingCount, R.plurals.vm_media_missing_sources))
        if (unknownCount > 0) add(MediaRelinkOpenToastPart(unknownCount, R.plurals.vm_media_unverified_sources))
        if (healthBlockingCount > 0) add(MediaRelinkOpenToastPart(healthBlockingCount, R.plurals.vm_media_repair_items))
        if (healthWarningCount > 0) add(MediaRelinkOpenToastPart(healthWarningCount, R.plurals.vm_media_warnings))
    }
    return parts
}
