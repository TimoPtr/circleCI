/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.lifetimesmiles

import com.kolibree.android.synchronizator.Bundle
import com.kolibree.android.synchronizator.SynchronizableReadOnlyBundle
import com.kolibree.android.synchronizator.models.BundleCreator
import javax.inject.Inject

internal class LifetimeSmilesSynchronizableReadOnlyCreator
@Inject constructor(
    private val api: LifetimeSmilesSynchronizableReadOnlyApi,
    private val datastore: LifetimeSmilesSynchronizableReadOnlyDatastore,
    private val challengeProgressSynchronizationKeyBuilder: LifetimeSmilesSynchronizationKeyBuilder
) : BundleCreator {
    override fun create(): Bundle {
        return SynchronizableReadOnlyBundle(
            api = api,
            dataStore = datastore,
            synchronizeAccountKeyBuilder = challengeProgressSynchronizationKeyBuilder
        )
    }
}
