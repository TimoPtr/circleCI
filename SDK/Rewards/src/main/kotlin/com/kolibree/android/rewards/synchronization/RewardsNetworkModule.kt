package com.kolibree.android.rewards.synchronization

import com.kolibree.android.rewards.synchronization.challengeprogress.ChallengeProgressSynchronizableReadOnlyCreator
import com.kolibree.android.rewards.synchronization.challenges.ChallengesSynchronizableCatalogBundleCreator
import com.kolibree.android.rewards.synchronization.lifetimesmiles.LifetimeSmilesSynchronizableReadOnlyCreator
import com.kolibree.android.rewards.synchronization.personalchallenge.ProfilePersonalChallengeSynchronizableCreator
import com.kolibree.android.rewards.synchronization.prizes.PrizesSynchronizableCatalogBundleCreator
import com.kolibree.android.rewards.synchronization.profilesmiles.ProfileSmilesSynchronizableReadOnlyCreator
import com.kolibree.android.rewards.synchronization.profilesmileshistory.ProfileSmilesHistorySynchronizableReadOnlyCreator
import com.kolibree.android.rewards.synchronization.profiletier.ProfileTierSynchronizableReadOnlyCreator
import com.kolibree.android.rewards.synchronization.redeem.RedeemNetworkService
import com.kolibree.android.rewards.synchronization.redeem.RedeemNetworkServiceImpl
import com.kolibree.android.rewards.synchronization.tiers.TiersSynchronizableCatalogBundleCreator
import com.kolibree.android.rewards.synchronization.transfer.TransferNetworkService
import com.kolibree.android.rewards.synchronization.transfer.TransferNetworkServiceImpl
import com.kolibree.android.synchronizator.models.BundleCreator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import retrofit2.Retrofit

@Module
internal abstract class RewardsNetworkModule {

    companion object {
        @Provides
        internal fun providesAccountApiService(retrofit: Retrofit): RewardsApi {
            return retrofit.create(RewardsApi::class.java)
        }
    }

    @Binds
    internal abstract fun bindsRedeemNetworkService(
        redeemNetworkServiceImpl: RedeemNetworkServiceImpl
    ): RedeemNetworkService

    @Binds
    internal abstract fun bindsTransferNetworkService(
        transferNetworkServiceImpl: TransferNetworkServiceImpl
    ): TransferNetworkService

    @Binds
    @IntoSet
    internal abstract fun bindsLifetimeSmilesSynchronizableReadOnlyCreator(impl: LifetimeSmilesSynchronizableReadOnlyCreator): BundleCreator

    @Binds
    @IntoSet
    internal abstract fun bindsChallengesSynchronizableCatalogBundleCreator(impl: ChallengesSynchronizableCatalogBundleCreator): BundleCreator

    @Binds
    @IntoSet
    internal abstract fun bindsChallengeProgressSynchronizableReadOnlyCreator(impl: ChallengeProgressSynchronizableReadOnlyCreator): BundleCreator

    @Binds
    @IntoSet
    internal abstract fun bindsTiersSynchronizableCatalogBundleCreator(impl: TiersSynchronizableCatalogBundleCreator): BundleCreator

    @Binds
    @IntoSet
    internal abstract fun bindsProfileTierSynchronizableReadOnlyCreator(impl: ProfileTierSynchronizableReadOnlyCreator): BundleCreator

    @Binds
    @IntoSet
    internal abstract fun bindsProfileSmilesSynchronizableReadOnlyCreator(impl: ProfileSmilesSynchronizableReadOnlyCreator): BundleCreator

    @Binds
    @IntoSet
    internal abstract fun bindsPrizesSynchronizableCatalogBundleCreator(impl: PrizesSynchronizableCatalogBundleCreator): BundleCreator

    @Binds
    @IntoSet
    internal abstract fun bindsProfileSmilesHistorySynchronizableReadOnlyCreator(impl: ProfileSmilesHistorySynchronizableReadOnlyCreator): BundleCreator

    @Binds
    @IntoSet
    internal abstract fun bindsProfilePersonalChallengeSynchronizableCreator(impl: ProfilePersonalChallengeSynchronizableCreator): BundleCreator
}
