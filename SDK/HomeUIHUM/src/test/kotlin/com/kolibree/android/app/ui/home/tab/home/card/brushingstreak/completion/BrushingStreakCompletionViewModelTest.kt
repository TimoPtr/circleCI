/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushingstreak.completion

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

internal class BrushingStreakCompletionViewModelTest : BaseUnitTest() {

    @Test
    fun `when user click on completion it sends an action`() {
        val viewModel =
            BrushingStreakCompletionViewModel(BrushingStreakCompletionViewState.initial())

        val testObserver = viewModel.actionsObservable.test()

        viewModel.onCompleteClick()

        testObserver.assertValue(BrushingStreakCompletionActions.CompleteChallenge)
    }

    @Test
    fun `when user click on completion it sends an analytics event`() {
        val viewModel =
            BrushingStreakCompletionViewModel(BrushingStreakCompletionViewState.initial())

        viewModel.actionsObservable.test()

        viewModel.onCompleteClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("BrushingStreak_Celebration_Complete"))
    }
}
