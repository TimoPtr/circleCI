/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.utils

import androidx.annotation.ColorInt
import androidx.annotation.Keep
import com.kolibree.android.coachplus.ui.CoachPlusColorSet
import com.kolibree.android.sdk.plaqless.PlaqlessRingLedState
import javax.inject.Inject

/** Helper for [PlaqlessRingLedState] to [ColorInt] [Int] mapping */
@Keep
interface CoachPlaqlessRingLedColorMapper {

    @ColorInt
    fun getRingLedColor(plaqlessRingLedState: PlaqlessRingLedState): Int
}

/** [CoachPlaqlessRingLedColorMapper] implementation */
// https://kolibree.atlassian.net/wiki/spaces/PROD/pages/88113208/Coach+with+Plaqless+Pro#Mirroring-LED-ring-color
internal class CoachPlaqlessRingLedColorMapperImpl @Inject constructor(
    private val coachPlusColorSet: CoachPlusColorSet
) : CoachPlaqlessRingLedColorMapper {

    @ColorInt
    override fun getRingLedColor(plaqlessRingLedState: PlaqlessRingLedState): Int =
        when {
            plaqlessRingLedState.red.toInt() != 0 -> coachPlusColorSet.plaqlessLedRed
            plaqlessRingLedState.blue.toInt() != 0 -> coachPlusColorSet.plaqlessLedBlue
            else -> coachPlusColorSet.plaqlessLedWhite
        }
}
