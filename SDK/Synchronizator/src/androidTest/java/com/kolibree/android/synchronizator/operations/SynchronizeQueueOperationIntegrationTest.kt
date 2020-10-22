/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.operations

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.android.synchronizator.itemBundleConsumable
import com.android.synchronizator.itemConsumable
import com.android.synchronizator.localSynchronizableItem
import com.android.synchronizator.mockConflictResolutionToReturnRemote
import com.android.synchronizator.remoteSynchronizableItem
import com.android.synchronizator.synchronizableItemBundle
import com.android.synchronizator.synchronizableTrackingEntity
import com.android.synchronizator.synchronizeAccountKeyBuilder
import com.android.synchronizator.testItemDatastore
import com.kolibree.android.synchronizator.BundleConsumable
import com.kolibree.android.synchronizator.BundleConsumableBuilder
import com.kolibree.android.synchronizator.ConflictResolution
import com.kolibree.android.synchronizator.QueueOperationExecutor
import com.kolibree.android.synchronizator.SynchronizationBundles
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntityDao
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.UploadStatus
import com.kolibree.android.synchronizator.test.DaggerSynchronizatorTestComponent
import com.kolibree.android.synchronizator.test.TestQueueOperationExecutor
import com.kolibree.android.test.BaseInstrumentationTest
import com.nhaarman.mockitokotlin2.whenever
import java.util.UUID
import javax.inject.Inject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class SynchronizeQueueOperationIntegrationTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Inject
    lateinit var bundleConsumableBuilder: BundleConsumableBuilder

    @Inject
    lateinit var queueOperationExecutor: QueueOperationExecutor

    @Inject
    lateinit var trackingDao: SynchronizableTrackingEntityDao

    @Inject
    lateinit var syncOperation: SynchronizeQueueOperation

    override fun setUp() {
        super.setUp()

        DaggerSynchronizatorTestComponent.factory().create(context()).inject(this)

        trackingDao.truncate().blockingGet()
        SynchronizationBundles.clear()
    }

    /*
    Process updated ids
     */

    @Test
    fun whenSyncUpdatedIdsDontHaveLocalEquivalent_ThenInserttrackingEntityWithUploadStatusCompletedAndSynchronizableItem() {
        val updatedId = 56L
        prepareConsumable(listOf(updatedId))

        val remoteItem = remoteSynchronizableItem(kolibreeId = updatedId)
        whenever(itemBundle.api.get(updatedId)).thenReturn(remoteItem)

        val conflictResolution = ConflictResolution(
            remoteSynchronizable = remoteItem,
            localSynchronizable = null,
            resolvedSynchronizable = remoteItem

        )
        whenever(itemBundle.conflictStrategy.resolve(null, remoteItem))
            .thenReturn(conflictResolution)

        assertTrue(bundleDataStore.items().isEmpty())
        assertTrue(trackingDao.readAll().isEmpty())

        syncOperation.run()

        val trackingEntity = trackingDao.readAll().single()
        assertEquals(bundleKey, trackingEntity.bundleKey)
        assertEquals(UploadStatus.COMPLETED, trackingEntity.uploadStatus)
        assertFalse(trackingEntity.isDeletedLocally)

        assertEquals(1, bundleDataStore.items().size)

        val bundleEntity = bundleDataStore.getByUuid(trackingEntity.uuid)
        assertNotNull(bundleEntity)
        assertEquals(updatedId, bundleEntity.kolibreeId)
    }

    /*
    When
    - SyncUpdatedIdsHaveLocalEquivalentButNoTrackingEntity
    - ConflictResolvesThatRemoteShouldBeInserted

    Then
    - InserttrackingEntityWithUploadStatusCompletedAndUpdateSynchronizableItemWithUuid
     */
    @Test
    fun whenUpdatedIdsHaveNoTrackingEntity_thenInsertTrackingEntityWithUploadStatusCompletedAndUpdateSynchronizableItemWithUuid() {
        val updatedId = 56L
        prepareConsumable(listOf(updatedId))

        val localItem = localSynchronizableItem(kolibreeId = updatedId)
        bundleDataStore.insert(localItem)

        val remoteItem = remoteSynchronizableItem(
            kolibreeId = updatedId
        )
        whenever(itemBundle.api.get(updatedId)).thenReturn(remoteItem)

        mockConflictResolutionToReturnRemote(itemBundle)

        assertEquals(localItem, bundleDataStore.items().single())
        assertTrue(trackingDao.readAll().isEmpty())

        syncOperation.run()

        val trackingEntity = trackingDao.readAll().single()
        assertEquals(bundleKey, trackingEntity.bundleKey)
        assertEquals(UploadStatus.COMPLETED, trackingEntity.uploadStatus)
        assertFalse(trackingEntity.isDeletedLocally)

        assertEquals(1, bundleDataStore.items().size)

        val bundleEntity = bundleDataStore.getByUuid(trackingEntity.uuid)
        assertNotNull(bundleEntity)
        assertEquals(updatedId, bundleEntity.kolibreeId)
        assertEquals(trackingEntity.uuid, bundleEntity.uuid)
    }

    /*
    When
    - SyncUpdatedIdsHaveLocalEquivalentAndTrackingEntity
    - ConflictResolvesThatRemoteShouldBeInserted

    Then
    - InserttrackingEntityWithUploadStatusCompletedAndUpdateSynchronizableItemWithUuid
     */
    @Test
    fun whenUpdatedIdsHaveTrackingEntity_thenUploadTrackingEntityWithUploadStatusCompletedAndUpdateSynchronizableItemWithUuid() {
        val updatedId = 56L
        prepareConsumable(listOf(updatedId))

        val localtrackingEntity = synchronizableTrackingEntity(
            bundleKey = bundleKey
        )
        trackingDao.insert(localtrackingEntity)

        val localItem =
            localSynchronizableItem(kolibreeId = updatedId, uuid = localtrackingEntity.uuid)
        bundleDataStore.insert(localItem)

        val remoteItem = remoteSynchronizableItem(kolibreeId = updatedId)
        whenever(itemBundle.api.get(updatedId)).thenReturn(remoteItem)

        val conflictResolution = ConflictResolution(
            remoteSynchronizable = remoteItem,
            localSynchronizable = localItem,
            resolvedSynchronizable = remoteItem

        )
        whenever(itemBundle.conflictStrategy.resolve(localItem, remoteItem))
            .thenReturn(conflictResolution)

        assertEquals(localItem, bundleDataStore.items().single())
        assertEquals(localtrackingEntity, trackingDao.readAll().single())

        syncOperation.run()

        val trackingEntity = trackingDao.readAll().single()
        assertEquals(bundleKey, trackingEntity.bundleKey)
        assertEquals(UploadStatus.COMPLETED, trackingEntity.uploadStatus)
        assertFalse(trackingEntity.isDeletedLocally)

        assertEquals(1, bundleDataStore.items().size)

        val bundleEntity = bundleDataStore.getByUuid(trackingEntity.uuid)
        assertNotNull(bundleEntity)
        assertEquals(updatedId, bundleEntity.kolibreeId)
        assertEquals(trackingEntity.uuid, bundleEntity.uuid)
    }

    /*
    Process deleted ids
     */

    @Test
    fun whenSyncDeletedIdsHaveLocalEquivalentButNoTrackingEntity_ThenDeleteSynchronizableItem() {
        val deletedId = 56L
        prepareConsumable(deletedIds = listOf(deletedId))

        val localItem = localSynchronizableItem(kolibreeId = deletedId, uuid = UUID.randomUUID())
        bundleDataStore.insert(localItem)

        assertEquals(localItem, bundleDataStore.items().single())
        assertTrue(trackingDao.readAll().isEmpty())

        syncOperation.run()

        assertTrue(trackingDao.readAll().isEmpty())
        assertTrue(bundleDataStore.items().isEmpty())
    }

    @Test
    fun whenSyncDeletedIdsHaveLocalEquivalentAndTrackingEntity_ThenDeleteSynchronizableItemAndTheTrackingEntity() {
        val deletedId = 56L
        prepareConsumable(deletedIds = listOf(deletedId))

        val trackingEntity = synchronizableTrackingEntity(bundleKey = bundleKey)
        trackingDao.insert(trackingEntity)

        val localItem = localSynchronizableItem(kolibreeId = deletedId, uuid = trackingEntity.uuid)
        bundleDataStore.insert(localItem)

        assertEquals(localItem, bundleDataStore.items().single())
        assertEquals(trackingEntity, trackingDao.readAll().single())

        syncOperation.run()

        assertTrue(trackingDao.readAll().isEmpty())
        assertTrue(bundleDataStore.items().isEmpty())
    }

    /*
    Pending create
     */
    @Test
    fun withPendingLocalItems_runsCreateOrEditOperationOnItems() {
        prepareConsumable()

        val trackingEntity = synchronizableTrackingEntity(
            bundleKey = bundleKey,
            uploadStatus = UploadStatus.PENDING
        )
        trackingDao.insert(trackingEntity)

        val localItem = localSynchronizableItem(uuid = trackingEntity.uuid)
        bundleDataStore.insert(localItem)

        bundleDataStore.enableCanHandle()

        testOperationExecutor().enableDryRun()

        assertTrue(testOperationExecutor().operations().isEmpty())

        syncOperation.run()

        val operation =
            testOperationExecutor().operations().single() as RemoteCreateOrEditQueueOperation

        val updatedtrackingEntity = trackingDao.readAll().single()
        assertEquals(bundleKey, updatedtrackingEntity.bundleKey)
        assertEquals(trackingEntity.uuid, updatedtrackingEntity.uuid)

        /*
        tracking entity is now flagged as IN_PROGRESS. We didn't run the RemoteCreateOrEditOperation
         */
        assertEquals(UploadStatus.IN_PROGRESS, updatedtrackingEntity.uploadStatus)
        assertFalse(updatedtrackingEntity.isDeletedLocally)

        val expectedWrapper = SynchronizableItemWrapper(
            trackingEntity = updatedtrackingEntity,
            synchronizableItem = localItem
        )
        assertEquals(expectedWrapper, operation.wrapper)
    }

    /*
    Utils
     */

    private fun testOperationExecutor() = (queueOperationExecutor as TestQueueOperationExecutor)

    private fun prepareConsumable(
        updatedIds: List<Long> = listOf(),
        deletedIds: List<Long> = listOf()
    ) {
        val itemConsumable = itemBundleConsumable(
            synchronizableItemBundle = itemBundle,
            itemConsumable = itemConsumable(updatedIds = updatedIds, deletedIds = deletedIds)
        )
        whenever(bundleConsumableBuilder.buildBundleConsumables())
            .thenReturn(listOf(itemConsumable))

        SynchronizationBundles.register(itemConsumable.itemBundle)
    }

    private fun withBundleConsumables(consumables: List<BundleConsumable> = listOf()) {
        whenever(bundleConsumableBuilder.buildBundleConsumables()).thenReturn(consumables)
    }

    private val bundleKey = SynchronizableKey.PRIZES_CATALOG
    private val bundleDataStore = testItemDatastore()
    private val itemBundle = synchronizableItemBundle(
        dataStore = bundleDataStore,
        synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(key = bundleKey)
    )
}
