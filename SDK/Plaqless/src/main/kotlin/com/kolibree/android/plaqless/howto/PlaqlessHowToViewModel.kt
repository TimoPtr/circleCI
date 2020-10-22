/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import javax.inject.Inject

@Keep
class PlaqlessHowToViewModel(initialViewState: PlaqlessHowToViewState?) :
    BaseViewModel<PlaqlessHowToViewState, PlaqlessHowToAction>(initialViewState ?: PlaqlessHowToViewState()) {

    @Keep
    class Factory @Inject constructor() : BaseViewModel.Factory<PlaqlessHowToViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PlaqlessHowToViewModel(viewState) as T
    }
}
