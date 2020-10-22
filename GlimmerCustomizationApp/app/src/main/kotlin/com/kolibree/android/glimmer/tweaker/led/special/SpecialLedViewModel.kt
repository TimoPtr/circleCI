/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.led.special

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.glimmer.tweaker.TweakerSharedViewModel
import com.kolibree.android.sdk.connection.toothbrush.led.SpecialLed
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class SpecialLedViewModel(
    initialViewState: SpecialLedViewState,
    sharedViewModel: TweakerSharedViewModel
) : BaseViewModel<SpecialLedViewState, NoActions>(
    initialViewState
), TweakerSharedViewModel by sharedViewModel {

    val pwm: LiveData<Int> =
        mapNonNull(viewStateLiveData, initialViewState.pwm) { state ->
            state.pwm
        }

    fun onLedSelected(position: Int) {
        val selectedLed = SpecialLed.values()[position]
        updateViewState { copy(led = selectedLed) }
        getLedPwm(selectedLed)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        getViewState()?.led?.let(::getLedPwm)
    }

    private fun getLedPwm(selectedLed: SpecialLed) {
        disposeOnCleared {
            connection.toothbrush().getSpecialLedPwm(selectedLed)
                .doOnSubscribe { showProgress(true) }
                .doFinally { showProgress(false) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::onPwm, ::showError)
        }
    }

    private fun onPwm(pwm: Int) {
        updateViewState { copy(pwm = pwm) }
    }

    fun onPwmValue(value: Int) = updateViewState { copy(pwm = value) }

    fun onApplyButtonClick() = getViewState()?.let { state ->
        disposeOnCleared {
            connection.toothbrush().setSpecialLedPwm(
                led = state.led,
                pwm = state.pwm
            )
                .doOnSubscribe { showProgress(true) }
                .doFinally { showProgress(false) }
                .subscribeOn(Schedulers.io()).subscribe({}, ::showError)
        }
    }

    class Factory @Inject constructor(
        private val sharedViewModel: TweakerSharedViewModel
    ) : BaseViewModel.Factory<SpecialLedViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SpecialLedViewModel(
                initialViewState = viewState ?: SpecialLedViewState.initial(),
                sharedViewModel = sharedViewModel
            ) as T
    }
}
