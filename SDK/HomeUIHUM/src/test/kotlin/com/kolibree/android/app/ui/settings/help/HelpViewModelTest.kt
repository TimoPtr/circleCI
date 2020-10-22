/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.help

import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class HelpViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: HelpViewModel
    private val helpNavigator = mock<HelpNavigator>()

    override fun setup() {
        super.setup()
        viewModel = HelpViewModel(EmptyBaseViewState, helpNavigator)
    }

    @Test
    fun `onClickHelpCenter invokes navigator showHelpCenter`() {
        viewModel.onClickHelpCenter()
        verify(helpNavigator).showHelpCenter()
    }

    @Test
    fun `onClickHelpCenter sends HelpCenter analytics event`() {
        viewModel.onClickHelpCenter()
        verify(eventTracker).sendEvent(AnalyticsEvent("Help_HelpCenter"))
    }

    @Test
    fun `onClickContactUs invokes navigator showContactUs`() {
        viewModel.onClickContactUs()
        verify(helpNavigator).showContactUs()
    }

    @Test
    fun `onClickContactUs sends ContactUs analytics event`() {
        viewModel.onClickContactUs()
        verify(eventTracker).sendEvent(AnalyticsEvent("Help_ContactUs"))
    }

    @Test
    fun `onCloseClick invokes navigator closeScreen`() {
        viewModel.onCloseClick()
        verify(helpNavigator).closeScreen()
    }

    @Test
    fun `onCloseClick sends GoBack analytics event`() {
        viewModel.onCloseClick()
        verify(eventTracker).sendEvent(AnalyticsEvent("Help_GoBack"))
    }
}
