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
 * Brushing Mode's last segment brushing end strategy */
@Keep
enum class BrushingModeLastSegmentStrategy(internal val bleValue: Int) {

    /**
     * The Brushing Mode will use the toothbrush's default value
     */
    UseSystemDefault(bleValue = BrushingModeLastSegmentStrategy.SYSTEM_DEFAULT_BLE_VALUE),

    /**
     * The brushing session will end as soon as the last segment is completed
     */
    EndAfterLastSegment(bleValue = BrushingModeLastSegmentStrategy.END_AFTER_LAST_SEGMENT_BLE_VALUE),

    /**
     * The brushing session will keep on running even after the last segment completion
     */
    KeepRunningAfterLastSegment(
        bleValue = BrushingModeLastSegmentStrategy.KEEP_RUNNING_AFTER_LAST_SEGMENT_BLE_VALUE
    );

    internal companion object {

        @VisibleForTesting
        const val SYSTEM_DEFAULT_BLE_VALUE = 0

        @VisibleForTesting
        const val END_AFTER_LAST_SEGMENT_BLE_VALUE = 1

        @VisibleForTesting
        const val KEEP_RUNNING_AFTER_LAST_SEGMENT_BLE_VALUE = 2

        fun fromBleValue(bleValue: Int) = values().first { it.bleValue == bleValue }
    }
}
