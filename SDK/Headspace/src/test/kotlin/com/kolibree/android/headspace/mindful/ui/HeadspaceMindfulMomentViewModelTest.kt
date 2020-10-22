/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.ui

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.headspace.mindful.ui.shared.SAMPLE_MINDFUL_MOMENT
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertNotNull
import org.junit.Test

internal class HeadspaceMindfulMomentViewModelTest : BaseUnitTest() {
    private val navigator: HeadspaceMindfulMomentActivityNavigator = mock()

    private lateinit var viewModel: HeadspaceMindfulMomentViewModel

    override fun setup() {
        super.setup()

        viewModel = HeadspaceMindfulMomentViewModel(
            initialViewState = HeadspaceMindfulMomentViewState(SAMPLE_MINDFUL_MOMENT),
            navigator = navigator
        )
    }

    @Test
    fun `mindfulMoment is available right after viewModel creation`() {
        assertNotNull(viewModel.getViewState()?.mindfulMoment)
    }

    @Test
    fun `onCollectSmilesClick invokes navigator finishWithSuccess and posts analytics event`() {
        viewModel.onCollectSmilesClick()

        verify(navigator).finishWithSuccess(any())
        verify(eventTracker).sendEvent(AnalyticsEvent("HeadSpace_MM_CollectPoints"))
    }

    @Test
    fun `onShareClick posts OpenHeadspaceWebsite and posts analytics event`() {
        val actions = viewModel.actionsObservable.test()

        viewModel.onShareClick()

        actions
            .assertValueCount(1)
            .assertValue { it is HeadspaceMindfulMomentActions.OpenHeadspaceWebsite }

        verify(eventTracker).sendEvent(AnalyticsEvent("HeadSpace_MM_VisitHeadSpace"))
    }
}
