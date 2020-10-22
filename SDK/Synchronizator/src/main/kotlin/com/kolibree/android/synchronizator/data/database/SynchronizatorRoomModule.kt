/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.data.database

import android.content.Context
import androidx.room.Room
import com.kolibree.android.app.dagger.AppScope
import dagger.Module
import dagger.Provides

@Module
internal object SynchronizatorRoomModule {

    @Provides
    @AppScope
    internal fun providesDatabase(context: Context): SynchronizatorDatabase {
        return Room.databaseBuilder(
            context,
            SynchronizatorDatabase::class.java,
            SynchronizatorDatabase.DATABASE_NAME
        )
            .build()
    }

    @Provides
    internal fun providesSynchronizatorEntityDao(database: SynchronizatorDatabase): SynchronizableTrackingEntityDao {
        return database.synchronizatorEntityDao()
    }
}
