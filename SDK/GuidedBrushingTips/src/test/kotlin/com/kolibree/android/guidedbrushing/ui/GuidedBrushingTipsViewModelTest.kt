/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.ui

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.guidedbrushing.domain.BrushingTipsUseCase
import com.kolibree.android.guidedbrushing.ui.navigator.GuidedBrushingTipsNavigator
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Before
import org.junit.Test

class GuidedBrushingTipsViewModelTest : BaseUnitTest() {

    private val navigator: GuidedBrushingTipsNavigator = mock()

    private val brushingTipsUseCase: BrushingTipsUseCase = mock()

    private lateinit var viewModel: GuidedBrushingTipsViewModel

    @Before
    fun setUp() {
        viewModel = GuidedBrushingTipsViewModel(
            GuidedBrushingTipsViewState(),
            navigator,
            brushingTipsUseCase
        )
    }

    @Test
    fun `close should call the navigator finish method and analytics`() {
        viewModel.close()

        verify(eventTracker).sendEvent(AnalyticsEvent("GuidedBrushing_Tips_Close"))
        verify(navigator).finish()
    }

    @Test
    fun `onClickGotIt should call the navigator finish method and analytics`() {
        viewModel.onClickGotIt()

        verify(eventTracker).sendEvent(AnalyticsEvent("GuidedBrushing_Tips_GotIt"))
        verify(navigator).finish()
    }

    @Test
    fun `onClickNoShowAgain should notify the useCase and call the navigator finish method with analytics`() {
        whenever(brushingTipsUseCase.setHasClickedNoShowAgain())
            .thenReturn(Completable.complete())

        viewModel.onClickNoShowAgain()

        verify(brushingTipsUseCase).setHasClickedNoShowAgain()

        verify(eventTracker).sendEvent(AnalyticsEvent("GuidedBrushing_Tips_NoShowAgain"))
        verify(navigator).finish()
    }
}
