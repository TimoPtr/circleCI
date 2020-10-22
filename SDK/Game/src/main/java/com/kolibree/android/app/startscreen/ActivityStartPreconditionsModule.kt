/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.startscreen

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.createViewModelAndBindToLifeCycle
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class ActivityStartPreconditionsModule {

    @Binds
    abstract fun bindsActivityStartPreconditions(
        viewModel: ActivityStartPreconditionsViewModel
    ): ActivityStartPreconditions

    internal companion object {

        @Provides
        internal fun providesActivityStartPreconditionsViewModel(
            activity: AppCompatActivity,
            viewModelFactory: ActivityStartPreconditionsViewModel.Factory
        ): ActivityStartPreconditionsViewModel =
            activity.createViewModelAndBindToLifeCycle<ActivityStartPreconditionsViewModel> { viewModelFactory }
    }
}
