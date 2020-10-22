/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.chart

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import javax.inject.Inject

internal class ChartPlaygroundViewModel(initialViewState: ChartPlaygroundViewState?) :
    BaseViewModel<ChartPlaygroundViewState, ChartPlaygroundActions>(
        initialViewState ?: ChartPlaygroundViewState.initial()
    ) {

    class Factory @Inject constructor() : BaseViewModel.Factory<ChartPlaygroundViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ChartPlaygroundViewModel(viewState) as T
    }
}
