/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.ui.mindyourspeed

import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kolibree.R
import com.kolibree.android.app.ui.home.HomeScreenActivityEspressoTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.createAndroidLess
import org.junit.Test

internal class MindYourSpeedActivityEspressoTest : HomeScreenActivityEspressoTest() {

    @Test
    fun checkStartViews() {
        prepareMocks(connectionBuilder = createAndroidLess())
        launchMindYourSpeed()
        verifyViews()
    }

    private fun verifyViews() {
        onView(withId(R.id.mind_your_speed_title)).check(matches(isDisplayed()))
        onView(withId(R.id.speedometer)).check(matches(isDisplayed()))
        onView(withId(R.id.legend)).check(matches(isDisplayed()))
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
        onView(withId(R.id.start_message)).check(matches(isDisplayed()))
    }

    private fun launchMindYourSpeed() {
        launchActivity()
        context().startActivity(
            startMindYourSpeedIntent(
                context(),
                KLTBConnectionBuilder.DEFAULT_MAC,
                ToothbrushModel.CONNECT_B1
            ).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
        })
    }
}
