/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder

import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.Keep
import androidx.room.Room
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.brushsyncreminder.data.BrushSyncReminderDao
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.hum.brushsyncreminder.ReminderNotificationBroadcastReceiver
import com.kolibree.android.hum.brushsyncreminder.RestoreReminderNotificationBroadcastReceiver
import com.kolibree.android.hum.brushsyncreminder.data.BrushSyncReminderDatabase
import com.kolibree.android.hum.reminder.synchronization.BrushSyncReminderBundleCreator
import com.kolibree.android.synchronizator.models.BundleCreator
import com.kolibree.android.utils.lifecycle.ApplicationLifecycleObserver
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoSet
import javax.inject.Qualifier

@Module(includes = [BrushSyncReminderFeatureModule::class, BrushSyncReminderDatabaseModule::class])
object BrushSyncReminderModule

@Module
abstract class BrushSyncReminderFeatureModule {

    @Binds
    internal abstract fun bindsUseCase(
        implementation: BrushSyncReminderUseCaseImpl
    ): BrushSyncReminderUseCase

    @Binds
    internal abstract fun bindsScheduler(
        implementation: BrushReminderSchedulerImpl
    ): BrushReminderScheduler

    @Binds
    internal abstract fun bindsComponentsToggle(
        implementation: BrushReminderComponentsToggleImpl
    ): BrushSyncReminderComponentsToggle

    @Binds
    internal abstract fun bindsRepository(
        implementation: BrushSyncReminderRepositoryImpl
    ): BrushSyncReminderRepository

    @Binds
    @IntoSet
    internal abstract fun bindsBrushReminderBundleCreator(
        impl: BrushSyncReminderBundleCreator
    ): BundleCreator

    @Binds
    @IntoSet
    internal abstract fun bindsBrushSyncReminderMonitor(
        impl: BrushSyncReminderMonitor
    ): ApplicationLifecycleObserver

    @ContributesAndroidInjector
    internal abstract fun contributeNotificationBroadcastReceiver():
        ReminderNotificationBroadcastReceiver

    @ContributesAndroidInjector
    internal abstract fun contributeRestoreNotificationBroadcastReceiver():
        RestoreReminderNotificationBroadcastReceiver

    @VisibleForApp
    internal companion object {

        @Provides
        fun providesAlarmManager(context: Context): AlarmManager {
            return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }

        @Provides
        fun providesPackageManager(context: Context): PackageManager {
            return context.packageManager
        }

        @Provides
        @ComponentNotification
        fun providesNotificationComponent(context: Context): ComponentName {
            return ComponentName(context, ReminderNotificationBroadcastReceiver::class.java)
        }

        @Provides
        @ComponentRestoreNotification
        fun providesRestoreNotificationComponent(context: Context): ComponentName {
            return ComponentName(context, RestoreReminderNotificationBroadcastReceiver::class.java)
        }

        @Provides
        fun providesBrushSyncReminderDao(appDatabase: BrushSyncReminderDatabase): BrushSyncReminderDao =
            appDatabase.brushSyncReminderDao()

        @IntoSet
        @Provides
        fun bindsTruncableBrushSyncReminder(dao: BrushSyncReminderDao): Truncable = dao
    }
}

@Module
internal object BrushSyncReminderDatabaseModule {
    @Provides
    @AppScope
    @Suppress("SpreadOperator")
    fun providesBrushSyncReminderDatabase(context: Context): BrushSyncReminderDatabase {
        return Room.databaseBuilder(
            context,
            BrushSyncReminderDatabase::class.java,
            BrushSyncReminderDatabase.DATABASE_NAME
        ).addMigrations(*BrushSyncReminderDatabase.migrations).build()
    }
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Keep
internal annotation class ComponentNotification

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Keep
internal annotation class ComponentRestoreNotification
