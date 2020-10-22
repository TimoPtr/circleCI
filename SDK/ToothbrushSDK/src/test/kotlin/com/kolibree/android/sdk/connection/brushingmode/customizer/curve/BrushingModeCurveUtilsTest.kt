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
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_BRUSHING_MODE_CURVE
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** BrushingModeCurveUtils unit tests */
class BrushingModeCurveUtilsTest : BaseUnitTest() {

    /*
    setCustomBrushingModeCurveSettingsPayload
     */

    @Test
    fun `setCustomBrushingModeCurveSettingsPayload returns expected payload`() {
        val expectedCurve = BrushingModeCurve.Custom
        val expectedReferenceVoltageMv = 124
        val expectedDivider = 16
        val expectedSlope10PercentsDutyCycle = 10
        val expectedSlope20PercentsDutyCycle = 20
        val expectedSlope30PercentsDutyCycle = 30
        val expectedSlope40PercentsDutyCycle = 40
        val expectedSlope50PercentsDutyCycle = 50
        val expectedSlope60PercentsDutyCycle = 60
        val expectedSlope70PercentsDutyCycle = 70
        val expectedSlope80PercentsDutyCycle = 80
        val expectedSlope90PercentsDutyCycle = 90

        val payload = setCustomBrushingModeCurveSettingsPayload(
            BrushingModeCurveSettings(
                curveId = expectedCurve.bleIndex,
                modifiable = true,
                referenceVoltageMv = expectedReferenceVoltageMv,
                divider = expectedDivider,
                slope10PercentsDutyCycle = expectedSlope10PercentsDutyCycle,
                slope20PercentsDutyCycle = expectedSlope20PercentsDutyCycle,
                slope30PercentsDutyCycle = expectedSlope30PercentsDutyCycle,
                slope40PercentsDutyCycle = expectedSlope40PercentsDutyCycle,
                slope50PercentsDutyCycle = expectedSlope50PercentsDutyCycle,
                slope60PercentsDutyCycle = expectedSlope60PercentsDutyCycle,
                slope70PercentsDutyCycle = expectedSlope70PercentsDutyCycle,
                slope80PercentsDutyCycle = expectedSlope80PercentsDutyCycle,
                slope90PercentsDutyCycle = expectedSlope90PercentsDutyCycle
            )
        )

        assertArrayEquals(
            byteArrayOf(
                DEVICE_PARAMETERS_BRUSHING_MODE_CURVE,
                0b10000011.toByte(),
                0x7C, 0x00,
                0x10, 0x00,
                expectedSlope10PercentsDutyCycle.toByte(),
                expectedSlope20PercentsDutyCycle.toByte(),
                expectedSlope30PercentsDutyCycle.toByte(),
                expectedSlope40PercentsDutyCycle.toByte(),
                expectedSlope50PercentsDutyCycle.toByte(),
                expectedSlope60PercentsDutyCycle.toByte(),
                expectedSlope70PercentsDutyCycle.toByte(),
                expectedSlope80PercentsDutyCycle.toByte(),
                expectedSlope90PercentsDutyCycle.toByte()
            ),
            payload
        )
    }

    /*
    parseCustomBrushingModeCurveSettingsPayload
     */

    @Test
    fun `parseCustomBrushingModeCurveSettingsPayload returns expected settings`() {
        val expectedCurve = BrushingModeCurve.Custom
        val expectedReferenceVoltageMv = 124
        val expectedDivider = 16
        val expectedSlope10PercentsDutyCycle = 10
        val expectedSlope20PercentsDutyCycle = 20
        val expectedSlope30PercentsDutyCycle = 30
        val expectedSlope40PercentsDutyCycle = 40
        val expectedSlope50PercentsDutyCycle = 50
        val expectedSlope60PercentsDutyCycle = 60
        val expectedSlope70PercentsDutyCycle = 70
        val expectedSlope80PercentsDutyCycle = 80
        val expectedSlope90PercentsDutyCycle = 90

        val settings = parseCustomBrushingModeCurveSettingsPayload(PayloadReader(
            byteArrayOf(
                DEVICE_PARAMETERS_BRUSHING_MODE_CURVE,
                0b10000011.toByte(),
                0x7C, 0x00,
                0x10, 0x00,
                expectedSlope10PercentsDutyCycle.toByte(),
                expectedSlope20PercentsDutyCycle.toByte(),
                expectedSlope30PercentsDutyCycle.toByte(),
                expectedSlope40PercentsDutyCycle.toByte(),
                expectedSlope50PercentsDutyCycle.toByte(),
                expectedSlope60PercentsDutyCycle.toByte(),
                expectedSlope70PercentsDutyCycle.toByte(),
                expectedSlope80PercentsDutyCycle.toByte(),
                expectedSlope90PercentsDutyCycle.toByte()
            )
        ))

        assertTrue(settings.modifiable)
        assertEquals(expectedCurve.bleIndex, settings.curveId)
        assertEquals(expectedReferenceVoltageMv, settings.referenceVoltageMv)
        assertEquals(expectedDivider, settings.divider)
        assertEquals(expectedSlope10PercentsDutyCycle, settings.slope10PercentsDutyCycle)
        assertEquals(expectedSlope20PercentsDutyCycle, settings.slope20PercentsDutyCycle)
        assertEquals(expectedSlope30PercentsDutyCycle, settings.slope30PercentsDutyCycle)
        assertEquals(expectedSlope40PercentsDutyCycle, settings.slope40PercentsDutyCycle)
        assertEquals(expectedSlope50PercentsDutyCycle, settings.slope50PercentsDutyCycle)
        assertEquals(expectedSlope60PercentsDutyCycle, settings.slope60PercentsDutyCycle)
        assertEquals(expectedSlope70PercentsDutyCycle, settings.slope70PercentsDutyCycle)
        assertEquals(expectedSlope80PercentsDutyCycle, settings.slope80PercentsDutyCycle)
        assertEquals(expectedSlope90PercentsDutyCycle, settings.slope90PercentsDutyCycle)
    }
}
