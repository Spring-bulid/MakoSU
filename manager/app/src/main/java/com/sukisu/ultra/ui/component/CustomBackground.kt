/*
 * Adapted from LyraVoid/FolkPatch BackgroundLayer.kt (GPL-3.0).
 * Modified for MakoSU on 2026-07-18.
 */
package com.sukisu.ultra.ui.component

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import androidx.compose.material3.MaterialTheme

@Composable
fun CustomBackground(
    uriString: String,
    opacity: Float = 1f,
    blurRadius: Float = 6f,
    dimAmount: Float = 0.05f,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val windowSize = LocalWindowInfo.current.containerSize
    val fallbackColor = MaterialTheme.colorScheme.surface.copy(alpha = 1f)
    val request = remember(
        context,
        uriString,
        windowSize.width,
        windowSize.height,
        blurRadius,
        density,
    ) {
        ImageRequest.Builder(context)
            .data(uriString)
            .size(windowSize.width.coerceAtLeast(1), windowSize.height.coerceAtLeast(1))
            .scale(Scale.FILL)
            .crossfade(false)
            .apply {
                if (blurRadius > 0f) {
                    allowHardware(false)
                    transformations(
                        BackgroundBlurTransformation(
                            radiusPx = blurRadius.coerceIn(0f, 24f) * density,
                        )
                    )
                }
            }
            .build()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(fallbackColor),
    ) {
        val painter = rememberAsyncImagePainter(
            model = request,
            onError = { error ->
                Log.e(
                    "CustomBackground",
                    "Failed to load background",
                    error.result.throwable,
                )
            },
        )

        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = opacity.coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(-2f),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(-1f)
                .background(Color.Black.copy(alpha = dimAmount.coerceIn(0f, 0.3f))),
        )
    }
}
