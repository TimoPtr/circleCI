/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.shorttask.di

import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.game.persistence.GamesRoomDatabase
import com.kolibree.android.game.shorttask.data.api.ShortTaskApi
import com.kolibree.android.game.shorttask.data.persistence.ShortTaskDao
import com.kolibree.android.game.shorttask.domain.logic.ShortTaskRepository
import com.kolibree.android.game.shorttask.domain.logic.ShortTaskRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import retrofit2.Retrofit

@Module
internal abstract class ShortTaskModule {

    @Binds
    abstract fun bindsShortTaskRepository(impl: ShortTaskRepositoryImpl): ShortTaskRepository

    @Binds
    @IntoSet
    abstract fun bindsTruncable(dao: ShortTaskDao): Truncable

    internal companion object {
        @Provides
        fun providesShortTaskApi(retrofit: Retrofit): ShortTaskApi =
            retrofit.create(ShortTaskApi::class.java)

        @Provides
        fun providesShortTaskDao(database: GamesRoomDatabase): ShortTaskDao =
            database.shortTaskDao()
    }
}
