/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kolibree.R
import com.kolibree.android.app.dagger.EspressoSingleThreadSchedulerModule.scheduler
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.idlingresources.IdlingResourceFactory
import java.util.concurrent.TimeUnit.MILLISECONDS
import org.junit.Test

internal class ToolboxEspressoTest : HomeScreenActivityEspressoTest() {

    @Test
    fun toolboxExplanationIsShownWhenThereIsNoTestBrushingToDisplay() {
        prepareMocks(
            brushingNumber = 1,
            hasToolboxExplanationBeenShown = false,
            mockConnectionWithState = KLTBConnectionState.ACTIVE
        )

        launchActivity()

        advanceTime()

        checkToolboxIsDisplayed()
    }

    @Test
    fun toolboxExplanationIsShownAfterTestBrushingAppears() {
        prepareMocks(
            brushingNumber = 0,
            hasToolboxExplanationBeenShown = false,
            mockConnectionWithState = KLTBConnectionState.ACTIVE
        )

        launchActivity()

        advanceTime()

        checkTestBrushingIsDisplayed()

        advanceTime()

        checkToolboxIsDisplayed()
    }

    @Test
    fun toolboxExplanationShouldNotBeDisplayedIfItHasAlreadyBeenShown() {
        prepareMocks(
            brushingNumber = 1,
            hasToolboxExplanationBeenShown = true,
            mockConnectionWithState = KLTBConnectionState.ACTIVE
        )

        launchActivity()

        advanceTime()

        onView(withId(R.id.toolbox))
            .check(matches(withEffectiveVisibility(GONE)))
    }

    private fun advanceTime() {
        scheduler.advanceTimeBy(1000L, MILLISECONDS)
    }

    private fun checkTestBrushingIsDisplayed() {
        onView(withId(R.id.remind_me_later_button))
            .perform(click())
    }

    private fun checkToolboxIsDisplayed() {
        IdlingResourceFactory.viewVisibility(
            R.id.pulsing_dot_toolbox,
            View.VISIBLE
        ).waitForIdle()

        onView(withId(R.id.pulsing_dot_toolbox)).check(matches(isDisplayed()))

        onView(withId(R.id.confirmButton))
            .perform(click())

        onView(withId(R.id.toolbox))
            .check(matches(withEffectiveVisibility(GONE)))
    }
}
