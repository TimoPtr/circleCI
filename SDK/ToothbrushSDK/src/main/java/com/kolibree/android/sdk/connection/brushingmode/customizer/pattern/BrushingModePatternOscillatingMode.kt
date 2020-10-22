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

/** [BrushingModePattern] oscillating modes definitions */
// Do not keep, internal use only /!\
@VisibleForApp
enum class BrushingModePatternOscillatingMode(internal val bleIndex: Int) {

    NoOscillation(bleIndex = BrushingModePatternOscillatingMode.NO_OSCILLATION_BLE_INDEX),

    Triangular(bleIndex = BrushingModePatternOscillatingMode.TRIANGULAR_OSCILLATION_BLE_INDEX),

    ComplexPulse(bleIndex = BrushingModePatternOscillatingMode.COMPLEX_PULSE_OSCILLATION_BLE_INDEX);

    @VisibleForApp
    companion object {

        @VisibleForTesting
        internal const val NO_OSCILLATION_BLE_INDEX = 0

        @VisibleForTesting
        internal const val TRIANGULAR_OSCILLATION_BLE_INDEX = 1

        @VisibleForTesting
        internal const val COMPLEX_PULSE_OSCILLATION_BLE_INDEX = 2

        fun fromBleIndex(bleIndex: Int) = values().first { it.bleIndex == bleIndex }
    }
}
