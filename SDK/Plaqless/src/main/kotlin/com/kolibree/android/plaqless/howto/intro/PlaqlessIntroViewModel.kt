/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import javax.inject.Inject

internal class PlaqlessIntroViewModel(
    initialViewState: PlaqlessIntroViewState?,
    private val navigator: PlaqlessHowToNavigator
) : BaseViewModel<PlaqlessIntroViewState, PlaqlessIntroAction>(initialViewState ?: PlaqlessIntroViewState()) {

    fun userClickStart() {
        navigator.navigateToSlides()
    }

    class Factory @Inject constructor(
        private val navigator: PlaqlessHowToNavigator
    ) : BaseViewModel.Factory<PlaqlessIntroViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PlaqlessIntroViewModel(
            viewState, navigator
        ) as T
    }
}
