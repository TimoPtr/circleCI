/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.start

import androidx.lifecycle.ViewModel
import com.google.common.base.Optional
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.app.mvi.brushstart.BaseBrushStartViewModel
import com.kolibree.android.app.mvi.brushstart.BrushStartViewState
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.testbrushing.TestBrushingNavigator
import com.kolibree.android.testbrushing.di.PACKAGE_NAME
import javax.inject.Inject
import javax.inject.Named

@VisibleForApp
class TestBrushingStartViewModel(
    viewState: BrushStartViewState,
    gameInteractor: GameInteractor,
    private val navigator: TestBrushingNavigator
) : BaseBrushStartViewModel<NoActions>(viewState, gameInteractor) {

    override fun onBrushStarted(state: BrushStartViewState) {
        navigator.startOngoingBrushing()
    }

    @VisibleForApp
    class Factory @Inject constructor(
        private val gameInteractor: GameInteractor,
        @Named(PACKAGE_NAME) private val packageName: String,
        private val toothbrushModel: ToothbrushModel,
        @ToothbrushMac private val toothbrushMac: Optional<String>,
        private val navigator: TestBrushingNavigator
    ) : BaseViewModel.Factory<BrushStartViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TestBrushingStartViewModel(
                viewState ?: BrushStartViewState(
                    toothbrushModel,
                    packageName,
                    toothbrushMac.get()
                ),
                gameInteractor,
                navigator
            ) as T
    }
}
