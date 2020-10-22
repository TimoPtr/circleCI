/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.badges

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.extensions.withValueAnimator
import com.kolibree.android.app.ui.widget.ZoneProgressData
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import javax.inject.Inject

@Suppress("MagicNumber")
internal class BadgesPlaygroundActivityViewModel(initialViewState: BadgesPlaygroundActivityViewState?) :
    BaseViewModel<BadgesPlaygroundActivityViewState, BadgesPlaygroundActivityActions>(
        initialViewState ?: BadgesPlaygroundActivityViewState.initial()
    ) {

    val zone16: LiveData<ZoneProgressData> = map(viewStateLiveData) { viewState ->
        viewState?.zones16 ?: ZoneProgressData()
    }

    val zone8: LiveData<ZoneProgressData> = map(viewStateLiveData) { viewState ->
        viewState?.zones8 ?: ZoneProgressData()
    }

    val zone4: LiveData<ZoneProgressData> = map(viewStateLiveData) { viewState ->
        viewState?.zones4 ?: ZoneProgressData()
    }

    private var valueAnimator: ValueAnimator? = null

    class Factory @Inject constructor() :
        BaseViewModel.Factory<BadgesPlaygroundActivityViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BadgesPlaygroundActivityViewModel(viewState) as T
    }

    fun simulateZone8Update() {
        valueAnimator?.cancel()
        updateViewState { copy(zones8 = ZoneProgressData.create(8)) }
        valueAnimator = withValueAnimator(
            duration = 8000,
            interpolator = LinearInterpolator()
        ) { progress ->
            val progressValue = 8000 * progress
            progressOn(progressValue)
            if (progress >= 1f) {
                updateViewState { copy(zones8 = zones8.brushingFinished()) }
            }
        }
    }

    @Suppress("LongMethod")
    private fun progressOn(progress: Float) {
        if (progress <= 1000) {
            val zoneProgress = progress / 1000f
            updateViewState { copy(zones8 = zones8.updateProgressOnZone(0, zoneProgress)) }
        } else if (progress <= 2000) {
            val zoneProgress = (progress - 1000f) / 1000f
            updateViewState { copy(zones8 = zones8.updateProgressOnZone(1, zoneProgress * 0.8f)) }
        } else if (progress <= 3000) {
            val zoneProgress = (progress - 2000f) / 1000f
            updateViewState { copy(zones8 = zones8.updateProgressOnZone(2, zoneProgress * 0.9f)) }
        } else if (progress <= 4000) {
            val zoneProgress = (progress - 3000f) / 1000f
            updateViewState { copy(zones8 = zones8.updateProgressOnZone(3, zoneProgress * 0.7f)) }
        } else if (progress <= 5000) {
            val zoneProgress = (progress - 4000f) / 1000f
            updateViewState { copy(zones8 = zones8.updateProgressOnZone(4, zoneProgress)) }
        } else if (progress <= 6000) {
            val zoneProgress = (progress - 5000f) / 1000f
            updateViewState { copy(zones8 = zones8.updateProgressOnZone(5, zoneProgress * 0.3f)) }
        } else if (progress <= 7000) {
            val zoneProgress = (progress - 6000f) / 1000f
            updateViewState { copy(zones8 = zones8.updateProgressOnZone(6, zoneProgress)) }
        } else {
            val zoneProgress = (progress - 7000f) / 1000f
            updateViewState { copy(zones8 = zones8.updateProgressOnZone(7, zoneProgress * 0.7f)) }
        }
    }
}
