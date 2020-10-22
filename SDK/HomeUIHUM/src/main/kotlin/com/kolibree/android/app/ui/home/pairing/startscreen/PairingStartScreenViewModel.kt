/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pairing.startscreen

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.base.NoActions
import javax.inject.Inject

internal class PairingStartScreenViewModel(
    private val navigator: PairingStartScreenNavigator
) : BaseViewModel<EmptyBaseViewState, NoActions>(EmptyBaseViewState) {

    fun connectMyBrushClicked() {
        navigator.navigateToPairingFlowAndFinish()

        PairingStartScreenAnalytics.onConnectBrushClicked()
    }

    fun shopClicked() {
        navigator.navigateToShopAndFinish()

        PairingStartScreenAnalytics.onShowShopClicked()
    }

    class Factory @Inject constructor(
        private val navigator: PairingStartScreenNavigator
    ) : BaseViewModel.Factory<EmptyBaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PairingStartScreenViewModel(navigator) as T
    }
}
