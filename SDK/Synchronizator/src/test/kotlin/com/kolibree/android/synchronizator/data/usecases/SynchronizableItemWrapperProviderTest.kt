/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.data.usecases

import com.android.synchronizator.TestSynchronizableItem
import com.android.synchronizator.synchronizableItem
import com.android.synchronizator.synchronizableItemBundle
import com.android.synchronizator.synchronizableTrackingEntity
import com.android.synchronizator.testItemDatastore
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.data.SynchronizableTrackingEntityDataStore
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class SynchronizableItemWrapperProviderTest : BaseUnitTest() {
    private val entityDataStore: SynchronizableTrackingEntityDataStore = mock()
    private val provider = SynchronizableItemWrapperProvider(entityDataStore)

    @Test
    fun `when SynchronizableItem uuid is null, provide returns an updated version with the uuid equal to the returned SynchronizabletrackingEntity`() {
        val item = synchronizableItem(uuid = null)

        val storedEntity = synchronizableTrackingEntity()
        whenever(entityDataStore.fromSynchronizableItem(item, bundle.key()))
            .thenReturn(storedEntity)

        val expectedWrapper = SynchronizableItemWrapper(
            trackingEntity = storedEntity,
            synchronizableItem = item.withUuid(storedEntity.uuid)
        )

        val wrapper = provider.provide(item, bundle)

        assertEquals(expectedWrapper, wrapper)
    }

    @Test
    fun `provide returns the value from the bundle dataStore`() {
        val item = synchronizableItem(uuid = null, testLocalId = null)

        val storedEntity = synchronizableTrackingEntity()
        whenever(entityDataStore.fromSynchronizableItem(item, bundle.key()))
            .thenReturn(storedEntity)

        val returnedWrapper = provider.provide(item, bundle)

        val insertedItem = dataStore.items().single()

        val localItem = returnedWrapper.synchronizableItem as TestSynchronizableItem
        assertNotNull(localItem.localId)

        assertEquals(localItem, insertedItem)
    }

    @Test
    fun `when SynchronizableItem uuid is not null, provide never inserts an updated version`() {
        val storedEntity = synchronizableTrackingEntity()

        val item = synchronizableItem(uuid = storedEntity.uuid)

        whenever(entityDataStore.fromSynchronizableItem(item, bundle.key()))
            .thenReturn(storedEntity)

        provider.provide(item, bundle)

        assertTrue(dataStore.items().isEmpty())
    }

    @Test
    fun `when SynchronizableItem uuid is not null, provide returns a new SynchronizableItemWrapper with the entity and the same item`() {
        val storedEntity = synchronizableTrackingEntity()

        val item = synchronizableItem(uuid = storedEntity.uuid)

        whenever(entityDataStore.fromSynchronizableItem(item, bundle.key()))
            .thenReturn(storedEntity)

        val wrapper = provider.provide(item, bundle)

        val expectedWrapper = SynchronizableItemWrapper(
            synchronizableItem = item,
            trackingEntity = storedEntity
        )
        assertEquals(expectedWrapper, wrapper)
    }

    @Test
    fun `when SynchronizableItem uuid is null, provide returns a new SynchronizableItemWrapper the new entity and the item with the entity's uuid`() {
        val storedEntity = synchronizableTrackingEntity()

        val item = synchronizableItem(uuid = null)

        whenever(entityDataStore.fromSynchronizableItem(item, bundle.key()))
            .thenReturn(storedEntity)

        val wrapper = provider.provide(item, bundle)

        val expectedItem = item.withUuid(storedEntity.uuid)
        val expectedWrapper = SynchronizableItemWrapper(
            synchronizableItem = expectedItem,
            trackingEntity = storedEntity
        )
        assertEquals(expectedWrapper, wrapper)
    }

    /*
    Utils
     */

    private val dataStore = testItemDatastore()
    private val bundle = synchronizableItemBundle(dataStore = dataStore)
}
