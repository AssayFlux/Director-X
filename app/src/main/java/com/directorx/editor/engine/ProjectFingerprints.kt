package com.directorx.editor.engine

import com.directorx.editor.model.Clip
import com.directorx.editor.model.Project
import java.security.MessageDigest
import org.json.JSONObject

/**
 * Stable fingerprints for persisted project state.
 *
 * These functions deliberately live outside the autosave coordinator so their
 * canonicalization rules can be tested and reused without depending on file I/O,
 * scheduling, or recovery orchestration.
 */
internal fun autoSavePersistenceFingerprint(state: AutoSaveState): String {
    val canonical = state.copy(timestamp = 0L).serialize()
    return sha256(canonical)
}

/**
 * Fingerprint the complete persisted envelope while ignoring fields that change
 * on every save. This keeps metadata-only edits from being skipped when the
 * autosave already contains the same edit document.
 */
internal fun projectDocumentPersistenceFingerprint(document: ProjectDocument): String {
    val canonical = ProjectDocumentApplicator.capture(
        project = document.project.copy(updatedAt = 0L),
        state = document.state.copy(timestamp = 0L),
    )
    return sha256(ProjectDocumentApplicator.encode(canonical))
}

internal fun projectStateFingerprint(project: Project, state: AutoSaveState): String {
    val canonicalTracks = state.tracks.map { track ->
        track.copy(clips = track.clips.map(Clip::canonicalDocumentClip))
    }
    val canonicalState = state.copy(
        timestamp = 0L,
        playheadMs = 0L,
        tracks = canonicalTracks,
        mediaAssets = emptyList(),
    ).serialize()
    val projectIdentity = JSONObject().apply {
        put("id", project.id)
        put("name", project.name)
        put("aspectRatio", project.aspectRatio.name)
        put("frameRateNumerator", project.frameRateNumerator)
        put("frameRateDenominator", project.frameRateDenominator)
        put("resolution", project.resolution.name)
        put("templateId", project.templateId)
        put("proxyEnabled", project.proxyEnabled)
        put("version", project.version)
        put("notes", project.notes)
    }.toString()
    return sha256("$projectIdentity\n$canonicalState")
}

private fun Clip.canonicalDocumentClip(): Clip = copy(
    assetId = null,
    proxyUri = null,
    sourceColorMetadata = sourceColorMetadata.copy(inspectedAtMs = 0L),
    compoundClips = compoundClips.map(Clip::canonicalDocumentClip),
)

private fun sha256(contents: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(contents.toByteArray(Charsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte) }
