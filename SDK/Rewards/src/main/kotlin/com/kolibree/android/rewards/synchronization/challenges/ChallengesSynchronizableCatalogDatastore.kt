package com.kolibree.android.rewards.synchronization.challenges

import com.kolibree.android.rewards.persistence.CategoriesDao
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.synchronizator.SynchronizableCatalogDataStore
import com.kolibree.android.synchronizator.models.SynchronizableCatalog
import javax.inject.Inject
import timber.log.Timber

internal class ChallengesSynchronizableCatalogDatastore
@Inject constructor(
    private val categoryDao: CategoriesDao,
    private val rewardsSynchronizedVersions: RewardsSynchronizedVersions
) : SynchronizableCatalogDataStore {
    override fun updateVersion(newVersion: Int) = rewardsSynchronizedVersions.setChallengesCatalogVersion(newVersion)

    override fun replace(catalog: SynchronizableCatalog) {
        (catalog as? ChallengesCatalogApi)?.let {
            categoryDao.replace(it.categories)
        } ?: Timber.e("Expected ChallengesCatalogApi, was %s", catalog)
    }
}
