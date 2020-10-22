/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.crypto

import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class CryptoUtilsTest : BaseUnitTest() {

    @Test
    fun testGenerateRandomIV() {
        val iv = generateRandomIV()
        val iv2 = generateRandomIV(64)

        assertEquals(16, iv.size)
        assertEquals(64, iv2.size)
    }

    @Test
    fun testExtractHexAndToStringHex() {
        val iv = generateRandomIV()

        assertArrayEquals(iv, iv.toCompactStringHex().extractHexToByteArray())
    }

    @Test
    fun testExtractHexToByteArray() {
        assertArrayEquals(byteArrayOf(), "".extractHexToByteArray())
        assertArrayEquals(byteArrayOf(0xff.toByte(), 0, 0x42.toByte()), "FF0042".extractHexToByteArray())
    }

    @Test
    fun testToCompactStringHex() {
        assertEquals("", byteArrayOf().toCompactStringHex())
        assertEquals("FF0042", byteArrayOf(0xff.toByte(), 0, 0x42.toByte()).toCompactStringHex())
    }

    @Test
    fun testXOR() {
        assertArrayEquals(
            byteArrayOf(0x00, 0x1a, 0x0b, 0x04, 0x00, 0x06, 0x16),
            "bonjour".toByteArray().xor("buenosdias".toByteArray())
        )
    }
}
