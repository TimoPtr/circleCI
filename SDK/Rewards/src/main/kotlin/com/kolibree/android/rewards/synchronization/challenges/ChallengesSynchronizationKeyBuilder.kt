package com.kolibree.android.rewards.synchronization.challenges

import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.SynchronizeAccountKeyBuilder
import javax.inject.Inject

internal class ChallengesSynchronizationKeyBuilder
@Inject constructor(private val rewardsSynchronizedVersions: RewardsSynchronizedVersions) :
    SynchronizeAccountKeyBuilder(SynchronizableKey.CHALLENGE_CATALOG) {
    override fun version(): Int = rewardsSynchronizedVersions.challengesCatalogVersion()
}
