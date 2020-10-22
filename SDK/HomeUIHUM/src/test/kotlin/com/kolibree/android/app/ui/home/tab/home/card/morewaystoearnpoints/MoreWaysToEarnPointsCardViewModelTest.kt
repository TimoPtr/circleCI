/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.morewaystoearnpoints

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.navigation.HomeNavigator
import com.kolibree.android.app.ui.settings.SettingsInitialAction
import com.kolibree.android.extensions.toLoopedFlowable
import com.kolibree.android.rewards.morewaystoearnpoints.logic.MoreWaysToGetPointsCardUseCase
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import org.junit.Test

class MoreWaysToEarnPointsCardViewModelTest : BaseUnitTest() {

    private val useCase: MoreWaysToGetPointsCardUseCase = mock()

    private val navigator: MoreWaysToEarnPointsCardNavigator = mock()

    private val homeNavigator: HomeNavigator = mock()

    private val resourceProvider = MoreWaysToEarnPointsCardItemResourceProviderImpl()

    @Test
    fun `fetched cards are pushed to state`() {
        whenever(useCase.getChallengesToBeDisplayedStream())
            .thenReturn(
                Flowable.just(
                    EarnPointsChallenge.Id.values()
                        .map { EarnPointsChallenge(it, POINTS_FOR_EACH_CARD) })
            )
        val viewModel = createViewModel()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        with(viewModel.getViewState()!!) {
            EarnPointsChallenge.Id.values().forEach { cardId ->
                assertEquals(
                    MoreWaysToEarnPointsCardItemBindingModel(
                        EarnPointsChallenge(cardId, POINTS_FOR_EACH_CARD),
                        resourceProvider.getIcon(cardId),
                        resourceProvider.getHeader(cardId),
                        resourceProvider.getBody(cardId)
                    ), cards[cardId.ordinal]
                )
            }
        }
    }

    @Test
    fun `card list is update each time new data is emitted`() {
        val states = listOf(
            EarnPointsChallenge.Id.values().map { EarnPointsChallenge(it, POINTS_FOR_EACH_CARD) },
            listOf(EarnPointsChallenge(EarnPointsChallenge.Id.RATE_THE_APP, POINTS_FOR_EACH_CARD)),
            listOf(
                EarnPointsChallenge(EarnPointsChallenge.Id.RATE_THE_APP, POINTS_FOR_EACH_CARD),
                EarnPointsChallenge(
                    EarnPointsChallenge.Id.COMPLETE_YOUR_PROFILE,
                    POINTS_FOR_EACH_CARD
                )
            ),
            emptyList()
        )

        val scheduler = TestScheduler()

        whenever(useCase.getChallengesToBeDisplayedStream())
            .thenReturn(states.toLoopedFlowable(SECONDS_INTERVAL, TimeUnit.SECONDS, scheduler))

        val viewModel = createViewModel()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        states.forEach { state ->
            scheduler.advanceTimeBy(SECONDS_INTERVAL, TimeUnit.SECONDS)
            0.until(state.size).forEach { cardIndex ->
                val stateCard = state[cardIndex]
                assertEquals(
                    MoreWaysToEarnPointsCardItemBindingModel(
                        stateCard,
                        resourceProvider.getIcon(stateCard.id),
                        resourceProvider.getHeader(stateCard.id),
                        resourceProvider.getBody(stateCard.id)
                    ), viewModel.getViewState()!!.cards[cardIndex]
                )
            }
        }
    }

    @Test
    fun `card clicks emit analytics events`() {
        val viewModel = createViewModel()

        EarnPointsChallenge.Id.values().forEach { challengeId ->
            viewModel.onItemClick(EarnPointsChallenge(challengeId, POINTS_FOR_EACH_CARD))
            verify(eventTracker)
                .sendEvent(MoreWaysToEarnPointsCardAnalytics.challengeCardClick(challengeId))
        }
    }

    @Test
    fun `click on Complete Your Profile card navigates to settings`() {
        val viewModel = createViewModel()

        viewModel.onItemClick(
            EarnPointsChallenge(EarnPointsChallenge.Id.COMPLETE_YOUR_PROFILE, POINTS_FOR_EACH_CARD)
        )

        verify(navigator).showSettingsScreen(withInitialAction = null)
    }

    @Test
    fun `click on Reminder cards navigates to notifications`() {
        val viewModel = createViewModel()
        val reminderIds = listOf(
            EarnPointsChallenge.Id.TURN_ON_EMAIL_NOTIFICATIONS,
            EarnPointsChallenge.Id.TURN_ON_BRUSH_SYNC_REMINDERS,
            EarnPointsChallenge.Id.TURN_ON_BRUSHING_REMINDERS
        )

        0.until(reminderIds.size).forEach { index ->
            viewModel.onItemClick(EarnPointsChallenge(reminderIds[index], POINTS_FOR_EACH_CARD))
            verify(navigator, times(index + 1)).showNotificationScreen()
        }
    }

    @Test
    fun `click on Weekly Review card navigates to settings with scroll action`() {
        val viewModel = createViewModel()

        viewModel.onItemClick(
            EarnPointsChallenge(
                EarnPointsChallenge.Id.SUBSCRIBE_FOR_WEEKLY_REVIEW,
                POINTS_FOR_EACH_CARD
            )
        )

        verify(navigator)
            .showSettingsScreen(withInitialAction = SettingsInitialAction.SCROLL_TO_WEEKLY_REVIEW)
    }

    @Test
    fun `click on Amazon Dash card navigates to Amazon Dash connect`() {
        val viewModel = createViewModel()

        viewModel.onItemClick(
            EarnPointsChallenge(
                EarnPointsChallenge.Id.AMAZON_DASH,
                POINTS_FOR_EACH_CARD
            )
        )

        verify(homeNavigator).showAmazonDashConnectScreen()
    }

    private fun createViewModel(
        initialState: MoreWaysToEarnPointsCardViewState =
            MoreWaysToEarnPointsCardViewState.initial(DynamicCardPosition.ZERO)
    ) = MoreWaysToEarnPointsCardViewModel(
        initialState,
        useCase,
        resourceProvider,
        navigator,
        homeNavigator
    )
}

private const val SECONDS_INTERVAL = 3L
private const val POINTS_FOR_EACH_CARD = 2990
