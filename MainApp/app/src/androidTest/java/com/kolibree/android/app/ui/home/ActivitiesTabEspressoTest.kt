/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import com.kolibree.R
import com.kolibree.android.app.dagger.EspressoSingleThreadSchedulerModule
import com.kolibree.android.app.ui.activity.BaseActivity
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.utils.createGruwareDataFromOtaUpdateType
import com.kolibree.android.test.utils.runAndCheckIntent
import com.kolibree.android.toothbrushupdate.OtaUpdateType
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Test
@LargeTest
internal class ActivitiesTabEspressoTest : HomeScreenActivityEspressoTest() {

    @Test
    fun openActivitiesTab() {
        prepareMocks()

        launchActivity()

        BottomNavigationUtils.navigateToActivities()
        makeScreenshot("InitialState")

        checkShortTasksCard()

        checkGuidedBrushingCard()
    }

    @Test
    fun noConnectedToothbrush_showNoToothbrushDialog() {
        prepareMocks()

        launchActivity()

        BottomNavigationUtils.navigateToActivities()

        onView(withId(R.id.task_test_brushing)).perform(click())

        checkNoToothbrushSnackbar()
    }

    @Test
    fun connectedToothbrush_startTasks() {
        prepareMocks(mockConnectionWithState = KLTBConnectionState.ACTIVE, showMindYourSpeed = true)

        launchActivity()

        BottomNavigationUtils.navigateToActivities()

        checkTestBrushingScreen()

        checkSpeedControlScreen()

        checkTestAngleScreen()

        checkGuidedBrushingScreen()
    }

    @Test
    fun checkMandatoryOta() {
        prepareMocks(
            connectionBuilder = KLTBConnectionBuilder.createAndroidLess().withBootloader(true)
                .withState(KLTBConnectionState.ACTIVE),
            gruwareData = createGruwareDataFromOtaUpdateType(context(), OtaUpdateType.MANDATORY)
        )

        launchActivity()

        checkMandatoryOtaDialog()

        BottomNavigationUtils.navigateToActivities()

        onView(withId(R.id.task_test_brushing))
            .perform(scrollTo(), click())

        checkMandatoryOtaDialog()

        onView(withId(R.id.guided_brushing_card))
            .perform(scrollTo(), click())

        checkMandatoryOtaDialog()
    }

