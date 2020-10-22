/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus

import androidx.annotation.VisibleForTesting
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.EventTracker
import javax.inject.Inject

/** Coach+ analytics wrapper */
internal interface CoachPlusAnalyticsHelper {

    /**
     * To be called when the user clicks on an option in the [CoachPlusBrushingModeDialog]
     *
     * @param optionIndex [Int] option index in the screen
     */
    fun onBrushingModeDialogOptionChosen(optionIndex: Int)

    /**
     * To be called when the [CoachPlusBrushingModeDialog] has been shown to the user
     */
    fun onBrushingModeDialogShown()
}

/** [CoachPlusAnalyticsHelper] implementation */
internal class CoachPlusAnalyticsHelperImpl @Inject constructor(
    private val eventTracker: EventTracker
) : CoachPlusAnalyticsHelper {

    override fun onBrushingModeDialogOptionChosen(optionIndex: Int) {
        if (optionIndex in 0..eventIndexMap.size) {
            eventTracker.sendEvent(eventIndexMap[optionIndex])
        } else {
            FailEarly.fail(exception = IndexOutOfBoundsException())
        }
    }

    override fun onBrushingModeDialogShown() =
        eventTracker.sendEvent(SCREEN_NAME_COACH_BRUSHING_MODE_DIALOG)

    companion object {

        @VisibleForTesting
        val SCREEN_NAME_COACH_BRUSHING_MODE_DIALOG = AnalyticsEvent("BrushingProgram_CoachPlus")

        @VisibleForTesting
        val EVENT_OPTION_1_CHOSEN = SCREEN_NAME_COACH_BRUSHING_MODE_DIALOG + "Option1"

        @VisibleForTesting
        val EVENT_OPTION_2_CHOSEN = SCREEN_NAME_COACH_BRUSHING_MODE_DIALOG + "Option2"

        @VisibleForTesting
        val EVENT_OPTION_3_CHOSEN = SCREEN_NAME_COACH_BRUSHING_MODE_DIALOG + "Option3"

        @VisibleForTesting
        val EVENT_OPTION_4_CHOSEN = SCREEN_NAME_COACH_BRUSHING_MODE_DIALOG + "Option4"

        @VisibleForTesting
        val EVENT_OPTION_5_CHOSEN = SCREEN_NAME_COACH_BRUSHING_MODE_DIALOG + "Option5"

        private val eventIndexMap = listOf(
            EVENT_OPTION_1_CHOSEN,
            EVENT_OPTION_2_CHOSEN,
            EVENT_OPTION_3_CHOSEN,
            EVENT_OPTION_4_CHOSEN,
            EVENT_OPTION_5_CHOSEN
        )
    }
}
