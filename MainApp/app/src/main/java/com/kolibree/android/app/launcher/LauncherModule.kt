/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.launcher

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.update.AppUpdateUseCase
import com.kolibree.android.app.update.AppUpdateUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
internal abstract class LauncherModule {

    @Binds
    abstract fun bindAppCompatActivity(activity: LauncherActivity): AppCompatActivity

    @Binds
    abstract fun bindsAppUpdateUseCase(impl: AppUpdateUseCaseImpl): AppUpdateUseCase

    internal companion object {

        @Provides
        fun providesNavigator(
            activity: LauncherActivity,
            factory: LauncherNavigator.Factory
        ): LauncherNavigator {
            val navigator = activity.viewModels<LauncherNavigator> { factory }.value
            activity.lifecycle.addObserver(navigator)
            return navigator
        }
    }
}
