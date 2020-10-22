/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.processedbrushings.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.kolibree.kml.MouthZone16

internal data class ZoneData private constructor(
    @SerializedName(EXPECTED_TIME_JSON_KEY) val expectedTenthOfSecondsPerZone: Int,
    @SerializedName(PASSES_JSON_KEY) val passes: List<ZonePass> = listOf()
) {
    /**
     * Returns a copy of the current ZoneData with the added ZonePass
     */
    fun addPass(zonePass: ZonePass): ZoneData {
        val newPasses: MutableList<ZonePass> = mutableListOf()

        newPasses.addAll(passes)

        newPasses.add(zonePass)

        return copy(
            expectedTenthOfSecondsPerZone = expectedTenthOfSecondsPerZone,
            passes = newPasses.toList()
        )
    }

    internal companion object {
        const val EXPECTED_TIME_JSON_KEY = "expected_time"
        const val PASSES_JSON_KEY = "passes"

        @JvmStatic
        fun createFromGoalBrushingTime(
            goalBrushingTime: Int,
            passes: List<ZonePass>
        ): ZoneData {
            val expectedTenthOfSecondsPerZone = 10 * goalBrushingTime / MouthZone16.values().size

            return ZoneData(
                expectedTenthOfSecondsPerZone,
                passes
            )
        }

        @JvmStatic
        fun createFromExpectedTime(goalBrushingTime: Int) =
            createFromGoalBrushingTime(
                goalBrushingTime,
                listOf()
            )
    }
}

@Keep
data class ZonePass(
    @SerializedName(START_TIME_JSON_KEY) val startTime: Long,
    @SerializedName(DURATION_JSON_KEY) val durationTenthSecond: Long
) {
    @Keep
    internal companion object {
        const val START_TIME_JSON_KEY = "pass_datetime"
        const val DURATION_JSON_KEY = "effective_time"
    }
}
