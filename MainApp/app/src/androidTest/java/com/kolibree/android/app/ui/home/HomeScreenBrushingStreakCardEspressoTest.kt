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
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.google.common.base.Optional
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.R
import com.kolibree.android.app.dagger.EspressoHumChallengeModule
import com.kolibree.android.app.ui.home.HomeTabNavigationUtils.scrollToBrushingStreakCard
import com.kolibree.android.app.ui.home.guidedbrushing.startscreen.GuidedBrushingStartScreenActivity
import com.kolibree.android.rewards.personalchallenge.presentation.HumChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.completedDiscoverGuidedBrushingChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.notAcceptedDiscoverGuidedBrushingChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.onGoingBrushFor5Days
import com.kolibree.android.rewards.personalchallenge.presentation.onGoingDiscoverGuidedBrushingChallenge
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.idlingresources.IdlingResourceFactory
import com.kolibree.android.test.utils.runAndCheckIntent
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import org.junit.Test

internal class HomeScreenBrushingStreakCardEspressoTest : HomeScreenActivityEspressoTest() {

    @Test
    fun singleDayChallenge_happyPath() {
        prepareMocks()

        val challengeStream = HumChallengeStream()

        launchActivity()
        scrollToBrushingStreakCard()

        challengeStream.nextChallenge(notAcceptedDiscoverGuidedBrushingChallenge())

        clickCard()

        checkCard(
            title = R.string.brushing_streak_title_for_1_day_challenge,
            subtitle = R.string.brushing_streak_discover_guided_brushing_subtitle,
            descriptionTitle = R.string.brushing_streak_discover_guided_brushing_title_description_not_accepted,
            description = R.string.brushing_streak_discover_guided_brushing_description_ongoing,
            actionButtonTitle = R.string.brushing_streak_accept_challenge,
            isActionButtonVisible = true,
            isSubtitleVisible = true
        )
        makeScreenshot(
            activity.findViewById(R.id.brushing_streak_card),
            "HomeTab_BrushingStreakCard_NotCompleted"
        )

        // accept challenge
        challengeStream.nextChallenge(onGoingDiscoverGuidedBrushingChallenge())

        checkCard(
            title = R.string.brushing_streak_title_for_1_day_challenge,
            subtitle = R.string.brushing_streak_discover_guided_brushing_subtitle,
            descriptionTitle = R.string.brushing_streak_discover_guided_brushing_title_description_not_accepted,
            description = R.string.brushing_streak_discover_guided_brushing_description_ongoing,
            actionButtonTitle = R.string.brushing_streak_discover_guided_brushing_accept_challenge,
            isActionButtonVisible = true,
            isSubtitleVisible = false
        )
        makeScreenshot(
            activity.findViewById(R.id.brushing_streak_card),
            "HomeTab_BrushingStreakCard_OnGoing"
        )

        // challenge is completed
        challengeStream.nextChallenge(completedDiscoverGuidedBrushingChallenge())
        checkCard(
            title = R.string.brushing_streak_title_for_1_day_challenge,
            subtitle = R.string.brushing_streak_discover_guided_brushing_subtitle,
            descriptionTitle = R.string.brushing_streak_title_description_completed,
            description = R.string.brushing_streak_discover_guided_brushing_description_complete,
            actionButtonTitle = R.string.brushing_streak_discover_complete_challenge,
            isActionButtonVisible = true,
            isSubtitleVisible = false
        )
        makeScreenshot(
            activity.findViewById(R.id.brushing_streak_card),
            "HomeTab_BrushingStreakCard_Completed"
        )
    }

    @Test
    fun whenChallengeIsCompletedThenShowChallengeCompletedScreen() {
        prepareMocks()

        val challengeStream = HumChallengeStream()

        launchActivity()

        scrollToBrushingStreakCard()

        val challenge = completedDiscoverGuidedBrushingChallenge()
        challengeStream.nextChallenge(challenge)

        whenVisible(R.id.brushing_streak_action_button) {
            onView(withId(R.id.brushing_streak_action_button)).check(matches(isDisplayed()))
        }

        onView(withId(R.id.brushing_streak_action_button))
            .perform(click())

        checkCompletedChallengeScreen(challenge.smiles)
        makeScreenshot("ChallengeCompletedScreen")
    }

