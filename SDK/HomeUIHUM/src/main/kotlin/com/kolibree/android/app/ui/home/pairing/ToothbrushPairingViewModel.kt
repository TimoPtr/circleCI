/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pairing

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.ui.pairing.PairingFlowHost
import com.kolibree.android.app.ui.pairing.PairingSharedViewModel
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.databinding.livedata.LiveDataTransformations.combineLatest
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.LiveDataTransformations.twoWayMap
import com.kolibree.databinding.livedata.distinctUntilChanged
import javax.inject.Inject

@VisibleForApp
class ToothbrushPairingViewModel(
    initialViewState: ToothbrushPairingViewState?,
    pairingSharedViewModel: PairingSharedViewModel
) : BaseViewModel<ToothbrushPairingViewState, NoActions>(
    initialViewState ?: ToothbrushPairingViewState.initial()
),
    PairingFlowHost,
    PairingSharedViewModel by pairingSharedViewModel {

    private val sharedViewStateLiveData: LiveData<ToothbrushPairingViewState> =
        combineLatest(
            viewStateLiveData,
            pairingViewStateLiveData
        ) { sharedState, pairingState ->
            sharedState?.withPairingViewState(newPairingViewState = pairingState)
        }

    fun getSharedViewState(): ToothbrushPairingViewState? =
        getViewState()?.copy(pairingViewState = getPairingViewState())

    val progressVisible = map(sharedViewStateLiveData) { state ->
        state?.progressVisible() ?: false
    }.distinctUntilChanged()

    val toolbarBackNavigationEnabled = map(sharedViewStateLiveData) { state ->
        state?.let { !it.progressVisible() } ?: false
    }.distinctUntilChanged()

    val snackbarConfiguration = twoWayMap(sharedViewStateLiveData,
        { state -> state?.snackbarConfiguration },
        { configuration -> configuration?.let { updateViewState { copy(snackbarConfiguration = configuration) } } })

    override fun showError(error: Error) = updateViewState {
        copy(snackbarConfiguration = SnackbarConfiguration(isShown = true, error = error))
    }

    override fun hideError() {
        getViewState()?.takeIf { it.snackbarConfiguration.isShown }?.let {
            updateViewState { withSnackbarDismissed() }
        }
    }

    override fun showHostBackNavigation(show: Boolean) {
        // no-op, back is always displayed in pairing flow from home
    }

    override fun isOnboardingFlow(): Boolean = false

    @VisibleForApp
    class Factory @Inject constructor(
        private val pairingSharedViewModel: PairingSharedViewModel
    ) : BaseViewModel.Factory<ToothbrushPairingViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ToothbrushPairingViewModel(
                initialViewState = viewState,
                pairingSharedViewModel = pairingSharedViewModel
            ) as T
    }
}
