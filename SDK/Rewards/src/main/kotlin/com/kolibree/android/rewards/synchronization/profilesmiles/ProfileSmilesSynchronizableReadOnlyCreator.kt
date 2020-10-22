/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profilesmiles

import com.kolibree.android.synchronizator.SynchronizableReadOnlyBundle
import com.kolibree.android.synchronizator.models.BundleCreator
import javax.inject.Inject

internal class ProfileSmilesSynchronizableReadOnlyCreator
@Inject constructor(
    private val api: ProfileSmilesSynchronizableReadOnlyApi,
    private val datastore: ProfileSmilesSynchronizableReadOnlyDatastore,
    private val challengeProgressSynchronizationKeyBuilder: ProfileSmilesSynchronizationKeyBuilder
) : BundleCreator {
    override fun create(): SynchronizableReadOnlyBundle {
        return SynchronizableReadOnlyBundle(
            api = api,
            dataStore = datastore,
            synchronizeAccountKeyBuilder = challengeProgressSynchronizationKeyBuilder
        )
    }
}
