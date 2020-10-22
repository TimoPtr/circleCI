/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.main

import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import javax.inject.Inject

internal class MainActivityViewModel(initialViewState: MainActivityViewState?) :
    BaseViewModel<MainActivityViewState, MainActivityAction>(initialViewState ?: MainActivityViewState.initial()) {

    fun legacyMainActivityClicked() = pushAction(OpenLegacyMainActivity)

    fun singleConnectionActivityClicked() = pushAction(OpenSingleConnectionActivity)

    fun otaActivityClicked() = pushAction(OpenOtaActivity)

    fun freeBrushingActivityClicked() = pushAction(OpenFreeBrushingActivity)

    class Factory @Inject constructor() : BaseViewModel.Factory<MainActivityViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MainActivityViewModel(viewState) as T
    }
}
