/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.hum.checkup

import androidx.annotation.IdRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeRight
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.KOLIBREE_DAY_START_HOUR
import com.kolibree.android.app.ui.checkup.day.DayCheckupActivity
import com.kolibree.android.app.ui.checkup.day.startDayCheckupActivityIntent
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.DEFAULT_BRUSHING_GOAL
import com.kolibree.android.game.Game
import com.kolibree.android.test.BaseEspressoTest
import com.kolibree.android.test.KLBaseActivityTestRule
import com.kolibree.android.test.KolibreeActivityTestRule
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.android.test.utils.SdkBuilder
import com.kolibree.sdkws.data.model.Brushing
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

/** [DayCheckupActivity] Espresso tests */
class DayCheckupActivityEspressoTest : BaseEspressoTest<DayCheckupActivity>() {

    override fun createRuleForActivity(): KLBaseActivityTestRule<DayCheckupActivity> =
        KolibreeActivityTestRule.Builder(DayCheckupActivity::class.java)
            .launchActivity(false).build()

    @Test
    fun dayCheckupActivity_happyPath() {
        TrustedClock.setFixedDate(ZonedDateTime.of(
            LocalDate.now(),
            // this way all of our brushings will be on the same day
            LocalTime.of(KOLIBREE_DAY_START_HOUR + 2, 5),
            ZoneOffset.systemDefault()
        ))

        val activeProfile = ProfileBuilder.create().build()

        val brushingDuration1 = 120L
        val brushingDuration2 = 80L

        val sdkBuilder = SdkBuilder.create()
            .withActiveProfile(activeProfile)
            .withBrushingsForProfile(
                ProfileBuilder.DEFAULT_ID,
                createBrushing(
                    brushingDuration1,
                    TrustedClock.getNowOffsetDateTime().minusHours(1)
                ),
                createBrushing(
                    brushingDuration2,
                    TrustedClock.getNowOffsetDateTime().minusHours(2)
                )
            )
            .prepareForMainScreen()

        AppMocker.create().withSdkBuilder(sdkBuilder).mock()

        activityTestRule.launchActivity(defaultIntent())

        // Activity widgets
        assertVisible(R.id.checkup_back)
        assertVisible(R.id.checkup_date)
        assertVisible(R.id.checkup_charts)
        assertVisible(R.id.checkup_pager_indicator)
        assertVisible(R.id.checkup_delete)
        assertVisible(R.id.checkup_pager)

        // CheckupView widgets
        assertVisible(R.id.checkup_mouth_map_section)
        assertVisible(R.id.checkup_left)
        assertVisible(R.id.checkup_right)
        assertVisible(R.id.checkup_legend)

        // We are showing the first brushing
        assertDuration("2:00")
        makeScreenshot("CheckupScreen_Day_FirstBrushing")

        // We left swipe to show the second brushing
        onView(withId(R.id.checkup_pager)).perform(swipeLeft())

        // We should be showing the second one
        assertDuration("1:20")
        makeScreenshot("CheckupScreen_Day_SecondBrushing")

        // We left swipe back to the first brushing
        onView(withId(R.id.checkup_pager)).perform(swipeRight())

        // We are showing the first brushing
        assertDuration("2:00")

        // Then we delete it
        deleteAndConfirm()

        // We should be showing the second one
        assertDuration("1:20")
        makeScreenshot("CheckupScreen_Day_SecondBrushing_AfterDeletion")

        // Then we delete the second brushing
        deleteAndConfirm()

        // No more brushing session, screen should be closed
        assertTrue(activity.isFinishing)
    }

    private fun assertVisible(@IdRes id: Int) =
        onView(withId(id))
            .check(matches(isDisplayed()))

    private fun assertDuration(duration: String) =
        onView(withId(R.id.checkup_duration))
            .check(matches(withText(duration)))

    private fun deleteAndConfirm() {
        onView(withId(R.id.checkup_delete)).perform(click())
        onView(withText(R.string.um_yes)).perform(click())
    }

    private fun createBrushing(duration: Long, date: OffsetDateTime) = Brushing(
        duration = duration,
        coins = 0,
        dateTime = date,
        game = Game.COACH_PLUS.name,
        goalDuration = DEFAULT_BRUSHING_GOAL,
        kolibreeId = null,
        points = 0,
        processedData = "",
        profileId = ProfileBuilder.DEFAULT_ID,
        toothbrushMac = null
    )

    private fun defaultIntent() =
        startDayCheckupActivityIntent(
            context(),
            TrustedClock.getNowOffsetDateTime()
        )
}
