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
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence
import kotlinx.android.parcel.Parcelize

/** Brushing Mode segment data */
@VisibleForApp
@Parcelize
data class BrushingModeSegment(
    val sequenceId: Int,
    val strength: Int
) : Parcelable {

    fun sequence() = BrushingModeSequence.fromBleIndex(sequenceId)

    @VisibleForApp
    companion object {

        fun default() = BrushingModeSegment(
            sequenceId = 0,
            strength = 1
        )
    }
}
