/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.pattern

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.glimmer.tweaker.TweakerSharedViewModel
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurve
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePatternOscillatingMode
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePatternSettings
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import com.kolibree.databinding.livedata.LiveDataTransformations.twoWayMap
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class PatternViewModel(
    initialViewState: PatternViewState,
    sharedViewModel: TweakerSharedViewModel
) : BaseViewModel<PatternViewState, NoActions>(
    initialViewState
), TweakerSharedViewModel by sharedViewModel {

    val settings: LiveData<BrushingModePatternSettings> =
        mapNonNull(viewStateLiveData, initialViewState.settings) { viewState ->
            viewState.settings
        }

    val patternTypes =
        BrushingModePatternOscillatingMode.values().map(BrushingModePatternOscillatingMode::name)
            .toList()

    val patternTypePosition = twoWayMap(viewStateLiveData,
        { viewState -> viewState?.settings?.oscillatingMode?.ordinal },
        { position ->
            position?.let {
                val patternType = BrushingModePatternOscillatingMode.fromBleIndex(position)
                updateViewState { withPatternTypeMode(patternType) }
            }
        })

    val curves = BrushingModeCurve.values().map(BrushingModeCurve::name).toList()

    val curvesPosition = twoWayMap(viewStateLiveData,
        { viewState -> viewState?.settings?.curve?.ordinal },
        { position ->
            position?.let {
                val selectedCurve = BrushingModeCurve.fromBleIndex(position)
                updateViewState { withSelectedCurve(selectedCurve) }
            }
        })

    val showParam1 = mapNonNull(viewStateLiveData, initialViewState.showParam1) { viewState ->
        viewState.showParam1
    }

    val showParam2 = mapNonNull(viewStateLiveData, initialViewState.showParam2) { viewState ->
        viewState.showParam2
    }

    val showParam3 = mapNonNull(viewStateLiveData, initialViewState.showParam3) { viewState ->
        viewState.showParam3
    }

    val param1Title = mapNonNull(viewStateLiveData, initialViewState.param1Title) { viewState ->
        viewState.param1Title
    }

    val param2Title = mapNonNull(viewStateLiveData, initialViewState.param2Title) { viewState ->
        viewState.param2Title
    }

    val param3Title = mapNonNull(viewStateLiveData, initialViewState.param3Title) { viewState ->
        viewState.param3Title
    }

    fun onPatternFrequency(value: Int) =
        updateViewState { withPatternFrequency(value) }

    fun onDutyStrength1Value(value: Int) =
        updateViewState { withStrength1DutyCycleHalfPercent(value) }

    fun onDutyStrength10Value(value: Int) =
        updateViewState { withStrength10DutyCycleHalfPercent(value) }

    fun onMinimalDutyCycleHalfPercentValue(value: Int) =
        updateViewState { withMinimalDutyCycleHalfPercent(value) }

    fun onOscillatingPeriodTenthSecondValue(value: Int) =
        updateViewState { withOscillatingPeriodTenthSecond(value) }

    fun onOscillationParam1Value(value: Int) =
        updateViewState { withOscillationParam1(value) }

    fun onOscillationParam2Value(value: Int) =
        updateViewState { withOscillationParam2(value) }

    fun onOscillationParam3Value(value: Int) =
        updateViewState { withOscillationParam3(value) }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        getViewState()?.settings?.pattern()?.let(::getSettings)
    }

    fun onPatternSelected(position: Int) {
        val selectedPattern = BrushingModePattern.fromBleIndex(position)
        getSettings(selectedPattern)
    }

    fun onApplyButtonClick() {
        getViewState()?.settings?.let { settings ->
            disposeOnCleared {
                modeTweaker.setPatternSettings(settings)
                    .doOnSubscribe { showProgress(true) }
                    .doFinally { showProgress(false) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({}, ::showError)
            }
        }
    }

    private fun getSettings(pattern: BrushingModePattern) {
        disposeOnStop {
            modeTweaker.getPatternSettings(pattern)
                .doOnSubscribe { showProgress(true) }
                .doFinally { showProgress(false) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::onPatternSettings, ::showError)
        }
    }

    private fun onPatternSettings(patternSettings: BrushingModePatternSettings) {
        updateViewState { copy(settings = patternSettings) }
    }

    class Factory @Inject constructor(
        private val sharedViewModel: TweakerSharedViewModel
    ) : BaseViewModel.Factory<PatternViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PatternViewModel(
                viewState ?: PatternViewState.initial(),
                sharedViewModel
            ) as T
    }
}
