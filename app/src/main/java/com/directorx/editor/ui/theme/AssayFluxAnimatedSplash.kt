package com.directorx.editor.ui.theme

import android.graphics.Movie
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.runtime.withFrameNanos
import com.directorx.editor.R
import kotlin.math.min

/**
 * Displays the approved AssayFlux animated GIF from res/raw.
 *
 * Movie is used rather than a network-backed image loader so the launch/exit
 * mark is available immediately and works fully offline.
 */
@Composable
fun AssayFluxAnimatedSplash(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val movie = remember(context) {
        runCatching {
            context.resources.openRawResource(R.raw.assayflux_splash).use(Movie::decodeStream)
        }.getOrNull()
    }

    if (movie == null || movie.width() <= 0 || movie.height() <= 0) {
        AssayFluxLogo(
            modifier = modifier,
            contentDescription = "Director X by AssayFlux",
        )
        return
    }

    var frame by remember(movie) { mutableIntStateOf(0) }
    LaunchedEffect(movie) {
        val durationMs = movie.duration().coerceAtLeast(1)
        val startedAt = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val elapsedMs = ((now - startedAt) / 1_000_000L).coerceAtLeast(0L)
            movie.setTime((elapsedMs % durationMs).toInt())
            frame++
        }
    }

    Canvas(
        modifier = modifier.semantics {
            contentDescription = "AssayFlux animated splash"
        },
    ) {
        frame // Read frame state to schedule a redraw for every GIF frame.
        val scale = min(size.width / movie.width(), size.height / movie.height())
        val width = movie.width() * scale
        val height = movie.height() * scale
        val left = (size.width - width) / 2f
        val top = (size.height - height) / 2f
        drawIntoCanvas { canvas ->
            movie.draw(canvas.nativeCanvas, left, top)
        }
    }
}
