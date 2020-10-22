/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.guidedbrushing.startscreen

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.startscreen.ActivityStartPreconditionsModule
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ActivityStartPreconditionsModule::class])
abstract class GuidedBrushingStartScreenModule {

    @Binds
    internal abstract fun bindAppCompatActivity(activity: GuidedBrushingStartScreenActivity): AppCompatActivity

    internal companion object {

        @Provides
        fun providesNavigator(activity: GuidedBrushingStartScreenActivity): GuidedBrushingStartScreenNavigator =
            activity.createNavigatorAndBindToLifecycle(GuidedBrushingStartScreenNavigator::class)

        @Provides
        fun providesMacAddress(activity: GuidedBrushingStartScreenActivity): String? =
            activity.providesMacAddress()
    }
}
