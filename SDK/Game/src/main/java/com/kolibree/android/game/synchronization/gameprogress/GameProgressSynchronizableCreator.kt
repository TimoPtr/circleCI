/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.gameprogress

import com.kolibree.android.synchronizator.SynchronizableItemBundle
import javax.inject.Inject

internal class GameProgressSynchronizableCreator @Inject constructor(
    private val api: GameProgressSynchronizableApi,
    private val datastore: GameProgressDatastore,
    private val synchronizationKeyBuilder: GameProgressSynchronizationKeyBuilder
) {

    fun create(): SynchronizableItemBundle = SynchronizableItemBundle(
        api = api,
        dataStore = datastore,
        conflictStrategy = GameProgressConflictResolutionStrategy,
        synchronizeAccountKeyBuilder = synchronizationKeyBuilder
    )
}
