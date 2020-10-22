/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.tiers

import com.kolibree.android.rewards.models.TierEntity
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.rewards.persistence.TiersDao
import com.kolibree.android.synchronizator.SynchronizableCatalogDataStore
import com.kolibree.android.synchronizator.models.SynchronizableCatalog
import javax.inject.Inject
import timber.log.Timber

internal class TiersSynchronizableCatalogDatastore
@Inject constructor(
    private val tiersDao: TiersDao,
    private val rewardsSynchronizedVersions: RewardsSynchronizedVersions
) : SynchronizableCatalogDataStore {
    override fun updateVersion(newVersion: Int) = rewardsSynchronizedVersions.setTiersCatalogVersion(newVersion)

    override fun replace(catalog: SynchronizableCatalog) {
        (catalog as? TiersCatalogApi)?.let {
            val tierEntityList = mapTiersToTierEntities(it)

            tiersDao.replace(tierEntityList)
        } ?: Timber.e("Expected TiersCatalog, was %s", catalog)
    }

    private fun mapTiersToTierEntities(it: TiersCatalogApi): List<TierEntity> {
        return it.tiers.map { entry ->
            val tier = entry.value

            TierEntity(
                entry.key,
                tier.smilesPerBrushing,
                tier.challengesNeeded,
                tier.pictureUrl,
                tier.rank,
                tier.creationDate,
                tier.message
            )
        }
    }
}
