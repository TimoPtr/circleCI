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
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurve
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_BRUSHING_MODE_PATTERN
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** BrushingModePatternUtils tests */
class BrushingModePatternUtilsTest : BaseUnitTest() {

    /*
    parseBrushingPatternSettings
     */

    @Test
    fun `parseBrushingPatternSettings correctly parses pattern settings payload`() {
        val expectedPatternFrequency = 20
        val expectedCurve = BrushingModeCurve.GumCare
        val expectedMinimalDutyCycleHalfPercent = 50
        val expectedStrength1DutyCycleHalfPercent = 70
        val expectedStrength10DutyCycleHalfPercent = 20

        val payload = PayloadReader(
            byteArrayOf(
                DEVICE_PARAMETERS_BRUSHING_MODE_PATTERN,
                0b10000110.toByte(),
                BrushingModePatternOscillatingMode.NoOscillation.bleIndex.toByte(),
                0x14, 0x00,
                expectedMinimalDutyCycleHalfPercent.toByte(),
                expectedStrength1DutyCycleHalfPercent.toByte(),
                expectedStrength10DutyCycleHalfPercent.toByte(),
                0x00,
                0X00, 0x00, 0x00,
                expectedCurve.bleIndex.toByte()
            )
        )

        val settings = parseBrushingPatternSettings(payload)

        assertTrue(settings.modifiable)
        assertEquals(BrushingModePattern.Customizable.bleIndex, settings.patternId)
        assertEquals(expectedPatternFrequency, settings.patternFrequency)
        assertEquals(expectedCurve, settings.curve)
        assertEquals(
            expectedMinimalDutyCycleHalfPercent,
            settings.minimalDutyCycleHalfPercent
        )
        assertEquals(
            expectedStrength1DutyCycleHalfPercent,
            settings.strength1DutyCycleHalfPercent
        )
        assertEquals(
            expectedStrength10DutyCycleHalfPercent,
            settings.strength10DutyCycleHalfPercent
        )
        assertEquals(
            BrushingModePatternOscillatingMode.NoOscillation,
            settings.oscillatingMode
        )
        assertEquals(0, settings.oscillatingPeriodTenthSecond)
        assertEquals(0, settings.oscillationParam1)
        assertEquals(0, settings.oscillationParam2)
        assertEquals(0, settings.oscillationParam3)
    }

    /*
    setBrushingModePatternSettingsPayload
     */

    @Test
    fun `setBrushingModePatternSettingsPayload returns expected payload`() {
        val expectedPatternFrequency = 20
        val expectedCurve = BrushingModeCurve.Flat
        val expectedMinimalDutyCycleHalfPercent = 50
        val expectedStrength1DutyCycleHalfPercent = 170
        val expectedStrength10DutyCycleHalfPercent = 200
        val expectedOscillatingPeriodTenthSecond = 20
        val expectedHighFrequencyPulseCount = 4
        val expectedHighFrequencyPulsePeriodMs = 30
        val expectedInitialTimeInLowDutyCycleTenthSecond = 2

        val settings = BrushingModePatternSettings.createComplexPulse(
            patternFrequency = expectedPatternFrequency,
            minimalDutyCycleHalfPercent = expectedMinimalDutyCycleHalfPercent,
            strength10DutyCycleHalfPercent = expectedStrength10DutyCycleHalfPercent,
            strength1DutyCycleHalfPercent = expectedStrength1DutyCycleHalfPercent,
            curve = expectedCurve,
            oscillatingPeriodTenthSecond = expectedOscillatingPeriodTenthSecond,
            highFrequencyPulseCount = expectedHighFrequencyPulseCount,
            highFrequencyPulsePeriodMs = expectedHighFrequencyPulsePeriodMs,
            initialTimeInLowDutyCycleTenthSecond = expectedInitialTimeInLowDutyCycleTenthSecond
        )

        val payload = setBrushingModePatternSettingsPayload(settings)
        assertArrayEquals(
            byteArrayOf(
                DEVICE_PARAMETERS_BRUSHING_MODE_PATTERN,
                0b10000101.toByte(),
                BrushingModePatternOscillatingMode.ComplexPulse.bleIndex.toByte(),
                0x14, 0x00,
                expectedMinimalDutyCycleHalfPercent.toByte(),
                expectedStrength1DutyCycleHalfPercent.toByte(),
                expectedStrength10DutyCycleHalfPercent.toByte(),
                expectedOscillatingPeriodTenthSecond.toByte(),
                expectedHighFrequencyPulsePeriodMs.toByte(),
                expectedInitialTimeInLowDutyCycleTenthSecond.toByte(),
                expectedHighFrequencyPulseCount.toByte(),
                expectedCurve.bleIndex.toByte()
            ), payload
        )
    }
}
