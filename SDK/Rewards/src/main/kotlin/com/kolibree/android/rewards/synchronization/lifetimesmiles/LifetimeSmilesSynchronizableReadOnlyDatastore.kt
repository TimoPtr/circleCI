/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.lifetimesmiles

import com.kolibree.android.rewards.models.LifetimeSmilesEntity
import com.kolibree.android.rewards.persistence.LifetimeSmilesDao
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.synchronizator.SynchronizableReadOnlyDataStore
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import javax.inject.Inject
import timber.log.Timber

internal class LifetimeSmilesSynchronizableReadOnlyDatastore
@Inject constructor(
    private val lifetimeSmilesDao: LifetimeSmilesDao,
    private val versionsPersistence: RewardsSynchronizedVersions
) : SynchronizableReadOnlyDataStore {
    override fun updateVersion(newVersion: Int) =
        versionsPersistence.setLifetimeSmilesVersion(newVersion)

    override fun replace(synchronizable: SynchronizableReadOnly) {
        (synchronizable as? LifetimeSmilesEntity)?.let {
            lifetimeSmilesDao.insertOrReplace(it)
        } ?: Timber.e("Expected LifetimeStatsEntity, was %s", synchronizable)
    }
}
