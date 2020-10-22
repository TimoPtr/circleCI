/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.di

import android.content.Context
import androidx.room.Room
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.brushreminder.BrushReminderRepository
import com.kolibree.android.brushreminder.BrushReminderRepositoryImpl
import com.kolibree.android.brushreminder.BrushReminderUseCase
import com.kolibree.android.brushreminder.BrushReminderUseCaseImpl
import com.kolibree.android.brushreminder.data.BrushReminderDao
import com.kolibree.android.brushreminder.data.BrushReminderDatabase
import com.kolibree.android.brushreminder.receiver.BrushingReminderBroadcastReceiver
import com.kolibree.android.brushreminder.receiver.RestoreBrushingReminderBroadcastReceiver
import com.kolibree.android.brushreminder.scheduler.BrushingReminderScheduler
import com.kolibree.android.brushreminder.scheduler.BrushingReminderSchedulerImpl
import com.kolibree.android.brushreminder.scheduler.CancelReminderLogout
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.commons.interfaces.UserLogoutHook
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoSet

@Module(includes = [BrushReminderDatabaseModule::class])
abstract class BrushReminderModule {

    @Binds
    internal abstract fun bindsRepository(
        implementation: BrushReminderRepositoryImpl
    ): BrushReminderRepository

    @Binds
    internal abstract fun bindsUseCase(
        implementation: BrushReminderUseCaseImpl
    ): BrushReminderUseCase

    @Binds
    internal abstract fun bindsReminderScheduler(
        implementation: BrushingReminderSchedulerImpl
    ): BrushingReminderScheduler

    @ContributesAndroidInjector
    internal abstract fun contributeBrushingReminderBroadcastReceiver():
        BrushingReminderBroadcastReceiver

    @ContributesAndroidInjector
    internal abstract fun contributeNotificationBroadcastReceiver():
        RestoreBrushingReminderBroadcastReceiver

    @Binds
    @IntoSet
    internal abstract fun bindsCancelReminderLogout(cancelReminderLogout: CancelReminderLogout):
        UserLogoutHook
}

@Module(includes = [DaoModule::class])
internal object BrushReminderDatabaseModule {
    @Provides
    @AppScope
    @Suppress("SpreadOperator")
    fun providesBrushSyncReminderDatabase(context: Context): BrushReminderDatabase {
        return Room.databaseBuilder(
            context,
            BrushReminderDatabase::class.java,
            BrushReminderDatabase.DATABASE_NAME
        ).addMigrations(*BrushReminderDatabase.migrations).build()
    }
}

@Module
private object DaoModule {
    @Provides
    fun providesBrushSyncReminderDao(appDatabase: BrushReminderDatabase): BrushReminderDao =
        appDatabase.brushSyncReminderDao()

    @IntoSet
    @Provides
    fun bindsTruncableBrushSyncReminder(dao: BrushReminderDao): Truncable = dao
}
