package com.kolibree.android.rewards.synchronization

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.rewards.synchronization.challengeprogress.ChallengeProgressSynchronizableReadOnlyCreator
import com.kolibree.android.rewards.synchronization.challenges.ChallengesSynchronizableCatalogBundleCreator
import com.kolibree.android.rewards.synchronization.personalchallenge.ProfilePersonalChallengeSynchronizableCreator
import com.kolibree.android.rewards.synchronization.prizes.PrizesSynchronizableCatalogBundleCreator
import com.kolibree.android.rewards.synchronization.profilesmiles.ProfileSmilesSynchronizableReadOnlyCreator
import com.kolibree.android.rewards.synchronization.profilesmileshistory.ProfileSmilesHistorySynchronizableReadOnlyCreator
import com.kolibree.android.rewards.synchronization.profiletier.ProfileTierSynchronizableReadOnlyCreator
import com.kolibree.android.rewards.synchronization.tiers.TiersSynchronizableCatalogBundleCreator
import com.kolibree.android.synchronizator.SynchronizationBundles
import javax.inject.Inject

@VisibleForApp
class RewardsSynchronizationRegistrar @Inject constructor() {
    @Inject
    internal lateinit var challengesBundleCreator: ChallengesSynchronizableCatalogBundleCreator

    @Inject
    internal lateinit var challengeProgressBundleCreator: ChallengeProgressSynchronizableReadOnlyCreator

    @Inject
    internal lateinit var tiersBundleCreator: TiersSynchronizableCatalogBundleCreator

    @Inject
    internal lateinit var profileTierSynchronizableReadOnlyCreator: ProfileTierSynchronizableReadOnlyCreator

    @Inject
    internal lateinit var profileSmilesSynchronizableReadOnlyCreator: ProfileSmilesSynchronizableReadOnlyCreator

    @Inject
    internal lateinit var prizesSynchronizableCatalogBundleCreator: PrizesSynchronizableCatalogBundleCreator

    @Inject
    internal lateinit var smilesHistorySynchronizableReadOnlyCreator: ProfileSmilesHistorySynchronizableReadOnlyCreator

    @Inject
    internal lateinit var personalChallengeSynchronizableCreator: ProfilePersonalChallengeSynchronizableCreator

    fun register() {
        SynchronizationBundles.register(challengesBundleCreator.create())
        SynchronizationBundles.register(challengeProgressBundleCreator.create())
        SynchronizationBundles.register(tiersBundleCreator.create())
        SynchronizationBundles.register(profileTierSynchronizableReadOnlyCreator.create())
        SynchronizationBundles.register(profileSmilesSynchronizableReadOnlyCreator.create())
        SynchronizationBundles.register(prizesSynchronizableCatalogBundleCreator.create())
        SynchronizationBundles.register(smilesHistorySynchronizableReadOnlyCreator.create())
        SynchronizationBundles.register(personalChallengeSynchronizableCreator.create())
    }
}
