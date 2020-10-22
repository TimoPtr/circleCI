/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.opengl.colors

import androidx.annotation.ColorInt
import androidx.annotation.VisibleForTesting
import kotlin.math.roundToInt

/**
 * Helper for Android / OpenGL colors translations that doesn't use Android SDK's non mockable
 * classes
 *
 *
 * Supported OpenGL formats are RGBA8888 and RGB888 Android uses ARGB8888
 */
@Suppress("MagicNumber", "SpreadOperator")
internal object ColorConverter {

    /**
     * Convert an OpenGL color to a Java Color int
     *
     * @param openGlColor float array like {1f, 1f, 1f, 1f} for opaque white
     * @return ColorInt int Java color
     */
    @ColorInt
    fun toAndroidColor(vararg openGlColor: Float): Int {
        checkOpenGlColor(*openGlColor)
        var androidColor = 0

        // RGB part
        for (i in 0..2) {
            androidColor += (openGlColor[i] * 255f).roundToInt() shl 16 - 8 * i
        }

        // Then the alpha layer
        androidColor += if (openGlColor.size == 4) { // RGBA
            (openGlColor[3] * 255f).roundToInt() shl 24
        } else { // RGB (alpha is always 255
            -0x1000000
        }

        return androidColor
    }

    /**
     * Convert an Android ColorInt to an OpenGL RGB byte array
     *
     * @param color [ColorInt] int ARGB color
     * @return non null byte array
     */
    fun toEglRGB(@ColorInt color: Int) =
        byteArrayOf(
            extractAndConvertComponent(
                color,
                1
            ),
            extractAndConvertComponent(
                color,
                2
            ),
            extractAndConvertComponent(
                color,
                3
            )
        )

    /**
     * Convert an Android ColorInt to an OpenGL RGBA float array
     *
     * @param color [ColorInt] int ARGB color
     * @return non null byte array
     */
    fun toEglRGBA(@ColorInt color: Int) =
        byteArrayOf(
            extractAndConvertComponent(
                color,
                1
            ),
            extractAndConvertComponent(
                color,
                2
            ),
            extractAndConvertComponent(
                color,
                3
            ),
            extractAndConvertComponent(
                color,
                0
            )
        )

    /**
     * Convert an Android ColorInt to an OpenGL RGBA float array
     *
     * @param color [ColorInt] int ARGB color
     * @return [FloatArray]
     */
    fun toEglHdr(@ColorInt color: Int) =
        floatArrayOf(
            extractAndConvertHdrComponent(
                color,
                1
            ),
            extractAndConvertHdrComponent(
                color,
                2
            ),
            extractAndConvertHdrComponent(
                color,
                3
            ),
            extractAndConvertHdrComponent(
                color,
                0
            )
        )

    @VisibleForTesting
    internal fun checkOpenGlColor(vararg openGlColor: Float) {
        require(!(openGlColor.size != 3 && openGlColor.size != 4)) {
            "Invalid OpenGL color: " + openGlColor.contentToString()
        }

        for (component in openGlColor) {
            require(!(component < 0f || component > 1f)) {
                ("Invalid OpenGL color: " +
                    openGlColor.contentToString() +
                    ", components must be [0f, 1f]")
            }
        }
    }

    @VisibleForTesting
    internal fun extractAndConvertComponent(@ColorInt color: Int, componentIndex: Int) =
        (color shr 24 - 8 * componentIndex and 0xFF).toByte()

    /**
     * Extract a color component from a 32 bits ARGB color and convert it to HDR color format
     *
     * @param color RGBA color int
     * @param componentIndex R G B A component index
     * @return HDR float RGBA component
     */
    fun extractAndConvertHdrComponent(@ColorInt color: Int, componentIndex: Int) =
        (color shr 24 - 8 * componentIndex and 0xFF) / 255f
}
