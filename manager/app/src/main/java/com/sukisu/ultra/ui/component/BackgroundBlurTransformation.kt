package com.sukisu.ultra.ui.component

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import coil.size.Size
import coil.transform.Transformation
import kotlin.math.roundToInt

internal class BackgroundBlurTransformation(
    radiusPx: Float,
) : Transformation {
    private val radiusPx = radiusPx.coerceAtLeast(0f)

    override val cacheKey: String =
        "${BackgroundBlurTransformation::class.qualifiedName}:${radiusPx.roundToInt()}:$SAMPLE_SCALE:$PASSES"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        if (radiusPx < 0.5f || input.width < 2 || input.height < 2) return input

        val width = (input.width * SAMPLE_SCALE).roundToInt().coerceAtLeast(1)
        val height = (input.height * SAMPLE_SCALE).roundToInt().coerceAtLeast(1)
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Canvas(output).drawBitmap(
            input,
            null,
            Rect(0, 0, width, height),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG),
        )

        val pixels = IntArray(width * height)
        output.getPixels(pixels, 0, width, 0, 0, width, height)
        FastBoxBlur.apply(
            pixels = pixels,
            width = width,
            height = height,
            radius = (radiusPx * SAMPLE_SCALE).roundToInt().coerceAtLeast(1),
            passes = PASSES,
        )
        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }

    private companion object {
        const val SAMPLE_SCALE = 0.35f
        const val PASSES = 2
    }
}
