/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer.pattern

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePatternOscillatingMode.Companion.COMPLEX_PULSE_OSCILLATION_BLE_INDEX
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePatternOscillatingMode.Companion.NO_OSCILLATION_BLE_INDEX
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePatternOscillatingMode.Companion.TRIANGULAR_OSCILLATION_BLE_INDEX
import org.junit.Assert.assertEquals
import org.junit.Test

/** [BrushingModePatternOscillatingMode] unit tests */
class BrushingModePatternOscillatingModeTest : BaseUnitTest() {

    /*
    Constants
     */

    @Test
    fun `value of NO_OSCILLATION_BLE_INDEX is 0`() {
        assertEquals(0, NO_OSCILLATION_BLE_INDEX)
    }

    @Test
    fun `value of TRIANGULAR_OSCILLATION_BLE_INDEX is 1`() {
        assertEquals(1, TRIANGULAR_OSCILLATION_BLE_INDEX)
    }

    @Test
    fun `value of COMPLEX_PULSE_OSCILLATION_BLE_INDEX is 2`() {
        assertEquals(2, COMPLEX_PULSE_OSCILLATION_BLE_INDEX)
    }

    /*
    bleIndex
     */

    @Test
    fun `bleIndex of NoOscillation is NO_OSCILLATION_BLE_INDEX`() {
        assertEquals(
            NO_OSCILLATION_BLE_INDEX,
            BrushingModePatternOscillatingMode.NoOscillation.bleIndex
        )
    }

    @Test
    fun `bleIndex of Triangular is TRIANGULAR_OSCILLATION_BLE_INDEX`() {
        assertEquals(
            TRIANGULAR_OSCILLATION_BLE_INDEX,
            BrushingModePatternOscillatingMode.Triangular.bleIndex
        )
    }

    @Test
    fun `bleIndex of ComplexPulse is COMPLEX_PULSE_OSCILLATION_BLE_INDEX`() {
        assertEquals(
            COMPLEX_PULSE_OSCILLATION_BLE_INDEX,
            BrushingModePatternOscillatingMode.ComplexPulse.bleIndex
        )
    }

    /*
    fromBleIndex
     */

    @Test
    fun `fromBleIndex returns expected values`() {
        BrushingModePatternOscillatingMode.values().forEach {
            assertEquals(it, BrushingModePatternOscillatingMode.fromBleIndex(it.bleIndex))
        }
    }
}
