/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.mindyourspeed.startscreen

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.startscreen.ActivityStartPreconditionsModule
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ActivityStartPreconditionsModule::class])
internal abstract class MindYourSpeedStartScreenModule {

    @Binds
    abstract fun bindAppCompatActivity(activity: MindYourSpeedStartScreenActivity): AppCompatActivity

    internal companion object {

        @Provides
        fun providesNavigator(activity: MindYourSpeedStartScreenActivity): MindYourSpeedStartScreenNavigator {
            return activity.createNavigatorAndBindToLifecycle(MindYourSpeedStartScreenNavigator::class)
        }

        @Provides
        fun providesMacAddress(activity: MindYourSpeedStartScreenActivity): String? =
            activity.providesMacAddress()
    }
}
