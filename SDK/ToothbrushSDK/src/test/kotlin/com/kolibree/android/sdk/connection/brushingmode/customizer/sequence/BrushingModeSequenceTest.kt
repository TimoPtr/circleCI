/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer.sequence

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence.Companion.CLEAN_MODE_SEQUENCE_BLE_INDEX
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence.Companion.CUSTOM_MODE_SEQUENCE_BLE_INDEX
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence.Companion.GUM_CARE_SEQUENCE_BLE_INDEX
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence.Companion.POLISHING_MODE_SEQUENCE_BLE_INDEX
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence.Companion.WHITENING_MODE_SEQUENCE_BLE_INDEX
import org.junit.Assert.assertEquals
import org.junit.Test

/** [BrushingModeSequence] unit tests */
class BrushingModeSequenceTest : BaseUnitTest() {

    /*
    bleIndex
     */

    @Test
    fun `bleIndex of CleanMode is CLEAN_MODE_SEQUENCE_BLE_INDEX`() {
        assertEquals(CLEAN_MODE_SEQUENCE_BLE_INDEX, BrushingModeSequence.CleanMode.bleIndex)
    }

    @Test
    fun `bleIndex of WhiteningMode is WHITENING_MODE_SEQUENCE_BLE_INDEX`() {
        assertEquals(WHITENING_MODE_SEQUENCE_BLE_INDEX, BrushingModeSequence.WhiteningMode.bleIndex)
    }

    @Test
    fun `bleIndex of GumCare is GUM_CARE_SEQUENCE_BLE_INDEX`() {
        assertEquals(GUM_CARE_SEQUENCE_BLE_INDEX, BrushingModeSequence.GumCare.bleIndex)
    }

    @Test
    fun `bleIndex of PolishingMode is POLISHING_MODE_SEQUENCE_BLE_INDEX`() {
        assertEquals(POLISHING_MODE_SEQUENCE_BLE_INDEX, BrushingModeSequence.PolishingMode.bleIndex)
    }

    /*
    Constants
     */

    @Test
    fun `value of CLEAN_MODE_SEQUENCE_BLE_INDEX is 0`() {
        assertEquals(0, CLEAN_MODE_SEQUENCE_BLE_INDEX)
    }

    @Test
    fun `value of WHITENING_MODE_SEQUENCE_BLE_INDEX is 1`() {
        assertEquals(1, WHITENING_MODE_SEQUENCE_BLE_INDEX)
    }

    @Test
    fun `value of GUM_CARE_SEQUENCE_BLE_INDEX is 2`() {
        assertEquals(2, GUM_CARE_SEQUENCE_BLE_INDEX)
    }

    @Test
    fun `value of POLISHING_MODE_SEQUENCE_BLE_INDEX is 3`() {
        assertEquals(3, POLISHING_MODE_SEQUENCE_BLE_INDEX)
    }

    @Test
    fun `value of CUSTOM_MODE_SEQUENCE_BLE_INDEX is 4`() {
        assertEquals(4, CUSTOM_MODE_SEQUENCE_BLE_INDEX)
    }
}
