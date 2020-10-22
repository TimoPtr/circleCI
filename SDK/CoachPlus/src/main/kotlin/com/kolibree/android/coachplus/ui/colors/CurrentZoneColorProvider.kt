/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.ui.colors

import android.animation.ArgbEvaluator
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.coachplus.ui.CoachPlusColorSet
import javax.inject.Inject

/** Helper class that provides business logic for Coach+ current zone color */
@Keep
interface CurrentZoneColorProvider {

    /**
     * Provide current zone color according to plaque level and brushing completion
     *
     * @param completionPercent [Int]
     * @return [ColorInt] color [Int]
     */
    @ColorInt
    fun provideCurrentZoneColor(completionPercent: Int): Int
}

/** [CurrentZoneColorProvider] implementation */
internal class CurrentZoneColorProviderImpl @Inject constructor(
    private val colorSet: CoachPlusColorSet,
    private val argbEvaluator: ArgbEvaluator
) : CurrentZoneColorProvider {

    override fun provideCurrentZoneColor(completionPercent: Int) =
        evaluateColor(completionPercent, colorSet.neglectedColor)

    @VisibleForTesting
    @ColorInt
    fun evaluateColor(percents: Int, @ColorInt neglectedColor: Int) =
        argbEvaluator.evaluate(
            percents / HUNDRED_PERCENTS,
            neglectedColor,
            colorSet.cleanColor
        ) as Int
}

private const val HUNDRED_PERCENTS = 100f
