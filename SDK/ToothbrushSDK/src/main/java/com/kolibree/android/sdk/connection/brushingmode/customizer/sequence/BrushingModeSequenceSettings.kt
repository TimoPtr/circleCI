/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer.sequence

import android.os.Parcelable
import com.kolibree.android.annotation.VisibleForApp
import kotlinx.android.parcel.Parcelize

/** [BrushingModeSequence] settings */
@VisibleForApp
@Parcelize
data class BrushingModeSequenceSettings(
    val sequenceId: Int,
    val modifiable: Boolean,
    val patternCount: Int,
    val patterns: List<BrushingModeSequencePattern>
) : Parcelable {

    fun sequence(): BrushingModeSequence = BrushingModeSequence.fromBleIndex(sequenceId)

    @VisibleForApp
    companion object {

        fun default() = BrushingModeSequenceSettings(
            sequenceId = BrushingModeSequence.CLEAN_MODE_SEQUENCE_BLE_INDEX,
            modifiable = false,
            patternCount = 0,
            patterns = listOf()
        )
    }
}
