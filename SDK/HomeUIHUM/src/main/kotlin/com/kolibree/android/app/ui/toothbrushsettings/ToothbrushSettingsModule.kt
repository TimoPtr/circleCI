/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
internal abstract class ToothbrushSettingsModule {

    @Binds
    abstract fun bindAppCompatActivity(activity: ToothbrushSettingsActivity): AppCompatActivity

    internal companion object {

        @Provides
        fun providesNavigator(activity: ToothbrushSettingsActivity): ToothbrushSettingsNavigator {
            return activity.createNavigatorAndBindToLifecycle(ToothbrushSettingsNavigator::class)
        }

        @Provides
        fun providesToothbrushMac(activity: ToothbrushSettingsActivity): String {
            return activity.toothbrushMac()
        }
    }
}
