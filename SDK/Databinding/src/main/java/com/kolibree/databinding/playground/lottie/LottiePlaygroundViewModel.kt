/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.databinding.playground.lottie

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.databinding.R
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import javax.inject.Inject

internal class LottiePlaygroundViewModel(initialViewState: LottiePlaygroundViewState?) :
    BaseViewModel<LottiePlaygroundViewState, LottiePlaygroundActions>(
        initialViewState ?: LottiePlaygroundViewState.initial()
    ) {

    val lottieRes: LiveData<Int?> = map(viewStateLiveData) {
        it?.lottieAnimation ?: R.raw.trailblazer
    }

    val lottieUrl: LiveData<String> = map(viewStateLiveData) {
        DEMO_URL
    }

    val lottieDelayUrl: LiveData<String?> = map(viewStateLiveData) { viewState ->
        viewState?.isDelayOver?.takeIf { it }?.run { DEMO_URL }
    }

    val wrongUrl: LiveData<String> = map(viewStateLiveData) {
        "https://gvhqsdqdqjudhqsudys.lan/ghdhsd.json"
    }

    fun onLottieAnimationClick() {
        updateViewState {
            val nextAnimationIndex = (possibleAnimations.indexOf(this.lottieAnimation) + 1) % possibleAnimations.size
            copy(lottieAnimation = possibleAnimations[nextAnimationIndex])
        }
    }

    fun onLottieDisplayUrlClick() {
        updateViewState {
            copy(isDelayOver = true)
        }
    }

    class Factory @Inject constructor() : BaseViewModel.Factory<LottiePlaygroundViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = LottiePlaygroundViewModel(viewState) as T
    }
}

private val possibleAnimations = listOf(R.raw.voyager, R.raw.explorer, R.raw.trailblazer)
private const val DEMO_URL = "https://kolibree-website.s3.amazonaws.com/data.json"
