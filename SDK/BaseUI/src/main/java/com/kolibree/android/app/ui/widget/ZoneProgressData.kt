/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.widget

import android.os.Parcelable
import androidx.annotation.FloatRange
import com.kolibree.android.annotation.VisibleForApp
import kotlinx.android.parcel.Parcelize

@VisibleForApp
@Parcelize
data class ZoneProgressData(
    val zones: List<ZoneData> = emptyList()
) : Parcelable {

    fun brushingFinished(): ZoneProgressData {
        val zones = zones.map {
            it.copy(isOngoing = false)
        }
        return ZoneProgressData(zones)
    }

    fun updateProgressOnZone(
        zoneIndex: Int,
        @FloatRange(from = 0.0, to = 1.0) progress: Float
    ): ZoneProgressData {
        val zones = zones.mapIndexed { index: Int, data: ZoneData ->
            if (index == zoneIndex) {
                data.copy(isOngoing = true, progress = progress)
            } else {
                data.copy(isOngoing = false)
            }
        }
        return ZoneProgressData(zones)
    }

    @VisibleForApp
    companion object {
        fun create(size: Int): ZoneProgressData {
            val zones = mutableListOf<ZoneData>()
            for (index in 0 until size) {
                zones += ZoneData(isOngoing = false, progress = 0f)
            }
            return ZoneProgressData(zones)
        }
    }
}

@VisibleForApp
@Parcelize
data class ZoneData(
    val isOngoing: Boolean,
    @FloatRange(from = 0.0, to = 1.0) val progress: Float
) : Parcelable
