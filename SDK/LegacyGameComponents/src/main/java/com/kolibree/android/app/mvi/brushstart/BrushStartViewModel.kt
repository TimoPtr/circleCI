/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.mvi.brushstart

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.common.base.Optional
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import javax.inject.Inject
import javax.inject.Named

private typealias DrawableResInt = /* @DrawableRes */ Int

@Keep
class BrushStartViewModel(
    viewState: BrushStartViewState,
    gameInteractor: GameInteractor,
    private val resourceProvider: BrushStartResourceProvider
) : BaseBrushStartViewModel<BrushStartViewAction>(viewState, gameInteractor) {

    val isManualToothbrush: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isManualToothbrush
    }

    val isStaticPreview: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isStaticPreview
    }

    val previewResource: LiveData<DrawableResInt> = map(viewStateLiveData) { viewState ->
        viewState?.let { resourceProvider.provideToothbrushPreviewImage(it.model) }
    }

    val previewGifRes: LiveData<Int> = map(viewStateLiveData) { viewState ->
        viewState?.let {
            if (it.isStaticPreview) return@map null
            return@map resourceProvider.provideToothbrushVideo(it.model)
        }
    }

    override fun onBrushStarted(state: BrushStartViewState) {
        pushAction(BrushStarted(state.model, state.mac))
    }

    class Factory @Inject constructor(
        private val gameInteractor: GameInteractor,
        private val resourceProvider: BrushStartResourceProvider,
        @Named(BrushStartConstants.Argument.PACKAGE_NAME) private val packageName: String,
        @ToothbrushMac private val mac: Optional<String>,
        private val model: ToothbrushModel
    ) : BaseViewModel.Factory<BrushStartViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return BrushStartViewModel(
                viewState ?: BrushStartViewState(
                    model,
                    packageName,
                    mac.get()
                ),
                gameInteractor,
                resourceProvider
            ) as T
        }
    }
}
