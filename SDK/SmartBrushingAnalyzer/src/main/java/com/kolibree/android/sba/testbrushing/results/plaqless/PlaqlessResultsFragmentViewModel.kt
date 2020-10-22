/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing.results.plaqless

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.sba.testbrushing.TestBrushingNavigator
import javax.inject.Inject

internal class PlaqlessResultsFragmentViewModel(private val navigator: TestBrushingNavigator) :
    BaseViewModel<BaseViewState, BaseAction>(EmptyBaseViewState) {

    fun userClickGoToHomePage() {
        navigator.finishScreen()
    }

    class Factory @Inject constructor(
        private val navigator: TestBrushingNavigator
    ) : BaseViewModel.Factory<BaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PlaqlessResultsFragmentViewModel(navigator) as T
    }
}
