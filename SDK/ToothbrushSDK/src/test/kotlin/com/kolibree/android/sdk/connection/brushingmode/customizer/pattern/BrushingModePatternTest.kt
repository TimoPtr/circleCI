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
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern.Companion.CLEAN_BRUSHING_PATTERN_INDEX
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern.Companion.CUSTOMIZABLE_PATTERN_INDEX
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern.Companion.GUM_CARE_PATTERN_INDEX
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern.Companion.OVER_PRESSURE_PATTERN_INDEX
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern.Companion.PHASE_CHANGE_PATTERN_INDEX
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern.Companion.POLISHING_PATTERN_INDEX
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern.Companion.WHITENING_BRUSHING_PATTERN_INDEX
import org.junit.Assert.assertEquals
import org.junit.Test

/** [BrushingModePattern] unit tests */
class BrushingModePatternTest : BaseUnitTest() {

    /*
    Constants
     */

    @Test
    fun `value of CLEAN_BRUSHING_PATTERN_INDEX is 0`() {
        assertEquals(0, CLEAN_BRUSHING_PATTERN_INDEX)
    }

    @Test
    fun `value of WHITENING_BRUSHING_PATTERN_INDEX is 1`() {
        assertEquals(1, WHITENING_BRUSHING_PATTERN_INDEX)
    }

    @Test
    fun `value of GUM_CARE_PATTERN_INDEX is 2`() {
        assertEquals(2, GUM_CARE_PATTERN_INDEX)
    }

    @Test
    fun `value of OVER_PRESSURE_PATTERN_INDEX is 3`() {
        assertEquals(3, OVER_PRESSURE_PATTERN_INDEX)
    }

    @Test
    fun `value of PHASE_CHANGE_PATTERN_INDEX is 4`() {
        assertEquals(4, PHASE_CHANGE_PATTERN_INDEX)
    }

    @Test
    fun `value of POLISHING_PATTERN_INDEX is 5`() {
        assertEquals(5, POLISHING_PATTERN_INDEX)
    }

    @Test
    fun `value of CUSTOMIZABLE_PATTERN_INDEX is 6`() {
        assertEquals(6, CUSTOMIZABLE_PATTERN_INDEX)
    }

    /*
    bleIndex
     */

    @Test
    fun `bleIndex of CleanBrushing is CLEAN_BRUSHING_PATTERN_INDEX`() {
        assertEquals(CLEAN_BRUSHING_PATTERN_INDEX, BrushingModePattern.CleanBrushing.bleIndex)
    }

    @Test
    fun `bleIndex of WhiteningBrushing is WHITENING_BRUSHING_PATTERN_INDEX`() {
        assertEquals(
            WHITENING_BRUSHING_PATTERN_INDEX,
            BrushingModePattern.WhiteningBrushing.bleIndex
        )
    }

    @Test
    fun `bleIndex of GumCare is GUM_CARE_PATTERN_INDEX`() {
        assertEquals(GUM_CARE_PATTERN_INDEX, BrushingModePattern.GumCare.bleIndex)
    }

    @Test
    fun `bleIndex of OverPressure is OVER_PRESSURE_PATTERN_INDEX`() {
        assertEquals(OVER_PRESSURE_PATTERN_INDEX, BrushingModePattern.OverPressure.bleIndex)
    }

    @Test
    fun `bleIndex of PhaseChange is PHASE_CHANGE_PATTERN_INDEX`() {
        assertEquals(PHASE_CHANGE_PATTERN_INDEX, BrushingModePattern.PhaseChange.bleIndex)
    }

    @Test
    fun `bleIndex of PolishingBrushing is POLISHING_PATTERN_INDEX`() {
        assertEquals(POLISHING_PATTERN_INDEX, BrushingModePattern.PolishingBrushing.bleIndex)
    }

    @Test
    fun `bleIndex of Customizable is CUSTOMIZABLE_PATTERN_INDEX`() {
        assertEquals(CUSTOMIZABLE_PATTERN_INDEX, BrushingModePattern.Customizable.bleIndex)
    }

    /*
    fromBleIndex
     */

    @Test
    fun `fromBleIndex returns expected values`() {
        BrushingModePattern.values().forEach {
            assertEquals(it, BrushingModePattern.fromBleIndex(it.bleIndex))
        }
    }
}
