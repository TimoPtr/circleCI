/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.toolbox

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDotUseCase
import com.kolibree.android.app.ui.home.toolbox.ToolboxConfiguration
import com.kolibree.android.app.ui.home.toolbox.ToolboxViewModel
import com.kolibree.android.app.ui.home.toolbox.ToolboxViewState
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority.ToolboxItem
import com.kolibree.android.app.ui.priority.DisplayPriorityItemUseCase
import com.kolibree.android.app.ui.toolbartoothbrush.NoBluetooth
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewModel
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewState
import com.kolibree.android.test.lifecycleTester
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import org.junit.Test

class ToolboxPriorityDisplayViewModelTest : BaseUnitTest() {

    private val timeScheduler = TestScheduler()

    private val pulsingDotUseCase: PulsingDotUseCase = mock()

    private val priorityItemUseCase: DisplayPriorityItemUseCase<HomeDisplayPriority> = mock()

    private val toolboxViewModel: ToolboxViewModel = mock()

    private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel = mock()

    private lateinit var viewModel: ToolboxPriorityDisplayViewModel

    override fun setup() {
        super.setup()

        mockDefaultStreams()

        viewModel = ToolboxPriorityDisplayViewModel(
            pulsingDotUseCase, priorityItemUseCase, toolboxViewModel,
            timeScheduler, toothbrushConnectionStateViewModel
        )
    }

    private fun mockDefaultStreams() {
        whenever(priorityItemUseCase.submitAndWaitFor(ToolboxItem)).thenReturn(Completable.never())
        whenever(pulsingDotUseCase.shouldShowExplanation()).thenReturn(Flowable.never())
        whenever(toolboxViewModel.viewStateFlowable).thenReturn(Flowable.never())
    }

    @Test
    fun `Toolbox Explanation should be launch when waitFor complete`() {
        val toolboxConfiguration = mock<ToolboxConfiguration>()

        toolboxDisplayableScenario(toolboxConfiguration)

        verify(toolboxViewModel).show(toolboxConfiguration)
    }

    @Test
    fun `onCreate should submit and wait for ToolboxExplanation when displayable`() {
        toolboxDisplayableScenario(toolboxConfiguration = mock())

        verify(priorityItemUseCase).submitAndWaitFor(ToolboxItem)
    }

    @Test
    fun `onCreate should mark as displayed ToolboxItem when Toolbox is not visible anymore`() {
        val toolboxViewState = PublishProcessor.create<ToolboxViewState>()
        whenever(toolboxViewModel.viewStateFlowable).thenReturn(toolboxViewState)

        toolboxDisplayableScenario(toolboxConfiguration = mock())

        verify(priorityItemUseCase, never()).markAsDisplayed(ToolboxItem)
        toolboxViewState.offer(ToolboxViewState.hidden())
        verify(priorityItemUseCase).markAsDisplayed(ToolboxItem)
    }

    @Test
    fun `onCreate should not submit ToolboxExplanation if it has already been shown`() {
        whenever(pulsingDotUseCase.shouldShowExplanation()).thenReturn(Flowable.just(false))
        whenever(toothbrushConnectionStateViewModel.viewStateFlowable)
            .thenReturn(Flowable.just(noBluetoothState))

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        timeScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        verifyZeroInteractions(toolboxViewModel)
        verify(priorityItemUseCase, never()).submitAndWaitFor(ToolboxItem)
    }

    private fun toolboxDisplayableScenario(toolboxConfiguration: ToolboxConfiguration) {
        val mockFactory = mock<ToolboxConfiguration.Factory>()
        whenever(pulsingDotUseCase.shouldShowExplanation()).thenReturn(Flowable.just(true))
        whenever(toothbrushConnectionStateViewModel.viewStateFlowable)
            .thenReturn(Flowable.just(noBluetoothState))
        whenever(toolboxViewModel.factory()).thenReturn(mockFactory)
        whenever(mockFactory.toolboxExplanation()).thenReturn(toolboxConfiguration)
        whenever(priorityItemUseCase.submitAndWaitFor(ToolboxItem)).thenReturn(Completable.complete())

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        timeScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
    }

    companion object {
        private val noBluetoothState =
            ToothbrushConnectionStateViewState(NoBluetooth(0, "wdc"), 123)
    }
}
