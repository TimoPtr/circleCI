/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro.slide2

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.plaqless.R
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import javax.inject.Inject

internal class SlideTwoViewModel(initialViewState: SlideTwoViewState?) :
    BaseViewModel<SlideTwoViewState, SlideTwoAction>(initialViewState ?: SlideTwoViewState.initial()) {

    val isDescriptionSelected1: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isDescriptionSelected1 ?: false
    }

    val isDescriptionSelected2: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isDescriptionSelected2 ?: false
    }

    val isDescriptionSelected3: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isDescriptionSelected3 ?: false
    }

    val toothbrushDrawable: LiveData<Int> = map(viewStateLiveData) { viewState ->
        when {
            viewState?.isDescriptionSelected1 == true -> R.drawable.ic_pql_slide_two_blue
            viewState?.isDescriptionSelected3 == true -> R.drawable.ic_pql_slide_two_red
            else -> R.drawable.ic_pql_slide_two_white
        }
    }

    fun userClickDescription1() {
        updateViewState {
            onlyDescription1()
        }
    }

    fun userClickDescription2() {
        updateViewState {
            onlyDescription2()
        }
    }

    fun userClickDescription3() {
        updateViewState {
            onlyDescription3()
        }
    }

    class Factory @Inject constructor() : BaseViewModel.Factory<SlideTwoViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SlideTwoViewModel(
            viewState
        ) as T
    }
}
