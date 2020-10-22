/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profilesmileshistory

import com.kolibree.android.synchronizator.SynchronizableReadOnlyBundle
import com.kolibree.android.synchronizator.models.BundleCreator
import javax.inject.Inject

internal class ProfileSmilesHistorySynchronizableReadOnlyCreator
@Inject constructor(
    private val api: ProfileSmilesHistorySynchronizableReadOnlyApi,
    private val datastore: ProfileSmilesHistorySynchronizableReadOnlyDatastore,
    private val challengeProgressSynchronizationKeyBuilder: ProfileSmilesHistorySynchronizationKeyBuilder
) : BundleCreator {
    override fun create(): SynchronizableReadOnlyBundle {
        return SynchronizableReadOnlyBundle(
            api = api,
            dataStore = datastore,
            synchronizeAccountKeyBuilder = challengeProgressSynchronizationKeyBuilder
        )
    }
}
