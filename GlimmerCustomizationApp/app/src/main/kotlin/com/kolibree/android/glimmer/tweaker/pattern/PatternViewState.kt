/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.pattern

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.glimmer.R
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurve
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePatternOscillatingMode
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePatternSettings
import kotlinx.android.parcel.Parcelize

@Suppress("TooManyFunctions")
@Parcelize
internal data class PatternViewState(
    val settings: BrushingModePatternSettings
) : BaseViewState {

    val showParam1: Boolean
        get() = settings.oscillatingMode != BrushingModePatternOscillatingMode.NoOscillation

    val showParam2: Boolean
        get() = settings.oscillatingMode == BrushingModePatternOscillatingMode.ComplexPulse

    val showParam3: Boolean
        get() = settings.oscillatingMode == BrushingModePatternOscillatingMode.ComplexPulse

    val param1Title: Int
        get() = when (settings.oscillatingMode) {
            BrushingModePatternOscillatingMode.NoOscillation -> R.string.empty
            BrushingModePatternOscillatingMode.Triangular -> R.string.pwm_change_period
            BrushingModePatternOscillatingMode.ComplexPulse -> R.string.high_fs_pulse
        }

    val param2Title: Int
        get() = when (settings.oscillatingMode) {
            BrushingModePatternOscillatingMode.ComplexPulse -> R.string.initial_time_low_dc
            else -> R.string.empty
        }

    val param3Title: Int
        get() = when (settings.oscillatingMode) {
            BrushingModePatternOscillatingMode.ComplexPulse -> R.string.high_fs_pulses_count
            else -> R.string.empty
        }

    fun withSelectedCurve(curve: BrushingModeCurve): PatternViewState =
        copy(settings = settings.copy(curve = curve))

    fun withPatternMode(pattern: BrushingModePattern): PatternViewState =
        copy(settings = settings.withPattern(pattern))

    fun withPatternTypeMode(patternType: BrushingModePatternOscillatingMode): PatternViewState =
        copy(settings = settings.copy(oscillatingMode = patternType))

    fun withPatternFrequency(patternFrequency: Int) =
        copy(settings = settings.copy(patternFrequency = patternFrequency))

    fun withMinimalDutyCycleHalfPercent(minimalDutyCycleHalfPercent: Int) =
        copy(settings = settings.copy(minimalDutyCycleHalfPercent = minimalDutyCycleHalfPercent))

    fun withStrength1DutyCycleHalfPercent(strength1DutyCycleHalfPercent: Int) =
        copy(settings = settings.copy(strength1DutyCycleHalfPercent = strength1DutyCycleHalfPercent))

    fun withStrength10DutyCycleHalfPercent(strength10DutyCycleHalfPercent: Int) =
        copy(settings = settings.copy(strength10DutyCycleHalfPercent = strength10DutyCycleHalfPercent))

    fun withOscillatingPeriodTenthSecond(oscillatingPeriodTenthSecond: Int) =
        copy(settings = settings.copy(oscillatingPeriodTenthSecond = oscillatingPeriodTenthSecond))

    fun withOscillationParam1(oscillationParam1: Int) =
        copy(settings = settings.copy(oscillationParam1 = oscillationParam1))

    fun withOscillationParam2(oscillationParam2: Int) =
        copy(settings = settings.copy(oscillationParam2 = oscillationParam2))

    fun withOscillationParam3(oscillationParam3: Int) =
        copy(settings = settings.copy(oscillationParam3 = oscillationParam3))

    companion object {
        fun initial(): PatternViewState =
            PatternViewState(BrushingModePatternSettings.default())
    }
}
