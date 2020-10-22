/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import org.junit.Assert.assertEquals
import org.junit.Test

/** [BrushingMode] tests */
class BrushingModeTest {

    /*
    bleIndex
     */

    @Test
    fun `bleIndex of Regular is 0`() {
        assertEquals(0, BrushingMode.Regular.bleIndex)
    }

    @Test
    fun `bleIndex of Slow is 1`() {
        assertEquals(1, BrushingMode.Slow.bleIndex)
    }

    @Test
    fun `bleIndex of Strong is 2`() {
        assertEquals(2, BrushingMode.Strong.bleIndex)
    }

    @Test
    fun `bleIndex of Polishing is 3`() {
        assertEquals(3, BrushingMode.Polishing.bleIndex)
    }

    @Test
    fun `bleIndex of UserDefined is 4`() {
        assertEquals(4, BrushingMode.UserDefined.bleIndex)
    }

    /*
    intensity
     */

    @Test
    fun `intensity of Regular is 1`() {
        assertEquals(1, BrushingMode.Regular.intensity)
    }

    @Test
    fun `intensity of Slow is 0`() {
        assertEquals(0, BrushingMode.Slow.intensity)
    }

    @Test
    fun `intensity of Strong is 2`() {
        assertEquals(2, BrushingMode.Strong.intensity)
    }

    @Test
    fun `intensity of Polishing is 3`() {
        assertEquals(3, BrushingMode.Polishing.intensity)
    }

    @Test
    fun `intensity of UserDefined is 4`() {
        assertEquals(4, BrushingMode.UserDefined.intensity)
    }

    /*
    defaultMode
     */

    @Test
    fun `defaultMode returns Regular`() {
        assertEquals(BrushingMode.Regular, BrushingMode.defaultMode())
    }

    /*
    lookupFromBleIndex
     */

    @Test
    fun `lookupFromBleIndex maps indexes to corresponding BrushingModes`() {
        for (brushingMode in BrushingMode.values()) {
            assertEquals(brushingMode, BrushingMode.lookupFromBleIndex(brushingMode.bleIndex))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `lookupFromBleIndex throws IllegalArgumentException on unknown ble indexes`() {
        BrushingMode.lookupFromBleIndex(99)
    }

    /*
    sortByIntensity
     */

    @Test
    fun `sortByIntensity sorts BrushingModes by ascending intensity`() {
        val sorted = BrushingMode
            .sortByIntensity(listOf(BrushingMode.Strong, BrushingMode.Slow, BrushingMode.Regular))

        assertEquals(listOf(BrushingMode.Slow, BrushingMode.Regular, BrushingMode.Strong), sorted)
    }
}
