/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.mode

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.glimmer.tweaker.TweakerSharedViewModel
import com.kolibree.android.glimmer.tweaker.mode.ModeViewState.Companion.MAX_SEGMENTS
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeLastSegmentStrategy
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeSegment
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeSettings
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeStrengthOption
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence
import com.kolibree.databinding.livedata.LiveDataTransformations
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@Suppress("LargeClass", "TooManyFunctions")
internal class ModeViewModel(
    initialViewState: ModeViewState,
    sharedViewModel: TweakerSharedViewModel
) : BaseViewModel<ModeViewState, NoActions>(
    initialViewState
), TweakerSharedViewModel by sharedViewModel {

    val sequences = BrushingModeSequence.values().map(BrushingModeSequence::getResourceId).toList()

    val strengthSpinnerItems = BrushingModeStrengthOption.values()
        .map(BrushingModeStrengthOption::getResourceId)

    val strengthSpinnerPosition = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.strengthOption?.ordinal },
        { position ->
            position?.let {
                val strengthOption = BrushingModeStrengthOption.values()[position]
                updateViewState { copy(strengthOption = strengthOption) }
            }
        })

    val lastSegmentStrategySpinnerItems = BrushingModeLastSegmentStrategy.values()
        .map(BrushingModeLastSegmentStrategy::getResourceId)

    val lastSegmentStrategySpinnerPosition = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.lastSegmentStrategy?.ordinal },
        { position ->
            position?.let {
                val lastSegmentStrategy = BrushingModeLastSegmentStrategy.values()[position]
                updateViewState { copy(lastSegmentStrategy = lastSegmentStrategy) }
            }
        })

    val addButtonEnabledLiveData: LiveData<Boolean> =
        LiveDataTransformations.mapNonNull(viewStateLiveData, false) {
            it.addButtonEnabled && it.modifiable
        }

    val removeButtonEnabledLiveData: LiveData<Boolean> =
        LiveDataTransformations.mapNonNull(viewStateLiveData, false) {
            it.removeButtonEnabled && it.modifiable
        }

    val enabledSegmentCountLiveData: LiveData<Int> =
        LiveDataTransformations.mapNonNull(viewStateLiveData, 0) {
            it.enabledSegmentCount
        }

    val modifiableLiveData: LiveData<Boolean> =
        LiveDataTransformations.mapNonNull(viewStateLiveData, false) { state ->
            state.modifiable
        }

    val sequenceSegment1LiveData: LiveData<BrushingModeSegment> =
        LiveDataTransformations.mapNonNull(
            viewStateLiveData,
            ModeViewState.initial().sequenceSegment1
        ) { state ->
            state.sequenceSegment1
        }

    val sequenceSegment1Position = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.sequenceSegment1?.sequence()?.bleIndex },
        { position ->
            position?.let {
                updateViewState { copy(sequenceSegment1 = sequenceSegment1
                    .copy(sequenceId = BrushingModeSequence.values()[position].bleIndex)) }
            }
        })

    val sequenceSegment2LiveData: LiveData<BrushingModeSegment> =
        LiveDataTransformations.mapNonNull(
            viewStateLiveData,
            ModeViewState.initial().sequenceSegment2
        ) { state ->
            state.sequenceSegment2
        }

    val sequenceSegment2Position = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.sequenceSegment2?.sequence()?.bleIndex },
        { position ->
            position?.let {
                updateViewState { copy(sequenceSegment2 = sequenceSegment2
                    .copy(sequenceId = BrushingModeSequence.values()[position].bleIndex)) }
            }
        })

    val sequenceSegment3LiveData: LiveData<BrushingModeSegment> =
        LiveDataTransformations.mapNonNull(
            viewStateLiveData,
            ModeViewState.initial().sequenceSegment3
        ) { state ->
            state.sequenceSegment3
        }

    val sequenceSegment3Position = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.sequenceSegment3?.sequence()?.bleIndex },
        { position ->
            position?.let {
                updateViewState { copy(sequenceSegment3 = sequenceSegment3
                    .copy(sequenceId = BrushingModeSequence.values()[position].bleIndex)) }
            }
        })

    val sequenceSegment4LiveData: LiveData<BrushingModeSegment> =
        LiveDataTransformations.mapNonNull(
            viewStateLiveData,
            ModeViewState.initial().sequenceSegment4
        ) { state ->
            state.sequenceSegment4
        }

    val sequenceSegment4Position = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.sequenceSegment4?.sequence()?.bleIndex },
        { position ->
            position?.let {
                updateViewState { copy(sequenceSegment4 = sequenceSegment4
                    .copy(sequenceId = BrushingModeSequence.values()[position].bleIndex)) }
            }
        })

    val sequenceSegment5LiveData: LiveData<BrushingModeSegment> =
        LiveDataTransformations.mapNonNull(
            viewStateLiveData,
            ModeViewState.initial().sequenceSegment5
        ) { state ->
            state.sequenceSegment5
        }

    val sequenceSegment5Position = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.sequenceSegment5?.sequence()?.bleIndex },
        { position ->
            position?.let {
                updateViewState { copy(sequenceSegment5 = sequenceSegment5
                    .copy(sequenceId = BrushingModeSequence.values()[position].bleIndex)) }
            }
        })

    val sequenceSegment6LiveData: LiveData<BrushingModeSegment> =
        LiveDataTransformations.mapNonNull(
            viewStateLiveData,
            ModeViewState.initial().sequenceSegment6
        ) { state ->
            state.sequenceSegment6
        }

    val sequenceSegment6Position = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.sequenceSegment6?.sequence()?.bleIndex },
        { position ->
            position?.let {
                updateViewState { copy(sequenceSegment6 = sequenceSegment6
                    .copy(sequenceId = BrushingModeSequence.values()[position].bleIndex)) }
            }
        })

    val sequenceSegment7LiveData: LiveData<BrushingModeSegment> =
        LiveDataTransformations.mapNonNull(
            viewStateLiveData,
            ModeViewState.initial().sequenceSegment7
        ) { state ->
            state.sequenceSegment7
        }

    val sequenceSegment7Position = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.sequenceSegment7?.sequence()?.bleIndex },
        { position ->
            position?.let {
                updateViewState { copy(sequenceSegment7 = sequenceSegment7
                    .copy(sequenceId = BrushingModeSequence.values()[position].bleIndex)) }
            }
        })

    val sequenceSegment8LiveData: LiveData<BrushingModeSegment> =
        LiveDataTransformations.mapNonNull(
            viewStateLiveData,
            ModeViewState.initial().sequenceSegment8
        ) { state ->
            state.sequenceSegment8
        }

    val sequenceSegment8Position = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.sequenceSegment8?.sequence()?.bleIndex },
        { position ->
            position?.let {
                updateViewState { copy(sequenceSegment8 = sequenceSegment8
                    .copy(sequenceId = BrushingModeSequence.values()[position].bleIndex)) }
            }
        })

    val sequenceSegmentLastLiveData: LiveData<BrushingModeSegment> =
        LiveDataTransformations.mapNonNull(
            viewStateLiveData,
            ModeViewState.initial().lastSegment
        ) { state ->
            state.lastSegment
        }

    val sequenceSegmentLastPosition = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.lastSegment?.sequence()?.bleIndex },
        { position ->
            position?.let {
                updateViewState { copy(lastSegment = lastSegment
                    .copy(sequenceId = BrushingModeSequence.values()[position].bleIndex)) }
            }
        })

    fun onSegment1StrengthValue(value: Int) {
        updateViewState { copy(sequenceSegment1 = sequenceSegment1.copy(strength = value)) }
    }

    fun onSegment2StrengthValue(value: Int) {
        updateViewState { copy(sequenceSegment2 = sequenceSegment2.copy(strength = value)) }
    }

    fun onSegment3StrengthValue(value: Int) {
        updateViewState { copy(sequenceSegment3 = sequenceSegment3.copy(strength = value)) }
    }

    fun onSegment4StrengthValue(value: Int) {
        updateViewState { copy(sequenceSegment4 = sequenceSegment4.copy(strength = value)) }
    }

    fun onSegment5StrengthValue(value: Int) {
        updateViewState { copy(sequenceSegment5 = sequenceSegment5.copy(strength = value)) }
    }

    fun onSegment6StrengthValue(value: Int) {
        updateViewState { copy(sequenceSegment6 = sequenceSegment6.copy(strength = value)) }
    }

    fun onSegment7StrengthValue(value: Int) {
        updateViewState { copy(sequenceSegment7 = sequenceSegment7.copy(strength = value)) }
    }

    fun onSegment8StrengthValue(value: Int) {
        updateViewState { copy(sequenceSegment8 = sequenceSegment8.copy(strength = value)) }
    }

    fun onLastSegmentStrengthValue(value: Int) {
        updateViewState { copy(lastSegment = lastSegment.copy(strength = value)) }
    }

    fun onRemoveButtonClick() =
        updateViewState { copy(enabledSegmentCount = enabledSegmentCount - 1) }

    fun onAddButtonClick() =
        updateViewState { copy(enabledSegmentCount = enabledSegmentCount + 1) }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        getViewState()?.selectedMode?.let(::getSettings)
    }

    fun onModeSelected(position: Int) = getSettings(BrushingMode.values()[position])

    fun onApplyButtonClick() = disposeOnCleared {
        modeTweaker.setCustomBrushingModeSettings(currentSettings())
            .doOnSubscribe { showProgress(true) }
            .doFinally { showProgress(false) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, ::showError)
    }

    private fun getSettings(mode: BrushingMode) {
        disposeOnStop {
            modeTweaker.getBrushingModeSettings(mode)
                .doOnSubscribe { showProgress(true) }
                .doFinally { showProgress(false) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::onModeSettings, ::showError)
        }
    }

    private fun onModeSettings(modeSettings: BrushingModeSettings) {
        updateViewState {
            ModeViewState.withSettings(modeSettings)
        }
    }

    private fun currentSettings() = getViewState()
        ?.let { state ->
            BrushingModeSettings.custom(
                strengthOption = state.strengthOption,
                lastSegmentStrategy = state.lastSegmentStrategy,
                segmentCount = state.enabledSegmentCount,
                segments = state.collectSegments()
            )
        }
        ?: BrushingModeSettings.default()

    class Factory @Inject constructor(
        private val sharedViewModel: TweakerSharedViewModel
    ) : BaseViewModel.Factory<ModeViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ModeViewModel(
                initialViewState = viewState ?: ModeViewState.initial(),
                sharedViewModel = sharedViewModel
            ) as T
    }
}

private fun ModeViewState.collectSegments(): List<BrushingModeSegment> {
    val segments = segmentList().take(enabledSegmentCount)
    return if (segments.size == MAX_SEGMENTS) segments else segments.plus(lastSegment)
}

private fun ModeViewState.segmentList() = listOf(
    sequenceSegment1,
    sequenceSegment2,
    sequenceSegment3,
    sequenceSegment4,
    sequenceSegment5,
    sequenceSegment6,
    sequenceSegment7,
    sequenceSegment8
)
