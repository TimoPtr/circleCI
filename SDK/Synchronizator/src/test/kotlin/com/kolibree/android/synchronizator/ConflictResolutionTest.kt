/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator

import com.android.synchronizator.remoteSynchronizableItem
import com.android.synchronizator.synchronizableItem
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import java.util.UUID
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import junit.framework.TestCase.assertNull
import org.junit.Test

internal class ConflictResolutionTest : BaseUnitTest() {
    private lateinit var conflictResolution: ConflictResolution

    @Test
    fun `synchronizableForDatastore returns null if resolvedSynchronizable is equal to localSynchronizable`() {
        val localSynchronizable = synchronizableItem()

        conflictResolution = ConflictResolution(
            localSynchronizable = localSynchronizable,
            remoteSynchronizable = remoteSynchronizableItem(),
            resolvedSynchronizable = localSynchronizable
        )

        assertNull(conflictResolution.synchronizableForDatastore())
    }

    @Test
    fun `synchronizableForDatastore returns null if resolvedSynchronizable and localSynchronizable are null`() {
        conflictResolution = ConflictResolution(
            localSynchronizable = null,
            remoteSynchronizable = remoteSynchronizableItem(),
            resolvedSynchronizable = null
        )

        assertNull(conflictResolution.synchronizableForDatastore())
    }

    @Test
    fun `synchronizableForDatastore returns remoteSynchronizable if resolvedSynchronizable is equal to remoteSynchronizable`() {
        val localSynchronizable = synchronizableItem()
        val remoteSynchronizable = remoteSynchronizableItem()

        conflictResolution = ConflictResolution(
            localSynchronizable = localSynchronizable,
            remoteSynchronizable = remoteSynchronizable,
            resolvedSynchronizable = remoteSynchronizable
        )

        assertEquals(
            remoteSynchronizable.updateFromLocalInstance(localSynchronizable),
            conflictResolution.synchronizableForDatastore()
        )
    }

    @Test
    fun `synchronizableForDatastore returns resolvedSynchronizable if localSynchronizable is null and resolvedSynchronizable is not null`() {
        val remoteSynchronizable = remoteSynchronizableItem()

        val expectedResolvedSynchronizable = synchronizableItem()

        conflictResolution = ConflictResolution(
            localSynchronizable = null,
            remoteSynchronizable = remoteSynchronizable,
            resolvedSynchronizable = expectedResolvedSynchronizable
        )

        assertEquals(
            expectedResolvedSynchronizable,
            conflictResolution.synchronizableForDatastore()
        )
    }

    @Test
    fun `synchronizableForDatastore returns resolvedSynchronizable with null uuid if resolvedSynchronizable is different from localSynchronizable and localSynchronizable uuid is null`() {
        val localSynchronizable = synchronizableItem()
        val remoteSynchronizable = remoteSynchronizableItem()

        val expectedResolvedSynchronizable =
            synchronizableItem(
                updatedAt = TrustedClock.getNowZonedDateTime().minusDays(1)
            )

        conflictResolution = ConflictResolution(
            localSynchronizable = localSynchronizable,
            remoteSynchronizable = remoteSynchronizable,
            resolvedSynchronizable = expectedResolvedSynchronizable
        )

        assertEquals(
            expectedResolvedSynchronizable,
            conflictResolution.synchronizableForDatastore()
        )
    }

    @Test
    fun `synchronizableForDatastore returns resolvedSynchronizable sanitized with localSynchronizable's data`() {
        val expectedUuid = UUID.randomUUID()
        val expectedLocalId = 8796L
        val localSynchronizable =
            synchronizableItem(testLocalId = expectedLocalId, uuid = expectedUuid)
        val remoteSynchronizable = remoteSynchronizableItem()

        val resolvedSynchronizable = remoteSynchronizableItem()

        assertNotSame(expectedLocalId, resolvedSynchronizable.localId)

        conflictResolution = ConflictResolution(
            localSynchronizable = localSynchronizable,
            remoteSynchronizable = remoteSynchronizable,
            resolvedSynchronizable = resolvedSynchronizable
        )

        assertEquals(
            resolvedSynchronizable.copy(localId = expectedLocalId).withUuid(expectedUuid),
            conflictResolution.synchronizableForDatastore()
        )
    }

    /*
    synchronizableForBackend
     */

    @Test
    fun `synchronizableForBackend returns null if resolvedSynchronizable is equal to localSynchronizable`() {
        val remoteSynchronizable = remoteSynchronizableItem()

        conflictResolution = ConflictResolution(
            localSynchronizable = synchronizableItem(),
            remoteSynchronizable = remoteSynchronizable,
            resolvedSynchronizable = remoteSynchronizable
        )

        assertNull(conflictResolution.synchronizableForBackend())
    }

    @Test
    fun `synchronizableForBackend returns null if resolvedSynchronizable is null`() {
        conflictResolution = ConflictResolution(
            localSynchronizable = synchronizableItem(),
            remoteSynchronizable = remoteSynchronizableItem(),
            resolvedSynchronizable = null
        )

        assertNull(conflictResolution.synchronizableForBackend())
    }

    @Test
    fun `synchronizableForBackend returns localSynchronizable if resolvedSynchronizable is equal to localSynchronizable`() {
        val localSynchronizable = synchronizableItem()
        val remoteSynchronizable = remoteSynchronizableItem()

        conflictResolution = ConflictResolution(
            localSynchronizable = localSynchronizable,
            remoteSynchronizable = remoteSynchronizable,
            resolvedSynchronizable = localSynchronizable
        )

        assertEquals(localSynchronizable, conflictResolution.synchronizableForBackend())
    }

    @Test
    fun `synchronizableForBackend returns resolvedSynchronizable if resolvedSynchronizable is different from remoteSynchronizable`() {
        val remoteSynchronizable = remoteSynchronizableItem()

        val expectedResolvedSynchronizable =
            remoteSynchronizableItem(kolibreeId = remoteSynchronizable.kolibreeId!! + 1)

        conflictResolution = ConflictResolution(
            localSynchronizable = synchronizableItem(),
            remoteSynchronizable = remoteSynchronizable,
            resolvedSynchronizable = expectedResolvedSynchronizable
        )

        assertEquals(
            expectedResolvedSynchronizable,
            conflictResolution.synchronizableForBackend()
        )
    }
}
