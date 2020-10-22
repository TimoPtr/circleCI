/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.prizes

import com.kolibree.android.rewards.models.PrizeEntity
import com.kolibree.android.rewards.persistence.PrizeDao
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.synchronizator.SynchronizableCatalogDataStore
import com.kolibree.android.synchronizator.models.SynchronizableCatalog
import javax.inject.Inject
import timber.log.Timber

internal class PrizesSynchronizableCatalogDatastore
@Inject constructor(
    private val prizeDao: PrizeDao,
    private val rewardsSynchronizedVersions: RewardsSynchronizedVersions
) : SynchronizableCatalogDataStore {
    override fun updateVersion(newVersion: Int) = rewardsSynchronizedVersions.setPrizesVersion(newVersion)

    override fun replace(catalog: SynchronizableCatalog) {
        (catalog as? PrizesCatalogApi)?.let {
            prizeDao.replace(mapToPrizeEntities(it))
        } ?: Timber.e("Expected PrizesCatalogApi, was %s", catalog)
    }

    private fun mapToPrizeEntities(it: PrizesCatalogApi): List<PrizeEntity> =
        it.prizes.map { prizeApi ->
            prizeApi.details.map { prizeDetails ->
                PrizeEntity(
                    id = prizeDetails.rewardsId,
                    category = prizeApi.category,
                    description = prizeDetails.description,
                    title = prizeDetails.title,
                    creationTime = prizeDetails.creationDate,
                    smilesRequired = prizeDetails.smilesRequired,
                    pictureUrl = prizeDetails.pictureUrl,
                    company = prizeDetails.company,
                    purchasable = prizeDetails.purchasable,
                    voucherDiscount = prizeDetails.voucherDiscount
                )
            }.toMutableList()
        }.flatten()
}
