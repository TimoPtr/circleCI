/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.calendar.logic.api.model

import com.google.gson.annotations.SerializedName

internal typealias BrushingStreakDateResponseBody = List<String?>

internal typealias BrushingStreakDatesResponseBody = List<BrushingStreakDateResponseBody?>

internal data class BrushingStreaksResponse(
    @SerializedName("brushing") val body: ResponseBody?
) {

    internal data class ResponseBody(
        @SerializedName("streaks_date") val streaks: BrushingStreakDatesResponseBody?
    )

    companion object {
        fun empty(): BrushingStreaksResponse = BrushingStreaksResponse(ResponseBody(emptyList()))

        internal fun withDates(dates: BrushingStreakDatesResponseBody): BrushingStreaksResponse =
            BrushingStreaksResponse(ResponseBody(dates))
    }
}
