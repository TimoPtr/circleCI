/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.calendar.di

import android.content.Context
import androidx.room.Room
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.calendar.logic.persistence.BrushingStreaksDao
import com.kolibree.android.calendar.logic.persistence.CalendarRoomDatabase
import dagger.Module
import dagger.Provides

@Module
object CalendarRoomModule {

    @Provides
    @AppScope
    internal fun providesDatabase(context: Context): CalendarRoomDatabase {
        return Room.databaseBuilder(
            context,
            CalendarRoomDatabase::class.java,
            CalendarRoomDatabase.DATABASE_NAME
        )
            .build()
    }

    @Provides
    internal fun providesBrushingStreaksDao(database: CalendarRoomDatabase): BrushingStreaksDao {
        return database.brushingStreaksDao()
    }
}
