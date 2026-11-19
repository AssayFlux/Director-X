package com.directorx.editor.ui.theme

import android.graphics.Color as AndroidColor
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import com.directorx.editor.R

/**
 * Renders the supplied AssayFlux SVG artwork with Android's built-in WebView SVG
 * renderer. The HTML is local, JavaScript is disabled, and network loads are blocked.
 */
@Composable
fun AssayFluxLogo(
    modifier: Modifier = Modifier,
    contentDescription: String? = "Director X by AssayFlux",
) {
    // Generated directly from Assets/Director-X Logo.svg. A packaged drawable
    // avoids starting a WebView during the first Compose frame, preventing a
    // blank/late logo flash after Android's native splash screen exits.
    Image(
        painter = painterResource(id = R.drawable.directorx_compose_logo),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

/** Low-opacity texture layer for the Dashboard and Settings surfaces only. */
@Composable
fun AssayFluxTexture(
    modifier: Modifier = Modifier,
    alpha: Float = 0.045f,
) {
    AssayFluxSvgArtwork(
        assetName = "UI Design Texture.svg",
        objectFit = "cover",
        modifier = modifier,
        alpha = alpha.coerceIn(0f, 1f),
        contentDescription = null,
    )
}

@Composable
private fun AssayFluxSvgArtwork(
    assetName: String,
    objectFit: String,
    modifier: Modifier,
    alpha: Float,
    contentDescription: String?,
) {
    val html = remember(assetName, objectFit) {
        """
        <!doctype html>
        <html>
          <head>
            <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
            <style>
              html, body {
                width: 100%;
                height: 100%;
                margin: 0;
                padding: 0;
                overflow: hidden;
                background: transparent;
              }
              img {
                display: block;
                width: 100%;
                height: 100%;
                object-fit: $objectFit;
              }
            </style>
          </head>
          <body><img src="$assetName" alt=""></body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(AndroidColor.TRANSPARENT)
                settings.apply {
                    javaScriptEnabled = false
                    domStorageEnabled = false
                    allowFileAccess = true
                    allowContentAccess = false
                    allowFileAccessFromFileURLs = true
                    allowUniversalAccessFromFileURLs = false
                    blockNetworkLoads = true
                    cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                }
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                isFocusable = false
                isFocusableInTouchMode = false
                isClickable = false
                overScrollMode = View.OVER_SCROLL_NEVER
                webViewClient = WebViewClient()
                loadDataWithBaseURL(
                    "file:///android_asset/",
                    html,
                    "text/html",
                    "UTF-8",
                    null,
                )
            }
        },
        modifier = modifier
            .alpha(alpha)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            ),
        onRelease = { webView ->
            webView.stopLoading()
            webView.destroy()
        },
    )
}
