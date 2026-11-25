package com.directorx.editor.ui.editor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import com.directorx.editor.model.Clip
import com.directorx.editor.model.Keyframe
import com.directorx.editor.model.KeyframeProperty

internal fun DrawScope.drawTimeRuler(
    scrollOffsetMs: Long,
    pixelsPerMs: Float,
    width: Float,
    height: Float,
    textMeasurer: TextMeasurer,
    labelColor: Color,
    majorTickColor: Color,
    minorTickColor: Color,
) {
    if (pixelsPerMs <= 0f || !pixelsPerMs.isFinite() || width <= 0f || height <= 0f) return

    // Keep major labels comfortably separated at every zoom level. The ruler
    // uses a 1-2-5 interval progression, like common non-linear editor rulers.
    val targetLabelSpacingPx = 72f * density
    val intervalsMs = longArrayOf(
        100L, 200L, 500L, 1_000L, 2_000L, 5_000L, 10_000L,
        15_000L, 30_000L, 60_000L, 120_000L, 300_000L, 600_000L,
        1_200_000L, 1_800_000L, 3_600_000L,
    )
    val majorIntervalMs = intervalsMs.firstOrNull {
        it * pixelsPerMs >= targetLabelSpacingPx
    } ?: intervalsMs.last()
    val minorIntervalMs = (majorIntervalMs / 5L).coerceAtLeast(1L)
    val firstTickMs = (scrollOffsetMs / minorIntervalMs) * minorIntervalMs
    val labelStyle = TextStyle(color = labelColor, fontSize = 9.sp)

    var currentMs = firstTickMs
    while (currentMs >= 0L) {
        val x = (currentMs - scrollOffsetMs) * pixelsPerMs
        if (x > width) break

        if (x >= 0f) {
            val isMajor = currentMs % majorIntervalMs == 0L
            val tickHeight = if (isMajor) height * 0.58f else height * 0.24f
            drawLine(
                color = if (isMajor) majorTickColor else minorTickColor,
                start = Offset(x, height - tickHeight),
                end = Offset(x, height),
                strokeWidth = if (isMajor) 1.35f * density else 0.7f * density,
            )

            if (isMajor) {
                val label = formatTimeRulerLabel(currentMs)
                val measured = textMeasurer.measure(label, labelStyle)
                // Anchor the first/last labels inside the viewport instead of
                // centering them over an edge (which previously left only "s"
                // visible for the 0-second label in an empty timeline).
                val labelLeft = (x - measured.size.width / 2f)
                    .coerceIn(0f, (width - measured.size.width).coerceAtLeast(0f))
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(labelLeft, 1f * density),
                )
            }
        }

        if (Long.MAX_VALUE - currentMs < minorIntervalMs) break
        currentMs += minorIntervalMs
    }
}

internal fun formatTimeRulerLabel(timeMs: Long): String {
    val safeMs = timeMs.coerceAtLeast(0L)
    if (safeMs < 1_000L) {
        return if (safeMs == 0L) "0:00" else "${"%.1f".format(safeMs / 1_000f)}s"
    }
    val totalSeconds = safeMs / 1_000L
    val seconds = totalSeconds % 60L
    val totalMinutes = totalSeconds / 60L
    return if (totalMinutes >= 60L) {
        "${totalMinutes / 60L}:${(totalMinutes % 60L).toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${totalMinutes}:${seconds.toString().padStart(2, '0')}"
    }
}

internal fun DrawScope.drawTimelineWaveform(samples: List<Float>, color: Color) {
    if (samples.isEmpty()) return
    val steps = (size.width / 3f).toInt().coerceAtLeast(1)
    val samplesPerStep = samples.size.toFloat() / steps
    val centerY = size.height / 2f
    val maxAmp = size.height * 0.45f

    for (i in 0 until steps) {
        val sampleIndex = (i * samplesPerStep).toInt().coerceIn(0, samples.size - 1)
        val raw = samples[sampleIndex]
        val amplitude = if (raw.isFinite()) raw.coerceIn(0f, 1f) else 0f
        val barH = (amplitude * maxAmp).coerceAtLeast(1f)
        val x = i * 3f

        drawLine(
            color = color.copy(alpha = 0.7f),
            start = Offset(x, centerY - barH),
            end = Offset(x, centerY + barH),
            strokeWidth = 2f
        )
    }
}

internal fun volumeKeyframesSorted(clip: Clip): List<Keyframe> =
    clip.keyframes
        .filter { it.property == KeyframeProperty.VOLUME }
        .sortedBy { it.timeOffsetMs }

internal fun DrawScope.drawTimelineWaveformPlaceholder(color: Color) {
    val steps = (size.width / 4f).toInt().coerceAtLeast(1)
    val centerY = size.height / 2f
    for (i in 0 until steps) {
        val x = i * 4f
        val amplitude = (kotlin.math.sin(i * 0.7) * 0.3 + 0.4).toFloat()
        val barH = (amplitude * size.height * 0.45f).coerceAtLeast(1f)
        drawLine(
            color = color.copy(alpha = 0.4f),
            start = Offset(x, centerY - barH),
            end = Offset(x, centerY + barH),
            strokeWidth = 2f
        )
    }
}
