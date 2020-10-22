/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profilesmiles

import com.kolibree.android.rewards.models.ProfileSmilesEntity
import com.kolibree.android.rewards.persistence.ProfileSmilesDao
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.synchronizator.SynchronizableReadOnlyDataStore
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import javax.inject.Inject
import timber.log.Timber

internal class ProfileSmilesSynchronizableReadOnlyDatastore
@Inject constructor(
    private val profileSmilesDao: ProfileSmilesDao,
    private val rewardsSynchronizedVersions: RewardsSynchronizedVersions
) : SynchronizableReadOnlyDataStore {
    override fun updateVersion(newVersion: Int) =
        rewardsSynchronizedVersions.setProfileSmilesVersion(newVersion)

    override fun replace(synchronizable: SynchronizableReadOnly) {
        (synchronizable as? ProfileSmilesEntity)?.let {
            profileSmilesDao.insert(it)
        } ?: Timber.e("Expected ProfileTierEntity, was %s", synchronizable)
    }
}
