/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.distinctUntilChanged
import javax.inject.Inject

internal class PairingViewModel(
    initialViewState: PairingViewState?,
    blinkingConnectionHolder: BlinkingConnectionHolder
) : BaseViewModel<PairingViewState, PairingActions>(
    initialViewState ?: PairingViewState.initial()
),
    PairingSharedViewModel,
    BlinkingConnectionHolder by blinkingConnectionHolder {

    val progressVisible = map(viewStateLiveData) { state ->
        state?.progressVisible ?: false
    }.distinctUntilChanged()

    override val pairingViewStateLiveData: LiveData<PairingViewState> = viewStateLiveData

    override fun getPairingViewState(): PairingViewState? = getViewState()

    override fun showProgress(show: Boolean) {
        updateViewState { copy(progressVisible = show) }
    }

    override fun resetState() = updateViewState { PairingViewState.initial() }

    override fun onPairingFlowSuccess() {
        /*
        Otherwise we will unpair in onCleared
         */
        blinkingConnection = null
    }

    override fun onCleared() {
        try {
            /*
            We need this to block or it'll be disposed in super.onCleared
             */
            unpairBlinkingConnectionCompletable().blockingAwait()
        } finally {
            super.onCleared()
        }
    }

    class Factory @Inject constructor(
        private val blinkingConnectionHolder: BlinkingConnectionHolder
    ) : BaseViewModel.Factory<PairingViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PairingViewModel(viewState, blinkingConnectionHolder) as T
    }
}
