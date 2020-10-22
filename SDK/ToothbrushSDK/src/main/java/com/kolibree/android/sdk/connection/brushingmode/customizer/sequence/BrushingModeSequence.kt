/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer.sequence

import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp

/** Brushing Mode segment sequences definitions */
@VisibleForApp
enum class BrushingModeSequence(val bleIndex: Int) {

    /**
     * Clean Brushing Mode's sequence
     */
    CleanMode(bleIndex = BrushingModeSequence.CLEAN_MODE_SEQUENCE_BLE_INDEX),

    /**
     * Whitening Brushing Mode's sequence
     */
    WhiteningMode(bleIndex = BrushingModeSequence.WHITENING_MODE_SEQUENCE_BLE_INDEX),

    /**
     * GumCare Brushing Mode's sequence
     */
    GumCare(bleIndex = BrushingModeSequence.GUM_CARE_SEQUENCE_BLE_INDEX),

    /**
     * Polishing Brushing Mode's sequence
     */
    PolishingMode(bleIndex = BrushingModeSequence.POLISHING_MODE_SEQUENCE_BLE_INDEX),

    /**
     * Customizable
     */
    Custom(bleIndex = BrushingModeSequence.CUSTOM_MODE_SEQUENCE_BLE_INDEX);

    @VisibleForApp
    companion object {

        @VisibleForTesting
        internal const val CLEAN_MODE_SEQUENCE_BLE_INDEX = 0

        @VisibleForTesting
        internal const val WHITENING_MODE_SEQUENCE_BLE_INDEX = 1

        @VisibleForTesting
        internal const val GUM_CARE_SEQUENCE_BLE_INDEX = 2

        @VisibleForTesting
        internal const val POLISHING_MODE_SEQUENCE_BLE_INDEX = 3

        internal const val CUSTOM_MODE_SEQUENCE_BLE_INDEX = 4

        fun fromBleIndex(bleIndex: Int) = values().first { it.bleIndex == bleIndex }
    }
}
