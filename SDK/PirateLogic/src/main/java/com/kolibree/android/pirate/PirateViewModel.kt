/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate

import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.app.ui.activity.mvi.BaseUnityViewModel
import com.kolibree.android.sdk.core.BackgroundJobManager
import javax.inject.Inject

@VisibleForApp
class PirateViewModel(
    viewState: PirateViewState,
    applicationContext: ApplicationContext,
    backgroundJobManager: Set<BackgroundJobManager>
) : BaseUnityViewModel<PirateViewState>(viewState, applicationContext, backgroundJobManager) {
    @VisibleForApp
    class Factory @Inject constructor(
        applicationContext: ApplicationContext,
        backgroundJobManager: Set<@JvmSuppressWildcards BackgroundJobManager>
    ) : BaseUnityViewModel.Factory<PirateViewState>(applicationContext, backgroundJobManager) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PirateViewModel(
                viewState ?: PirateViewState.initial(),
                applicationContext,
                backgroundJobManager
            ) as T
        }
    }
}
