/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro.slide3

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.plaqless.howto.intro.PlaqlessHowToNavigator
import javax.inject.Inject

internal class SlideThreeViewModel(
    initialViewState: SlideThreeViewState?,
    private val navigator: PlaqlessHowToNavigator
) :
    BaseViewModel<SlideThreeViewState, SlideThreeActions>(initialViewState ?: SlideThreeViewState) {

    fun tryNowClick() {
        navigator.navigateToTestBrushing()
    }

    fun tryLaterClick() {
        navigator.finish()
    }

    class Factory @Inject constructor(
        private val navigator: PlaqlessHowToNavigator
    ) : BaseViewModel.Factory<SlideThreeViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SlideThreeViewModel(
            viewState, navigator
        ) as T
    }
}
