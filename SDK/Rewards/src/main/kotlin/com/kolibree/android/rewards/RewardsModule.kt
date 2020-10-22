package com.kolibree.android.rewards

import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.interfaces.RemoteBrushingsProcessor
import com.kolibree.android.rewards.feedback.FeedbackRepository
import com.kolibree.android.rewards.feedback.FeedbackRepositoryImpl
import com.kolibree.android.rewards.feedback.FirstLoginDateImpl
import com.kolibree.android.rewards.feedback.FirstLoginDateProvider
import com.kolibree.android.rewards.feedback.FirstLoginDateUpdater
import com.kolibree.android.rewards.morewaystoearnpoints.di.MoreWaysToEarnPointsModule
import com.kolibree.android.rewards.persistence.LifetimeStatsRepository
import com.kolibree.android.rewards.persistence.LifetimeStatsRoomRepository
import com.kolibree.android.rewards.persistence.ProfileSmilesRepository
import com.kolibree.android.rewards.persistence.RewardsDatabaseModule
import com.kolibree.android.rewards.persistence.RewardsPersistenceModule
import com.kolibree.android.rewards.persistence.RewardsRepository
import com.kolibree.android.rewards.persistence.RewardsRepositoryImpl
import com.kolibree.android.rewards.synchronization.RewardsNetworkModule
import dagger.Binds
import dagger.Module
import javax.inject.Qualifier

@Module(
    includes = [
        RewardsUseCaseModule::class,
        RewardsDatabaseModule::class,
        RewardsPersistenceModule::class,
        RewardsNetworkModule::class,
        MoreWaysToEarnPointsModule::class
    ]
)
abstract class RewardsModule {
    @Binds
    @ProfileProgress
    internal abstract fun bindsProfileSmilesRepository(impl: RewardsRepositoryImpl): ProfileSmilesRepository

    @Binds
    internal abstract fun bindsRewardsRepository(impl: RewardsRepositoryImpl): RewardsRepository

    @Binds
    internal abstract fun bindsFirstRunDateUpdater(impl: FirstLoginDateImpl): FirstLoginDateUpdater

    @Binds
    internal abstract fun bindsFirstRunDateProvider(impl: FirstLoginDateImpl): FirstLoginDateProvider

    @Binds
    internal abstract fun bindsRewardsRemoteBrushingProcessor(impl: RewardsRemoteBrushingProcessor):
        RemoteBrushingsProcessor

    @Binds
    internal abstract fun bindsLifetimeStatsRepository(impl: LifetimeStatsRoomRepository): LifetimeStatsRepository

    @Binds
    internal abstract fun bindsEarnPointsUseCase(impl: EarnPointsChallengeUseCaseImpl): EarnPointsChallengeUseCase

    @Binds
    internal abstract fun bindsFeedbackRepository(impl: FeedbackRepositoryImpl): FeedbackRepository
}

/*
Cool way to expose dependencies without allowing external modules to use the dependency

https://www.zacsweers.dev/dagger-party-tricks-private-dependencies/
 */
@MustBeDocumented
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@VisibleForTesting
internal annotation class ProfileProgress
