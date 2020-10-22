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
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurveSettings.Companion.DEFAULT_DIVIDER
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurveSettings.Companion.DEFAULT_REFERENCE_VOLTAGE
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurveSettings.Companion.MAX_DIVIDER
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurveSettings.Companion.MAX_REFERENCE_VOLTAGE
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurveSettings.Companion.MAX_SLOPE
import org.junit.Assert.assertEquals
import org.junit.Test

/** [BrushingModeCurveSettings] unit tests */
class BrushingModeCurveSettingsTest : BaseUnitTest() {

    @Test
    fun `value of DEFAULT_REFERENCE_VOLTAGE is 3600`() {
        assertEquals(3600, DEFAULT_REFERENCE_VOLTAGE)
    }

    @Test
    fun `value of DEFAULT_DIVIDER is 600`() {
        assertEquals(600, DEFAULT_DIVIDER)
    }

    @Test
    fun `value of MAX_REFERENCE_VOLTAGE is 65535`() {
        assertEquals(65535, MAX_REFERENCE_VOLTAGE)
    }

    @Test
    fun `value of MAX_DIVIDER is 65535`() {
        assertEquals(65535, MAX_DIVIDER)
    }

    @Test
    fun `value of MAX_SLOPE is 255`() {
        assertEquals(255, MAX_SLOPE)
    }
}
