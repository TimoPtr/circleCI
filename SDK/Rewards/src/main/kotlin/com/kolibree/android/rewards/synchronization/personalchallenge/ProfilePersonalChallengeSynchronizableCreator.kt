/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.personalchallenge

import com.kolibree.android.synchronizator.SynchronizableItemBundle
import com.kolibree.android.synchronizator.models.BundleCreator
import javax.inject.Inject

internal class ProfilePersonalChallengeSynchronizableCreator
@Inject constructor(
    private val api: ProfilePersonalChallengeSynchronizableApi,
    private val datastore: ProfilePersonalChallengeDatastore,
    private val synchronizationKeyBuilder: ProfilePersonalChallengeSynchronizationKeyBuilder
) : BundleCreator {
    override fun create(): SynchronizableItemBundle {
        return SynchronizableItemBundle(
            api = api,
            dataStore = datastore,
            conflictStrategy = ProfilePersonalChallengeConflictResolutionStrategy,
            synchronizeAccountKeyBuilder = synchronizationKeyBuilder
        )
    }
}
