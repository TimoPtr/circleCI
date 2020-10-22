/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.data

import com.android.synchronizator.synchronizableItem
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntity
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntityDao
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.util.UUID
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Test

internal class SynchronizableTrackingEntityDataStoreTest : BaseUnitTest() {
    private val dao: SynchronizableTrackingEntityDao = mock()

    private val dataStore = SynchronizableTrackingEntityDataStore(dao)

    @Test
    fun `when item has null uuid and dao has no match, we insert & return a new Entity instance with non-null uuid`() {
        val item = synchronizableItem(uuid = null)
        val key = SynchronizableKey.PROFILES

        whenever(dao.read(eq(key), any())).thenReturn(null)

        val returnedInstance = dataStore.fromSynchronizableItem(item, key)

        assertNotNull(returnedInstance)
        assertNotNull(returnedInstance.uuid)

        verify(dao).insert(returnedInstance)
    }

    @Test
    fun `when item has uuid and dao has no match, we insert & return a new Entity instance with the same uuid`() {
        val expectedUuid = UUID.randomUUID()
        val item = synchronizableItem(uuid = expectedUuid)
        val key = SynchronizableKey.PROFILES

        whenever(dao.read(key, expectedUuid)).thenReturn(null)

        val returnedInstance = dataStore.fromSynchronizableItem(item, key)

        assertNotNull(returnedInstance)
        assertEquals(expectedUuid, returnedInstance.uuid)

        verify(dao).insert(returnedInstance)
    }

    @Test
    fun `when item has uuid and dao has match, we never insert & return the Entity instance from the dao`() {
        val expectedUuid = UUID.randomUUID()
        val item = synchronizableItem(uuid = expectedUuid)
        val key = SynchronizableKey.PROFILES

        val expectedInstance = mock<SynchronizableTrackingEntity>()
        whenever(dao.read(key, expectedUuid)).thenReturn(expectedInstance)

        val returnedInstance = dataStore.fromSynchronizableItem(item, key)

        assertEquals(expectedInstance, returnedInstance)

        verify(dao, never()).insert(any())
    }
}
