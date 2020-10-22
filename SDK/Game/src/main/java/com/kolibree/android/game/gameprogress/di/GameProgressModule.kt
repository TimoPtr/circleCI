/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.gameprogress.di

import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.game.gameprogress.data.api.GameProgressApi
import com.kolibree.android.game.gameprogress.data.persistence.GameProgressDao
import com.kolibree.android.game.gameprogress.domain.logic.GameProgressRepository
import com.kolibree.android.game.gameprogress.domain.logic.GameProgressRepositoryImpl
import com.kolibree.android.game.persistence.GamesRoomDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import retrofit2.Retrofit

@Module
internal abstract class GameProgressModule {

    @Binds
    abstract fun bindsGameProgressRepository(impl: GameProgressRepositoryImpl): GameProgressRepository

    @Binds
    @IntoSet
    abstract fun bindsTruncable(dao: GameProgressDao): Truncable

    internal companion object {
        @Provides
        fun providesGameProgressApi(retrofit: Retrofit): GameProgressApi =
            retrofit.create(GameProgressApi::class.java)

        @Provides
        fun providesGameProgressDao(gamesRoomDatabase: GamesRoomDatabase): GameProgressDao =
            gamesRoomDatabase.gameProgressDao()
    }
}
