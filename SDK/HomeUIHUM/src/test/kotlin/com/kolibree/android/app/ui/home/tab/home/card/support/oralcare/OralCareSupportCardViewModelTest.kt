/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.support.oralcare

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

internal class OralCareSupportCardViewModelTest : BaseUnitTest() {

    private val humHomeNavigator: HumHomeNavigator = mock()

    private lateinit var viewModel: OralCareSupportCardViewModel

    override fun setup() {
        super.setup()

        viewModel =
            OralCareSupportCardViewModel(
                OralCareSupportCardViewState.initial(DynamicCardPosition.ZERO),
                humHomeNavigator
            )
    }

    @Test
    fun `when user clicks on the card it show the oral care support`() {
        viewModel.onOralCareSupportClick()

        verify(humHomeNavigator).showOralCareSupport()
    }

    @Test
    fun `when user clicks on the card the app sends analytics`() {
        viewModel.onOralCareSupportClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Questions_OralCare"))
    }
}
