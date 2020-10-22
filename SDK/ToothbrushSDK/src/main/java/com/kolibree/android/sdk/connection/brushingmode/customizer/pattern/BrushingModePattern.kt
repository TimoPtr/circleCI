/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer.pattern

import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp

/** Brushing Modes Patterns definitions */
// Do not keep, internal use only /!\
@VisibleForApp
enum class BrushingModePattern(internal val bleIndex: Int) {

    CleanBrushing(bleIndex = BrushingModePattern.CLEAN_BRUSHING_PATTERN_INDEX),

    WhiteningBrushing(bleIndex = BrushingModePattern.WHITENING_BRUSHING_PATTERN_INDEX),

    GumCare(bleIndex = BrushingModePattern.GUM_CARE_PATTERN_INDEX),

    OverPressure(bleIndex = BrushingModePattern.OVER_PRESSURE_PATTERN_INDEX),

    PhaseChange(bleIndex = BrushingModePattern.PHASE_CHANGE_PATTERN_INDEX),

    PolishingBrushing(bleIndex = BrushingModePattern.POLISHING_PATTERN_INDEX),

    Customizable(bleIndex = BrushingModePattern.CUSTOMIZABLE_PATTERN_INDEX);

    @VisibleForApp
    companion object {

        @VisibleForTesting
        internal const val CLEAN_BRUSHING_PATTERN_INDEX = 0

        @VisibleForTesting
        internal const val WHITENING_BRUSHING_PATTERN_INDEX = 1

        @VisibleForTesting
        internal const val GUM_CARE_PATTERN_INDEX = 2

        @VisibleForTesting
        internal const val OVER_PRESSURE_PATTERN_INDEX = 3

        @VisibleForTesting
        internal const val PHASE_CHANGE_PATTERN_INDEX = 4

        @VisibleForTesting
        internal const val POLISHING_PATTERN_INDEX = 5

        @VisibleForTesting
        internal const val CUSTOMIZABLE_PATTERN_INDEX = 6

        fun fromBleIndex(bleIndex: Int) = values().first { it.bleIndex == bleIndex }
    }
}
