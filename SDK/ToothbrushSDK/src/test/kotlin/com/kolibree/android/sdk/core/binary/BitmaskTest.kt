/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.binary

import com.kolibree.android.app.test.BaseUnitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** [Bitmask] tests  */
class BitmaskTest : BaseUnitTest() {

    /*
    set
     */

    @Test
    fun `set correctly sets bits to 1`() {
        val mask = Bitmask(0b01001000)
        mask.set(1, true)
        assertEquals(0b01001010.toByte(), mask.get())
        mask.set(4, true)
        assertEquals(0b01011010.toByte(), mask.get())
    }

    @Test
    fun `sets conserves a bit to 1 after setting it to 1 again`() {
        val mask = Bitmask(0b01001000)
        mask.set(1, true)
        assertEquals(0b01001010.toByte(), mask.get())
        mask.set(1, true)
        assertEquals(0b01001010.toByte(), mask.get())
    }

    @Test
    fun `set correctly sets bits to 0`() {
        val mask = Bitmask(0b01101111)
        mask.set(0, false)
        assertEquals(0b01101110.toByte(), mask.get())
        mask.set(3, false)
        assertEquals(0b01100110.toByte(), mask.get())
    }

    @Test
    fun `sets conserves a bit to 0 after setting it to 0 again`() {
        val mask = Bitmask(0b01001000)
        mask.set(3, false)
        assertEquals(0b01000000.toByte(), mask.get())
        mask.set(3, false)
        assertEquals(0b01000000.toByte(), mask.get())
    }

    /*
    getBit
     */

    @Test
    fun `getBits correctly get bits at right indexes`() {
        val mask = Bitmask(0b01101111)
        assertTrue(mask.getBit(0))
        assertTrue(mask.getBit(1))
        assertTrue(mask.getBit(2))
        assertTrue(mask.getBit(3))
        assertFalse(mask.getBit(4))
        assertTrue(mask.getBit(5))
        assertTrue(mask.getBit(6))
        assertFalse(mask.getBit(7))
    }

    /*
    getLowNibble
     */

    @Test
    fun `getLowNibble returns expected value`() {
        val bits: Byte = 0xAB.toByte()

        assertEquals(0x0B.toByte(), Bitmask(bits).lowNibble)
    }

    /*
    setLowNibble
     */

    @Test
    fun `setLowNibble correctly sets low nibble`() {
        val bitmask = Bitmask(0x3C.toByte())

        bitmask.lowNibble = 0x01

        assertEquals(0x31.toByte(), bitmask.get())
    }

    /*
    getHighNibble
     */

    @Test
    fun `getHighNibble returns expected value`() {
        val bits: Byte = 0xAB.toByte()

        assertEquals(0x0A.toByte(), Bitmask(bits).highNibble)
    }

    /*
    setHighNibble
     */

    @Test
    fun `setHighNibble correctly sets high nibble`() {
        val bitmask = Bitmask(0x3C.toByte())

        bitmask.highNibble = 0x04

        assertEquals(0x4C.toByte(), bitmask.get())
    }
}
