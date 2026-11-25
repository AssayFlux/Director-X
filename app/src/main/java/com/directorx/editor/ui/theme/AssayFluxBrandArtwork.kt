package com.directorx.editor.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.directorx.editor.R

/** Build-time raster artwork; no browser process is created during Compose rendering. */
@Composable
fun AssayFluxLogo(
    modifier: Modifier = Modifier,
    contentDescription: String? = "Director X by AssayFlux",
) {
    Image(
        painter = painterResource(id = R.drawable.directorx_compose_logo),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

/** Low-opacity texture rasterized from the checked-in SVG during the Android build. */
@Composable
fun AssayFluxTexture(
    modifier: Modifier = Modifier,
    alpha: Float = 0.045f,
) {
    Image(
        painter = painterResource(id = R.drawable.assayflux_ui_texture),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.alpha(alpha.coerceIn(0f, 1f)),
    )
}
