/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.shorttask

import com.kolibree.android.synchronizator.SynchronizableItemBundle
import javax.inject.Inject

internal class ShortTaskSynchronizableCreator @Inject constructor(
    private val api: ShortTaskSynchronizableApi,
    private val datastore: ShortTaskDatastore,
    private val synchronizationKeyBuilder: ShortTaskSynchronizationKeyBuilder
) {

    fun create(): SynchronizableItemBundle = SynchronizableItemBundle(
        api = api,
        dataStore = datastore,
        conflictStrategy = ShortTaskConflictResolutionStrategy,
        synchronizeAccountKeyBuilder = synchronizationKeyBuilder
    )
}
