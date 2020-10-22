/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.is_brush_ready

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.PairingNavigator
import com.kolibree.android.tracker.Analytics
import javax.inject.Inject

internal class IsBrushReadyViewModel(
    pairingFlowSharedFacade: PairingFlowSharedFacade,
    private val navigator: PairingNavigator
) : BaseViewModel<EmptyBaseViewState, BaseAction>(EmptyBaseViewState),
    PairingFlowSharedFacade by pairingFlowSharedFacade {

    fun needHelpClick() {
        Analytics.send(IsBrushReadyAnalytics.moreHelp())
        navigator.navigateToNeedMoreHelp()
    }

    fun connectMyBrushClick() {
        Analytics.send(IsBrushReadyAnalytics.connect())
        navigator.navigateFromIsBrushReadyToWakeYourBrush()
    }

    class Factory @Inject constructor(
        private val pairingFlowSharedFacade: PairingFlowSharedFacade,
        private val navigator: PairingNavigator
    ) : BaseViewModel.Factory<EmptyBaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            IsBrushReadyViewModel(
                pairingFlowSharedFacade = pairingFlowSharedFacade,
                navigator = navigator
            ) as T
    }
}
