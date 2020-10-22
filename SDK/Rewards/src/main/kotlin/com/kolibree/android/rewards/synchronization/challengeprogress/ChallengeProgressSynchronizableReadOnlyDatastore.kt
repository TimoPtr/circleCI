package com.kolibree.android.rewards.synchronization.challengeprogress

import com.kolibree.android.rewards.persistence.ChallengeProgressDao
import com.kolibree.android.rewards.persistence.ChallengeProgressProfileCatalogInternal
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.synchronizator.SynchronizableReadOnlyDataStore
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import javax.inject.Inject
import timber.log.Timber

internal class ChallengeProgressSynchronizableReadOnlyDatastore
@Inject constructor(
    private val challengeProgressDao: ChallengeProgressDao,
    private val rewardsSynchronizedVersions: RewardsSynchronizedVersions
) : SynchronizableReadOnlyDataStore {
    override fun updateVersion(newVersion: Int) = rewardsSynchronizedVersions.setChallengeProgressVersion(newVersion)

    override fun replace(synchronizable: SynchronizableReadOnly) {
        (synchronizable as? ChallengeProgressProfileCatalogInternal)?.let {
            challengeProgressDao.replace(it)
        } ?: Timber.e("Expected ChallengeProgressProfileCatalogInternal, was %s", synchronizable)
    }
}
