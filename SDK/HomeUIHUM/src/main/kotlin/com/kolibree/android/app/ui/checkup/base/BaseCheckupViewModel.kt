/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.base

import androidx.annotation.CallSuper
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.checkup.CheckupUtils
import com.kolibree.android.clock.TrustedClock
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.sdkws.brushing.wrapper.BrushingFacade
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/** Checkup [BaseViewModel] base implementation */
@VisibleForApp
open class BaseCheckupViewModel<VS : BaseCheckupViewState>(
    initialViewState: VS,
    private val brushingFacade: BrushingFacade
) : BaseViewModel<VS, CheckupActions>(
    baseViewState = initialViewState
) {

    val coverageLiveData = map(viewStateLiveData) { viewState ->
        viewState?.coverage
    }

    val durationPercentageLiveData = map(viewStateLiveData) { viewState ->
        viewState?.durationPercentage
    }

    val durationSecondsLiveData = map(viewStateLiveData) { viewState ->
        viewState?.durationSeconds
    }

    val brushingTypeLiveData = map(viewStateLiveData) { viewState ->
        CheckupUtils.brushingType(viewState?.game)
    }

    val brushingDateLiveData = map(viewStateLiveData) { viewState ->
        viewState?.date ?: TrustedClock.getNowOffsetDateTime()
    }

    @CallSuper
    open fun onBackButtonClick() = pushAction(CheckupActions.FinishCancel)

    fun onDeleteButtonClick() = pushAction(CheckupActions.ConfirmDeletion)

    fun onDeleteConfirmed() {
        currentBrushingSession()?.let {
            disposeOnStop {
                brushingFacade.deleteBrushing(it)
                    .subscribeOn(Schedulers.io())
                    .subscribe({ onBrushingDeleted(it) }, Timber::e)
            }
        }
    }

    // Not abstract so the class is easier to unit test
    open fun onBrushingDeleted(brushing: IBrushing) = Unit

    open fun currentBrushingSession(): IBrushing? = null
}
