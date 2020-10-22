/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.notifications

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
internal abstract class NotificationsModule {

    @Binds
    abstract fun bindAppCompatActivity(activity: NotificationsActivity): AppCompatActivity

    internal companion object {

        @Provides
        fun providesNotificationsNavigator(
            activity: NotificationsActivity,
            factory: NotificationsNavigator.Factory
        ): NotificationsNavigator {
            return activity.createNavigatorAndBindToLifecycle(NotificationsNavigator::class) { factory }
        }
    }
}
