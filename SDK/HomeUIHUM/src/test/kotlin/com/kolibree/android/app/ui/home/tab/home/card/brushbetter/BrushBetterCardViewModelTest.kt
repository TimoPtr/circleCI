/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushbetter

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDot
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDotUseCase
import com.kolibree.android.app.ui.home.toolbox.ToolboxConfiguration
import com.kolibree.android.app.ui.home.toolbox.ToolboxViewModel
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.CompletableSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class BrushBetterCardViewModelTest : BaseUnitTest() {

    private val brushBetterResourceProvider = mock<BrushBetterResourceProvider>()
    private val brushBetterUseCase = mock<BrushBetterUseCase>()
    private val pulsingDotUseCase = mock<PulsingDotUseCase>()
    private val toolboxViewModel = mock<ToolboxViewModel>()

    private lateinit var viewModel: BrushBetterCardViewModel

    override fun setup() {
        super.setup()
        viewModel = BrushBetterCardViewModel(
            initialViewState = BrushBetterCardViewState.initial(DynamicCardPosition.ZERO),
            brushBetterResourceProvider = brushBetterResourceProvider,
            brushBetterUseCase = brushBetterUseCase,
            pulsingDotUseCase = pulsingDotUseCase,
            toolboxViewModel = toolboxViewModel
        )

        whenever(brushBetterUseCase.getItems()).thenReturn(Observable.empty())
        whenever(pulsingDotUseCase.shouldShowPulsingDot(PulsingDot.BRUSH_BETTER))
            .thenReturn(Flowable.just(false))
    }

    @Test
    fun `transfers the click to the use case`() {
        val subject = CompletableSubject.create()
        val mockItem = mock<BrushBetterItem>()
        whenever(brushBetterUseCase.onItemClick(mockItem)).thenReturn(subject)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        viewModel.onItemClick(mockItem)
        assertTrue(subject.hasObservers())
    }

    @Test
    fun `displays items when available`() {
        val mockItems = (0..10).map { mock<BrushBetterItem>() }.toList()
        whenever(brushBetterUseCase.getItems()).thenReturn(Observable.just(mockItems))
        whenever(brushBetterResourceProvider.createItemBinding(anyOrNull())).thenReturn(mock())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        val viewState = viewModel.getViewState()!!
        assertEquals(mockItems.size, viewState.items.size)
    }

    @Test
    fun `update view state after pulsing dot state changed`() {
        val shouldShowStream = PublishProcessor.create<Boolean>()
        whenever(pulsingDotUseCase.shouldShowPulsingDot(PulsingDot.BRUSH_BETTER))
            .thenReturn(shouldShowStream)
        whenever(toolboxViewModel.factory()).thenReturn(mock())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        shouldShowStream.offer(true)
        assertTrue(viewModel.getViewState()!!.pulsingDotVisible)

        shouldShowStream.offer(false)
        assertFalse(viewModel.getViewState()!!.pulsingDotVisible)
    }

    @Test
    fun `show proper toolbox and call use case after click on pulsing dot`() {
        val mockConfiguration: ToolboxConfiguration = mock()
        val mockFactory: ToolboxConfiguration.Factory = mock()

        whenever(pulsingDotUseCase.shouldShowPulsingDot(PulsingDot.BRUSH_BETTER))
            .thenReturn(Flowable.just(true))
        whenever(toolboxViewModel.factory()).thenReturn(mockFactory)
        whenever(mockFactory.brushingActivities()).thenReturn(mockConfiguration)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        viewModel.onPulsingDotClick()

        verify(pulsingDotUseCase).onPulsingDotClicked(eq(PulsingDot.BRUSH_BETTER))
        verify(toolboxViewModel).show(eq(mockConfiguration))
    }
}
