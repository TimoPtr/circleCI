/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.model_mismatch

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.PairingNavigator
import com.kolibree.android.app.ui.pairing.finishPairingFlow
import com.kolibree.android.tracker.Analytics
import javax.inject.Inject

/*
Once user is in this screen, Connection has already been associated to the profile
 */
internal class ModelMismatchViewModel(
    pairingFlowSharedFacade: PairingFlowSharedFacade,
    private val navigator: PairingNavigator
) : BaseViewModel<EmptyBaseViewState, BaseAction>(EmptyBaseViewState),
    PairingFlowSharedFacade by pairingFlowSharedFacade {

    fun continueAnywayClick() {
        Analytics.send(ModelMismatchAnalytics.continueAnyway())
        if (isOnboardingFlow()) {
            navigator.navigateFromModelMismatchToSignUp()
        } else {
            finishPairingFlow(navigator)
        }
    }

    fun changeApp() {
        Analytics.send(ModelMismatchAnalytics.changeApp())
        navigator.navigateToSecondAppPlayStore()
    }

    class Factory @Inject constructor(
        private val pairingFlowSharedFacade: PairingFlowSharedFacade,
        private val navigator: PairingNavigator
    ) : BaseViewModel.Factory<EmptyBaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ModelMismatchViewModel(
                pairingFlowSharedFacade = pairingFlowSharedFacade,
                navigator = navigator
            ) as T
    }
}
