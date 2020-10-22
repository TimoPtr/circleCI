/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushingstreak

import androidx.lifecycle.Lifecycle
import com.google.common.base.Optional
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.game.ActivityGame
import com.kolibree.android.app.ui.game.StartNonUnityGameUseCase
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.rewards.personalchallenge.logic.HumChallengeUseCase
import com.kolibree.android.rewards.personalchallenge.presentation.CompletedChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.HumChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.HumChallengeRecommendationAction
import com.kolibree.android.rewards.personalchallenge.presentation.NotAcceptedChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.OnGoingChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.notAcceptedDiscoverGuidedBrushingChallenge
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.tracker.Analytics
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.subjects.CompletableSubject
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class BrushingStreakCardViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: BrushingStreakCardViewModel

    private val humChallengeUseCase: HumChallengeUseCase = mock()

    private val homeNavigator: HumHomeNavigator = mock()

    private val startNonUnityGameUseCase: StartNonUnityGameUseCase = mock()

    override fun setup() {
        super.setup()

        Analytics.eventTracker = eventTracker
        viewModel = BrushingStreakCardViewModel(
            BrushingStreakCardViewState.initial(DynamicCardPosition.ZERO),
            humChallengeUseCase,
            homeNavigator,
            startNonUnityGameUseCase
        )
    }

    @Test
    fun `card subscribes for challenges if view is already started`() {
        val challenge: HumChallenge = notAcceptedDiscoverGuidedBrushingChallenge()
        whenever(humChallengeUseCase.challengeStream())
            .thenReturn(Flowable.just(Optional.of(challenge)))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        verify(humChallengeUseCase).challengeStream()

        val expectedViewState = BrushingStreakCardViewState(
            visible = true,
            position = DynamicCardPosition.ZERO,
            challenge = challenge,
            isExpanded = false
        )

        assertEquals(expectedViewState, viewModel.getViewState())
    }

    @Test
    fun `card is not visible if there is no challenge`() {
        whenever(humChallengeUseCase.challengeStream())
            .thenReturn(Flowable.just(Optional.absent()))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        verify(humChallengeUseCase).challengeStream()

        val expectedViewState = BrushingStreakCardViewState(
            visible = false,
            position = DynamicCardPosition.ZERO,
            challenge = null,
            isExpanded = false
        )

        assertEquals(expectedViewState, viewModel.getViewState())
    }

    @Test
    fun `when user clicks Complete Challenge then challenge is completed`() {
        val challenge = mock<CompletedChallenge>()
        whenever(humChallengeUseCase.completeChallenge(challenge))
            .thenReturn(Completable.complete())

        viewModel.onCompleteChallengeClick(challenge)

        verify(humChallengeUseCase).completeChallenge(challenge)
    }

    @Test
    fun `when user clicks Accept Challenge then challenge is accepted`() {
        val challenge = mock<NotAcceptedChallenge>()
        whenever(humChallengeUseCase.acceptChallenge(challenge))
            .thenReturn(Completable.complete())

        viewModel.onAcceptChallengeClick(challenge)

        verify(humChallengeUseCase).acceptChallenge(challenge)
    }

    @Test
    fun `when challenge is completed then show ChallengeCompleted dialog`() {
        val challenge: CompletedChallenge = mock()
        val smiles = 15
        whenever(challenge.smiles).thenReturn(smiles)
        whenever(humChallengeUseCase.completeChallenge(challenge))
            .thenReturn(Completable.complete())

        viewModel.onCompleteChallengeClick(challenge)

        verify(homeNavigator).showChallengeCompletedScreen(smiles)
    }

    @Test
    fun `when action is clicked then send Action event`() {
        val challenge: OnGoingChallenge = mock()
        whenever(challenge.action).thenReturn(HumChallengeRecommendationAction.NOTHING)

        viewModel.onActionClick(challenge)

        verify(eventTracker).sendEvent(AnalyticsEvent("BrushingStreak_DiscoverActivity_GoToGB"))
    }

    @Test
    fun `when one day challenge is accepted then send Accept event`() {
        val challenge: NotAcceptedChallenge = mock()
        whenever(challenge.isMoreThanOneDay()).thenReturn(false)
        whenever(humChallengeUseCase.acceptChallenge(challenge))
            .thenReturn(Completable.complete())

        viewModel.onAcceptChallengeClick(challenge)

        verify(eventTracker).sendEvent(AnalyticsEvent("BrushingStreak_DiscoverActivity_Accept"))
    }

    @Test
    fun `when multi day challenge is accepted then send Accept event`() {
        val challenge: NotAcceptedChallenge = mock()
        whenever(challenge.isMoreThanOneDay()).thenReturn(true)
        whenever(humChallengeUseCase.acceptChallenge(challenge))
            .thenReturn(Completable.complete())

        viewModel.onAcceptChallengeClick(challenge)

        verify(eventTracker).sendEvent(AnalyticsEvent("BrushingStreak_Challenge_Accept"))
    }

    @Test
    fun `when one day challenge is completed then send Complete event`() {
        val challenge: CompletedChallenge = mock()
        whenever(challenge.isMoreThanOneDay()).thenReturn(false)
        whenever(humChallengeUseCase.completeChallenge(challenge))
            .thenReturn(Completable.complete())

        viewModel.onCompleteChallengeClick(challenge)

        verify(eventTracker).sendEvent(AnalyticsEvent("BrushingStreak_DiscoverActivity_Complete"))
    }

    @Test
    fun `when multi day challenge is accepted then send Complete event`() {
        val challenge: CompletedChallenge = mock()
        whenever(challenge.isMoreThanOneDay()).thenReturn(true)
        whenever(humChallengeUseCase.completeChallenge(challenge))
            .thenReturn(Completable.complete())

        viewModel.onCompleteChallengeClick(challenge)

        verify(eventTracker).sendEvent(AnalyticsEvent("BrushingStreak_Challenge_Complete"))
    }

    @Test
    fun `when action COACH+ is clicked then app starts guided brushing`() {
        val challenge: OnGoingChallenge = mock()
        whenever(challenge.action).thenReturn(HumChallengeRecommendationAction.COACH_PLUS)
        val subject = CompletableSubject.create()
        whenever(startNonUnityGameUseCase.start(ActivityGame.CoachPlus, false)).thenReturn(subject)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        viewModel.onActionClick(challenge)

        assertTrue(subject.hasObservers())
    }

    @Test
    fun `onCardExpanded send Analytics event`() {
        viewModel.onCardExpanded()

        verify(eventTracker).sendEvent(AnalyticsEvent("BrushingStreak_Open"))
    }
}
