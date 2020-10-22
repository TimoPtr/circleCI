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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrushingModePatternSettingsTest : BaseUnitTest() {

    /*
    createNoOscillation
     */

    @Test
    fun `createNoOscillation returns expected settings`() {
        val expectedPatternFrequency = 120
        val expectedCurve = BrushingModeCurve.GumCare
        val expectedMinimalDutyCycleHalfPercent = 50
        val expectedStrength1DutyCycleHalfPercent = 170
        val expectedStrength10DutyCycleHalfPercent = 200

        val settings = BrushingModePatternSettings.createNoOscillation(
            patternFrequency = expectedPatternFrequency,
            minimalDutyCycleHalfPercent = expectedMinimalDutyCycleHalfPercent,
            strength10DutyCycleHalfPercent = expectedStrength10DutyCycleHalfPercent,
            strength1DutyCycleHalfPercent = expectedStrength1DutyCycleHalfPercent,
            curve = expectedCurve
        )

        assertTrue(settings.modifiable)
        assertEquals(BrushingModePattern.Customizable.bleIndex, settings.patternId)
        assertEquals(expectedPatternFrequency, settings.patternFrequency)
        assertEquals(expectedCurve, settings.curve)
        assertEquals(expectedMinimalDutyCycleHalfPercent, settings.minimalDutyCycleHalfPercent)
        assertEquals(expectedStrength1DutyCycleHalfPercent, settings.strength1DutyCycleHalfPercent)
        assertEquals(
            expectedStrength10DutyCycleHalfPercent,
            settings.strength10DutyCycleHalfPercent
        )
        assertEquals(BrushingModePatternOscillatingMode.NoOscillation, settings.oscillatingMode)
        assertEquals(0, settings.oscillatingPeriodTenthSecond)
        assertEquals(0, settings.oscillationParam1)
        assertEquals(0, settings.oscillationParam2)
        assertEquals(0, settings.oscillationParam3)
    }

    /*
    createTriangular
     */

    @Test
    fun `createTriangular returns expected settings`() {
        val expectedPatternFrequency = 120
        val expectedCurve = BrushingModeCurve.Flat
        val expectedMinimalDutyCycleHalfPercent = 50
        val expectedStrength1DutyCycleHalfPercent = 170
        val expectedStrength10DutyCycleHalfPercent = 200
        val expectedOscillatingPeriodTenthSecond = 20
        val expectedMotorPwmChangeIntervalHundredthSecond = 3

        val settings = BrushingModePatternSettings.createTriangular(
            patternFrequency = expectedPatternFrequency,
            minimalDutyCycleHalfPercent = expectedMinimalDutyCycleHalfPercent,
            strength10DutyCycleHalfPercent = expectedStrength10DutyCycleHalfPercent,
            strength1DutyCycleHalfPercent = expectedStrength1DutyCycleHalfPercent,
            curve = expectedCurve,
            oscillatingPeriodTenthSecond = expectedOscillatingPeriodTenthSecond,
            motorPwmChangeIntervalHundredthSecond = expectedMotorPwmChangeIntervalHundredthSecond
        )

        assertTrue(settings.modifiable)
        assertEquals(BrushingModePattern.Customizable.bleIndex, settings.patternId)
        assertEquals(expectedPatternFrequency, settings.patternFrequency)
        assertEquals(expectedCurve, settings.curve)
        assertEquals(expectedMinimalDutyCycleHalfPercent, settings.minimalDutyCycleHalfPercent)
        assertEquals(expectedStrength1DutyCycleHalfPercent, settings.strength1DutyCycleHalfPercent)
        assertEquals(
            expectedStrength10DutyCycleHalfPercent,
            settings.strength10DutyCycleHalfPercent
        )
        assertEquals(BrushingModePatternOscillatingMode.Triangular, settings.oscillatingMode)
        assertEquals(expectedOscillatingPeriodTenthSecond, settings.oscillatingPeriodTenthSecond)
        assertEquals(expectedMotorPwmChangeIntervalHundredthSecond, settings.oscillationParam1)
        assertEquals(0, settings.oscillationParam2)
        assertEquals(0, settings.oscillationParam3)
    }

    /*
    createComplexPulse
     */

    @Test
    fun `createComplexPulse returns expected settings`() {
        val expectedPatternFrequency = 120
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

        assertTrue(settings.modifiable)
        assertEquals(BrushingModePattern.Customizable.bleIndex, settings.patternId)
        assertEquals(expectedPatternFrequency, settings.patternFrequency)
        assertEquals(expectedCurve, settings.curve)
        assertEquals(expectedMinimalDutyCycleHalfPercent, settings.minimalDutyCycleHalfPercent)
        assertEquals(expectedStrength1DutyCycleHalfPercent, settings.strength1DutyCycleHalfPercent)
        assertEquals(
            expectedStrength10DutyCycleHalfPercent,
            settings.strength10DutyCycleHalfPercent
        )
        assertEquals(BrushingModePatternOscillatingMode.ComplexPulse, settings.oscillatingMode)
        assertEquals(expectedOscillatingPeriodTenthSecond, settings.oscillatingPeriodTenthSecond)
        assertEquals(expectedHighFrequencyPulsePeriodMs, settings.oscillationParam1)
        assertEquals(expectedInitialTimeInLowDutyCycleTenthSecond, settings.oscillationParam2)
        assertEquals(expectedHighFrequencyPulseCount, settings.oscillationParam3)
    }
}
