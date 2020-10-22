/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.support.product

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.tab.home.card.support.SupportCardAnalytics
import javax.inject.Inject

internal class ProductSupportCardViewModel(
    initialViewState: ProductSupportCardViewState,
    private val humHomeNavigator: HumHomeNavigator
) : DynamicCardViewModel<
    ProductSupportCardViewState,
    ProductSupportCardInteraction,
    ProductSupportCardBindingModel>(initialViewState), ProductSupportCardInteraction {

    override val interaction: ProductSupportCardInteraction = this

    override fun onProductSupportClick() {
        SupportCardAnalytics.productSupport()
        humHomeNavigator.showProductSupport()
    }

    override fun interactsWith(bindingModel: DynamicCardBindingModel): Boolean =
        bindingModel is ProductSupportCardBindingModel

    class Factory @Inject constructor(
        private val humHomeNavigator: HumHomeNavigator,
        private val dynamicCardListConfiguration: DynamicCardListConfiguration
    ) : BaseViewModel.Factory<ProductSupportCardViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProductSupportCardViewModel(
                viewState ?: ProductSupportCardViewState.initial(
                    dynamicCardListConfiguration.getInitialCardPosition(modelClass)
                ),
                humHomeNavigator
            ) as T
    }
}
