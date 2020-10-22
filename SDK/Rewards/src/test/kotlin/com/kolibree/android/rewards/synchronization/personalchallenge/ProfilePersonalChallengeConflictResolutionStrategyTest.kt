/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.personalchallenge

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.rewards.synchronization.personalchallenge.builder.synchroItem
import com.kolibree.android.synchronizator.ConflictResolution
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.test.extensions.setFixedDate
import junit.framework.TestCase.assertEquals
import org.junit.Test

class ProfilePersonalChallengeConflictResolutionStrategyTest : BaseUnitTest() {

    private val resolutionStrategy = ProfilePersonalChallengeConflictResolutionStrategy

    override fun setup() {
        super.setup()
        TrustedClock.setFixedDate()
    }

    @Test
    fun `if both objects represents the same state, resolvedSynchronizable is local`() {
        val local = synchroItem(
            backendId = 1001,
            updatedAt = TrustedClock.getNowZonedDateTime().minusDays(4),
            progress = 10
        )
        val remote = synchroItem(
            backendId = 1001,
            updatedAt = TrustedClock.getNowZonedDateTime().minusDays(4),
            progress = 10
        )

        val expectedResolution = conflictResolution(local, remote, local)

        assertEquals(expectedResolution, resolutionStrategy.resolve(local, remote))
    }

    @Test
    fun `if backendId is different, resolvedSynchronizable chooses remote`() {
        val local = synchroItem(
            backendId = 1002,
            creationDate = TrustedClock.getNowZonedDateTime().minusDays(2)
        )
        val remote = synchroItem(
            backendId = 1001,
            creationDate = TrustedClock.getNowZonedDateTime().minusDays(1)
        )

        val expectedResolution = conflictResolution(local, remote, remote)

        assertEquals(expectedResolution, resolutionStrategy.resolve(local, remote))
    }

    @Test
    fun `if backendId is different, resolvedSynchronizable chooses local if it was created later`() {
        val local = synchroItem(
            backendId = 1002,
            creationDate = TrustedClock.getNowZonedDateTime().minusDays(1)
        )
        val remote = synchroItem(
            backendId = 1001,
            creationDate = TrustedClock.getNowZonedDateTime().minusDays(2)
        )

        val expectedResolution = conflictResolution(local, remote, local)

        assertEquals(expectedResolution, resolutionStrategy.resolve(local, remote))
    }

    @Test
    fun `if backendId is the same, resolvedSynchronizable chooses local if it was updated later`() {
        val local = synchroItem(
            backendId = 1001,
            updatedAt = TrustedClock.getNowZonedDateTime().minusDays(4)
        )
        val remote = synchroItem(
            backendId = 1001,
            updatedAt = TrustedClock.getNowZonedDateTime().minusDays(5)
        )

        val expectedResolution = conflictResolution(local, remote, local)

        assertEquals(expectedResolution, resolutionStrategy.resolve(local, remote))
    }

    @Test
    fun `if backendId is the same, resolvedSynchronizable chooses remote if it was updated later`() {
        val local = synchroItem(
            backendId = 1001,
            updatedAt = TrustedClock.getNowZonedDateTime().minusDays(5)
        )
        val remote = synchroItem(
            backendId = 1001,
            updatedAt = TrustedClock.getNowZonedDateTime().minusDays(4)
        )

        val expectedResolution = conflictResolution(local, remote, remote)

        assertEquals(expectedResolution, resolutionStrategy.resolve(local, remote))
    }

    @Test
    fun `if backendId and update date are the same, resolvedSynchronizable chooses remote if it has higher progress`() {
        val local = synchroItem(
            backendId = 1001,
            updatedAt = TrustedClock.getNowZonedDateTime().minusDays(4),
            progress = 12
        )
        val remote = synchroItem(
            backendId = 1001,
            updatedAt = TrustedClock.getNowZonedDateTime().minusDays(4),
            progress = 13
        )

        val expectedResolution = conflictResolution(local, remote, remote)

        assertEquals(expectedResolution, resolutionStrategy.resolve(local, remote))
    }

    @Test
    fun `if backendId and update date are the same, resolvedSynchronizable chooses local if it has higher progress`() {
        val local = synchroItem(
            backendId = 1001,
            updatedAt = TrustedClock.getNowZonedDateTime().minusDays(4),
            progress = 13
        )
        val remote = synchroItem(
            backendId = 1001,
            updatedAt = TrustedClock.getNowZonedDateTime().minusDays(4),
            progress = 12
        )

        val expectedResolution = conflictResolution(local, remote, local)

        assertEquals(expectedResolution, resolutionStrategy.resolve(local, remote))
    }

    /*
    Utils
     */

    private fun conflictResolution(
        local: SynchronizableItem?,
        remote: SynchronizableItem,
        resolved: SynchronizableItem?
    ): ConflictResolution {
        return ConflictResolution(local, remote, resolved)
    }
}
