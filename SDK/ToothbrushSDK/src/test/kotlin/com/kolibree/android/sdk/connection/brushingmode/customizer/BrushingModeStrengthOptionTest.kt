/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeStrengthOption.Companion.ONE_LEVEL_BLE_VALUE
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeStrengthOption.Companion.TEN_LEVELS_BLE_VALUE
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeStrengthOption.Companion.THREE_LEVELS_BLE_VALUE
import org.junit.Assert.assertEquals
import org.junit.Test

/** [BrushingModeStrengthOption] unit tests */
class BrushingModeStrengthOptionTest : BaseUnitTest() {

    /*
    bleValue
     */

    @Test
    fun `bleValue of OneLevel is ONE_LEVEL_BLE_VALUE`() {
        assertEquals(ONE_LEVEL_BLE_VALUE, BrushingModeStrengthOption.OneLevel.bleValue)
    }

    @Test
    fun `bleValue of ThreeLevels is THREE_LEVELS_BLE_VALUE`() {
        assertEquals(THREE_LEVELS_BLE_VALUE, BrushingModeStrengthOption.ThreeLevels.bleValue)
    }

    @Test
    fun `bleValue of TenLevels is TEN_LEVELS_BLE_VALUE`() {
        assertEquals(TEN_LEVELS_BLE_VALUE, BrushingModeStrengthOption.TenLevels.bleValue)
    }

    /*
    Constants
     */

    @Test
    fun `value of ONE_LEVEL_BLE_VALUE is 0`() {
        assertEquals(0, ONE_LEVEL_BLE_VALUE)
    }

    @Test
    fun `value of THREE_LEVELS_BLE_VALUE is 1`() {
        assertEquals(1, THREE_LEVELS_BLE_VALUE)
    }

    @Test
    fun `value of TEN_LEVELS_BLE_VALUE is 2`() {
        assertEquals(2, TEN_LEVELS_BLE_VALUE)
    }
}
