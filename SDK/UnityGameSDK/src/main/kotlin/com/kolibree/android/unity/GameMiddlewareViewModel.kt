/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.app.ui.activity.mvi.BaseUnityViewModel
import com.kolibree.android.sdk.core.BackgroundJobManager
import javax.inject.Inject

@Keep
class GameMiddlewareViewModel(
    viewState: GameMiddlewareViewState,
    applicationContext: ApplicationContext,
    backgroundJobManager: Set<BackgroundJobManager>
) : BaseUnityViewModel<GameMiddlewareViewState>(
    viewState,
    applicationContext,
    backgroundJobManager
) {
    class Factory @Inject constructor(
        applicationContext: ApplicationContext,
        backgroundJobManager: Set<@JvmSuppressWildcards BackgroundJobManager>
    ) : BaseUnityViewModel.Factory<GameMiddlewareViewState>(
        applicationContext,
        backgroundJobManager
    ) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GameMiddlewareViewModel(
                viewState ?: GameMiddlewareViewState.initial(),
                applicationContext,
                backgroundJobManager
            ) as T
        }
    }
}
