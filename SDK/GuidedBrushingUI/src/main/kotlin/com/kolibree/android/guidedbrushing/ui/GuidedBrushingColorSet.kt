/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.ui

import android.content.Context
import com.kolibree.android.coachplus.ui.CoachPlusColorSet
import com.kolibree.android.guidedbrushing.R

internal object GuidedBrushingColorSet {

    @JvmStatic
    fun createDefault(context: Context): CoachPlusColorSet =
        CoachPlusColorSet(
            backgroundColor = context.getColor(R.color.guided_brushing_default_background_color),
            titleColor = context.getColor(R.color.guided_brushing_default_title_color),
            neglectedColor = context.getColor(R.color.guided_brushing_default_neglected_color),
            cleanColor = context.getColor(R.color.guided_brushing_default_clean_color),
            plaqueColor = context.getColor(R.color.guided_brushing_default_plaque_color),
            plaqlessLedWhite = context.getColor(R.color.coach_plus_plaqless_led_white),
            plaqlessLedRed = context.getColor(R.color.coach_plus_plaqless_led_red),
            plaqlessLedBlue = context.getColor(R.color.coach_plus_plaqless_led_blue)
        )
}
