/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.personalchallenge

import com.kolibree.android.rewards.synchronization.personalchallenge.model.ProfilePersonalChallengeSynchronizableItem
import com.kolibree.android.synchronizator.ConflictResolution
import com.kolibree.android.synchronizator.ConflictStrategy
import com.kolibree.android.synchronizator.models.SynchronizableItem

internal object ProfilePersonalChallengeConflictResolutionStrategy : ConflictStrategy {

    override fun resolve(
        localSynchronizable: SynchronizableItem?,
        remoteSynchronizable: SynchronizableItem
    ) = ConflictResolution(
        localSynchronizable = localSynchronizable,
        remoteSynchronizable = remoteSynchronizable,
        resolvedSynchronizable = resolveConflicts(localSynchronizable, remoteSynchronizable)
    )

    private fun resolveConflicts(
        local: SynchronizableItem?,
        remote: SynchronizableItem
    ): SynchronizableItem? = when {
        remote !is ProfilePersonalChallengeSynchronizableItem -> local
        local == null -> remote
        local !is ProfilePersonalChallengeSynchronizableItem -> remote
        else -> resolvePersonalChallengeConflicts(local, remote)
    }

    private fun resolvePersonalChallengeConflicts(
        local: ProfilePersonalChallengeSynchronizableItem,
        remote: ProfilePersonalChallengeSynchronizableItem
    ): SynchronizableItem? = when {
        local.backendId == remote.backendId -> when {
            local.updatedAt < remote.updatedAt -> remote
            local.challenge.progress < remote.challenge.progress -> remote
            else -> local
        }
        local.createdAt < remote.createdAt -> remote
        else -> local
    }
}
