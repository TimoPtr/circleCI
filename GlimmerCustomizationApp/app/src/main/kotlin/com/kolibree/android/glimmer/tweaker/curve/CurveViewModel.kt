/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.curve

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.glimmer.tweaker.TweakerSharedViewModel
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurve
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurveSettings
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

@Suppress("TooManyFunctions")
internal class CurveViewModel(
    initialViewState: CurveViewState,
    sharedViewModel: TweakerSharedViewModel
) : BaseViewModel<CurveViewState, NoActions>(
    initialViewState
), TweakerSharedViewModel by sharedViewModel {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        getViewState()?.curveSettings?.curve()?.let(::getSettings)
    }

    val curveSettingsLiveData: LiveData<BrushingModeCurveSettings> =
        mapNonNull(viewStateLiveData, BrushingModeCurveSettings.default()) {
            it.curveSettings
        }

    fun onCurveSelected(position: Int) {
        val selectedCurve = BrushingModeCurve.fromBleIndex(position)
        getSettings(selectedCurve)
    }

    fun onApplyButtonClick() {
        getViewState()?.curveSettings?.let { settings ->
            disposeOnCleared {
                modeTweaker.setCurveSettings(settings)
                    .doOnSubscribe { showProgress(true) }
                    .doFinally { showProgress(false) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({}, ::showError)
            }
        } ?: Timber.w("curve settings are null")
    }

    fun onReferenceVoltageValue(value: Int) =
        updateViewState { copy(curveSettings = curveSettings.copy(referenceVoltageMv = value)) }

    fun onDividerValue(value: Int) =
        updateViewState { copy(curveSettings = curveSettings.copy(divider = value)) }

    fun onSlope10Value(value: Int) =
        updateViewState { copy(curveSettings = curveSettings.copy(slope10PercentsDutyCycle = value)) }

    fun onSlope20Value(value: Int) =
        updateViewState { copy(curveSettings = curveSettings.copy(slope20PercentsDutyCycle = value)) }

    fun onSlope30Value(value: Int) =
        updateViewState { copy(curveSettings = curveSettings.copy(slope30PercentsDutyCycle = value)) }

    fun onSlope40Value(value: Int) =
        updateViewState { copy(curveSettings = curveSettings.copy(slope40PercentsDutyCycle = value)) }

    fun onSlope50Value(value: Int) =
        updateViewState { copy(curveSettings = curveSettings.copy(slope50PercentsDutyCycle = value)) }

    fun onSlope60Value(value: Int) =
        updateViewState { copy(curveSettings = curveSettings.copy(slope60PercentsDutyCycle = value)) }

    fun onSlope70Value(value: Int) =
        updateViewState { copy(curveSettings = curveSettings.copy(slope70PercentsDutyCycle = value)) }

    fun onSlope80Value(value: Int) =
        updateViewState { copy(curveSettings = curveSettings.copy(slope80PercentsDutyCycle = value)) }

    fun onSlope90Value(value: Int) =
        updateViewState { copy(curveSettings = curveSettings.copy(slope90PercentsDutyCycle = value)) }

    private fun getSettings(curve: BrushingModeCurve) {
        disposeOnStop {
            modeTweaker.getCurveSettings(curve)
                .doOnSubscribe { showProgress(true) }
                .doFinally { showProgress(false) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::onCurveSettings, ::showError)
        }
    }

    private fun onCurveSettings(curveSettings: BrushingModeCurveSettings) {
        updateViewState { copy(curveSettings = curveSettings) }
    }

    class Factory @Inject constructor(
        private val sharedViewModel: TweakerSharedViewModel
    ) : BaseViewModel.Factory<CurveViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CurveViewModel(viewState ?: CurveViewState.initial(), sharedViewModel) as T
    }
}
