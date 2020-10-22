/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.mindyourspeed.startscreen

import com.kolibree.android.app.startscreen.ActivityStartPreconditionsViewModel
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

internal class MindYourSpeedStartScreenViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: MindYourSpeedStartScreenViewModel

    private val activityStartPreconditionsViewModel: ActivityStartPreconditionsViewModel = mock()
    private val navigator: MindYourSpeedStartScreenNavigator = mock()

    override fun setup() {
        super.setup()
        viewModel = MindYourSpeedStartScreenViewModel(navigator, activityStartPreconditionsViewModel)
    }

    @Test
    fun `when Cancel is clicked then close screen`() {
        viewModel.cancelClicked()

        verify(navigator).closeScreen()
    }

    @Test
    fun `when Cancel is clicked then send Cancel event`() {
        viewModel.cancelClicked()

        verify(eventTracker).sendEvent(AnalyticsEvent("MindYourSpeed_IntroScreen_Cancel"))
    }

    @Test
    fun `when Start is clicked then show MindYourSpeed screen`() {
        viewModel.startClicked()

        verify(navigator).startMindYourSpeedScreen()
    }

    @Test
    fun `when Start is clicked then send Start event`() {
        viewModel.startClicked()

        verify(eventTracker).sendEvent(AnalyticsEvent("MindYourSpeed_IntroScreen_Start"))
    }
}