    @Test
    fun acceptedGuidedBrushingChallengeWithAction() {
        prepareMocks(mockConnectionWithState = KLTBConnectionState.ACTIVE)

        val challengeStream = HumChallengeStream()

        launchActivity()

        scrollToBrushingStreakCard()

        val challenge = onGoingDiscoverGuidedBrushingChallenge()
        challengeStream.nextChallenge(challenge)

        clickCard()

        checkCard(
            title = R.string.brushing_streak_title_for_1_day_challenge,
            subtitle = R.string.brushing_streak_discover_guided_brushing_subtitle,
            descriptionTitle = R.string.brushing_streak_discover_guided_brushing_title_description_not_accepted,
            description = R.string.brushing_streak_discover_guided_brushing_description_ongoing,
            actionButtonTitle = R.string.brushing_streak_discover_guided_brushing_accept_challenge,
            isActionButtonVisible = true,
            isSubtitleVisible = false
        )

        runAndCheckIntent(hasComponent(GuidedBrushingStartScreenActivity::class.java.name)) {
            whenVisible(R.id.brushing_streak_action_button) {
                onView(withId(R.id.brushing_streak_action_button)).perform(click())
            }
        }
    }

    @Test
    fun multiDaysChallenge_checkProgress() {
        prepareMocks()

        val challengeStream = HumChallengeStream()

        launchActivity()

        scrollToBrushingStreakCard()

        val challenge = onGoingBrushFor5Days(20)
        challengeStream.nextChallenge(challenge)

        checkCard(
            title = R.string.brushing_streak_title_for_5_days_challenge,
            subtitle = R.string.brushing_streak_five_consecutive_days_subtitle,
            descriptionTitle = R.string.brushing_streak_title_description_ongoing,
            description = R.string.brushing_streak_five_consecutive_days_description_ongoing,
            actionButtonTitle = R.string.empty,
            isActionButtonVisible = false,
            isSubtitleVisible = false
        )

        onView(withId(R.id.brushing_streak_progression)).check(matches(isDisplayed()))
    }

    private fun checkCompletedChallengeScreen(smiles: Int) {
        onView(withText(R.string.challenge_completed_dialog_title)).check(matches(isDisplayed()))

        val smilesPoints =
            context().getString(R.string.challenge_completed_dialog_body_highlight, smiles)
        val body = context().getString(R.string.challenge_completed_dialog_body, smilesPoints)

        onView(withText(body)).check(matches(isDisplayed()))

        onView(withText(R.string.challenge_completed_dialog_button)).check(matches(isDisplayed()))
    }

    private fun checkCard(
        @StringRes title: Int = R.string.empty,
        @StringRes subtitle: Int = R.string.empty,
        @StringRes descriptionTitle: Int = R.string.empty,
        @StringRes description: Int = R.string.empty,
        @StringRes actionButtonTitle: Int = R.string.empty,
        isActionButtonVisible: Boolean = false,
        isSubtitleVisible: Boolean = false
    ) {
        checkText(R.id.brushing_streak_title, title)
        checkText(R.id.brushing_streak_subtitle, subtitle, isSubtitleVisible)
        checkText(R.id.brushing_streak_description_title, descriptionTitle)
        checkText(R.id.brushing_streak_description, description)
        checkText(R.id.brushing_streak_action_button, actionButtonTitle, isActionButtonVisible)
    }

    private fun checkText(@IdRes id: Int, @StringRes title: Int, visible: Boolean = true) {
        if (visible) {
            val expectedText = context().getString(title)
            IdlingResourceFactory.textViewContent(id, expectedText).waitForIdle()
            onView(withId(id)).check(matches(withText(expectedText)))
        } else {
            whenGone(id) {
                onView(withId(id)).check(isGone())
            }
        }
    }

    private fun clickCard() {
        onView(withId(R.id.brushing_streak_top_container)).perform(click())
    }

    private fun whenVisible(@IdRes id: Int, check: () -> Unit) {
        IdlingResourceFactory.viewVisibility(id, View.VISIBLE).waitForIdle()
        check()
    }

    private fun whenGone(@IdRes id: Int, check: () -> Unit) {
        IdlingResourceFactory.viewVisibility(id, View.GONE).waitForIdle()
        check()
    }

    private fun isGone(): ViewAssertion =
        matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE))

    private fun isVisible(): ViewAssertion =
        matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
}

class HumChallengeStream {
    private val relay: PublishRelay<Optional<HumChallenge>> = PublishRelay.create()
    private val stream = relay.hide().toFlowable(BackpressureStrategy.BUFFER)

    init {
        val useCaseMock = EspressoHumChallengeModule.mock
        whenever(useCaseMock.challengeStream()).thenReturn(stream)
        whenever(useCaseMock.completeChallenge(any())).thenReturn(Completable.complete())
        whenever(useCaseMock.acceptChallenge(any())).thenReturn(Completable.complete())
    }

    fun nextChallenge(challenge: HumChallenge) {
        relay.accept(Optional.of(challenge))
    }
}
