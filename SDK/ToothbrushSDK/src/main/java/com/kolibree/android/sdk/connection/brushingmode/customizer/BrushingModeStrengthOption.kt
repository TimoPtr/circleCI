/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting

/**
 * Brushing Mode strength level management options
 *
 * Tells the device how many levels are available when using the + and - buttons
 */
@Keep
enum class BrushingModeStrengthOption(internal val bleValue: Int) {

    /**
     * The toothbrush will offer one strength level option (level 5)
     */
    OneLevel(bleValue = BrushingModeStrengthOption.ONE_LEVEL_BLE_VALUE),

    /**
     * The toothbrush will offer 3 strength level options (levels 1, 4, 7)
     */
    ThreeLevels(bleValue = BrushingModeStrengthOption.THREE_LEVELS_BLE_VALUE),

    /**
     * The toothbrush will offer 10 strength level options (all levels)
     */
    TenLevels(bleValue = BrushingModeStrengthOption.TEN_LEVELS_BLE_VALUE);

    internal companion object {

        @VisibleForTesting
        const val ONE_LEVEL_BLE_VALUE = 0

        @VisibleForTesting
        const val THREE_LEVELS_BLE_VALUE = 1

        @VisibleForTesting
        const val TEN_LEVELS_BLE_VALUE = 2

        fun fromBleValue(bleValue: Int) = values().first { it.bleValue == bleValue }
    }
}
