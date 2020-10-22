/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.celebration

import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.EarnPointsChallengeUseCase
import com.kolibree.android.rewards.morewaystoearnpoints.model.CompleteEarnPointsChallenge
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.COMPLETE_YOUR_PROFILE
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.RATE_THE_APP
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.REFER_A_FRIEND
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.TURN_ON_BRUSH_SYNC_REMINDERS
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge.Id.TURN_ON_EMAIL_NOTIFICATIONS
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import junit.framework.Assert.assertEquals
import org.junit.Test

class EarnPointsCelebrationViewModelTest : BaseUnitTest() {

    private val navigator: EarnPointsCelebrationNavigator = mock()
    private val resourceProvider: EarnPointsCelebrationResourceProvider = mock()
    private val earnPointsChallengeUseCase: EarnPointsChallengeUseCase = mock()

    private lateinit var viewModel: EarnPointsCelebrationViewModel

    private val viewState: EarnPointsCelebrationViewState
        get() = viewModel.getViewState()!!

    override fun setup() {
        super.setup()
        whenever(earnPointsChallengeUseCase.markAsConsumed(any()))
            .thenReturn(Completable.complete())
    }

    @Test
    fun `first item is selected by default`() {
        mockViewModel(
            challenge(id = COMPLETE_YOUR_PROFILE, points = 10),
            challenge(id = TURN_ON_EMAIL_NOTIFICATIONS, points = 15),
            challenge(id = TURN_ON_BRUSH_SYNC_REMINDERS, points = 20)
        )

        assertEquals(0, viewState.selectedIndex)
    }

    @Test
    fun `finishes after last item`() {
        mockViewModel(
            challenge(id = COMPLETE_YOUR_PROFILE, points = 10),
            challenge(id = TURN_ON_EMAIL_NOTIFICATIONS, points = 15)
        )

        viewModel.onButtonClick()
        viewModel.onButtonClick()
        verify(navigator).finish()
    }

    @Test
    fun `increases selected index after each click`() {
        mockViewModel(
            challenge(id = COMPLETE_YOUR_PROFILE, points = 10),
            challenge(id = TURN_ON_EMAIL_NOTIFICATIONS, points = 15),
            challenge(id = TURN_ON_BRUSH_SYNC_REMINDERS, points = 20)
        )

        assertEquals(0, viewState.selectedIndex)

        viewModel.onButtonClick()
        assertEquals(1, viewState.selectedIndex)

        viewModel.onButtonClick()
        assertEquals(2, viewState.selectedIndex)

        viewModel.onButtonClick()
        assertEquals(2, viewState.selectedIndex)
    }

    @Test
    fun `animates only selected item`() {
        val selectedItem = challenge(id = COMPLETE_YOUR_PROFILE, points = 10)
        val notSelectedItem = challenge(id = TURN_ON_EMAIL_NOTIFICATIONS, points = 15)

        mockViewModel(
            selectedItem, // by default first item is selected
            notSelectedItem
        )

        val selectedObserver = viewModel.animate(selectedItem.challenge).test()
        selectedObserver.assertValue(true)

        val notSelectedObserver = viewModel.animate(notSelectedItem.challenge).test()
        notSelectedObserver.assertValue(false)
    }

    @Test
    fun `marks challenges as consumed`() {
        val mockChallenges = listOf(
            challenge(id = COMPLETE_YOUR_PROFILE, points = 10, feedbackActionId = 1),
            challenge(id = TURN_ON_EMAIL_NOTIFICATIONS, points = 15, feedbackActionId = 1),
            challenge(id = RATE_THE_APP, points = 20, feedbackActionId = 2),
            challenge(id = REFER_A_FRIEND, points = 25, feedbackActionId = 2)
        )

        mockViewModel(*mockChallenges.toTypedArray())

        verify(earnPointsChallengeUseCase).markAsConsumed(mockChallenges)
    }

    private fun challenge(
        id: EarnPointsChallenge.Id,
        points: Int,
        feedbackActionId: Long = 0
    ) = CompleteEarnPointsChallenge(
        challenge = EarnPointsChallenge(id, points),
        feedbackId = feedbackActionId
    )

    private fun mockViewModel(vararg items: CompleteEarnPointsChallenge) {
        viewModel = EarnPointsCelebrationViewModel(
            initialViewState = null,
            challenges = items.toList(),
            navigator = navigator,
            resourceProvider = resourceProvider,
            earnPointsChallengeUseCase = earnPointsChallengeUseCase
        )
    }
}
