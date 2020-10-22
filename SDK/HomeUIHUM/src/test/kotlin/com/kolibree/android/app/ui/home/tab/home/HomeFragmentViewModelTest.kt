/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home

import androidx.lifecycle.Lifecycle
import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDot.SMILE
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDotUseCase
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.PlayIncrease
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterStateProvider
import com.kolibree.android.app.ui.home.toolbar.HomeToolbarViewModel
import com.kolibree.android.app.ui.home.toolbox.ToolboxConfiguration
import com.kolibree.android.app.ui.home.toolbox.ToolboxViewModel
import com.kolibree.android.app.ui.host.DynamicCardHostViewModel
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.processors.PublishProcessor
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class HomeFragmentViewModelTest : BaseUnitTest() {

    private val toolbarViewModel: HomeToolbarViewModel = mock()
    private val toolboxViewModel: ToolboxViewModel = mock()
    private val cardHostViewModel: DynamicCardHostViewModel = mock()
    private val pulsingDotUseCase: PulsingDotUseCase = mock()
    private val homeNavigator: HumHomeNavigator = mock()
    private val smilesCounterStateProvider = FakeSmilesCounterStateProvider()

    private lateinit var viewModel: HomeFragmentViewModel

    override fun setup() {
        super.setup()
        viewModel = spy(
            HomeFragmentViewModel(
                initialViewState = HomeViewState.initial(),
                cardHostViewModel = cardHostViewModel,
                toolbarViewModel = toolbarViewModel,
                toolboxViewModel = toolboxViewModel,
                pulsingDotUseCase = pulsingDotUseCase,
                smilesCounterStateProvider = smilesCounterStateProvider,
                homeNavigator = homeNavigator
            )
        )
    }

    @Test
    fun `view model passes toolbar VM to the base class`() {
        val children = viewModel.children
        assertEquals(1, children.size)
        assertNotNull(children.filterIsInstance<HomeToolbarViewModel>().first())
    }

    @Test
    fun `view model does not pass card host VM to the base class`() {
        val children = viewModel.children
        assertEquals(1, children.size)
        assertTrue(children.filterIsInstance<DynamicCardHostViewModel>().isEmpty())
    }

    @Test
    fun `onStart subscribes to needed data sources`() {
        whenever(pulsingDotUseCase.shouldShowPulsingDot(SMILE)).thenReturn(
            Flowable.just(false)
        )

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_START)

        smilesCounterStateProvider.assertHasObservers()
    }

    @Test
    fun `onCreate should have the correct viewState if pulsing dot should be displayed`() {
        whenever(pulsingDotUseCase.shouldShowPulsingDot(SMILE)).thenReturn(
            Flowable.just(true)
        )

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        val isPulsingDotVisible = viewModel.getViewState()!!.pulsingDotVisible

        assertTrue(isPulsingDotVisible)
    }

    @Test
    fun `onCreate should have the correct viewState if pulsing dot should not be displayed`() {
        whenever(pulsingDotUseCase.shouldShowPulsingDot(SMILE)).thenReturn(
            Flowable.just(false)
        )

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        val isPulsingDotVisible = viewModel.getViewState()!!.pulsingDotVisible

        assertFalse(isPulsingDotVisible)
    }

    @Test
    fun `onClickPulsingDot should notify the usecase and update the view state`() {
        val toolboxConfiguration = ToolboxConfiguration("configuration")
        val toolboxFactory = mock<ToolboxConfiguration.Factory>()
        val showStream = PublishProcessor.create<Boolean>()
        whenever(pulsingDotUseCase.shouldShowPulsingDot(SMILE)).thenReturn(showStream)
        whenever(toolboxViewModel.factory()).thenReturn(toolboxFactory)
        whenever(toolboxFactory.smilePoints()).thenReturn(toolboxConfiguration)

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        viewModel.onClickPulsingDot()

        verify(pulsingDotUseCase).onPulsingDotClicked(SMILE)
        verify(toolboxViewModel).show(toolboxConfiguration)

        showStream.offer(false)

        val isPulsingDotVisible = viewModel.getViewState()!!.pulsingDotVisible
        assertFalse(isPulsingDotVisible)
    }

    /*
    Smiles state
     */

    @Test
    fun `new SmilesCounterState updates ViewState`() {
        whenever(pulsingDotUseCase.shouldShowPulsingDot(any())).thenReturn(Flowable.never())

        val viewStateFlowableTester = viewModel.viewStateFlowable.test()

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_START)

        val pendingState = SmilesCounterState.Pending
        val playIncreaseState = PlayIncrease(
            initialPoints = DUMMY_SMILES_1,
            finalPoints = DUMMY_SMILES_2
        )
        smilesCounterStateProvider.emitNewState(pendingState)
        smilesCounterStateProvider.emitNewState(playIncreaseState)

        val initialViewState = HomeViewState.initial()
        val pendingViewState = initialViewState.withSmilesCounterState(pendingState)
        viewStateFlowableTester.assertValues(
            initialViewState,
            pendingViewState,
            pendingViewState.withSmilesCounterState(playIncreaseState)
        )
    }

    @Test
    fun `onResume should send screen name`() {
        whenever(pulsingDotUseCase.shouldShowPulsingDot(any())).thenReturn(Flowable.never())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(eventTracker).sendEvent(AnalyticsEvent("Dashboard-Home"))
    }

    @Test
    fun `When user clicks on smiles counter it opens the smiles history`() {
        doNothing().whenever(homeNavigator).navigatesToSmilesHistory()

        viewModel.onClickSmilesCounter()

        verify(homeNavigator).navigatesToSmilesHistory()
    }

    companion object {
        const val DUMMY_SMILES_1 = 555
        const val DUMMY_SMILES_2 = 666
    }
}

private class FakeSmilesCounterStateProvider : SmilesCounterStateProvider {
    private val smilesCounterStateRelay =
        BehaviorRelay.create<SmilesCounterState>()

    override val smilesStateObservable: Observable<SmilesCounterState> = smilesCounterStateRelay

    fun emitNewState(newState: SmilesCounterState) = smilesCounterStateRelay.accept(newState)

    fun assertHasObservers() = assertTrue(smilesCounterStateRelay.hasObservers())
}
