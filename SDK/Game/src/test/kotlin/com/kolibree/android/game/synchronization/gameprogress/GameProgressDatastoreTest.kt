/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.gameprogress

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.gameprogress.data.persistence.GameProgressDao
import com.kolibree.android.game.gameprogress.data.persistence.model.GameProgressEntity
import com.kolibree.android.game.gameprogress.domain.model.GameProgress
import com.kolibree.android.game.synchronization.GameSynchronizedVersions
import com.kolibree.android.game.synchronization.gameprogress.model.ProfileGameProgressSynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.util.UUID
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class GameProgressDatastoreTest : BaseUnitTest() {

    private lateinit var datastore: GameProgressDatastore

    private val dao = mock<GameProgressDao>()
    private val gameSynchronizedVersions = mock<GameSynchronizedVersions>()

    override fun setup() {
        super.setup()
        datastore = spy(GameProgressDatastore(dao, gameSynchronizedVersions))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `insert throws IllegalArgumentException when synchronizable is not ProfileGameProgressSynchronizableItem`() {
        val item = mock<SynchronizableItem>()

        datastore.insert(item)
    }

    @Test
    fun `insert invokes insertInternal when synchronizable is not ProfileGameProgressSynchronizableItem`() {
        val item = mock<ProfileGameProgressSynchronizableItem>()
        doReturn(mock<SynchronizableItem>()).whenever(datastore).insertInternal(item)

        datastore.insert(item)

        verify(datastore).insertInternal(item)
    }

    @Test
    fun `insertInternal returns SynchronizableItem and invokes dao replace`() {
        val item = ProfileGameProgressSynchronizableItem(
            1,
            listOf(GameProgress("hell", "dd", TrustedClock.getNowZonedDateTimeUTC()))
        )

        doNothing().whenever(dao).replaceEntities(any(), any())

        datastore.insertInternal(item)

        verify(dao).replaceEntities(eq(1), any())
    }

    @Test(expected = IllegalStateException::class)
    fun `insertInternal throws IllegalStateException if GameProgress list is empty`() {
        val item = ProfileGameProgressSynchronizableItem(1, emptyList())

        doNothing().whenever(dao).replaceEntities(any(), any())

        datastore.insertInternal(item)
    }

    @Test
    fun `getByKolibreeId invokes dao getGameProgressForProfile`() {
        whenever(dao.getGameProgressEntitiesForProfile(1)).thenReturn(emptyList())

        assertNull(datastore.getByKolibreeId(1))

        verify(dao).getGameProgressEntitiesForProfile(1)
    }

    @Test
    fun `getByUuid invokes dao getByUuid`() {
        val uuid = mock<UUID>()
        val progress = listOf(GameProgressEntity(1, "a", "b", TrustedClock.getNowZonedDateTimeUTC()))
        whenever(dao.getEntitiesByUuid(uuid)).thenReturn(progress)

        datastore.getByUuid(uuid)
    }

    @Test(expected = IllegalStateException::class)
    fun `getByUuid throws when dao returns empty`() {
        val uuid = mock<UUID>()
        whenever(dao.getEntitiesByUuid(uuid)).thenReturn(emptyList())

        datastore.getByUuid(uuid)
    }

    @Test
    fun `delete invokes dao truncateForUuid`() {
        val uuid = mock<UUID>()
        doNothing().whenever(dao).truncateForUuid(uuid)
        datastore.delete(uuid)

        verify(dao).truncateForUuid(uuid)
    }

    @Test
    fun `updateVersion invokes gameSynchronizedVersions setGameProgressVersion`() {
        val version = 1
        doNothing().whenever(gameSynchronizedVersions).setGameProgressVersion(version)
        datastore.updateVersion(version)

        verify(gameSynchronizedVersions).setGameProgressVersion(version)
    }

    @Test
    fun `canHandle returns true when synchronizable is ProfileGameProgressSynchronizableItem`() {
        val item = mock<ProfileGameProgressSynchronizableItem>()
        assertTrue(datastore.canHandle(item))
    }

    @Test
    fun `canHandle returns false when synchronizable is not ProfileGameProgressSynchronizableItem`() {
        val item = mock<SynchronizableItem>()
        assertFalse(datastore.canHandle(item))
    }
}
