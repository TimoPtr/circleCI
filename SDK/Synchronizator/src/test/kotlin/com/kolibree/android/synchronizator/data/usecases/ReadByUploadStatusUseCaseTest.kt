/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.data.usecases

import com.android.synchronizator.synchronizableItemBundle
import com.android.synchronizator.synchronizableTrackingEntity
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.data.SynchronizableTrackingEntityDataStore
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntity
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.UploadStatus
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class ReadByUploadStatusUseCaseTest : BaseUnitTest() {
    private val dataStore: SynchronizableTrackingEntityDataStore = mock()

    private val useCase = ReadByUploadStatusUseCase(dataStore)

    @Test
    fun `when datastore returns empty list, we return empty list`() {
        mockDataStore()

        assertTrue(useCase.readByUploadStatus(bundle, *defaultUploadStatus).isEmpty())
    }

    @Test
    fun `when datastore returns single SynchronizabletrackingEntity, we create a SynchronizableItemWrapper instance that includes the SynchronizableItem from the bundle dataStore`() {
        val entity = synchronizableTrackingEntity()
        mockDataStore(listOf(entity))

        val expectedItem = mock<SynchronizableItem>()
        whenever(bundle.dataStore.getByUuid(entity.uuid))
            .thenReturn(expectedItem)

        val itemWrapper = useCase.readByUploadStatus(bundle, *defaultUploadStatus).single()

        val expectedWrapper = SynchronizableItemWrapper(
            trackingEntity = entity,
            synchronizableItem = expectedItem
        )
        assertEquals(expectedWrapper, itemWrapper)
    }

    @Test
    fun `when datastore returns multiple SynchronizabletrackingEntity, we create a SynchronizableItemWrapper for each returned Entity`() {
        val entity1 = synchronizableTrackingEntity()
        val entity2 = synchronizableTrackingEntity()
        mockDataStore(listOf(entity1, entity2))

        val expectedItem1 = mock<SynchronizableItem>()
        whenever(bundle.dataStore.getByUuid(entity1.uuid))
            .thenReturn(expectedItem1)

        val expectedItem2 = mock<SynchronizableItem>()
        whenever(bundle.dataStore.getByUuid(entity2.uuid))
            .thenReturn(expectedItem2)

        val itemWrappers = useCase.readByUploadStatus(bundle, *defaultUploadStatus)

        assertEquals(2, itemWrappers.size)

        val expectedWrapper1 = SynchronizableItemWrapper(
            trackingEntity = entity1,
            synchronizableItem = expectedItem1
        )
        val expectedWrapper2 = SynchronizableItemWrapper(
            trackingEntity = entity2,
            synchronizableItem = expectedItem2
        )

        assertTrue(itemWrappers.contains(expectedWrapper1))
        assertTrue(itemWrappers.contains(expectedWrapper2))
    }

    /*
    Utils
     */
    private fun mockDataStore(entities: List<SynchronizableTrackingEntity> = listOf()) {
        whenever(dataStore.readByUploadStatus(any(), any()))
            .thenReturn(entities)
    }

    private val bundle = synchronizableItemBundle()
    private val defaultUploadStatus = arrayOf(UploadStatus.PENDING, UploadStatus.IN_PROGRESS)
}
