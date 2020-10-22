/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.prizes

import com.kolibree.android.synchronizator.SynchronizableCatalogBundle
import com.kolibree.android.synchronizator.models.BundleCreator
import javax.inject.Inject

internal class PrizesSynchronizableCatalogBundleCreator
@Inject constructor(
    private val api: PrizesSynchronizableCatalogApi,
    private val datastore: PrizesSynchronizableCatalogDatastore,
    private val prizesSynchronizationKeyBuilder: PrizesSynchronizationKeyBuilder
) : BundleCreator {
    override fun create(): SynchronizableCatalogBundle {
        return SynchronizableCatalogBundle(
            api = api,
            dataStore = datastore,
            synchronizeAccountKeyBuilder = prizesSynchronizationKeyBuilder
        )
    }
}
