/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.operations

import com.android.synchronizator.localSynchronizableItem
import com.android.synchronizator.mockedItemDatastore
import com.android.synchronizator.synchronizableItem
import com.android.synchronizator.synchronizableItemBundle
import com.android.synchronizator.synchronizableItemWrapper
import com.android.synchronizator.testItemDatastore
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.synchronizator.QueueOperationExecutor
import com.kolibree.android.synchronizator.SynchronizableItemDataStore
import com.kolibree.android.synchronizator.SynchronizationBundles
import com.kolibree.android.synchronizator.data.usecases.UpdateUploadStatusUseCase
import com.kolibree.android.synchronizator.insert
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.UploadStatus
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.extensions.setFixedDate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import junit.framework.TestCase.assertTrue
import org.junit.Test

class CreateOrEditOperationTest : BaseUnitTest() {
    private val updateUploadStatusUseCase: UpdateUploadStatusUseCase = mock()
    private val remoteCreateOrEditOperationFactory: RemoteCreateOrEditQueueOperationFactory = mock()
    private val queueOperationExecutor: QueueOperationExecutor = mock()

    private val operation = spy(
        CreateOrEditOperation(
            updateUploadStatusUseCase,
            remoteCreateOrEditOperationFactory,
            queueOperationExecutor
        )
    )

    override fun setup() {
        super.setup()

        SynchronizationBundles.clear()
    }

    override fun tearDown() {
        super.tearDown()

        SynchronizationBundles.clear()
    }

    @Test
    fun `run emits error if there's no bundle registered`() {
        operation.run(synchronizableItem()).test().assertError(AssertionError::class.java)
    }

    @Test
    fun `run emits error if there's no bundle registered for SynchronizableItem`() {
        SynchronizationBundles.register(
            synchronizableItemBundle(
                dataStore = testItemDatastore(canHandle = false)
            )
        )

        operation.run(synchronizableItem()).test()
            .assertError(AssertionError::class.java)
    }

    @Test
    fun `run subscribes to updateUploadStatusUseCase before inserting to bundle datastore`() {
        val bundle = synchronizableItemBundle(dataStore = mockedItemDatastore(canHandle = true))
        SynchronizationBundles.register(bundle)

        val item = synchronizableItem()

        val updateSingle = SingleSubject.create<SynchronizableItemWrapper>()
        whenever(updateUploadStatusUseCase.updateSingle(item, bundle, UploadStatus.IN_PROGRESS))
            .thenReturn(updateSingle)

        operation.run(item).test().assertNotComplete()

        assertTrue(updateSingle.hasObservers())

        verify(bundle.dataStore, never()).insert(any())
    }

    @Test
    fun `run inserts to bundle on updateUploadStatusUseCase success`() {
        TrustedClock.setFixedDate()

        val mockedDataStore = mockedItemDatastore(canHandle = true)
        val bundle = synchronizableItemBundle(dataStore = mockedDataStore)
        SynchronizationBundles.register(bundle)

        val item = synchronizableItem()

        val updateSingle = SingleSubject.create<SynchronizableItemWrapper>()
        whenever(updateUploadStatusUseCase.updateSingle(item, bundle, UploadStatus.IN_PROGRESS))
            .thenReturn(updateSingle)

        val expectedLocalSynchronizableItem = mockDataStoreInsert(mockedDataStore)

        val observer = operation.run(item).test().assertNotComplete()

        assertTrue(updateSingle.hasObservers())
        val wrapper = synchronizableItemWrapper()
        updateSingle.onSuccess(wrapper)

        observer.assertComplete().assertValue(expectedLocalSynchronizableItem)

        verify(mockedDataStore).insert(wrapper.touchUpdatedAt())
    }

    @Test
    fun `run enqueues RemoteAddEditQueueOperation on success`() {
        val bundle = synchronizableItemBundle(dataStore = mockedItemDatastore(canHandle = true))
        SynchronizationBundles.register(bundle)

        val item = synchronizableItem()

        val wrapper = synchronizableItemWrapper()
        whenever(updateUploadStatusUseCase.updateSingle(item, bundle, UploadStatus.IN_PROGRESS))
            .thenReturn(Single.just(wrapper))

        val expectedQueueOperation = mock<RemoteCreateOrEditQueueOperation>()
        whenever(remoteCreateOrEditOperationFactory.create(wrapper))
            .thenReturn(expectedQueueOperation)

        mockDataStoreInsert(bundle.dataStore)

        operation.run(item).test().assertComplete()

        verify(queueOperationExecutor).enqueue(expectedQueueOperation)
    }

    @Test
    fun `run never enqueues RemoteAddEditQueueOperation on updateSingle error`() {
        val bundle = synchronizableItemBundle(dataStore = testItemDatastore(canHandle = true))
        SynchronizationBundles.register(bundle)

        val item = synchronizableItem()

        whenever(updateUploadStatusUseCase.updateSingle(item, bundle, UploadStatus.IN_PROGRESS))
            .thenReturn(Single.error(TestForcedException()))

        operation.run(item).test().assertError(TestForcedException::class.java)

        verifyNoMoreInteractions(queueOperationExecutor)
    }

    @Test
    fun `run never enqueues RemoteAddEditQueueOperation on bundle datastore error`() {
        val bundle = synchronizableItemBundle(dataStore = mockedItemDatastore(canHandle = true))
        SynchronizationBundles.register(bundle)

        val item = synchronizableItem()

        val wrapper = synchronizableItemWrapper()
        whenever(updateUploadStatusUseCase.updateSingle(item, bundle, UploadStatus.IN_PROGRESS))
            .thenReturn(Single.just(wrapper))

        whenever(bundle.dataStore.insert(any())).thenAnswer { throw TestForcedException() }

        operation.run(item).test().assertError(TestForcedException::class.java)

        verifyNoMoreInteractions(queueOperationExecutor)
    }

    /*
    Utils
     */

    private fun mockDataStoreInsert(mockedDataStore: SynchronizableItemDataStore): SynchronizableItem {
        val expectedLocalSynchronizableItem = localSynchronizableItem()
        whenever(mockedDataStore.insert(any()))
            .thenReturn(expectedLocalSynchronizableItem)
        return expectedLocalSynchronizableItem
    }
}
