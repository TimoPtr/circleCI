/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.testbrushing.startscreen

import com.jraska.livedata.test
import com.kolibree.android.app.startscreen.ActivityStartPreconditionsViewModel
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.HomeSessionFlag.SHOW_REMIND_ME_LATER_ON_TEST_BRUSHING_START_SCREEN
import com.kolibree.android.app.ui.HomeSessionFlag.SUPPRESS_TEST_BRUSHING_REMINDER
import com.kolibree.android.persistence.SessionFlags
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class TestBrushingStartScreenViewModelTest : BaseUnitTest() {

    private val navigator: TestBrushingStartScreenNavigator = mock()
    private val activityStartPreconditionsViewModel: ActivityStartPreconditionsViewModel = mock()

    private val sessionFlags: SessionFlags = mock()

    @Test
    fun `setupViewState sets remindMeLater to true, if session flag was set to true`() {
        val viewModel = createViewModel(showRemindMeLater = true)

        assertTrue(viewModel.getViewState()!!.showRemindMeLaterButton)
        viewModel.showRemindMeLater.test().assertValue(true)
    }

    @Test
    fun `setupViewState sets remindMeLater to true, if session flag was set to false`() {
        val viewModel = createViewModel(showRemindMeLater = false)

        assertFalse(viewModel.getViewState()!!.showRemindMeLaterButton)
        viewModel.showRemindMeLater.test().assertValue(false)
    }

    @Test
    fun `setupViewState sets enables close button if notification is not suppressed and remind me later is visible`() {
        val viewModel = createViewModel(showRemindMeLater = true, reminderSuppressed = false)

        assertTrue(viewModel.getViewState()!!.showCloseButtonInToolbar)
        viewModel.showCloseToolbarButton.test().assertValue(true)
    }

    @Test
    fun `setupViewState sets showCloseToolbarButton to false if notification is suppressed`() {
        val viewModel = createViewModel(showRemindMeLater = true, reminderSuppressed = true)

        assertFalse(viewModel.getViewState()!!.showCloseButtonInToolbar)
        viewModel.showCloseToolbarButton.test().assertValue(false)
    }

    @Test
    fun `setupViewState sets showCloseToolbarButton to false if remind me later is not shown`() {
        val viewModel = createViewModel(showRemindMeLater = false, reminderSuppressed = false)

        assertFalse(viewModel.getViewState()!!.showCloseButtonInToolbar)
        viewModel.showCloseToolbarButton.test().assertValue(false)
    }

    @Test
    fun `setupViewState sets remind me later flag to false after processing`() {
        val viewModel = createViewModel(showRemindMeLater = true)

        verify(sessionFlags).setSessionFlag(SHOW_REMIND_ME_LATER_ON_TEST_BRUSHING_START_SCREEN, false)
    }

    @Test
    fun `startClicked invokes navigator and analytics`() {
        val viewModel = createViewModel()

        viewModel.startClicked()

        verify(navigator).startTestBrushing()
        verify(eventTracker).sendEvent(TestBrushingStartScreenAnalytics.start())
    }

    @Test
    fun `remindMeLaterClicked invokes navigator and analytics`() {
        val viewModel = createViewModel(showRemindMeLater = true)

        viewModel.remindMeLaterClicked()

        verify(navigator).finish()
        verify(eventTracker).sendEvent(TestBrushingStartScreenAnalytics.later())
    }

    @Test
    fun `closeClicked invokes navigator, sessionFlags and analytics`() {
        val viewModel = createViewModel(showRemindMeLater = true)

        viewModel.closeClicked()

        verify(navigator).finish()
        verify(eventTracker).sendEvent(TestBrushingStartScreenAnalytics.close())
        verify(sessionFlags).setSessionFlag(SUPPRESS_TEST_BRUSHING_REMINDER, true)
    }

    private fun createViewModel(
        showRemindMeLater: Boolean = false,
        reminderSuppressed: Boolean = false
    ): TestBrushingStartScreenViewModel {
        whenever(sessionFlags.readSessionFlag(SHOW_REMIND_ME_LATER_ON_TEST_BRUSHING_START_SCREEN))
            .thenReturn(showRemindMeLater)
        whenever(sessionFlags.readSessionFlag(SUPPRESS_TEST_BRUSHING_REMINDER))
            .thenReturn(reminderSuppressed)

        doNothing().whenever(sessionFlags).setSessionFlag(any(), any())

        return TestBrushingStartScreenViewModel(
            TestBrushingStartScreenViewState.initial(),
            navigator,
            sessionFlags,
            activityStartPreconditionsViewModel
        )
    }
}
