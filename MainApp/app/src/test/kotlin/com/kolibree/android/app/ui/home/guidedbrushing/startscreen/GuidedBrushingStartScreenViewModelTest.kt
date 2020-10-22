/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.guidedbrushing.startscreen

import com.kolibree.android.app.startscreen.ActivityStartPreconditionsViewModel
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.guidedbrushing.domain.BrushingTipsUseCase
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test

class GuidedBrushingStartScreenViewModelTest : BaseUnitTest() {

    private val navigator: GuidedBrushingStartScreenNavigator = mock()
    private val activityStartPreconditionsViewModel: ActivityStartPreconditionsViewModel = mock()
    private val brushingTipsUseCase: BrushingTipsUseCase = mock()

    private lateinit var viewModel: GuidedBrushingStartScreenViewModel

    override fun setup() {
        super.setup()
        viewModel = GuidedBrushingStartScreenViewModel(
            navigator,
            brushingTipsUseCase,
            activityStartPreconditionsViewModel
        )
    }

    @Test
    fun `startClicked invokes navigator and analytics`() {
        whenever(brushingTipsUseCase.isBrushingTipsDisplayable())
            .thenReturn(Single.just(false))

        viewModel.startClicked()

        verify(navigator).startGuidedBrushing()
        verify(eventTracker).sendEvent(GuidedBrushingStartScreenAnalytics.start())
    }

    @Test
    fun `startClicked invokes the guided brushings tips`() {
        whenever(brushingTipsUseCase.isBrushingTipsDisplayable())
            .thenReturn(Single.just(true))

        viewModel.startClicked()

        verify(navigator).startGuidedBrushingTips()
    }

    @Test
    fun `remindMeLaterClicked invokes navigator and analytics`() {
        viewModel.cancelClicked()

        verify(navigator).finish()
        verify(eventTracker).sendEvent(GuidedBrushingStartScreenAnalytics.cancel())
    }
}
