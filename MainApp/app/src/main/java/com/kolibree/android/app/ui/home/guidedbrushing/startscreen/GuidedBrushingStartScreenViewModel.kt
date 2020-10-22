/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.guidedbrushing.startscreen

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.startscreen.ActivityStartPreconditions
import com.kolibree.android.app.startscreen.ActivityStartPreconditionsViewModel
import com.kolibree.android.guidedbrushing.domain.BrushingTipsUseCase
import com.kolibree.android.tracker.Analytics
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class GuidedBrushingStartScreenViewModel(
    private val navigator: GuidedBrushingStartScreenNavigator,
    private val brushingTipsUseCase: BrushingTipsUseCase,
    private val activityStartPreconditionsViewModel: ActivityStartPreconditionsViewModel
) : BaseViewModel<EmptyBaseViewState, NoActions>(
    EmptyBaseViewState,
    children = setOf(activityStartPreconditionsViewModel)
), ActivityStartPreconditions by activityStartPreconditionsViewModel {

    fun startClicked() {
        getBrushingScreenState { isDisplayable ->
            if (isDisplayable) {
                navigator.startGuidedBrushingTips()
            } else {
                Analytics.send(GuidedBrushingStartScreenAnalytics.start())
                navigator.startGuidedBrushing()
            }
        }
    }

    fun cancelClicked() {
        Analytics.send(GuidedBrushingStartScreenAnalytics.cancel())
        navigator.finish()
    }

    private fun getBrushingScreenState(onSuccessBlock: (isDisplayable: Boolean) -> Unit) {
        disposeOnCleared {
            brushingTipsUseCase.isBrushingTipsDisplayable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccessBlock, Timber::e)
        }
    }

    class Factory @Inject constructor(
        private val navigator: GuidedBrushingStartScreenNavigator,
        private val brushingTipsUseCase: BrushingTipsUseCase,
        private val activityStartPreconditionsViewModel: ActivityStartPreconditionsViewModel
    ) : BaseViewModel.Factory<EmptyBaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GuidedBrushingStartScreenViewModel(
                navigator,
                brushingTipsUseCase,
                activityStartPreconditionsViewModel
            ) as T
    }
}
