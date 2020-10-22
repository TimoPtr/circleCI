/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.testbrushing.startscreen

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.startscreen.ActivityStartPreconditionsModule
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ActivityStartPreconditionsModule::class])
abstract class TestBrushingStartScreenModule {

    @Binds
    internal abstract fun bindAppCompatActivity(activity: TestBrushingStartScreenActivity): AppCompatActivity

    internal companion object {

        @Provides
        fun providesNavigator(activity: TestBrushingStartScreenActivity): TestBrushingStartScreenNavigator =
            activity.createNavigatorAndBindToLifecycle(TestBrushingStartScreenNavigator::class)

        @Provides
        fun providesMacAddress(activity: TestBrushingStartScreenActivity): String? =
            activity.providesMacAddress()
    }
}
