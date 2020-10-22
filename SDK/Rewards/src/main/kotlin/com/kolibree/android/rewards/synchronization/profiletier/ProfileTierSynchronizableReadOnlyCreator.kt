/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profiletier

import com.kolibree.android.synchronizator.SynchronizableReadOnlyBundle
import com.kolibree.android.synchronizator.models.BundleCreator
import javax.inject.Inject

internal class ProfileTierSynchronizableReadOnlyCreator
@Inject constructor(
    private val api: ProfileTierSynchronizableReadOnlyApi,
    private val datastore: ProfileTierSynchronizableReadOnlyDatastore,
    private val challengeProgressSynchronizationKeyBuilder: ProfileTierSynchronizationKeyBuilder
) : BundleCreator {
    override fun create(): SynchronizableReadOnlyBundle {
        return SynchronizableReadOnlyBundle(
            api = api,
            dataStore = datastore,
            synchronizeAccountKeyBuilder = challengeProgressSynchronizationKeyBuilder
        )
    }
}
