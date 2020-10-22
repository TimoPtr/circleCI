/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.pattern

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.glimmer.R
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurve
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePatternOscillatingMode
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertNotSame
import org.junit.Test

class PatternViewStateTest : BaseUnitTest() {

    @Test
    fun `showParam1 is true when not NoOscillation`() {
        assertFalse(
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.NoOscillation).showParam1
        )
        assertTrue(
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.ComplexPulse).showParam1
        )
        assertTrue(
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.Triangular).showParam1
        )
    }

    @Test
    fun `showParam2 is only true when ComplexPulse`() {
        assertFalse(
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.NoOscillation).showParam2
        )
        assertFalse(
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.Triangular).showParam2
        )
        assertTrue(
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.ComplexPulse).showParam2
        )
    }

    @Test
    fun `showParam3 is only true when ComplexPulse`() {
        assertFalse(
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.NoOscillation).showParam3
        )
        assertFalse(
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.Triangular).showParam3
        )
        assertTrue(
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.ComplexPulse).showParam3
        )
    }

    @Test
    fun `param1Title is well mapped`() {
        assertEquals(
            R.string.empty,
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.NoOscillation).param1Title
        )
        assertEquals(
            R.string.pwm_change_period,
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.Triangular).param1Title
        )
        assertEquals(
            R.string.high_fs_pulse,
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.ComplexPulse).param1Title
        )
    }

    @Test
    fun `param2title is well mapped`() {
        assertEquals(
            R.string.empty,
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.NoOscillation).param2Title
        )
        assertEquals(
            R.string.empty,
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.Triangular).param2Title
        )
        assertEquals(
            R.string.initial_time_low_dc,
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.ComplexPulse).param2Title
        )
    }

    @Test
    fun `param3title is well mapped`() {
        assertEquals(
            R.string.empty,
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.NoOscillation).param3Title
        )
        assertEquals(
            R.string.empty,
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.Triangular).param3Title
        )
        assertEquals(
            R.string.high_fs_pulses_count,
            PatternViewState.initial()
                .withPatternTypeMode(BrushingModePatternOscillatingMode.ComplexPulse).param3Title
        )
    }

    @Test
    fun `withSelectedCurve creates a copy of the settings and the VS`() {
        val initial = PatternViewState.initial()

        val updated = initial.withSelectedCurve(BrushingModeCurve.GumCare)

        assertNotSame(initial, updated)
        assertEquals(BrushingModeCurve.GumCare, updated.settings.curve)
    }

    @Test
    fun `withPatternMode creates a copy of the settings and the VS`() {
        val initial = PatternViewState.initial()

        val updated = initial.withPatternMode(BrushingModePattern.OverPressure)

        assertNotSame(initial, updated)
        assertEquals(BrushingModePattern.OverPressure, BrushingModePattern.fromBleIndex(updated.settings.patternId))
    }

    @Test
    fun `withPatternTypeMode creates a copy of the settings and the VS`() {
        val initial = PatternViewState.initial()

        val updated = initial.withPatternTypeMode(BrushingModePatternOscillatingMode.ComplexPulse)

        assertNotSame(initial, updated)
        assertEquals(BrushingModePatternOscillatingMode.ComplexPulse, updated.settings.oscillatingMode)
    }
}
