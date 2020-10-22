/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.operations

import com.android.synchronizator.catalogBundleConsumable
import com.android.synchronizator.itemBundleConsumable
import com.android.synchronizator.itemConsumable
import com.android.synchronizator.readOnlyBundleConsumable
import com.android.synchronizator.readOnlyConsumable
import com.android.synchronizator.synchronizableCatalogBundle
import com.android.synchronizator.synchronizableItemBundle
import com.android.synchronizator.synchronizableItemWrapper
import com.android.synchronizator.synchronizableReadOnlyBundle
import com.android.synchronizator.synchronizeAccountKeyBuilder
import com.android.synchronizator.testItemDatastore
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.BundleConsumableBuilder
import com.kolibree.android.synchronizator.CatalogBundleConsumable
import com.kolibree.android.synchronizator.ItemBundleConsumable
import com.kolibree.android.synchronizator.ReadOnlyBundleConsumable
import com.kolibree.android.synchronizator.SynchronizationBundles
import com.kolibree.android.synchronizator.data.usecases.PendingWrappersUseCase
import com.kolibree.android.synchronizator.models.SynchronizableCatalog
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import com.kolibree.android.synchronizator.models.exceptions.SynchronizatorException
import com.kolibree.android.synchronizator.operations.usecases.DeleteByKolibreeIdUseCase
import com.kolibree.android.synchronizator.operations.usecases.ProcessUpdatedIdUseCase
import com.kolibree.android.synchronizator.operations.utils.OperationProvider
import com.kolibree.android.synchronizator.operations.utils.mockWrappersDelete
import com.kolibree.android.synchronizator.operations.utils.mockWrapperssAddEdit
import com.kolibree.android.synchronizator.operations.utils.provider
import com.kolibree.android.synchronizator.usecases.BackendSynchronizationStateUseCase
import com.kolibree.android.test.TestForcedException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class SynchronizeQueueOperationTest : BaseUnitTest() {

    private val bundleConsumableBuilder: BundleConsumableBuilder = mock()

    private val createOrEditOperationProvider: OperationProvider<CreateOrEditOperation> = provider()
    private val deleteOperationProvider: OperationProvider<DeleteOperation> = provider()
    private val pendingWrappersUseCase: PendingWrappersUseCase = mock()
    private val deleteByKolibreeIdUseCase: DeleteByKolibreeIdUseCase = mock()
    private val processUpdatedIdUseCase: ProcessUpdatedIdUseCase = mock()
    private val ongoingSynchronizationUseCase: BackendSynchronizationStateUseCase = mock()

    private val operation =
        spy(
            SynchronizeQueueOperation(
                bundleConsumableBuilder = bundleConsumableBuilder,
                createOrEditOperationProvider = createOrEditOperationProvider.provider(),
                deleteOperationProvider = deleteOperationProvider.provider(),
                pendingWrappersUseCase = pendingWrappersUseCase,
                deleteByKolibreeIdUseCase = deleteByKolibreeIdUseCase,
                processUpdatedIdUseCase = processUpdatedIdUseCase,
                ongoingSynchronizationUseCase = ongoingSynchronizationUseCase
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

    /*onOperationNotRun*/

    @Test
    fun onOperationNotRun_does_nothing() {
        operation.onOperationNotRun()

        verify(operation).onOperationNotRun()

        verifyNoMoreInteractions(operation)
    }

    /*run*/

    @Test
    fun `run invokes operations on expected order`() {
        val inOrder = inOrder(ongoingSynchronizationUseCase, operation)

        operation.run()

        inOrder.verify(ongoingSynchronizationUseCase).onSyncStarted()
        inOrder.verify(operation).uploadPending()
        inOrder.verify(operation).fetchRemote()
        inOrder.verify(ongoingSynchronizationUseCase).onSyncSuccess()

        verify(ongoingSynchronizationUseCase, never()).onSyncFailed()
    }

    @Test
    fun `run does nothing if operation was canceled`() {
        operation.onOperationCanceled()

        operation.run()

        verifyNoMoreInteractions(ongoingSynchronizationUseCase)

        verify(operation, never()).uploadPending()
        verify(operation, never()).fetchRemote()
    }

    @Test
    fun `run invokes onSyncFailed if uploadPending throws exception`() {
        doAnswer {
            throw TestForcedException()
        }.whenever(operation).uploadPending()

        try {
            operation.run()
        } catch (e: TestForcedException) {
            // ignore
        }
        val inOrder = inOrder(ongoingSynchronizationUseCase)

        inOrder.verify(ongoingSynchronizationUseCase).onSyncStarted()
        inOrder.verify(ongoingSynchronizationUseCase).onSyncFailed()
        verify(ongoingSynchronizationUseCase, never()).onSyncSuccess()
    }

    @Test
    fun `run invokes onSyncFinalized even if fetchRemote throws exception`() {
        doAnswer {
            throw TestForcedException()
        }.whenever(operation).fetchRemote()

        try {
            operation.run()
        } catch (e: TestForcedException) {
            // ignore
        }
        val inOrder = inOrder(ongoingSynchronizationUseCase)

        inOrder.verify(ongoingSynchronizationUseCase).onSyncStarted()
        inOrder.verify(ongoingSynchronizationUseCase).onSyncFailed()
        verify(ongoingSynchronizationUseCase, never()).onSyncSuccess()
    }

    /*FETCH REMOTE*/

    @Test
    fun fetchRemote_emptyConsumables_doesNothing() {
        whenever(bundleConsumableBuilder.buildBundleConsumables()).thenReturn(listOf())

        operation.fetchRemote()
    }

    @Test
    fun fetchRemote_withItemConsumable_invokesInternalProcessItemBundleConsumable() {
        val expectedConsumable = itemBundleConsumable()
        whenever(bundleConsumableBuilder.buildBundleConsumables())
            .thenReturn(listOf(expectedConsumable))

        doNothing().whenever(operation).internalProcessItemBundleConsumable(any())

        operation.fetchRemote()

        verify(operation).internalProcessItemBundleConsumable(expectedConsumable)
    }

    @Test
    fun fetchRemote_withListConsumable_invokesInternalProcessReadOnlyBundleConsumable() {
        val expectedConsumable = readOnlyBundleConsumable()
        whenever(bundleConsumableBuilder.buildBundleConsumables())
            .thenReturn(listOf(expectedConsumable))

        doNothing().whenever(operation).internalProcessReadOnlyBundleConsumable(any())

        operation.fetchRemote()

        verify(operation).internalProcessReadOnlyBundleConsumable(expectedConsumable)
    }

    @Test
    fun fetchRemote_withCatalogConsumable_invokesInternalProcessCatalogBundleConsumable() {
        val expectedConsumable = catalogBundleConsumable()
        whenever(bundleConsumableBuilder.buildBundleConsumables())
            .thenReturn(listOf(expectedConsumable))

        doNothing().whenever(operation).internalProcessCatalogBundleConsumable(any())

        operation.fetchRemote()

        verify(operation).internalProcessCatalogBundleConsumable(expectedConsumable)
    }

    @Test
    fun fetchRemote_withMultipleConsumables_invokesMultipleInternalProcess() {
        val catalogConsumable1 = catalogBundleConsumable()
        val catalogConsumable2 = catalogBundleConsumable()
        val readOnlyConsumable = readOnlyBundleConsumable()
        val itemConsumable = itemBundleConsumable()
        whenever(bundleConsumableBuilder.buildBundleConsumables()).thenReturn(
            listOf(
                catalogConsumable1,
                catalogConsumable2,
                itemConsumable,
                readOnlyConsumable
            )
        )

        doNothing().whenever(operation).internalProcessCatalogBundleConsumable(any())
        doNothing().whenever(operation).internalProcessReadOnlyBundleConsumable(any())
        doNothing().whenever(operation).internalProcessItemBundleConsumable(any())

        operation.fetchRemote()

        verify(operation).internalProcessCatalogBundleConsumable(catalogConsumable1)
        verify(operation).internalProcessCatalogBundleConsumable(catalogConsumable2)
        verify(operation).internalProcessReadOnlyBundleConsumable(readOnlyConsumable)
        verify(operation).internalProcessItemBundleConsumable(itemConsumable)
    }

    @Test
    fun `fetchRemote processes all consumables, even if an exception is thrown while processing one`() {
        val catalogConsumable1 = catalogBundleConsumable()
        val catalogConsumable2 = catalogBundleConsumable()
        val readOnlyConsumable = readOnlyBundleConsumable()
        val itemConsumable = itemBundleConsumable()
        whenever(bundleConsumableBuilder.buildBundleConsumables())
            .thenReturn(
                listOf(
                    catalogConsumable1,
                    catalogConsumable2,
                    itemConsumable,
                    readOnlyConsumable
                )
            )

        doAnswer { throw TestForcedException() }.whenever(operation)
            .internalProcessCatalogBundleConsumable(
                any()
            )
        doAnswer { throw TestForcedException() }.whenever(operation)
            .internalProcessReadOnlyBundleConsumable(
                any()
            )
        doAnswer { throw TestForcedException() }.whenever(operation)
            .internalProcessItemBundleConsumable(
                any()
            )

        try {
            operation.fetchRemote()
        } catch (synchronizatorException: SynchronizatorException) {
            verify(operation).internalProcessCatalogBundleConsumable(catalogConsumable1)
            verify(operation).internalProcessCatalogBundleConsumable(catalogConsumable2)
            verify(operation).internalProcessReadOnlyBundleConsumable(readOnlyConsumable)
            verify(operation).internalProcessItemBundleConsumable(itemConsumable)
        }
    }

    @Test
    fun `fetchRemote processes all consumables and rethrows exception`() {
        val catalogConsumable1 = catalogBundleConsumable()
        val catalogConsumable2 = catalogBundleConsumable()
        val readOnlyConsumable = readOnlyBundleConsumable()
        val itemConsumable = itemBundleConsumable()
        whenever(bundleConsumableBuilder.buildBundleConsumables())
            .thenReturn(
                listOf(
                    catalogConsumable1,
                    catalogConsumable2,
                    itemConsumable,
                    readOnlyConsumable
                )
            )

        doAnswer { throw TestForcedException() }
            .whenever(operation)
            .internalProcessCatalogBundleConsumable(any())

        doAnswer { throw TestForcedException() }
            .whenever(operation)
            .internalProcessReadOnlyBundleConsumable(any())

        doAnswer { throw TestForcedException() }
            .whenever(operation)
            .internalProcessItemBundleConsumable(any())

        try {
            operation.fetchRemote()
        } catch (synchronizatorException: SynchronizatorException) {
            assertEquals(4, synchronizatorException.suppressed.size)
        }
    }

    @Test
    fun fetchRemote_withMultipleConsumables_processesThemInExpectedOrder() {
        val keysWithDifferentPriority = SynchronizableKey
            .values()
            .distinctBy { it.priority }
            .sortedByDescending { it.priority }

        val consumables = keysWithDifferentPriority.mapIndexed { index, synchronizableKey ->
            when (index % 4) {
                0 -> catalogBundleConsumable(
                    synchronizableCatalogBundle = synchronizableCatalogBundle(
                        synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(
                            key = synchronizableKey
                        )
                    )
                )
                1 -> readOnlyBundleConsumable(
                    synchronizableReadOnlyBundle = synchronizableReadOnlyBundle(
                        synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(
                            key = synchronizableKey
                        )
                    )
                )
                2 -> itemBundleConsumable(
                    synchronizableItemBundle = synchronizableItemBundle(
                        synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(
                            key = synchronizableKey
                        )
                    )
                )
                else -> itemBundleConsumable(
                    synchronizableItemBundle = synchronizableItemBundle(
                        synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(
                            key = synchronizableKey
                        )
                    )
                )
            }
        }

        // now, return BundleConsumables in wrong order
        whenever(bundleConsumableBuilder.buildBundleConsumables()).thenReturn(
            consumables.reversed()
        )

        doNothing().whenever(operation).internalProcessCatalogBundleConsumable(any())
        doNothing().whenever(operation).internalProcessReadOnlyBundleConsumable(any())
        doNothing().whenever(operation).internalProcessItemBundleConsumable(any())

        operation.fetchRemote()

        inOrder(operation) {
            for (consumable in consumables) {
                when (consumable) {
                    is ItemBundleConsumable -> verify(operation)
                        .internalProcessItemBundleConsumable(consumable)

                    is CatalogBundleConsumable -> verify(operation)
                        .internalProcessCatalogBundleConsumable(consumable)

                    is ReadOnlyBundleConsumable -> verify(operation)
                        .internalProcessReadOnlyBundleConsumable(consumable)
                }
            }
        }
    }

    @Test
    fun `fetchRemote does not run bundles after operation is canceled`() {
        val catalogBundleConsumable = catalogBundleConsumable()
        val readOnlyBundleConsumable = readOnlyBundleConsumable()
        val itemBundleConsumable = itemBundleConsumable()
        whenever(bundleConsumableBuilder.buildBundleConsumables())
            .thenReturn(
                listOf(readOnlyBundleConsumable, catalogBundleConsumable, itemBundleConsumable)
            )

        doAnswer {
            operation.onOperationCanceled()
        }
            .whenever(operation)
            .internalProcessReadOnlyBundleConsumable(readOnlyBundleConsumable)

        operation.fetchRemote()

        verify(operation, never()).internalProcessItemBundleConsumable(any())

        verify(operation, never()).internalProcessCatalogBundleConsumable(any())

        verify(operation).internalProcessReadOnlyBundleConsumable(readOnlyBundleConsumable)
    }

    @Test
    fun `fetchRemote does nothing if operation is canceled`() {
        operation.onOperationCanceled()

        whenever(bundleConsumableBuilder.buildBundleConsumables())
            .thenReturn(
                listOf(
                    readOnlyBundleConsumable(),
                    catalogBundleConsumable(),
                    itemBundleConsumable()
                )
            )

        operation.fetchRemote()

        verify(operation, never()).internalProcessItemBundleConsumable(any())

        verify(operation, never()).internalProcessCatalogBundleConsumable(any())

        verify(operation, never()).internalProcessReadOnlyBundleConsumable(any())
    }

    /*INTERNAL PROCESS CATALOG BUNDLE CONSUMABLE*/

    @Test
    fun internalProcessCatalogBundleConsumable_invokesDatastoreReplaceWithApiResponse() {
        val expectedCatalog = object : SynchronizableCatalog {}

        val catalogBundle = synchronizableCatalogBundle()
        whenever(catalogBundle.api.get()).thenReturn(expectedCatalog)
        val catalogConsumable = catalogBundleConsumable(
            synchronizableCatalogBundle = catalogBundle
        )

        operation.internalProcessCatalogBundleConsumable(catalogConsumable)

        verify(catalogBundle.dataStore).replace(expectedCatalog)
    }

    @Test(expected = TestForcedException::class)
    fun `internalProcessCatalogBundleConsumable propagates exception`() {
        val expectedCatalog = object : SynchronizableCatalog {}

        val catalogBundle = synchronizableCatalogBundle()
        whenever(catalogBundle.api.get()).thenAnswer { throw TestForcedException() }
        val catalogConsumable = catalogBundleConsumable(
            synchronizableCatalogBundle = catalogBundle
        )

        operation.internalProcessCatalogBundleConsumable(catalogConsumable)

        verify(catalogBundle.dataStore, never()).replace(expectedCatalog)

        verify(catalogBundle.api).get()
    }

    /*INTERNAL PROCESS LIST BUNDLE CONSUMABLE*/

    @Test
    fun internalProcessReadOnlyBundleConsumable_singleUpdatedId_invokesDatastoreReplaceWithApiResponseForId() {
        val expectedList = object : SynchronizableReadOnly {}
        val consumableUpdateId = 1L

        val readOnlyBundle = synchronizableReadOnlyBundle()
        val readOnlyConsumable = readOnlyConsumable(updatedIds = listOf(consumableUpdateId))
        val readOnlyBundleConsumable = readOnlyBundleConsumable(
            readOnlyConsumable = readOnlyConsumable,
            synchronizableReadOnlyBundle = readOnlyBundle
        )

        whenever(readOnlyBundle.api.get(consumableUpdateId)).thenReturn(expectedList)

        operation.internalProcessReadOnlyBundleConsumable(readOnlyBundleConsumable)

        verify(readOnlyBundle.dataStore).replace(expectedList)
    }

    @Test
    fun internalProcessReadOnlyBundleConsumable_multipleUpdatedId_invokesDataStoreReplaceWithApiResponseMultipleIds() {
        val consumableUpdateId1 = 1L
        val consumableUpdateId2 = 2L

        val readOnlyBundle = synchronizableReadOnlyBundle()
        val readOnlyConsumable =
            readOnlyConsumable(updatedIds = listOf(consumableUpdateId1, consumableUpdateId2))
        val readOnlyBundleConsumable = readOnlyBundleConsumable(
            readOnlyConsumable = readOnlyConsumable,
            synchronizableReadOnlyBundle = readOnlyBundle
        )

        val expectedList1 = object : SynchronizableReadOnly {}
        val expectedList2 = object : SynchronizableReadOnly {}
        whenever(readOnlyBundle.api.get(consumableUpdateId1)).thenReturn(expectedList1)
        whenever(readOnlyBundle.api.get(consumableUpdateId2)).thenReturn(expectedList2)

        operation.internalProcessReadOnlyBundleConsumable(readOnlyBundleConsumable)

        verify(readOnlyBundle.dataStore).replace(expectedList1)
        verify(readOnlyBundle.dataStore).replace(expectedList2)
    }

    @Test(expected = SynchronizatorException::class)
    fun `internalProcessReadOnlyBundleConsumable processes all consumables before throwing an Exception`() {
        val consumableUpdateId1 = 1L
        val consumableUpdateId2 = 2L

        val readOnlyBundle = synchronizableReadOnlyBundle()
        val readOnlyConsumable =
            readOnlyConsumable(updatedIds = listOf(consumableUpdateId1, consumableUpdateId2))
        val readOnlyBundleConsumable = readOnlyBundleConsumable(
            readOnlyConsumable = readOnlyConsumable,
            synchronizableReadOnlyBundle = readOnlyBundle
        )

        val expectedList1 = object : SynchronizableReadOnly {}
        val expectedList2 = object : SynchronizableReadOnly {}
        whenever(
            readOnlyBundle.api.get(consumableUpdateId1)
        ).thenAnswer { throw TestForcedException() }
        whenever(readOnlyBundle.api.get(consumableUpdateId2)).thenReturn(expectedList2)

        try {
            operation.internalProcessReadOnlyBundleConsumable(readOnlyBundleConsumable)
        } catch (synchronizatorException: SynchronizatorException) {
            verify(readOnlyBundle.api).get(consumableUpdateId1)
            verify(readOnlyBundle.api).get(consumableUpdateId2)

            verify(readOnlyBundle.dataStore, never()).replace(expectedList1)
            verify(readOnlyBundle.dataStore).replace(expectedList2)

            throw synchronizatorException
        }
    }

    /*INTERNAL PROCESS ITEM BUNDLE CONSUMABLE*/

    @Test
    fun internalProcessItemBundleConsumable_empty_doesNothing() {
        val itemBundleConsumable = itemBundleConsumable()

        operation.internalProcessItemBundleConsumable(itemBundleConsumable)

        verifyNoMoreInteractions(itemBundleConsumable.itemBundle.api)
        verifyNoMoreInteractions(itemBundleConsumable.itemBundle.dataStore)
        verifyNoMoreInteractions(itemBundleConsumable.itemBundle.conflictStrategy)
    }

    @Test
    fun `internalProcessItemBundleConsumable withDeletedIds invokes deleteByKolibreeIdUseCase delete on each id`() {
        val expectedId1 = 1L
        val expectedId2 = 2L

        val itemBundleConsumable = itemBundleConsumable(
            itemConsumable = itemConsumable(deletedIds = listOf(expectedId1, expectedId2))
        )

        operation.internalProcessItemBundleConsumable(itemBundleConsumable)

        verify(deleteByKolibreeIdUseCase).delete(expectedId1, itemBundleConsumable.itemBundle)
        verify(deleteByKolibreeIdUseCase).delete(expectedId2, itemBundleConsumable.itemBundle)
    }

    @Test(expected = SynchronizatorException::class)
    fun `internalProcessItemBundleConsumable with multiple deletedIds processes all of them, even if they crash, and returns SynchronizatorException with two exceptions`() {
        val id1 = 1L
        val id2 = 2L

        val itemBundleConsumable = itemBundleConsumable(
            itemConsumable = itemConsumable(deletedIds = listOf(id1, id2))
        )

        whenever(deleteByKolibreeIdUseCase.delete(id1, itemBundleConsumable.itemBundle))
            .thenAnswer { throw TestForcedException() }

        try {
            operation.internalProcessItemBundleConsumable(itemBundleConsumable)
        } catch (e: TestForcedException) {
            verify(deleteByKolibreeIdUseCase).delete(id1, itemBundleConsumable.itemBundle)
            verify(deleteByKolibreeIdUseCase).delete(id2, itemBundleConsumable.itemBundle)

            assertEquals(2, e.suppressed.size)

            throw e
        }
    }

    @Test(expected = SynchronizatorException::class)
    fun `internalProcessItemBundleConsumable processes updatedIds even if a deletedId crashes`() {
        val deletedId = 1L
        val updatedId = 2L

        val itemBundleConsumable = itemBundleConsumable(
            itemConsumable = itemConsumable(
                deletedIds = listOf(deletedId),
                updatedIds = listOf(updatedId)
            )
        )

        whenever(deleteByKolibreeIdUseCase.delete(deletedId, itemBundleConsumable.itemBundle))
            .thenAnswer { throw TestForcedException() }

        whenever(processUpdatedIdUseCase.process(deletedId, itemBundleConsumable.itemBundle))
            .thenAnswer { throw TestForcedException() }

        try {
            operation.internalProcessItemBundleConsumable(itemBundleConsumable)
        } catch (e: TestForcedException) {
            verify(deleteByKolibreeIdUseCase).delete(deletedId, itemBundleConsumable.itemBundle)
            verify(processUpdatedIdUseCase).process(updatedId, itemBundleConsumable.itemBundle)

            throw e
        }
    }

    @Test(expected = SynchronizatorException::class)
    fun `internalProcessItemBundleConsumable processes all updatedIds, even if one crashes`() {
        val updatedId1 = 1L
        val updatedId2 = 2L

        val itemBundleConsumable = itemBundleConsumable(
            itemConsumable = itemConsumable(updatedIds = listOf(updatedId1, updatedId2))
        )

        whenever(processUpdatedIdUseCase.process(any(), eq(itemBundleConsumable.itemBundle)))
            .thenAnswer { throw TestForcedException() }

        operation.internalProcessItemBundleConsumable(itemBundleConsumable)

        verify(processUpdatedIdUseCase).process(updatedId1, itemBundleConsumable.itemBundle)
        verify(processUpdatedIdUseCase).process(updatedId2, itemBundleConsumable.itemBundle)
    }

    /*UPLOAD PENDING*/
    @Test
    fun uploadPending_emptyBundle_doesNothing() {
        operation.uploadPending()
    }

    @Test
    fun uploadPending_withCatalogBundleAndReadOnlyBundle_doesNothing() {
        SynchronizationBundles.register(synchronizableReadOnlyBundle())
        SynchronizationBundles.register(synchronizableCatalogBundle())

        operation.uploadPending()
    }

    @Test
    fun uploadPending_withItemBundle_emptyCreateAndDelete_doesNothing() {
        SynchronizationBundles.register(synchronizableItemBundle(dataStore = testItemDatastore()))

        operation.uploadPending()
    }

    @Test
    fun `uploadPending withItemBundle withPendingCreate runsCreateOrEditOperation on each item returned by pendingWrappersUseCase`() {
        val wrapper1 = synchronizableItemWrapper()
        val wrapper2 = synchronizableItemWrapper()
        val bundle = synchronizableItemBundle()
        SynchronizationBundles.register(bundle)

        whenever(pendingWrappersUseCase.getPendingCreate(bundle))
            .thenReturn(listOf(wrapper1, wrapper2))

        var subscribedToWrapper1 = false
        var subscribedToWrapper2 = false
        createOrEditOperationProvider.mockWrapperssAddEdit(
            listOf(wrapper1, wrapper2),
            arrayOf(
                Single.just(wrapper1.synchronizableItem)
                    .doOnSubscribe { subscribedToWrapper1 = true },
                Single.just(wrapper2.synchronizableItem)
                    .doOnSubscribe { subscribedToWrapper2 = true }
            )
        )

        operation.uploadPending()

        assertTrue(subscribedToWrapper1)
        assertTrue(subscribedToWrapper2)
    }

    @Test
    fun `uploadPending withItemBundle withPendingCreate does nothing if operation is canceled`() {
        operation.onOperationCanceled()

        val wrapper1 = synchronizableItemWrapper()
        val wrapper2 = synchronizableItemWrapper()
        val bundle = synchronizableItemBundle()
        SynchronizationBundles.register(bundle)

        whenever(pendingWrappersUseCase.getPendingCreate(bundle))
            .thenReturn(listOf(wrapper1, wrapper2))

        var subscribedToWrapper1 = false
        var subscribedToWrapper2 = false
        createOrEditOperationProvider.mockWrapperssAddEdit(
            listOf(wrapper1, wrapper2),
            arrayOf(
                Single.just(wrapper1.synchronizableItem)
                    .doOnSubscribe { subscribedToWrapper1 = true },
                Single.just(wrapper2.synchronizableItem)
                    .doOnSubscribe { subscribedToWrapper2 = true }
            )
        )

        operation.uploadPending()

        assertFalse(subscribedToWrapper1)
        assertFalse(subscribedToWrapper2)
    }

    @Test
    fun `uploadPending withItemBundle withPendingDelete runs DeleteOperation on each item returned by pendingWrappersUseCase`() {
        val wrapper1 = synchronizableItemWrapper()
        val wrapper2 = synchronizableItemWrapper()
        val bundle = synchronizableItemBundle()
        SynchronizationBundles.register(bundle)

        whenever(pendingWrappersUseCase.getPendingDelete(bundle))
            .thenReturn(listOf(wrapper1, wrapper2))

        var subscribedToWrapper1 = false
        var subscribedToWrapper2 = false
        deleteOperationProvider.mockWrappersDelete(
            listOf(wrapper1, wrapper2),
            arrayOf(
                Completable.complete().doOnSubscribe { subscribedToWrapper1 = true },
                Completable.complete().doOnSubscribe { subscribedToWrapper2 = true }
            )
        )

        operation.uploadPending()

        assertTrue(subscribedToWrapper1)
        assertTrue(subscribedToWrapper2)
    }

    @Test
    fun `uploadPending withItemBundle withPendingDelete runs does nothing if operation is canceled`() {
        operation.onOperationCanceled()

        val wrapper1 = synchronizableItemWrapper()
        val wrapper2 = synchronizableItemWrapper()
        val bundle = synchronizableItemBundle()
        SynchronizationBundles.register(bundle)

        whenever(pendingWrappersUseCase.getPendingDelete(bundle))
            .thenReturn(listOf(wrapper1, wrapper2))

        var subscribedToWrapper1 = false
        var subscribedToWrapper2 = false
        deleteOperationProvider.mockWrappersDelete(
            listOf(wrapper1, wrapper2),
            arrayOf(
                Completable.complete().doOnSubscribe { subscribedToWrapper1 = true },
                Completable.complete().doOnSubscribe { subscribedToWrapper2 = true }
            )
        )

        operation.uploadPending()

        assertFalse(subscribedToWrapper1)
        assertFalse(subscribedToWrapper2)
    }

    @Test
    fun `uploadPending withItemBundle withPendingCreate runs CreateOrEditOperation on expected order`() {
        val keysWithDifferentPriority = SynchronizableKey
            .values()
            .distinctBy { it.priority }
            .sortedByDescending { it.priority }

        val bundles = keysWithDifferentPriority.map { synchronizableKey ->
            synchronizableItemBundle(synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(key = synchronizableKey))
        }

        // now, register them in wrong priority order
        bundles
            .reversed()
            .forEach(SynchronizationBundles::register)

        operation.uploadPending()

        // verify pending create is in order
        inOrder(pendingWrappersUseCase) {
            for (bundle in bundles) {
                verify(pendingWrappersUseCase).getPendingCreate(bundle)
            }
        }

        // verify pending delete is in order
        inOrder(pendingWrappersUseCase) {
            for (bundle in bundles) {
                verify(pendingWrappersUseCase).getPendingDelete(bundle)
            }
        }
    }
}
