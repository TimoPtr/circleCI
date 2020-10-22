/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selecttoothbrush

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class SelectToothbrushModule {

    @Binds
    internal abstract fun bindsIconProvider(
        impl: SelectToothbrushIconProviderImpl
    ): SelectToothbrushIconProvider

    @Binds
    internal abstract fun bindsUseCase(
        impl: SelectToothbrushUseCaseImpl
    ): SelectToothbrushUseCase

    internal companion object {

        @Provides
        internal fun providesNavigator(activity: AppCompatActivity): SelectToothbrushNavigator {
            return activity.createNavigatorAndBindToLifecycle(SelectToothbrushNavigator::class)
        }
    }
}
