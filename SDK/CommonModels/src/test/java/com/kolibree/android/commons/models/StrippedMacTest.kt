/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons.models

import junit.framework.TestCase.assertEquals
import org.junit.Test

class StrippedMacTest {
    @Test
    fun `StrippedMac removes surrounding blank spaces`() {
        val expectedString = "AA"
        assertEquals(expectedString, " $expectedString".asStrippedMac())
        assertEquals(expectedString, "$expectedString ".asStrippedMac())
        assertEquals(expectedString, "    $expectedString   ".asStrippedMac())
    }

    @Test
    fun `StrippedMac removes new lines`() {
        val expectedString = "AA"
        assertEquals(expectedString, "\n$expectedString".asStrippedMac())
        assertEquals(expectedString, "$expectedString\n".asStrippedMac())
        assertEquals(expectedString, "\n$expectedString\n\n".asStrippedMac())
    }

    @Test
    fun `StrippedMac removes new lines and surrounding blank spaces`() {
        val expectedString = "AA"
        assertEquals(expectedString, "\n  $expectedString".asStrippedMac())
        assertEquals(expectedString, " $expectedString\n  ".asStrippedMac())
        assertEquals(expectedString, "   \n$expectedString  \n  \n".asStrippedMac())
    }

    @Test
    fun `StrippedMac removes colons`() {
        val originalString = "AA:BB"
        val expectedString = "AABB"
        assertEquals(expectedString, originalString.asStrippedMac())
    }

    @Test
    fun `StrippedMac removes hypens`() {
        val originalString = "AA-BB"
        val expectedString = "AABB"
        assertEquals(expectedString, originalString.asStrippedMac())
    }

    @Test
    fun `StrippedMac removes blank spaces`() {
        val originalString = "AA BB"
        val expectedString = "AABB"
        assertEquals(expectedString, originalString.asStrippedMac())
    }

    private fun String.asStrippedMac() = StrippedMac.fromMac(this).value
}
