/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.shop

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.toolbar.HomeToolbarViewModel
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

internal class ShopViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: ShopViewModel

    private val toolbarViewModel: HomeToolbarViewModel = mock()

    override fun setup() {
        super.setup()

        viewModel = ShopViewModel(
            initialViewState = EmptyBaseViewState,
            toolbarViewModel = toolbarViewModel
        )
    }

    @Test
    fun `onResume should send screen name`() {
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(eventTracker).sendEvent(AnalyticsEvent("Shop-Home"))
    }
}
