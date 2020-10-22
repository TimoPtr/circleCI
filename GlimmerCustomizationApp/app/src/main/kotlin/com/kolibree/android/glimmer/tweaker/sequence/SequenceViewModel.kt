/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.sequence

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.glimmer.tweaker.TweakerSharedViewModel
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern.values
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequencePattern
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequenceSettings
import com.kolibree.databinding.livedata.LiveDataTransformations
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@Suppress("TooManyFunctions")
internal class SequenceViewModel(
    initialViewState: SequenceViewState,
    sharedViewModel: TweakerSharedViewModel
) : BaseViewModel<SequenceViewState, NoActions>(
    initialViewState
), TweakerSharedViewModel by sharedViewModel {

    val patterns = values().map(BrushingModePattern::name).toList()

    val addButtonEnabledLiveData: LiveData<Boolean> = mapNonNull(viewStateLiveData, false) {
        it.addButtonEnabled && it.modifiable
    }

    val removeButtonEnabledLiveData: LiveData<Boolean> = mapNonNull(viewStateLiveData, false) {
        it.removeButtonEnabled && it.modifiable
    }

    val enabledPatternCountLiveData: LiveData<Int> = mapNonNull(viewStateLiveData, 0) {
        it.enabledPatternCount
    }

    val modifiableLiveData: LiveData<Boolean> =
        mapNonNull(viewStateLiveData, false) { state ->
            state.modifiable
        }

    val sequencePattern1LiveData: LiveData<SequencePatternItemData> =
        mapNonNull(viewStateLiveData, SequenceViewState.initial().sequencePattern1) { state ->
            state.sequencePattern1
        }

    val sequencePattern1Position = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.sequencePattern1?.pattern?.ordinal },
        { position ->
            position?.let {
                updateViewState { copy(sequencePattern1 = sequencePattern1.copy(pattern = values()[position])) }
            }
        })

    val sequencePattern2LiveData: LiveData<SequencePatternItemData> =
        mapNonNull(viewStateLiveData, SequenceViewState.initial().sequencePattern2) { state ->
            state.sequencePattern2
        }

    val sequencePattern2Position = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.sequencePattern2?.pattern?.ordinal },
        { position ->
            position?.let {
                updateViewState { copy(sequencePattern2 = sequencePattern2.copy(pattern = values()[position])) }
            }
        })

    val sequencePattern3LiveData: LiveData<SequencePatternItemData> =
        mapNonNull(viewStateLiveData, SequenceViewState.initial().sequencePattern3) { state ->
            state.sequencePattern3
        }

    val sequencePattern3Position = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.sequencePattern3?.pattern?.ordinal },
        { position ->
            position?.let {
                updateViewState { copy(sequencePattern3 = sequencePattern3.copy(pattern = values()[position])) }
            }
        })

    val sequencePattern4LiveData: LiveData<SequencePatternItemData> =
        mapNonNull(viewStateLiveData, SequenceViewState.initial().sequencePattern4) { state ->
            state.sequencePattern4
        }

    val sequencePattern4Position = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.sequencePattern4?.pattern?.ordinal },
        { position ->
            position?.let {
                updateViewState { copy(sequencePattern4 = sequencePattern4.copy(pattern = values()[position])) }
            }
        })

    val sequencePattern5LiveData: LiveData<SequencePatternItemData> =
        mapNonNull(viewStateLiveData, SequenceViewState.initial().sequencePattern5) { state ->
            state.sequencePattern5
        }

    val sequencePattern5Position = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.sequencePattern5?.pattern?.ordinal },
        { position ->
            position?.let {
                updateViewState { copy(sequencePattern5 = sequencePattern5.copy(pattern = values()[position])) }
            }
        })

    val sequencePattern6LiveData: LiveData<SequencePatternItemData> =
        mapNonNull(viewStateLiveData, SequenceViewState.initial().sequencePattern6) { state ->
            state.sequencePattern6
        }

    val sequencePattern6Position = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.sequencePattern6?.pattern?.ordinal },
        { position ->
            position?.let {
                updateViewState { copy(sequencePattern6 = sequencePattern6.copy(pattern = values()[position])) }
            }
        })

    val sequencePattern7LiveData: LiveData<SequencePatternItemData> =
        mapNonNull(viewStateLiveData, SequenceViewState.initial().sequencePattern7) { state ->
            state.sequencePattern7
        }

    val sequencePattern7Position = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.sequencePattern7?.pattern?.ordinal },
        { position ->
            position?.let {
                updateViewState { copy(sequencePattern7 = sequencePattern7.copy(pattern = values()[position])) }
            }
        })

    val sequencePattern8LiveData: LiveData<SequencePatternItemData> =
        mapNonNull(viewStateLiveData, SequenceViewState.initial().sequencePattern8) { state ->
            state.sequencePattern8
        }

    val sequencePattern8Position = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.sequencePattern8?.pattern?.ordinal },
        { position ->
            position?.let {
                updateViewState { copy(sequencePattern8 = sequencePattern8.copy(pattern = values()[position])) }
            }
        })

    fun onSequencePattern1DurationValue(value: Int) {
        updateViewState { copy(sequencePattern1 = sequencePattern1.copy(durationSeconds = value)) }
    }

    fun onSequencePattern2DurationValue(value: Int) {
        updateViewState { copy(sequencePattern2 = sequencePattern2.copy(durationSeconds = value)) }
    }

    fun onSequencePattern3DurationValue(value: Int) {
        updateViewState { copy(sequencePattern3 = sequencePattern3.copy(durationSeconds = value)) }
    }

    fun onSequencePattern4DurationValue(value: Int) {
        updateViewState { copy(sequencePattern4 = sequencePattern4.copy(durationSeconds = value)) }
    }

    fun onSequencePattern5DurationValue(value: Int) {
        updateViewState { copy(sequencePattern5 = sequencePattern5.copy(durationSeconds = value)) }
    }

    fun onSequencePattern6DurationValue(value: Int) {
        updateViewState { copy(sequencePattern6 = sequencePattern6.copy(durationSeconds = value)) }
    }

    fun onSequencePattern7DurationValue(value: Int) {
        updateViewState { copy(sequencePattern7 = sequencePattern7.copy(durationSeconds = value)) }
    }

    fun onSequencePattern8DurationValue(value: Int) {
        updateViewState { copy(sequencePattern8 = sequencePattern8.copy(durationSeconds = value)) }
    }

    fun onRemoveButtonClick() =
        updateViewState { copy(enabledPatternCount = enabledPatternCount - 1) }

    fun onAddButtonClick() =
        updateViewState { copy(enabledPatternCount = enabledPatternCount + 1) }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        getViewState()?.selectedSequence?.let(::getSettings)
    }

    fun onSequenceSelected(position: Int) = getSettings(BrushingModeSequence.fromBleIndex(position))

    @Suppress("LongMethod")
    fun onApplyButtonClick() {
        getViewState()?.let { state ->
            val patterns = state.sequencePatternList()
                .take(state.enabledPatternCount)
                .map(SequencePatternItemData::toBrushingModeSequencePattern)

            disposeOnCleared {
                modeTweaker.setSequenceSettings(patterns)
                    .doOnSubscribe { showProgress(true) }
                    .doFinally { showProgress(false) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({}, ::showError)
            }
        }
    }

    private fun getSettings(sequence: BrushingModeSequence) {
        disposeOnStop {
            modeTweaker.getSequenceSettings(sequence)
                .doOnSubscribe { showProgress(true) }
                .doFinally { showProgress(false) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::onPatternSettings, ::showError)
        }
    }

    private fun onPatternSettings(sequenceSettings: BrushingModeSequenceSettings) {
        updateViewState { SequenceViewState.withSettings(sequenceSettings) }
    }

    class Factory @Inject constructor(
        private val sharedViewModel: TweakerSharedViewModel
    ) : BaseViewModel.Factory<SequenceViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SequenceViewModel(
                initialViewState = viewState ?: SequenceViewState.initial(),
                sharedViewModel = sharedViewModel
            ) as T
    }
}

private fun SequencePatternItemData.toBrushingModeSequencePattern() =
    BrushingModeSequencePattern(
        pattern = pattern,
        durationSeconds = durationSeconds
    )

private fun SequenceViewState.sequencePatternList() = listOf(
    sequencePattern1,
    sequencePattern2,
    sequencePattern3,
    sequencePattern4,
    sequencePattern5,
    sequencePattern6,
    sequencePattern7,
    sequencePattern8
)
