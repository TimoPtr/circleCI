/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer.curve

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurve.Companion.CLEAN_MODE_CURVE_INDEX
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurve.Companion.CUSTOM_CURVE_INDEX
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurve.Companion.FLAT_CURVE_INDEX
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurve.Companion.GUM_CARE_CURVE_INDEX
import org.junit.Assert.assertEquals
import org.junit.Test

/** [BrushingModeCurve] unit tests */
class BrushingModeCurveTest : BaseUnitTest() {

    /*
    Constants
     */

    @Test
    fun `value of FLAT_CURVE_INDEX is 0`() {
        assertEquals(0, FLAT_CURVE_INDEX)
    }

    @Test
    fun `value of CLEAN_MODE_CURVE_INDEX is 1`() {
        assertEquals(1, CLEAN_MODE_CURVE_INDEX)
    }

    @Test
    fun `value of GUM_CARE_CURVE_INDEX is 2`() {
        assertEquals(2, GUM_CARE_CURVE_INDEX)
    }

    @Test
    fun `value of CUSTOM_CURVE_INDEX is 3`() {
        assertEquals(3, CUSTOM_CURVE_INDEX)
    }

    /*
    bleIndex
     */

    @Test
    fun `bleIndex of Flat is FLAT_CURVE_INDEX`() {
        assertEquals(FLAT_CURVE_INDEX, BrushingModeCurve.Flat.bleIndex)
    }

    @Test
    fun `bleIndex of CleanMode is CLEAN_MODE_CURVE_INDEX`() {
        assertEquals(
            CLEAN_MODE_CURVE_INDEX,
            BrushingModeCurve.CleanMode.bleIndex
        )
    }

    @Test
    fun `bleIndex of GumCare is GUM_CARE_CURVE_INDEX`() {
        assertEquals(GUM_CARE_CURVE_INDEX, BrushingModeCurve.GumCare.bleIndex)
    }

    @Test
    fun `bleIndex of Custom is CUSTOM_CURVE_INDEX`() {
        assertEquals(CUSTOM_CURVE_INDEX, BrushingModeCurve.Custom.bleIndex)
    }

    /*
    fromBleIndex
     */

    @Test
    fun `fromBleIndex returns expected values`() {
        BrushingModeCurve.values().forEach {
            assertEquals(it, BrushingModeCurve.fromBleIndex(it.bleIndex))
        }
    }
}
