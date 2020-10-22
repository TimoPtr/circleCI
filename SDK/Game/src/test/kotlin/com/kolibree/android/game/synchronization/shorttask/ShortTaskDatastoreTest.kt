/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.shorttask

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ShortTask
import com.kolibree.android.game.shorttask.data.persistence.ShortTaskDao
import com.kolibree.android.game.shorttask.data.persistence.model.ShortTaskEntity
import com.kolibree.android.game.synchronization.shorttask.mapper.toPersistentEntities
import com.kolibree.android.game.synchronization.shorttask.mapper.toSynchronizableItem
import com.kolibree.android.game.synchronization.shorttask.model.ShortTaskSynchronizableItem
import com.kolibree.android.test.extensions.setFixedDate
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.util.UUID
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class ShortTaskDatastoreTest : BaseUnitTest() {

    private val dao: ShortTaskDao = mock()

    lateinit var datastore: ShortTaskDatastore

    override fun setup() {
        super.setup()

        datastore = ShortTaskDatastore(dao)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `insert with an unknown item throws`() {
        datastore.insert(mock())
    }

    @Test
    fun `insert insert the item into the DB`() {
        TrustedClock.setFixedDate()

        val item = ShortTaskSynchronizableItem(
            ShortTask.TEST_YOUR_ANGLE,
            1,
            TrustedClock.getNowOffsetDateTime().minusDays(1).toZonedDateTime(),
            TrustedClock.getNowOffsetDateTime().toZonedDateTime()
        )

        assertEquals(item.toPersistentEntities().toSynchronizableItem(), datastore.insert(item))

        verify(dao).insert(item.toPersistentEntities())
    }

    @Test
    fun `retrieve an item with id always returns null`() {
        assertNull(datastore.getByKolibreeId(0))
    }

    @Test
    fun `retrieve an item with uuid invokes dao`() {
        TrustedClock.setFixedDate()
        val uuid = mock<UUID>()
        val item = ShortTaskEntity(
            1,
            ShortTask.TEST_YOUR_ANGLE,
            TrustedClock.getNowOffsetDateTime(),
            uuid
        )
        whenever(dao.getByUuid(uuid)).thenReturn(item)
        assertEquals(item.toSynchronizableItem(), datastore.getByUuid(uuid))
        verify(dao).getByUuid(uuid)
    }

    @Test
    fun `delete an item with uuid invokes dao`() {
        TrustedClock.setFixedDate()
        val uuid = mock<UUID>()

        datastore.delete(uuid)

        verify(dao).delete(uuid)
    }

    @Test
    fun `datastore can handle only ShortTask`() {
        assertTrue(datastore.canHandle(ShortTaskSynchronizableItem(
            ShortTask.TEST_YOUR_ANGLE,
            1,
            TrustedClock.getNowOffsetDateTime().minusDays(1).toZonedDateTime(),
            TrustedClock.getNowOffsetDateTime().toZonedDateTime()
        )))

        assertFalse(datastore.canHandle(mock()))
    }
}
