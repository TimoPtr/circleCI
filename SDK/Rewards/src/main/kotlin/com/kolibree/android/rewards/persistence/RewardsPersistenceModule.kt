package com.kolibree.android.rewards.persistence

import android.content.Context
import androidx.room.Room
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.commons.interfaces.Truncable
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
internal object RewardsDatabaseModule {

    @Provides
    @AppScope
    internal fun providesRewardsDatabase(context: Context): RewardsRoomDatabase {
        return Room.databaseBuilder(
            context,
            RewardsRoomDatabase::class.java,
            RewardsRoomDatabase.DATABASE_NAME
        ).addMigrations(*RewardsRoomDatabase.migrations).build()
    }
}

@Module(includes = [RewardsPersistenceTruncableModule::class])
internal object RewardsPersistenceModule {

    @Provides
    internal fun providesChallengesDao(rewardsDatabase: RewardsRoomDatabase): ChallengesDao {
        return rewardsDatabase.challengesDao()
    }

    @Provides
    internal fun providesChallengeProgressDao(rewardsDatabase: RewardsRoomDatabase): ChallengeProgressDao {
        return rewardsDatabase.challengeProgressDao()
    }

    @Provides
    internal fun providesTiersDao(rewardsDatabase: RewardsRoomDatabase): TiersDao {
        return rewardsDatabase.tiersDao()
    }

    @Provides
    internal fun providesProfileTierDao(rewardsDatabase: RewardsRoomDatabase): ProfileTierDao {
        return rewardsDatabase.profileTierDao()
    }

    @Provides
    internal fun providesProfileSmilesDao(rewardsDatabase: RewardsRoomDatabase): ProfileSmilesDao {
        return rewardsDatabase.profileSmilesDao()
    }

    @Provides
    internal fun providesPrizeDao(rewardsDatabase: RewardsRoomDatabase): PrizeDao {
        return rewardsDatabase.prizeDao()
    }

    @Provides
    internal fun providesSmilesHistoryEventsDao(rewardsDatabase: RewardsRoomDatabase): SmilesHistoryEventsDao {
        return rewardsDatabase.smilesHistoryEventsDao()
    }

    @Provides
    internal fun providesFeedbackDao(rewardsDatabase: RewardsRoomDatabase): FeedbackDao {
        return rewardsDatabase.feedbackDao()
    }

    @Provides
    internal fun providesLifetimeSmilesDao(rewardsDatabase: RewardsRoomDatabase): LifetimeSmilesDao {
        return rewardsDatabase.lifetimeSmilesDao()
    }

    @Provides
    internal fun providesCategoriesDao(
        rewardsDatabase: RewardsRoomDatabase,
        challengesDao: ChallengesDao
    ): CategoriesDao {
        val categoriesDao = rewardsDatabase.categoriesDao()

        categoriesDao.challengesDao = challengesDao

        return categoriesDao
    }
}

@Module
internal abstract class RewardsPersistenceTruncableModule {
    @Binds
    @IntoSet
    internal abstract fun bindsTruncableProfileSmiles(dao: ProfileSmilesDao): Truncable

    @Binds
    @IntoSet
    internal abstract fun bindsTruncableRewardsSynchronizedVersions(
        synchronizedVersions: RewardsSynchronizedVersions
    ): Truncable

    @Binds
    @IntoSet
    internal abstract fun bindsLifetimeSmilesDaoTruncable(dao: LifetimeSmilesDao): Truncable

    @Binds
    @IntoSet
    internal abstract fun bindsChallengeProgressDaoTruncable(dao: ChallengeProgressDao): Truncable

    @Binds
    @IntoSet
    internal abstract fun bindsProfileTierDaoTruncable(dao: ProfileTierDao): Truncable

    @Binds
    @IntoSet
    internal abstract fun bindsFeedbackDaoTruncable(dao: FeedbackDao): Truncable

    @Binds
    @IntoSet
    internal abstract fun bindsSmilesHistoryEventsDaoTruncable(dao: SmilesHistoryEventsDao): Truncable
}
