/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer.curve

import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
enum class BrushingModeCurve(internal val bleIndex: Int) {

    Flat(bleIndex = BrushingModeCurve.FLAT_CURVE_INDEX),

    CleanMode(bleIndex = BrushingModeCurve.CLEAN_MODE_CURVE_INDEX),

    GumCare(bleIndex = BrushingModeCurve.GUM_CARE_CURVE_INDEX),

    Custom(bleIndex = BrushingModeCurve.CUSTOM_CURVE_INDEX);

    @VisibleForApp
    companion object {

        @VisibleForTesting
        internal const val FLAT_CURVE_INDEX = 0

        @VisibleForTesting
        internal const val CLEAN_MODE_CURVE_INDEX = 1

        @VisibleForTesting
        internal const val GUM_CARE_CURVE_INDEX = 2

        @VisibleForTesting
        internal const val CUSTOM_CURVE_INDEX = 3

        fun fromBleIndex(bleIndex: Int) = values().first { it.bleIndex == bleIndex }
    }
}
