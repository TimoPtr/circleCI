/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz

import androidx.test.espresso.Espresso.pressBack
import com.kolibree.R
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.utils.SdkBuilder
import org.junit.Test

class BrushingProgramLogoEspressoTest : BrushingProgramActivityBaseEspressoBaseTest() {

    @Test
    fun displayCorrectHumLogoForEveryMode() {

        val b1Active: KLTBConnection = KLTBConnectionBuilder.createWithDefaultState()
            .withMac("5")
            .withModel(ToothbrushModel.CONNECT_B1)
            .withState(KLTBConnectionState.ACTIVE)
            .withBrushingMode()
            .build()

        SdkBuilder.create()
            .withKLTBConnections(b1Active)
            .build()

        activityTestRule.launchActivity(createLaunchBrushingQuizActivityIntent())

        waitForQuizPager()

        selectAnswer(R.string.brushing_quiz_screen_1_answer_2)
        selectAnswer(R.string.brushing_quiz_screen_2_answer_1)

        selectAnswer(R.string.brushing_quiz_screen_3_answer_1)
        logoIsDisplayed(R.drawable.ic_brushing_program_logo3)

        pressBack()
        selectAnswer(R.string.brushing_quiz_screen_3_answer_2)
        logoIsDisplayed(R.drawable.ic_brushing_program_logo2)

        pressBack()
        selectAnswer(R.string.brushing_quiz_screen_3_answer_3)
        logoIsDisplayed(R.drawable.ic_brushing_program_logo1)
    }
}
