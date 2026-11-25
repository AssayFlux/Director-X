package com.directorx.editor.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.directorx.editor.R
import com.directorx.editor.ui.ClearCutTestTags
import com.directorx.editor.ui.theme.ClearCutAccents
import com.directorx.editor.ui.theme.LocalClearCutColors

@Composable
internal fun TrimNumericInputRow(
    clipId: String,
    trimStartMs: Long,
    trimEndMs: Long,
    sourceDurationMs: Long,
    onTrimChanged: (String, Long?, Long?) -> Unit,
    onTrimDragStarted: () -> Unit,
    onTrimDragEnded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalClearCutColors.current
    var startText by remember(clipId) {
        mutableStateOf(formatTrimTime(trimStartMs))
    }
    var endText by remember(clipId) {
        mutableStateOf(formatTrimTime(trimEndMs))
    }
    var startEditActive by remember(clipId) { mutableStateOf(false) }
    var endEditActive by remember(clipId) { mutableStateOf(false) }
    val trimStartDescription = stringResource(R.string.timeline_trim_start_seconds_cd)
    val trimEndDescription = stringResource(R.string.timeline_trim_end_seconds_cd)
    // A typed trim is one undo entry that commits when its field loses focus. Done
    // on the keyboard hands focus back to the editor, which keeps its keyboard
    // shortcuts working, and a row that leaves the screen mid-edit (another clip
    // selected, the tool closed) commits what was typed rather than leaving the edit
    // open with no undo entry and nothing saved.
    val focusManager = LocalFocusManager.current
    val doneKeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
    val doneKeyboardActions = KeyboardActions(onDone = {
        if (!focusManager.moveFocus(FocusDirection.Exit)) focusManager.clearFocus()
    })
    val currentOnTrimDragEnded by rememberUpdatedState(onTrimDragEnded)
    DisposableEffect(clipId) {
        onDispose {
            if (startEditActive || endEditActive) {
                startEditActive = false
                endEditActive = false
                currentOnTrimDragEnded()
            }
        }
    }

    LaunchedEffect(clipId, trimStartMs, startEditActive) {
        if (!startEditActive) startText = formatTrimTime(trimStartMs)
    }
    LaunchedEffect(clipId, trimEndMs, endEditActive) {
        if (!endEditActive) endText = formatTrimTime(trimEndMs)
    }

    Row(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.timeline_trim_in),
            style = MaterialTheme.typography.labelSmall,
            color = ClearCutAccents.Peach
        )
        OutlinedTextField(
            value = startText,
            onValueChange = { text ->
                startText = text
                val parsed = parseTrimTime(text) ?: return@OutlinedTextField
                val clamped = parsed.coerceIn(0L, (trimEndMs - 100L).coerceAtLeast(0L))
                if (clamped == trimStartMs) return@OutlinedTextField
                if (!startEditActive) {
                    startEditActive = true
                    onTrimDragStarted()
                }
                onTrimChanged(clipId, clamped, null)
            },
            singleLine = true,
            keyboardOptions = doneKeyboardOptions,
            keyboardActions = doneKeyboardActions,
            textStyle = MaterialTheme.typography.bodySmall.copy(color = semanticColors.text),
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .onFocusChanged { focus ->
                    if (!focus.isFocused && startEditActive) {
                        startEditActive = false
                        onTrimDragEnded()
                    }
                    if (!focus.isFocused) startText = formatTrimTime(trimStartMs)
                }
                .testTag(ClearCutTestTags.TIMELINE_TRIM_START)
                .semantics { contentDescription = trimStartDescription },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ClearCutAccents.Peach,
                unfocusedBorderColor = semanticColors.surface
            )
        )
        Text(
            text = stringResource(R.string.timeline_trim_out),
            style = MaterialTheme.typography.labelSmall,
            color = ClearCutAccents.Peach
        )
        OutlinedTextField(
            value = endText,
            onValueChange = { text ->
                endText = text
                val parsed = parseTrimTime(text) ?: return@OutlinedTextField
                val clamped = parsed.coerceIn((trimStartMs + 100L), sourceDurationMs)
                if (clamped == trimEndMs) return@OutlinedTextField
                if (!endEditActive) {
                    endEditActive = true
                    onTrimDragStarted()
                }
                onTrimChanged(clipId, null, clamped)
            },
            singleLine = true,
            keyboardOptions = doneKeyboardOptions,
            keyboardActions = doneKeyboardActions,
            textStyle = MaterialTheme.typography.bodySmall.copy(color = semanticColors.text),
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .onFocusChanged { focus ->
                    if (!focus.isFocused && endEditActive) {
                        endEditActive = false
                        onTrimDragEnded()
                    }
                    if (!focus.isFocused) endText = formatTrimTime(trimEndMs)
                }
                .testTag(ClearCutTestTags.TIMELINE_TRIM_END)
                .semantics { contentDescription = trimEndDescription },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ClearCutAccents.Peach,
                unfocusedBorderColor = semanticColors.surface
            )
        )
        Text(
            text = formatTrimTime(sourceDurationMs),
            style = MaterialTheme.typography.labelSmall,
            color = semanticColors.subtext
        )
    }
}

private fun formatTrimTime(ms: Long): String {
    val totalSec = ms / 1000.0
    val min = (totalSec / 60).toInt()
    val sec = totalSec % 60
    return if (min > 0) String.format(java.util.Locale.US, "%d:%05.2f", min, sec)
    else String.format(java.util.Locale.US, "%.2f", sec)
}


internal fun parseTrimTime(text: String): Long? {
    val cleaned = text.replace(',', '.')
    if (cleaned.contains(':')) {
        val parts = cleaned.split(':')
        if (parts.size != 2) return null
        val min = parts[0].toLongOrNull() ?: return null
        val sec = parts[1].toDoubleOrNull() ?: return null
        // Reject negatives on both fields; "-1:30" must not parse to a bogus time.
        if (min < 0 || sec < 0 || sec >= 60.0 || sec.isNaN() || sec.isInfinite()) return null
        val minuteMillis = try {
            Math.multiplyExact(min, 60_000L)
        } catch (_: ArithmeticException) {
            return null
        }
        val secondMillis = (sec * 1000.0).toLong()
        return try {
            Math.addExact(minuteMillis, secondMillis)
        } catch (_: ArithmeticException) {
            null
        }
    }
    val sec = cleaned.toDoubleOrNull() ?: return null
    if (sec < 0 || sec.isNaN() || sec.isInfinite()) return null
    return (sec * 1000).toLong()
}

