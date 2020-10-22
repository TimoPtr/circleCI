/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.common.base.Optional
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.testbrushing.TestBrushingViewState.Companion.initial
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.distinctUntilChanged
import javax.inject.Inject

@VisibleForApp
class TestBrushingViewModel(
    initialViewState: TestBrushingViewState
) : BaseViewModel<TestBrushingViewState, TestBrushingActions>(initialViewState),
    TestBrushingSharedViewModel {

    val progressVisible = map(viewStateLiveData) { state ->
        state?.progressVisible ?: false
    }.distinctUntilChanged()

    override val sharedViewStateLiveData: LiveData<TestBrushingViewState> = viewStateLiveData

    override fun getSharedViewState(): TestBrushingViewState? = getViewState()

    override fun showProgress(show: Boolean) {
        updateViewState { copy(progressVisible = show) }
    }

    override fun showError(error: Error) = pushAction(TestBrushingActions.ShowError(error))

    override fun resetState() = updateViewState {
        initial(model = toothbrushModel(), mac = toothbrushMac())
    }

    @VisibleForApp
    class Factory @Inject constructor(
        private val toothbrushModel: ToothbrushModel,
        @ToothbrushMac private val toothbrushMac: Optional<String>
    ) : BaseViewModel.Factory<TestBrushingViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TestBrushingViewModel(
                viewState ?: initial(toothbrushModel, toothbrushMac.get())
            ) as T
    }
}
