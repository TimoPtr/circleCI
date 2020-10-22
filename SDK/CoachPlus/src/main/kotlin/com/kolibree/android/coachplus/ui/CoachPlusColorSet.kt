/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.ui

import android.content.Context
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import com.kolibree.android.coachplus.logic.R
import kotlinx.android.parcel.Parcelize

/** Color set to be injected into the Coach+ activity */
@Keep
@Parcelize
data class CoachPlusColorSet(
    @ColorInt
    val backgroundColor: Int,
    @ColorInt
    val titleColor: Int,
    @ColorInt
    val neglectedColor: Int,
    @ColorInt
    val cleanColor: Int,
    @ColorInt
    val plaqueColor: Int,
    @ColorInt
    internal val plaqlessLedWhite: Int,
    @ColorInt
    internal val plaqlessLedRed: Int,
    @ColorInt
    internal val plaqlessLedBlue: Int
) : Parcelable {

    companion object {

        /**
         * Create a [CoachPlusColorSet] from a bunch of colors
         *
         * @param context: [Context]
         * @param backgroundColor: the color of the activity's background
         * @param titleColor: the color of the "Coach+" labeled title
         * @param neglectedColor: the neglected teeth color (not brushed color)
         * @param cleanColor: the clean teeth color (fully brushed color)
         * @param plaqueColor: plaque color (not brushed color)
         */
        @JvmStatic
        @Suppress("LongParameterList")
        fun create(
            context: Context,
            @ColorInt backgroundColor: Int,
            @ColorInt titleColor: Int,
            @ColorInt neglectedColor: Int,
            @ColorInt cleanColor: Int,
            @ColorInt plaqueColor: Int
        ): CoachPlusColorSet =
            CoachPlusColorSet(
                backgroundColor = backgroundColor,
                titleColor = titleColor,
                neglectedColor = neglectedColor,
                cleanColor = cleanColor,
                plaqueColor = plaqueColor,
                plaqlessLedWhite = context.getColor(R.color.coach_plus_plaqless_led_white),
                plaqlessLedRed = context.getColor(R.color.coach_plus_plaqless_led_red),
                plaqlessLedBlue = context.getColor(R.color.coach_plus_plaqless_led_blue)
            )
    }
}
