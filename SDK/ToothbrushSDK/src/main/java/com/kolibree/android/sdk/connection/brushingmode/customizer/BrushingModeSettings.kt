/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer

import android.os.Parcelable
import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeLastSegmentStrategy.UseSystemDefault
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeStrengthOption.OneLevel
import kotlinx.android.parcel.Parcelize

/** Brushing Mode settings data */
@Keep
@Parcelize
data class BrushingModeSettings internal constructor(
    val brushingModeId: Int,
    val modifiable: Boolean,
    val strengthOption: BrushingModeStrengthOption,
    val segmentCount: Int,
    val lastSegmentStrategy: BrushingModeLastSegmentStrategy,
    val segments: List<BrushingModeSegment>
) : Parcelable {

    fun mode(): BrushingMode = BrushingMode.lookupFromBleIndex(brushingModeId)

    companion object {

        fun default() = BrushingModeSettings(
            brushingModeId = BrushingMode.UserDefined.bleIndex,
            modifiable = true,
            strengthOption = OneLevel,
            segmentCount = 0,
            lastSegmentStrategy = UseSystemDefault,
            segments = listOf()
        )

        fun custom(
            strengthOption: BrushingModeStrengthOption,
            segmentCount: Int,
            lastSegmentStrategy: BrushingModeLastSegmentStrategy,
            segments: List<BrushingModeSegment>
        ) = BrushingModeSettings(
            brushingModeId = BrushingMode.UserDefined.bleIndex,
            modifiable = true,
            strengthOption = strengthOption,
            segmentCount = segmentCount,
            lastSegmentStrategy = lastSegmentStrategy,
            segments = segments
        )
    }
}
