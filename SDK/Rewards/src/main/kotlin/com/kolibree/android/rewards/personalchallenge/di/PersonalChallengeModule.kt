/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.di

import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.rewards.persistence.RewardsRoomDatabase
import com.kolibree.android.rewards.personalchallenge.data.api.PersonalChallengeApi
import com.kolibree.android.rewards.personalchallenge.data.persistence.PersonalChallengeDao
import com.kolibree.android.rewards.personalchallenge.domain.logic.PersonalChallengeV1Repository
import com.kolibree.android.rewards.personalchallenge.domain.logic.PersonalChallengeV1RepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import retrofit2.Retrofit

@Module(includes = [BrushingEventsModule::class])
abstract class PersonalChallengeModule {

    internal companion object {
        @Provides
        internal fun provideApi(retrofit: Retrofit): PersonalChallengeApi =
            retrofit.create(PersonalChallengeApi::class.java)

        @Provides
        internal fun providesDao(rewardsDatabase: RewardsRoomDatabase): PersonalChallengeDao =
            rewardsDatabase.personalChallengeDao()
    }

    @Binds
    internal abstract fun providesPersonalChallengeV1Repository(
        repository: PersonalChallengeV1RepositoryImpl
    ): PersonalChallengeV1Repository

    @IntoSet
    @Binds
    internal abstract fun providesTruncableRepository(
        repository: PersonalChallengeV1RepositoryImpl
    ): Truncable
}
