/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.mvi.intro

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import javax.inject.Inject

/**
 * Game intro view model
 */
@Keep
class GameIntroViewModel : BaseViewModel<EmptyBaseViewState, GameIntroAction>(EmptyBaseViewState) {

    fun startButtonClick() = pushAction(OpenBrushScreen)

    class Factory @Inject constructor() : BaseViewModel.Factory<EmptyBaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GameIntroViewModel() as T
    }
}
