/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.support.product

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

internal class ProductSupportCardViewModelTest : BaseUnitTest() {

    private val humHomeNavigator: HumHomeNavigator = mock()

    private lateinit var viewModel: ProductSupportCardViewModel

    override fun setup() {
        super.setup()

        viewModel = ProductSupportCardViewModel(
            ProductSupportCardViewState.initial(DynamicCardPosition.ZERO),
            humHomeNavigator
        )
    }

    @Test
    fun `when user clicks on the card it show the product support`() {
        viewModel.onProductSupportClick()

        verify(humHomeNavigator).showProductSupport()
    }

    @Test
    fun `when user clicks on the card the app sends analytics`() {
        viewModel.onProductSupportClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("Questions_product"))
    }
}