    @Test
    fun checkTestBrushingDialogWithoutRemindMeLaterButton() {
        prepareMocks(mockConnectionWithState = KLTBConnectionState.ACTIVE)

        launchActivity()

        BottomNavigationUtils.navigateToActivities()

        onView(withId(R.id.task_test_brushing)).perform(scrollTo(), click())

        onView(withText(R.string.dialog_test_brushing_headline)).check(matches(isDisplayed()))

        onView(withText(R.string.dialog_test_brushing_remind_later_button))
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun checkTestBrushingDialogShowAutomaticallyWithRemindMeLaterButton() {
        prepareMocks(mockConnectionWithState = KLTBConnectionState.ACTIVE, brushingNumber = 0)

        launchActivity()

        EspressoSingleThreadSchedulerModule.scheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        onView(withText(R.string.dialog_test_brushing_headline)).check(matches(isDisplayed()))

        onView(withText(R.string.dialog_test_brushing_remind_later_button))
            .check(matches(isDisplayed()))
    }

    @Test
    fun adjustYourAnglesTaskShouldBeHidden() {
        prepareMocks()

        launchActivity()

        BottomNavigationUtils.navigateToActivities()

        onView(withId(R.id.task_test_angle)).check(matches(not(isDisplayed())))
    }

    private fun checkMandatoryOtaDialog() {
        onView(withText(R.string.mandatory_ota_dialog_title))
            .check(matches(isDisplayed()))

        onView(withText(R.string.mandatory_ota_dialog_body))
            .check(matches(isDisplayed()))

        runAndCheckIntent(
            allOf(
                hasComponent("com.kolibree.android.app.ui.ota.OtaUpdateActivity"),
                hasExtra(BaseActivity.INTENT_TOOTHBRUSH_MAC, KLTBConnectionBuilder.DEFAULT_MAC),
                hasExtra(BaseActivity.INTENT_TOOTHBRUSH_MODEL, KLTBConnectionBuilder.DEFAULT_MODEL),
                hasExtra("intent_is_mandatory", true)
            )
        ) {
            onView(withText(R.string.mandatory_ota_dialog_proceed))
                .check(matches(isDisplayed()))
                .perform(click())
        }

        // cancel ota mandatory dialog
        onView(withText(R.string.cancel)).perform(click())
    }

    private fun checkTestBrushingScreen() {
        onView(withId(R.id.task_test_brushing))
            .perform(scrollTo(), click())

        onView(withText(R.string.dialog_test_brushing_no_more_smiles))
            .check(matches(isDisplayed()))

        onView(withText(R.string.dialog_test_brushing_start_button))
            .perform(click())

        onView(withText(R.string.test_brushing_start_brushing_header))
            .check(matches(isDisplayed()))

        pressBack()
    }

    private fun checkNoToothbrushSnackbar() {
        onView(withText(R.string.home_error_no_toothbrush_connected)).check(matches(isDisplayed()))
    }

    private fun checkTestAngleScreen() {
        // TODO add support for new test angles
    }

    private fun checkSpeedControlScreen() {
        onView(withId(R.id.task_test_speed))
            .perform(scrollTo(), click())

        onView(withText(R.string.mind_your_speed_start_screen_title))
            .check(matches(isDisplayed()))

        onView(withText(R.string.mind_your_speed_start_screen_description))
            .check(matches(isDisplayed()))

        onView(withText(R.string.mind_your_speed_start_screen_description2))
            .check(matches(isDisplayed()))

        onView(withText(R.string.mind_your_speed_start_screen_start))
            .check(matches(isDisplayed()))

        onView(withText(R.string.mind_your_speed_start_screen_cancel))
            .check(matches(isDisplayed()))

        onView(withText(R.string.mind_your_speed_start_screen_start))
            .perform(click())

        onView(withText(R.string.mind_your_speed_title))
            .check(matches(isDisplayed()))
        onView(withText(R.string.mind_your_speed_legend_slow_or_fast))
            .check(matches(isDisplayed()))
        onView(withText(R.string.mind_your_speed_no_feedback))
            .check(matches(isDisplayed()))
        onView(withText(R.string.mind_your_speed_start_message))
            .check(matches(isDisplayed()))

        pressBack()
    }

    private fun checkGuidedBrushingScreen() {
        onView(withId(R.id.guided_brushing_card))
            .perform(scrollTo(), click())

        onView(withText(R.string.guided_brushing_start_screen_no_more_smiles))
            .check(matches(isDisplayed()))

        onView(withText(R.string.guided_brushing_start_screen_body))
            .check(matches(isDisplayed()))

        onView(withText(R.string.guided_brushing_start_screen_headline))
            .check(matches(isDisplayed()))

        makeScreenshot("GudedBrushingStartScreen")

        onView(withId(R.id.start_button))
            .perform(click())

        onView(withText(R.string.guided_brushing_title))
            .check(matches(isDisplayed()))

        pressBack()
    }

    private fun checkShortTasksCard() {
        onView(withId(R.id.task_title))
            .perform(scrollTo())
            .check(matches(withText(R.string.activities_tasks_title)))

        onView(withId(R.id.task_description))
            .perform(scrollTo())
            .check(matches(withText(R.string.activities_tasks_description)))

        checkTask(
            id = R.id.task_test_brushing,
            title = R.string.activities_task_test_brushing_title,
            description = R.string.activities_task_test_brushing_description
        )

        checkTask(
            id = R.id.task_test_speed,
            title = R.string.activities_task_speed_title,
            description = R.string.activities_task_speed_description
        )

        checkTask(
            id = R.id.task_test_angle,
            title = R.string.activities_task_angle_title,
            description = R.string.activities_task_angle_description
        )
    }

    private fun checkTask(id: Int, title: Int, description: Int) {
        onView(withId(id)).check(
            matches(hasDescendant(withText(title)))
        )

        onView(withId(id)).check(
            matches(hasDescendant(withText(description)))
        )
    }

    private fun checkGuidedBrushingCard() {
        onView(withId(R.id.guided_brushing_points))
            .perform(scrollTo())
            .check(matches(withText(R.string.activities_guided_brushing_points)))

        onView(withId(R.id.guided_brushing_title))
            .perform(scrollTo())
            .check(matches(withText(R.string.activities_guided_brushing_title)))

        onView(withId(R.id.guided_brushing_description))
            .perform(scrollTo())
            .check(matches(withText(R.string.activities_guided_brushing_description)))
    }
}
