/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.gameprogress

import com.kolibree.android.synchronizator.ConflictResolution
import com.kolibree.android.synchronizator.ConflictStrategy
import com.kolibree.android.synchronizator.models.SynchronizableItem

internal object GameProgressConflictResolutionStrategy : ConflictStrategy {

    override fun resolve(
        localSynchronizable: SynchronizableItem?,
        remoteSynchronizable: SynchronizableItem
    ): ConflictResolution = ConflictResolution(
        localSynchronizable = localSynchronizable,
        remoteSynchronizable = remoteSynchronizable,
        resolvedSynchronizable = resolveConflicts(localSynchronizable, remoteSynchronizable)
    )

    private fun resolveConflicts(
        local: SynchronizableItem?,
        remote: SynchronizableItem
    ): SynchronizableItem? = remote // Dummy implementation
}
