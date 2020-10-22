/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.mouthmap.logic

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.kolibree.android.mouthmap.R

@Keep
data class ResultColorsProvider(
    @ColorInt val overspeed: Int,
    @ColorInt val underspeed: Int,
    @ColorInt val missed: Int,
    @ColorInt val buildUpRemains: Int
) {
    companion object {

        fun create(context: Context): ResultColorsProvider {
            val coverageDirtyColor = ContextCompat.getColor(context, R.color.neglectedZoneColor)
            val underspeedColor = ContextCompat.getColor(context, R.color.speed_too_slow)
            val overspeedColor = ContextCompat.getColor(context, R.color.speed_too_fast)
            return ResultColorsProvider(
                overspeed = overspeedColor,
                underspeed = underspeedColor,
                missed = coverageDirtyColor,
                buildUpRemains = underspeedColor
            )
        }
    }
}
