/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.support.oralcare

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.tab.home.card.support.SupportCardAnalytics
import javax.inject.Inject

internal class OralCareSupportCardViewModel(
    initialViewState: OralCareSupportCardViewState,
    private val humHomeNavigator: HumHomeNavigator
) : DynamicCardViewModel<
    OralCareSupportCardViewState,
    OralCareSupportCardInteraction,
    OralCareSupportCardBindingModel>(initialViewState), OralCareSupportCardInteraction {

    override val interaction: OralCareSupportCardInteraction = this

    override fun onOralCareSupportClick() {
        SupportCardAnalytics.oralCareSupport()
        humHomeNavigator.showOralCareSupport()
    }

    override fun interactsWith(bindingModel: DynamicCardBindingModel): Boolean =
        bindingModel is OralCareSupportCardBindingModel

    class Factory @Inject constructor(
        private val humHomeNavigator: HumHomeNavigator,
        private val dynamicCardListConfiguration: DynamicCardListConfiguration
    ) : BaseViewModel.Factory<OralCareSupportCardViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OralCareSupportCardViewModel(
                viewState ?: OralCareSupportCardViewState.initial(
                    dynamicCardListConfiguration.getInitialCardPosition(modelClass)
                ),
                humHomeNavigator
            ) as T
    }
}
