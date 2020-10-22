/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game

import android.content.Context
import androidx.room.Room
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.game.persistence.GamesRoomDatabase
import dagger.Module
import dagger.Provides

@Module
internal object GameDatabaseModule {

    @Provides
    @AppScope
    @Suppress("SpreadOperator")
    internal fun providesGamesDatabase(context: Context): GamesRoomDatabase =
        Room.databaseBuilder(
            context,
            GamesRoomDatabase::class.java,
            GamesRoomDatabase.DATABASE_NAME
        ).addMigrations(*GamesRoomDatabase.migrations).build()
}
