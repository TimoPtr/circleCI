/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.mvi.confirmation

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import javax.inject.Inject

/**
 * Game confirmation view model
 */
@Keep
class GameConfirmationViewModel : BaseViewModel<
    EmptyBaseViewState,
    GameConfirmationAction
    >(EmptyBaseViewState) {

    fun closeFeature() {
        pushAction(CloseFeature)
    }

    class Factory @Inject constructor() : BaseViewModel.Factory<EmptyBaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GameConfirmationViewModel() as T
    }
}
