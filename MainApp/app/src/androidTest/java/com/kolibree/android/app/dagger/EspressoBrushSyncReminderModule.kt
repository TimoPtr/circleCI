/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import android.content.Context
import androidx.room.Room
import com.kolibree.android.brushsyncreminder.BrushSyncReminderFeatureModule
import com.kolibree.android.hum.brushsyncreminder.data.BrushSyncReminderDatabase
import dagger.Module
import dagger.Provides

@Module(includes = [BrushSyncReminderFeatureModule::class, EspressoBrushSyncReminderDatabaseModule::class])
object EspressoBrushSyncReminderModule

@Module
private object EspressoBrushSyncReminderDatabaseModule {
    @Provides
    @AppScope
    @Suppress("SpreadOperator")
    fun providesBrushSyncReminderDatabase(context: Context): BrushSyncReminderDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            BrushSyncReminderDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }
}
