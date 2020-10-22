/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.notifications

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.kolibree.android.app.dagger.scopes.ActivityScope
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module(
    includes = [
        NotificationsBindingSharedModule::class,
        SystemNotificationsEnabledModule::class
    ]
)
object NotificationsBindingModule

@Module
abstract class NotificationsBindingSharedModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [NotificationsModule::class])
    internal abstract fun bindNotificationsActivity(): NotificationsActivity
}

@Module
internal abstract class SystemNotificationsEnabledModule {

    @Binds
    internal abstract fun bindSystemNotificationsEnabledUseCase(
        implementation: SystemNotificationsEnabledUseCaseImpl
    ): SystemNotificationsEnabledUseCase

    internal companion object {

        @Provides
        fun providesNotificationManagerCompat(context: Context): NotificationManagerCompat {
            return NotificationManagerCompat.from(context)
        }
    }
}
