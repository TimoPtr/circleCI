/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.shop

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.ui.home.toolbar.HomeToolbarViewModel
import com.kolibree.android.app.ui.home.tracker.BottomNavigationEventTracker
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import javax.inject.Inject

internal class ShopViewModel(
    initialViewState: EmptyBaseViewState,
    val toolbarViewModel: HomeToolbarViewModel
) : BaseViewModel<EmptyBaseViewState, HomeScreenAction>(
    initialViewState,
    children = setOf(toolbarViewModel)
) {
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        BottomNavigationEventTracker.shopVisible()
    }

    class Factory @Inject constructor(private val toolbarViewModel: HomeToolbarViewModel) :
        BaseViewModel.Factory<EmptyBaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShopViewModel(
            viewState ?: EmptyBaseViewState,
            toolbarViewModel
        ) as T
    }
}
