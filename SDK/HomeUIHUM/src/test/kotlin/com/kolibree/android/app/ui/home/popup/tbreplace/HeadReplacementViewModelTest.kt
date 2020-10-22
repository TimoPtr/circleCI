/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.tbreplace

import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority.ReplaceHeadItem
import com.kolibree.android.app.ui.priority.DisplayPriorityItemUseCase
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnected
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnecting
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushDisconnected
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewModel
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewState
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate

class HeadReplacementViewModelTest : BaseUnitTest() {

    private val priorityItemUseCase: DisplayPriorityItemUseCase<HomeDisplayPriority> = mock()
    private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel = mock()
    private val headReplacementUseCase: HeadReplacementUseCase = mock()
    private val navigator: HumHomeNavigator = mock()

    private lateinit var viewModel: HeadReplacementViewModel

    @Before
    fun setUp() {
        viewModel = HeadReplacementViewModel(
            priorityItemUseCase,
            toothbrushConnectionStateViewModel,
            headReplacementUseCase,
            navigator
        )
    }

    @Test
    fun `monitorHeadReplacement submits the item if it is displayable and shows the dialog if it is consumed`() {
        val (mac, date) = headReplacementHappyPath()

        viewModel.pushLifecycleTo(ON_RESUME)

        verify(priorityItemUseCase).submitAndWaitFor(ReplaceHeadItem)
        verify(headReplacementUseCase).setReplaceHeadShown(mac, date)
        verify(navigator).showHeadReplacementDialog()
    }

    @Test
    fun `monitorHeadReplacement does nothing if the toothbrush is not connected`() {
        headReplacementHappyPath()

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(
            Flowable.just(
                ToothbrushConnectionStateViewState(),
                ToothbrushConnectionStateViewState(SingleToothbrushDisconnected("mac")),
                ToothbrushConnectionStateViewState(SingleToothbrushConnecting("mac"))
            )
        )

        viewModel.pushLifecycleTo(ON_RESUME)

        verifyZeroInteractions(navigator, headReplacementUseCase, priorityItemUseCase)
    }

    @Test
    fun `monitorHeadReplacement does not submit and display the dialog if the it is not displayable`() {
        val (mac, _) = headReplacementHappyPath()

        whenever(headReplacementUseCase.isDisplayable(mac)).thenReturn(Maybe.empty())

        viewModel.pushLifecycleTo(ON_RESUME)

        verifyZeroInteractions(navigator, priorityItemUseCase)
    }

    @Test
    fun `monitorHeadReplacement submits the item if it is displayable but does not show the dialog if it is not consumed`() {
        headReplacementHappyPath()

        whenever(priorityItemUseCase.submitAndWaitFor(ReplaceHeadItem)).thenReturn(Completable.never())

        viewModel.pushLifecycleTo(ON_RESUME)

        verify(priorityItemUseCase).submitAndWaitFor(ReplaceHeadItem)
        verifyZeroInteractions(navigator)
    }

    private fun headReplacementHappyPath(): Pair<String, LocalDate> {
        val mac = "mac"
        val date = LocalDate.of(1, 2, 3)

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(
            Flowable.just(ToothbrushConnectionStateViewState(SingleToothbrushConnected(mac)))
        )
        whenever(headReplacementUseCase.isDisplayable(mac))
            .thenReturn(Maybe.just(date))
        whenever(priorityItemUseCase.submitAndWaitFor(ReplaceHeadItem))
            .thenReturn(Completable.complete())
        whenever(headReplacementUseCase.setReplaceHeadShown(mac, date))
            .thenReturn(Completable.complete())
        return Pair(mac, date)
    }
}
