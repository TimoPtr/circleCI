/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.opengl.colors

import junit.framework.AssertionFailedError
import org.junit.Assert.assertEquals
import org.junit.Test

/** [ColorConverter] tests  */
class ColorConverterTest {

    /*
    checkOpenGlColor
     */

    @Test
    fun checkOpenGlColor_supportsRGB888() {
        ColorConverter.checkOpenGlColor(0f, 0f, 0f)
    }

    @Test
    fun checkOpenGlColor_supportsRGBA8888() {
        ColorConverter.checkOpenGlColor(0f, 0f, 0f, 0f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun checkOpenGlColor_throwsExceptionOnNotColorArrayArgument() {
        ColorConverter.checkOpenGlColor(0f, 0f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun checkOpenGlColor_throwsExceptionIfComponentOutOfRange() {
        ColorConverter.checkOpenGlColor(1f, 1f, 2f, 1f)
    }

    /*
    extractAndConvertComponent
     */

    @Test
    fun extractAndConvertComponent_isLinear() {
        assertEquals(0.toByte(), ColorConverter.extractAndConvertComponent(0x00000000, 0))
        assertEquals((-52).toByte(), ColorConverter.extractAndConvertComponent(-0x34000000, 0))
        assertEquals((-1).toByte(), ColorConverter.extractAndConvertComponent(-0x1000000, 0))
    }

    @Test
    fun extractAndConvertComponent_convertsGoodIndex() {
        assertEquals((-1).toByte(), ColorConverter.extractAndConvertComponent(-0x1000000, 0))
        assertEquals((-1).toByte(), ColorConverter.extractAndConvertComponent(0x00FF0000, 1))
        assertEquals((-1).toByte(), ColorConverter.extractAndConvertComponent(0x0000FF00, 2))
        assertEquals((-1).toByte(), ColorConverter.extractAndConvertComponent(0x000000FF, 3))
    }

    /*
    toAndroidColor
     */

    @Test
    fun toAndroidColor_correctlyConvertsRGBARed() {
        assertEquals(JAVA_RED, ColorConverter.toAndroidColor(1f, 0f, 0f, 1f))
    }

    @Test
    fun toAndroidColor_correctlyConvertsRGBAGreen() {
        assertEquals(JAVA_GREEN, ColorConverter.toAndroidColor(0f, 1f, 0f, 1f))
    }

    @Test
    fun toAndroidColor_correctlyConvertsRGBABlue() {
        assertEquals(JAVA_BLUE, ColorConverter.toAndroidColor(0f, 0f, 1f, 1f))
    }

    @Test
    fun toAndroidColor_correctlyConvertsRGBABlack() {
        assertEquals(JAVA_BLACK, ColorConverter.toAndroidColor(0f, 0f, 0f, 1f))
    }

    @Test
    fun toAndroidColor_correctlyConvertsRGBAWhite() {
        assertEquals(JAVA_WHITE, ColorConverter.toAndroidColor(1f, 1f, 1f, 1f))
    }

    @Test
    fun toAndroidColor_correctlyConvertsRGBATransparent() {
        assertEquals(JAVA_TRANSPARENT, ColorConverter.toAndroidColor(0f, 0f, 0f, 0f))
    }

    @Test
    fun toAndroidColor_correctlyConvertsRGBRed() {
        assertEquals(JAVA_RED, ColorConverter.toAndroidColor(1f, 0f, 0f))
    }

    @Test
    fun toAndroidColor_correctlyConvertsRGBGreen() {
        assertEquals(JAVA_GREEN, ColorConverter.toAndroidColor(0f, 1f, 0f))
    }

    @Test
    fun toAndroidColor_correctlyConvertsRGBBlue() {
        assertEquals(JAVA_BLUE, ColorConverter.toAndroidColor(0f, 0f, 1f))
    }

    @Test
    fun toAndroidColor_correctlyConvertsRGBBlack() {
        assertEquals(JAVA_BLACK, ColorConverter.toAndroidColor(0f, 0f, 0f))
    }

    @Test
    fun toAndroidColor_correctlyConvertsRGBWhite() {
        assertEquals(JAVA_WHITE, ColorConverter.toAndroidColor(1f, 1f, 1f))
    }

    @Test
    fun toAndroidColor_isPreciseNotLikeAndroidColorClass() {
        // 0.999 is around 254,7 so it has to be 255 not 254
        assertEquals(JAVA_WHITE, ColorConverter.toAndroidColor(0.999f, 0.999f, 0.999f))
    }

    /*
    toEglRGB
     */

    @Test
    fun toEglRGB_correctlyConvertsRGBRed() {
        assertByteArrayEquals(
            byteArrayOf(255.toByte(), 0, 0),
            ColorConverter.toEglRGB(JAVA_RED)
        )
    }

    @Test
    fun toEglRGB_correctlyConvertsRGBGreen() {
        assertByteArrayEquals(
            byteArrayOf(0, 255.toByte(), 0),
            ColorConverter.toEglRGB(JAVA_GREEN)
        )
    }

    @Test
    fun toEglRGB_correctlyConvertsRGBBlue() {
        assertByteArrayEquals(
            byteArrayOf(0, 0, 255.toByte()),
            ColorConverter.toEglRGB(JAVA_BLUE)
        )
    }

    @Test
    fun toEglRGB_correctlyConvertsRGBBlack() {
        assertByteArrayEquals(
            byteArrayOf(0, 0, 0),
            ColorConverter.toEglRGB(JAVA_BLACK)
        )
    }

    @Test
    fun toEglRGB_correctlyConvertsRGBWhite() {
        assertByteArrayEquals(
            byteArrayOf(255.toByte(), 255.toByte(), 255.toByte()),
            ColorConverter.toEglRGB(JAVA_WHITE)
        )
    }

    /*
    toEglRGBA
     */

    @Test
    fun toEglRGBA_correctlyConvertsRGBARed() {
        assertByteArrayEquals(
            byteArrayOf(255.toByte(), 0, 0, 255.toByte()),
            ColorConverter.toEglRGBA(JAVA_RED)
        )
    }

    @Test
    fun toEglRGBA_correctlyConvertsRGBAGreen() {
        assertByteArrayEquals(
            byteArrayOf(0, 255.toByte(), 0, 255.toByte()),
            ColorConverter.toEglRGBA(JAVA_GREEN)
        )
    }

    @Test
    fun toEglRGBA_correctlyConvertsRGBABlue() {
        assertByteArrayEquals(
            byteArrayOf(0, 0, 255.toByte(), 255.toByte()),
            ColorConverter.toEglRGBA(JAVA_BLUE)
        )
    }

    @Test
    fun toEglRGBA_correctlyConvertsRGBABlack() {
        assertByteArrayEquals(
            byteArrayOf(0, 0, 0, 255.toByte()),
            ColorConverter.toEglRGBA(JAVA_BLACK)
        )
    }

    @Test
    fun toEglRGBA_correctlyConvertsRGBAWhite() {
        assertByteArrayEquals(
            byteArrayOf(255.toByte(), 255.toByte(), 255.toByte(), 255.toByte()),
            ColorConverter.toEglRGBA(JAVA_WHITE)
        )
    }

    @Test
    fun toEglRGBA_correctlyConvertsRGBATransparent() {
        assertByteArrayEquals(
            byteArrayOf(0, 0, 0, 0),
            ColorConverter.toEglRGBA(JAVA_TRANSPARENT)
        )
    }

    companion object {

        /*
        The following values have been copied from the Android SDK
         */

        private const val JAVA_RED = -0x10000
        private const val JAVA_GREEN = -0xff0100
        private const val JAVA_BLUE = -0xffff01
        private const val JAVA_BLACK = -0x1000000
        private const val JAVA_WHITE = -0x1
        private const val JAVA_TRANSPARENT = 0x0

        /*
        Utils
         */

        private fun assertByteArrayEquals(f1: ByteArray, f2: ByteArray) {
            if (f1.size != f2.size) {
                throw AssertionFailedError("Lengths are different")
            }

            for (i in f1.indices) {
                assertEquals(f1[i], f2[i])
            }
        }
    }
}
