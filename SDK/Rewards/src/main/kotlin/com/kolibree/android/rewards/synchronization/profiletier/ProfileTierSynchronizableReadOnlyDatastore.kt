/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profiletier

import com.kolibree.android.rewards.models.ProfileTierEntity
import com.kolibree.android.rewards.persistence.ProfileTierDao
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.synchronizator.SynchronizableReadOnlyDataStore
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import javax.inject.Inject
import timber.log.Timber

internal class ProfileTierSynchronizableReadOnlyDatastore
@Inject constructor(
    private val profileTierDao: ProfileTierDao,
    private val rewardsSynchronizedVersions: RewardsSynchronizedVersions
) : SynchronizableReadOnlyDataStore {
    override fun updateVersion(newVersion: Int) = rewardsSynchronizedVersions.setProfileTiersVersion(newVersion)

    override fun replace(synchronizable: SynchronizableReadOnly) {
        (synchronizable as? ProfileTierEntity)?.let {
            profileTierDao.insert(it)
        } ?: Timber.e("Expected ProfileTierEntity, was %s", synchronizable)
    }
}
