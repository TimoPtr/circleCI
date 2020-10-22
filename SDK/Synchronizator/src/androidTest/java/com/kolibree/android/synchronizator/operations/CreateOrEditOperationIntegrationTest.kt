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
import com.android.synchronizator.TestSynchronizableItem
import com.android.synchronizator.mockConflictResolutionToReturnRemote
import com.android.synchronizator.remoteSynchronizableItem
import com.android.synchronizator.synchronizableItem
import com.android.synchronizator.synchronizableItemBundle
import com.android.synchronizator.synchronizableTrackingEntity
import com.android.synchronizator.synchronizeAccountKeyBuilder
import com.android.synchronizator.testItemDatastore
import com.kolibree.android.synchronizator.SynchronizationBundles
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntityDao
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.UploadStatus
import com.kolibree.android.synchronizator.test.DaggerSynchronizatorTestComponent
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.utils.failearly.retrofitError
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import javax.inject.Inject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import retrofit2.HttpException

internal class CreateOrEditOperationIntegrationTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Inject
    lateinit var trackingDao: SynchronizableTrackingEntityDao

    @Inject
    lateinit var operation: CreateOrEditOperation

    override fun setUp() {
        super.setUp()

        DaggerSynchronizatorTestComponent.factory().create(context()).inject(this)

        trackingDao.truncate().blockingGet()
        SynchronizationBundles.clear()
    }

    /*
    Create
     */

    @Test
    fun whenCreateAndApiUploadSucceeds_thenCreatesAWrapperWithUploadStatusCompletedAndUpdatesBundleItemWithUuid() {
        SynchronizationBundles.register(itemBundle)

        val localItem = synchronizableItem()

        assertTrue(bundleDataStore.items().isEmpty())
        assertTrue(trackingDao.readAll().isEmpty())

        val kolibreeId = 543L
        whenever(itemBundle.api.createOrEdit(any()))
            .thenReturn(remoteSynchronizableItem(kolibreeId = kolibreeId))

        mockConflictResolutionToReturnRemote(itemBundle)

        operation.run(localItem).test().assertComplete()

        val trackingEntity = trackingDao.readAll().single()
        assertEquals(bundleKey, trackingEntity.bundleKey)
        assertEquals(UploadStatus.COMPLETED, trackingEntity.uploadStatus)
        assertFalse(trackingEntity.isDeletedLocally)

        val updatedItem = bundleDataStore.getByUuid(trackingEntity.uuid)
        assertNotNull(updatedItem)
        assertEquals(kolibreeId, updatedItem.kolibreeId)
    }

    @Test
    fun whenCreateAndApiUploadFailsWithRecoverableException_thenCreatesAWrapperWithUploadStatusPendingAndUpdatesBundleItemWithUuid() {
        SynchronizationBundles.register(itemBundle)

        val localItem = synchronizableItem()

        assertTrue(bundleDataStore.items().isEmpty())
        assertTrue(trackingDao.readAll().isEmpty())

        whenever(itemBundle.api.createOrEdit(any()))
            .thenThrow(HttpException(retrofitError<TestSynchronizableItem>(500)))

        mockConflictResolutionToReturnRemote(itemBundle)

        operation.run(localItem).test().assertComplete()

        val trackingEntity = trackingDao.readAll().single()
        assertEquals(bundleKey, trackingEntity.bundleKey)
        assertEquals(UploadStatus.PENDING, trackingEntity.uploadStatus)
        assertFalse(trackingEntity.isDeletedLocally)

        val updatedItem = bundleDataStore.getByUuid(trackingEntity.uuid)
        assertNotNull(updatedItem)
        assertNull(updatedItem.kolibreeId)
    }

    @Test
    fun whenCreateAndApiUploadFailsWithUnrecoverableException_thenDeletesLocalItem() {
        SynchronizationBundles.register(itemBundle)

        val localItem = synchronizableItem()

        assertTrue(bundleDataStore.items().isEmpty())
        assertTrue(trackingDao.readAll().isEmpty())

        whenever(itemBundle.api.createOrEdit(any()))
            .thenThrow(HttpException(retrofitError<TestSynchronizableItem>(404)))

        mockConflictResolutionToReturnRemote(itemBundle)

        operation.run(localItem).test().assertComplete()

        assertTrue(bundleDataStore.items().isEmpty())
        assertTrue(trackingDao.readAll().isEmpty())
    }

    /*
    Edit
     */

    @Test
    fun whenEditWithCompletedWrapperEntityAndApiUploadSucceeds_thenWrapperAndItemRemainUnchanged() {
        SynchronizationBundles.register(itemBundle)

        val trackingEntity = synchronizableTrackingEntity(bundleKey = bundleKey)
        trackingDao.insert(trackingEntity)

        val kolibreeId = 543L
        val localItem = synchronizableItem(uuid = trackingEntity.uuid, kolibreeId = kolibreeId)

        assertTrue(bundleDataStore.items().isEmpty())
        assertEquals(trackingEntity, trackingDao.readAll().single())

        whenever(itemBundle.api.createOrEdit(localItem))
            .thenReturn(remoteSynchronizableItem(kolibreeId = kolibreeId))

        mockConflictResolutionToReturnRemote(itemBundle)

        operation.run(localItem).test().assertComplete()

        assertEquals(trackingEntity, trackingDao.readAll().single())

        val updatedItem = bundleDataStore.getByUuid(trackingEntity.uuid)
        assertNotNull(updatedItem)
        assertEquals(kolibreeId, updatedItem.kolibreeId)
        assertEquals(1, bundleDataStore.items().size)

        verify(itemBundle.api).createOrEdit(localItem)
    }

    @Test
    fun whenEditWithCompletedWrapperEntityAndApiUploadFailsWithRecoverableError_thenWrapperHasUploadStatusPendingAndItemRemainsUnchanged() {
        SynchronizationBundles.register(itemBundle)

        val trackingEntity = synchronizableTrackingEntity(bundleKey = bundleKey)
        trackingDao.insert(trackingEntity)

        val kolibreeId = 543L
        val localItem = synchronizableItem(uuid = trackingEntity.uuid, kolibreeId = kolibreeId)

        assertTrue(bundleDataStore.items().isEmpty())
        assertEquals(trackingEntity, trackingDao.readAll().single())

        whenever(itemBundle.api.createOrEdit(localItem))
            .thenThrow(HttpException(retrofitError<TestSynchronizableItem>(500)))

        mockConflictResolutionToReturnRemote(itemBundle)

        operation.run(localItem).test().assertComplete()

        val newWrapperEntity = trackingDao.readAll().single()
        assertEquals(bundleKey, newWrapperEntity.bundleKey)
        assertEquals(UploadStatus.PENDING, newWrapperEntity.uploadStatus)
        assertFalse(newWrapperEntity.isDeletedLocally)

        val updatedItem = bundleDataStore.getByUuid(trackingEntity.uuid)
        assertNotNull(updatedItem)
        assertEquals(kolibreeId, updatedItem.kolibreeId)
        assertEquals(1, bundleDataStore.items().size)

        verify(itemBundle.api).createOrEdit(localItem)
    }

    @Test
    fun whenEditWithCompletedWrapperEntityAndApiUploadFailsWithUnrecoverableError_thenWrapperAndItemAreDeleted() {
        SynchronizationBundles.register(itemBundle)

        val trackingEntity = synchronizableTrackingEntity(bundleKey = bundleKey)
        trackingDao.insert(trackingEntity)

        val kolibreeId = 543L
        val localItem = synchronizableItem(uuid = trackingEntity.uuid, kolibreeId = kolibreeId)

        assertTrue(bundleDataStore.items().isEmpty())
        assertEquals(trackingEntity, trackingDao.readAll().single())

        whenever(itemBundle.api.createOrEdit(localItem))
            .thenThrow(HttpException(retrofitError<TestSynchronizableItem>(404)))

        mockConflictResolutionToReturnRemote(itemBundle)

        operation.run(localItem).test().assertComplete()

        assertTrue(bundleDataStore.items().isEmpty())
        assertTrue(trackingDao.readAll().isEmpty())
    }

    /*
    Utils
     */

    private val bundleKey = SynchronizableKey.CHALLENGE_CATALOG
    private val bundleDataStore = testItemDatastore(canHandle = true)
    private val itemBundle = synchronizableItemBundle(
        dataStore = bundleDataStore,
        synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(key = bundleKey)
    )
}
