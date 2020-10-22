/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.smileshistory

import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class SmilesHistoryViewModelTest : BaseUnitTest() {

    private val useCase: SmilesHistoryUseCase = mock()

    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }

    override fun tearDown() {
        super.tearDown()

        FailEarly.overrideDelegateWith(TestDelegate)
    }

    private fun createViewModel(viewState: SmilesHistoryViewState = SmilesHistoryViewState()): SmilesHistoryViewModel {
        whenever(useCase.smilesHistoryStream()).thenReturn(Flowable.never())
        return SmilesHistoryViewModel(
            useCase,
            viewState
        )
    }

    @Test
    fun `initial ViewState`() {
        val viewModel = createViewModel()
        val observerItems = viewModel.items.test()

        observerItems.assertHasValue()
        observerItems.assertValue(listOf(SimilesHistoryHeaderListItem))
    }

    @Test
    fun `restore ViewState`() {
        val expectedItems = listOf(
            SimilesHistoryHeaderListItem,
            mock()
        )
        val viewState =
            SmilesHistoryViewState(
                expectedItems
            )

        val viewModel = createViewModel(viewState)

        val observerItems = viewModel.items.test()

        observerItems.assertHasValue()
        observerItems.assertValue(expectedItems)
    }

    @Test
    fun `when event are unknown it does not create item`() {
        whenever(useCase.smilesHistoryStream()).thenReturn(
            Flowable.just(
                Pair(
                    listOf(
                        SmilesHistoryItem.UnknownHistoryItem
                    ), listOf()
                )
            )
        )

        val viewModel = createViewModel()

        val observerItems = viewModel.items.test()

        observerItems.assertHasValue()
        observerItems.assertValue(listOf(SimilesHistoryHeaderListItem))
    }

    @Test
    fun `never two headerItem in the items livedata`() {
        val viewState = SmilesHistoryViewState(
            listOf(
                SimilesHistoryHeaderListItem,
                SimilesHistoryHeaderListItem
            )
        )

        assertTrue(viewState.itemsWithHeader.count { it == SimilesHistoryHeaderListItem } == 1)
    }

    @Test
    fun `when there is no history apps should show information`() {
        val viewModel = createViewModel()

        val testObserver = viewModel.isEmpty.test()

        testObserver.assertValue(true)

        viewModel.updateViewState {
            copy(itemsResources = listOf(mock()))
        }

        testObserver.assertValue(false)
    }

    @Test
    fun `onBackPressed sends Analytics event`() {
        createViewModel().onBackPressed()

        verify(eventTracker).sendEvent(AnalyticsEvent("PointsHistory_Quit"))
    }
}
