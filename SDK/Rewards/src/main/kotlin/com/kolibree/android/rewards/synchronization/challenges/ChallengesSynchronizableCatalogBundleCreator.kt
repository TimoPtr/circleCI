package com.kolibree.android.rewards.synchronization.challenges

import com.kolibree.android.synchronizator.SynchronizableCatalogBundle
import com.kolibree.android.synchronizator.models.BundleCreator
import javax.inject.Inject

internal class ChallengesSynchronizableCatalogBundleCreator
@Inject constructor(
    private val api: ChallengesSynchronizableCatalogApi,
    private val datastore: ChallengesSynchronizableCatalogDatastore,
    private val challengesSynchronizationKeyBuilder: ChallengesSynchronizationKeyBuilder
) : BundleCreator {
    override fun create(): SynchronizableCatalogBundle {
        return SynchronizableCatalogBundle(
            api = api,
            dataStore = datastore,
            synchronizeAccountKeyBuilder = challengesSynchronizationKeyBuilder
        )
    }
}
