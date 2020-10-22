/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.led.signal

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.glimmer.tweaker.TweakerSharedViewModel
import com.kolibree.android.sdk.connection.toothbrush.led.LedPattern
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import com.kolibree.databinding.livedata.LiveDataTransformations.twoWayMap
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class LedSignalViewModel(
    initialViewState: LedSignalViewState,
    sharedViewModel: TweakerSharedViewModel
) : BaseViewModel<LedSignalViewState, NoActions>(
    initialViewState
), TweakerSharedViewModel by sharedViewModel {

    val patterns = LedPattern.values().map(LedPattern::name).toList()

    val red: LiveData<Int> =
        mapNonNull(viewStateLiveData, initialViewState.red) { state ->
            state.red
        }

    val green: LiveData<Int> =
        mapNonNull(viewStateLiveData, initialViewState.green) { state ->
            state.green
        }

    val blue: LiveData<Int> =
        mapNonNull(viewStateLiveData, initialViewState.blue) { state ->
            state.blue
        }

    val patternPosition = twoWayMap(viewStateLiveData,
        { viewState -> viewState?.pattern?.ordinal },
        { position ->
            position?.let {
                updateViewState { copy(pattern = LedPattern.values()[position]) }
            }
        })

    val isPeriodModifiable: LiveData<Boolean> =
        mapNonNull(viewStateLiveData, initialViewState.isPeriodModifiable) { state ->
            state.isPeriodModifiable
        }

    val period: LiveData<Int> =
        mapNonNull(viewStateLiveData, initialViewState.periodMillis) { state ->
            state.periodMillis
        }

    val durationLiveData: LiveData<Int> =
        mapNonNull(viewStateLiveData, LedSignalViewState.initial().durationMillis) { state ->
            state.durationMillis
        }

    fun onRedValue(value: Int) = updateViewState { copy(red = value) }

    fun onGreenValue(value: Int) = updateViewState { copy(green = value) }

    fun onBlueValue(value: Int) = updateViewState { copy(blue = value) }

    fun onPeriodValue(value: Int) = updateViewState { copy(periodMillis = value) }

    fun onDurationValue(value: Int) = updateViewState { copy(durationMillis = value) }

    fun onPlayButtonClick() =
        getViewState()?.let { state ->
            disposeOnPause {
                connection.toothbrush().playLedSignal(
                    red = state.red.toByte(),
                    green = state.green.toByte(),
                    blue = state.blue.toByte(),
                    pattern = state.pattern,
                    period = state.periodMillis,
                    duration = state.durationMillis
                )
                    .doOnSubscribe { showProgress(true) }
                    .doFinally { showProgress(false) }
                    .subscribeOn(Schedulers.io()).subscribe({}, ::showError)
            }
        }

    class Factory @Inject constructor(
        private val sharedViewModel: TweakerSharedViewModel
    ) : BaseViewModel.Factory<LedSignalViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LedSignalViewModel(
                initialViewState = viewState ?: LedSignalViewState.initial(),
                sharedViewModel = sharedViewModel
            ) as T
    }
}
