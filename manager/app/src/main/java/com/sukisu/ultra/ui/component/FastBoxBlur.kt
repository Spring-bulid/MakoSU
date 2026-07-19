package com.sukisu.ultra.ui.component

internal object FastBoxBlur {
    fun apply(
        pixels: IntArray,
        width: Int,
        height: Int,
        radius: Int,
        passes: Int = 2,
    ) {
        require(width > 0 && height > 0)
        require(pixels.size == width * height)
        if (radius < 1 || passes < 1 || pixels.size < 2) return

        val safeRadius = radius.coerceAtMost(maxOf(width, height))
        val divisor = safeRadius * 2 + 1
        val division = IntArray(256 * divisor) { it / divisor }
        val scratch = IntArray(pixels.size)

        repeat(passes) {
            horizontal(pixels, scratch, width, height, safeRadius, division)
            vertical(scratch, pixels, width, height, safeRadius, division)
        }
    }

    private fun horizontal(
        source: IntArray,
        destination: IntArray,
        width: Int,
        height: Int,
        radius: Int,
        division: IntArray,
    ) {
        for (y in 0 until height) {
            val row = y * width
            var alpha = 0
            var red = 0
            var green = 0
            var blue = 0

            for (offset in -radius..radius) {
                val color = source[row + offset.coerceIn(0, width - 1)]
                alpha += color ushr 24
                red += color ushr 16 and 0xff
                green += color ushr 8 and 0xff
                blue += color and 0xff
            }

            for (x in 0 until width) {
                destination[row + x] = averageColor(alpha, red, green, blue, division)

                val outgoing = source[row + (x - radius).coerceIn(0, width - 1)]
                val incoming = source[row + (x + radius + 1).coerceIn(0, width - 1)]
                alpha += (incoming ushr 24) - (outgoing ushr 24)
                red += (incoming ushr 16 and 0xff) - (outgoing ushr 16 and 0xff)
                green += (incoming ushr 8 and 0xff) - (outgoing ushr 8 and 0xff)
                blue += (incoming and 0xff) - (outgoing and 0xff)
            }
        }
    }

    private fun vertical(
        source: IntArray,
        destination: IntArray,
        width: Int,
        height: Int,
        radius: Int,
        division: IntArray,
    ) {
        for (x in 0 until width) {
            var alpha = 0
            var red = 0
            var green = 0
            var blue = 0

            for (offset in -radius..radius) {
                val color = source[offset.coerceIn(0, height - 1) * width + x]
                alpha += color ushr 24
                red += color ushr 16 and 0xff
                green += color ushr 8 and 0xff
                blue += color and 0xff
            }

            for (y in 0 until height) {
                destination[y * width + x] = averageColor(alpha, red, green, blue, division)

                val outgoing = source[(y - radius).coerceIn(0, height - 1) * width + x]
                val incoming = source[(y + radius + 1).coerceIn(0, height - 1) * width + x]
                alpha += (incoming ushr 24) - (outgoing ushr 24)
                red += (incoming ushr 16 and 0xff) - (outgoing ushr 16 and 0xff)
                green += (incoming ushr 8 and 0xff) - (outgoing ushr 8 and 0xff)
                blue += (incoming and 0xff) - (outgoing and 0xff)
            }
        }
    }

    private fun averageColor(
        alpha: Int,
        red: Int,
        green: Int,
        blue: Int,
        division: IntArray,
    ): Int = (division[alpha] shl 24) or
            (division[red] shl 16) or
            (division[green] shl 8) or
            division[blue]
}
