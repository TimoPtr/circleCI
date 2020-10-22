/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.led.mode

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.glimmer.tweaker.TweakerSharedViewModel
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import org.threeten.bp.Duration

internal class ModeLedViewModel(
    initialViewState: ModeLedViewState,
    sharedViewModel: TweakerSharedViewModel
) : BaseViewModel<ModeLedViewState, NoActions>(
    initialViewState
), TweakerSharedViewModel by sharedViewModel {

    val pwmLed1LiveData: LiveData<Int> =
        mapNonNull(viewStateLiveData, ModeLedViewState.initial().led1pwm) { state ->
            state.led1pwm
        }

    val pwmLed2LiveData: LiveData<Int> =
        mapNonNull(viewStateLiveData, ModeLedViewState.initial().led2pwm) { state ->
            state.led2pwm
        }

    val pwmLed3LiveData: LiveData<Int> =
        mapNonNull(viewStateLiveData, ModeLedViewState.initial().led3pwm) { state ->
            state.led3pwm
        }

    val pwmLed4LiveData: LiveData<Int> =
        mapNonNull(viewStateLiveData, ModeLedViewState.initial().led4pwm) { state ->
            state.led4pwm
        }

    val pwmLed5LiveData: LiveData<Int> =
        mapNonNull(viewStateLiveData, ModeLedViewState.initial().led5pwm) { state ->
            state.led5pwm
        }

    val durationLiveData: LiveData<Int> =
        mapNonNull(viewStateLiveData, ModeLedViewState.initial().durationMillis) { state ->
            state.durationMillis
        }

    fun onPwmLed1Value(value: Int) = updateViewState { copy(led1pwm = value) }

    fun onPwmLed2Value(value: Int) = updateViewState { copy(led2pwm = value) }

    fun onPwmLed3Value(value: Int) = updateViewState { copy(led3pwm = value) }

    fun onPwmLed4Value(value: Int) = updateViewState { copy(led4pwm = value) }

    fun onPwmLed5Value(value: Int) = updateViewState { copy(led5pwm = value) }

    fun onDurationValue(value: Int) = updateViewState { copy(durationMillis = value) }

    fun onPlayButtonClick() =
        if (modeLedPwmSum() > MAX_PWM_SUM) {
            showError(IllegalArgumentException("Sum of all PWM must be <= $MAX_PWM_SUM"))
        } else {
            playPattern()
        }

    private fun playPattern() = getViewState()?.let { state ->
        disposeOnPause {
            connection.toothbrush().playModeLedPattern(
                pwmLed0 = state.led1pwm,
                pwmLed1 = state.led2pwm,
                pwmLed2 = state.led3pwm,
                pwmLed3 = state.led4pwm,
                pwmLed4 = state.led5pwm,
                patternDuration = Duration.ofMillis(state.durationMillis.toLong())
            )
                .doOnSubscribe { showProgress(true) }
                .doFinally { showProgress(false) }
                .subscribeOn(Schedulers.io()).subscribe({}, ::showError)
        }
    }

    private fun modeLedPwmSum() = getViewState()?.let { state ->
        state.led1pwm + state.led2pwm + state.led3pwm + state.led4pwm + state.led5pwm
    } ?: 0

    class Factory @Inject constructor(
        private val sharedViewModel: TweakerSharedViewModel
    ) : BaseViewModel.Factory<ModeLedViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ModeLedViewModel(
                initialViewState = viewState ?: ModeLedViewState.initial(),
                sharedViewModel = sharedViewModel
            ) as T
    }

    companion object {

        private const val MAX_PWM_SUM = 100
    }
}
