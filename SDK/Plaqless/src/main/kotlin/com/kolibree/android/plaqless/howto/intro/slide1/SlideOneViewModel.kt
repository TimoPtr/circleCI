/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro.slide1

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import javax.inject.Inject

internal class SlideOneViewModel(initialViewState: SlideOneViewState?) :
    BaseViewModel<SlideOneViewState, SlideOneAction>(initialViewState ?: SlideOneViewState()) {

    val isInfoSelected1: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isInfoSelected1 ?: false
    }

    val isInfoSelected2: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isInfoSelected2 ?: false
    }

    val isInfoSelected3: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isInfoSelected3 ?: false
    }

    fun userClickInfo1() {
        updateViewState {
            onlyInfo1()
        }
    }

    fun userClickInfo2() {
        updateViewState {
            onlyInfo2()
        }
    }

    fun userClickInfo3() {
        updateViewState {
            onlyInfo3()
        }
    }

    internal class Factory @Inject constructor() : BaseViewModel.Factory<SlideOneViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SlideOneViewModel(
            viewState
        ) as T
    }
}
