package com.kolibree.android.rewards.synchronization.challengeprogress

import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.SynchronizeAccountKeyBuilder
import javax.inject.Inject

internal class ChallengeProgressSynchronizationKeyBuilder
@Inject constructor(
    private val rewardsSynchronizedVersions: RewardsSynchronizedVersions
) : SynchronizeAccountKeyBuilder(SynchronizableKey.CHALLENGE_PROGRESS) {

    override fun version(): Int = rewardsSynchronizedVersions.challengeProgressVersion()
}
