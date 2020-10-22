/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer.pattern

import android.os.Parcelable
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurve
import kotlinx.android.parcel.Parcelize

/** [BrushingModePattern] settings */
// Do not keep, internal use only /!\
@VisibleForApp
@Parcelize
data class BrushingModePatternSettings internal constructor(
    val patternId: Int = BrushingModePattern.Customizable.bleIndex,
    val modifiable: Boolean = true,
    val patternFrequency: Int,
    val curve: BrushingModeCurve,
    val minimalDutyCycleHalfPercent: Int,
    val strength1DutyCycleHalfPercent: Int,
    val strength10DutyCycleHalfPercent: Int,
    val oscillatingMode: BrushingModePatternOscillatingMode,
    val oscillatingPeriodTenthSecond: Int,
    val oscillationParam1: Int = 0,
    val oscillationParam2: Int = 0,
    val oscillationParam3: Int = 0
) : Parcelable {

    fun pattern(): BrushingModePattern = BrushingModePattern.fromBleIndex(patternId)

    fun withPattern(pattern: BrushingModePattern): BrushingModePatternSettings = copy(patternId = pattern.bleIndex)

    @VisibleForApp
    companion object {

        const val MIN_PATTERN_FREQUENCY = 0
        const val MAX_PATTERN_FREQUENCY = 65535

        const val MIN_DUTY_CYCLE = 0
        const val MAX_DUTY_CYCLE = 200

        const val MIN_OSCILLATION = 0
        const val MAX_OSCILLATION = 255

        fun default() = BrushingModePatternSettings(
            patternId = 0,
            modifiable = false,
            patternFrequency = 250,
            curve = BrushingModeCurve.CleanMode,
            minimalDutyCycleHalfPercent = 40,
            strength1DutyCycleHalfPercent = 120,
            strength10DutyCycleHalfPercent = 180,
            oscillatingMode = BrushingModePatternOscillatingMode.NoOscillation,
            oscillatingPeriodTenthSecond = 0
        )

        fun createNoOscillation(
            patternFrequency: Int,
            curve: BrushingModeCurve,
            minimalDutyCycleHalfPercent: Int,
            strength1DutyCycleHalfPercent: Int,
            strength10DutyCycleHalfPercent: Int
        ) = BrushingModePatternSettings(
            patternFrequency = patternFrequency,
            curve = curve,
            oscillatingPeriodTenthSecond = 0,
            strength1DutyCycleHalfPercent = strength1DutyCycleHalfPercent,
            strength10DutyCycleHalfPercent = strength10DutyCycleHalfPercent,
            minimalDutyCycleHalfPercent = minimalDutyCycleHalfPercent,
            oscillatingMode = BrushingModePatternOscillatingMode.NoOscillation
        )

        @Suppress("LongParameterList")
        fun createTriangular(
            patternFrequency: Int,
            curve: BrushingModeCurve,
            minimalDutyCycleHalfPercent: Int,
            strength1DutyCycleHalfPercent: Int,
            strength10DutyCycleHalfPercent: Int,
            oscillatingPeriodTenthSecond: Int,
            motorPwmChangeIntervalHundredthSecond: Int
        ) = BrushingModePatternSettings(
            patternFrequency = patternFrequency,
            curve = curve,
            oscillatingMode = BrushingModePatternOscillatingMode.Triangular,
            minimalDutyCycleHalfPercent = minimalDutyCycleHalfPercent,
            strength1DutyCycleHalfPercent = strength1DutyCycleHalfPercent,
            strength10DutyCycleHalfPercent = strength10DutyCycleHalfPercent,
            oscillatingPeriodTenthSecond = oscillatingPeriodTenthSecond,
            oscillationParam1 = motorPwmChangeIntervalHundredthSecond
        )

        @Suppress("LongParameterList", "LongMethod")
        fun createComplexPulse(
            patternFrequency: Int,
            curve: BrushingModeCurve,
            minimalDutyCycleHalfPercent: Int,
            strength1DutyCycleHalfPercent: Int,
            strength10DutyCycleHalfPercent: Int,
            oscillatingPeriodTenthSecond: Int,
            highFrequencyPulsePeriodMs: Int,
            initialTimeInLowDutyCycleTenthSecond: Int,
            highFrequencyPulseCount: Int
        ) = BrushingModePatternSettings(
            patternFrequency = patternFrequency,
            curve = curve,
            oscillatingPeriodTenthSecond = oscillatingPeriodTenthSecond,
            strength1DutyCycleHalfPercent = strength1DutyCycleHalfPercent,
            strength10DutyCycleHalfPercent = strength10DutyCycleHalfPercent,
            minimalDutyCycleHalfPercent = minimalDutyCycleHalfPercent,
            oscillatingMode = BrushingModePatternOscillatingMode.ComplexPulse,
            oscillationParam1 = highFrequencyPulsePeriodMs,
            oscillationParam2 = initialTimeInLowDutyCycleTenthSecond,
            oscillationParam3 = highFrequencyPulseCount
        )
    }
}
