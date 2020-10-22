/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.celebration

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.app.ui.home.HomeScreenActivityEspressoTest
import com.kolibree.android.rewards.feedback.ChallengeCompletedFeedback
import com.kolibree.android.rewards.feedback.personal.BackendChallengeCompleted
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.AMAZON_DASH
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.COMPLETE_YOUR_PROFILE
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.RATE_THE_APP
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.REFER_A_FRIEND
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.SUBSCRIBE_FOR_WEEKLY_REVIEW
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.TURN_ON_BRUSHING_REMINDERS
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.TURN_ON_BRUSH_SYNC_REMINDERS
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.TURN_ON_EMAIL_NOTIFICATIONS
import com.kolibree.android.test.utils.rewards.FakeFeedbackRepository
import org.hamcrest.Matchers.allOf
import org.junit.Test

internal class EarnPointsCelebrationEspressoTest : HomeScreenActivityEspressoTest() {

    private val feedbackRepository: FakeFeedbackRepository
        get() = component().feedbackRepository()

    @Test
    fun doNotShowCelebrationIfThereAreNoCompletedChallenges() {
        feedbackRepository.mock(emptyList())

        launchActivity()

        checkCelebrationScreen(expectedChallenge = null)
    }

    @Test
    fun showCelebrationWhenChallengeCompleted() {
        val expectedChallenge = ExpectedChallenge(
            id = RATE_THE_APP,
            points = 10,
            nameRes = R.string.earn_points_celebration_rate_the_app_name
        )

        feedbackRepository.mock(
            ChallengeCompletedFeedback(
                id = 1,
                challengesCompleted = listOf(
                    expectedChallenge.asBackendChallenge()
                )
            )
        )

        launchActivity()
        checkCelebrationScreen(expectedChallenge)

        clickOnDone()
        checkCelebrationScreen(expectedChallenge = null)
    }

    @Test
    fun showMultipleCelebrationsAtOnce() {
        val expectedChallenges = EarnPointsChallenge.Id.all().mapIndexed { index, id ->
            ExpectedChallenge(
                id = id,
                points = index * 100,
                nameRes = when (id) {
                    COMPLETE_YOUR_PROFILE -> R.string.earn_points_celebration_complete_profile_name
                    TURN_ON_EMAIL_NOTIFICATIONS -> R.string.earn_points_celebration_email_notifications_name
                    TURN_ON_BRUSH_SYNC_REMINDERS -> R.string.earn_points_celebration_brush_sync_reminders_name
                    TURN_ON_BRUSHING_REMINDERS -> R.string.earn_points_celebration_brushing_reminders_name
                    RATE_THE_APP -> R.string.earn_points_celebration_rate_the_app_name
                    SUBSCRIBE_FOR_WEEKLY_REVIEW -> R.string.earn_points_celebration_weekly_review_name
                    REFER_A_FRIEND -> R.string.earn_points_celebration_refer_a_friend_name
                    AMAZON_DASH -> R.string.earn_points_celebration_amazon_dash
                }
            )
        }

        feedbackRepository.mock(
            ChallengeCompletedFeedback(
                id = 1,
                challengesCompleted = expectedChallenges.map { it.asBackendChallenge() }
            )
        )

        launchActivity()

        for (expectedChallenge in expectedChallenges) {
            checkCelebrationScreen(expectedChallenge)
            clickOnDone()
        }

        checkCelebrationScreen(expectedChallenge = null)
    }

    private fun checkCelebrationScreen(
        expectedChallenge: ExpectedChallenge?
    ) {
        if (expectedChallenge == null) {
            onView(withId(R.id.earn_points_celebration)).check(doesNotExist())
            return
        }

        onView(withId(R.id.earn_points_celebration)).check(matches(isDisplayed()))

        onView(allOf(withId(R.id.celebration_animation), isCompletelyDisplayed()))
            .check(matches(isDisplayed()))

        onView(allOf(withId(R.id.celebration_title), isCompletelyDisplayed()))
            .check(matches(isDisplayed()))

        val challengeName = context().getString(expectedChallenge.nameRes)

        val bodyPoints = context().resources.getQuantityString(
            R.plurals.earn_points_celebration_body_points,
            expectedChallenge.points,
            expectedChallenge.points.toString()
        )

        val expectedBody = context().getString(
            R.string.earn_points_celebration_body,
            challengeName,
            bodyPoints
        )

        onView(allOf(withId(R.id.celebration_body), isCompletelyDisplayed()))
            .check(matches(isDisplayed()))
            .check(matches(withText(expectedBody)))

        onView(allOf(withId(R.id.celebration_button), isCompletelyDisplayed()))
            .check(matches(isDisplayed()))
    }

    private fun clickOnDone() {
        onView(allOf(withId(R.id.celebration_button), isCompletelyDisplayed()))
            .perform(click())
    }

    private class ExpectedChallenge(
        val id: EarnPointsChallenge.Id,
        val points: Int,
        val nameRes: Int
    ) {

        fun asBackendChallenge() = BackendChallengeCompleted(
            id = id.backendId,
            name = "mock name",
            category = "mock category",
            greetingMessage = "mock greetingMessage",
            description = "mock description",
            pictureUrl = "mock pictureUrl",
            smilesReward = points,
            action = null
        )
    }
}
