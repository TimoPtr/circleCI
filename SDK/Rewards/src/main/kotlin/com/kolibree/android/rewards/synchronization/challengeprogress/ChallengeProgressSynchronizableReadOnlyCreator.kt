package com.kolibree.android.rewards.synchronization.challengeprogress

import com.kolibree.android.synchronizator.SynchronizableReadOnlyBundle
import com.kolibree.android.synchronizator.models.BundleCreator
import javax.inject.Inject

internal class ChallengeProgressSynchronizableReadOnlyCreator
@Inject constructor(
    private val api: ChallengeProgressSynchronizableReadOnlyApi,
    private val datastore: ChallengeProgressSynchronizableReadOnlyDatastore,
    private val challengeProgressSynchronizationKeyBuilder: ChallengeProgressSynchronizationKeyBuilder
) : BundleCreator {
    override fun create(): SynchronizableReadOnlyBundle {
        return SynchronizableReadOnlyBundle(
            api = api,
            dataStore = datastore,
            synchronizeAccountKeyBuilder = challengeProgressSynchronizationKeyBuilder
        )
    }
}
