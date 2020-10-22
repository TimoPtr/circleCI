/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro.slides

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import javax.inject.Inject

internal class SlidesViewModel(initialViewState: SlidesViewState?) :
    BaseViewModel<SlidesViewState, SlidesAction>(initialViewState ?: SlidesViewState()) {

    class Factory @Inject constructor() : BaseViewModel.Factory<SlidesViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SlidesViewModel(
            viewState
        ) as T
    }
}
