/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.glimmer.tweaker.TweakerActions.ShowError
import com.kolibree.android.glimmer.utils.brushingModeTweaker
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeTweaker
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import com.kolibree.databinding.livedata.distinctUntilChanged
import com.kolibree.pairing.assistant.PairingAssistant
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class TweakerViewModel(
    initialViewState: TweakerViewState,
    private val navigator: TweakerNavigator,
    override val connection: KLTBConnection,
    private val pairingAssistant: PairingAssistant
) : BaseViewModel<TweakerViewState, TweakerActions>(
    initialViewState
), TweakerSharedViewModel {

    val progressVisible = mapNonNull(viewStateLiveData, initialViewState.progressVisible) { state ->
        state.progressVisible
    }.distinctUntilChanged()

    override val modeTweaker: BrushingModeTweaker = connection.brushingModeTweaker()

    override val sharedViewStateLiveData: LiveData<TweakerViewState> = viewStateLiveData

    override fun getSharedViewState(): TweakerViewState? = getViewState()

    override fun showProgress(show: Boolean) = updateViewState { copy(progressVisible = show) }

    override fun showError(error: Throwable) {
        pushAction(ShowError(error))
    }

    override fun resetState() = updateViewState { TweakerViewState.initial() }

    fun onDisconnectButtonClick() {
        disposeOnCleared {
            pairingAssistant.unpair(connection.toothbrush().mac)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { navigator.navigateToPairingActivity() },
                    Timber::e
                )
        }
    }

    class Factory @Inject constructor(
        private val connection: KLTBConnection,
        private val navigator: TweakerNavigator,
        private val pairingAssistant: PairingAssistant
    ) : BaseViewModel.Factory<TweakerViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TweakerViewModel(
                initialViewState = viewState ?: TweakerViewState.initial(),
                navigator = navigator,
                connection = connection,
                pairingAssistant = pairingAssistant
            ) as T
    }
}
