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
import com.android.synchronizator.synchronizableItemWrapper
import com.android.synchronizator.synchronizableTrackingEntity
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.UploadStatus.IN_PROGRESS
import com.kolibree.android.synchronizator.models.UploadStatus.PENDING
import com.kolibree.android.test.extensions.assertContainsExclusively
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class PendingWrappersUseCaseTest : BaseUnitTest() {
    private val readByUploadStatusUseCase: ReadByUploadStatusUseCase = mock()
    private val filterPendingWrapperUseCase: FilterPendingWrapperUseCase = mock()

    private val useCase =
        PendingWrappersUseCase(readByUploadStatusUseCase, filterPendingWrapperUseCase)

    /*
    getPendingCreate
     */
    @Test
    fun `when readByUploadStatus returns empty list, getPendingCreate returns empty list`() {
        mockReadByUploadStatus()

        assertTrue(useCase.getPendingCreate(bundle).isEmpty())
    }

    @Test
    fun `getPendingCreate filters out items deleted locally`() {
        val expectedItem = synchronizableItemWrapper(
            entity = synchronizableTrackingEntity(isDeletedLocally = false)
        )
        val expectedItem1 = synchronizableItemWrapper(
            entity = synchronizableTrackingEntity(isDeletedLocally = false)
        )

        val nonExpectedItem = synchronizableItemWrapper(
            entity = synchronizableTrackingEntity(isDeletedLocally = true)
        )

        mockFilterPassing()

        mockReadByUploadStatus(expectedItem1, expectedItem, nonExpectedItem)

        useCase.getPendingCreate(bundle)
            .assertContainsExclusively(listOf(expectedItem, expectedItem1))
    }

    @Test
    fun `getPendingCreate filters out items that don't pass the filter`() {
        val expectedItem = synchronizableItemWrapper(
            entity = synchronizableTrackingEntity(isDeletedLocally = false)
        )
        val expectedItem1 = synchronizableItemWrapper(
            entity = synchronizableTrackingEntity(isDeletedLocally = false)
        )

        val nonExpectedItem = synchronizableItemWrapper(
            entity = synchronizableTrackingEntity(isDeletedLocally = false)
        )

        mockFilterPassing(expectedItem)
        mockFilterPassing(expectedItem1)
        mockFilterNotPassing(nonExpectedItem)

        mockReadByUploadStatus(expectedItem1, expectedItem, nonExpectedItem)

        useCase.getPendingCreate(bundle)
            .assertContainsExclusively(listOf(expectedItem, expectedItem1))
    }

    /*
    getPendingDelete
     */
    @Test
    fun `when readByUploadStatus returns empty list, getPendingDelete returns empty list`() {
        mockReadByUploadStatus()

        assertTrue(useCase.getPendingDelete(bundle).isEmpty())
    }

    @Test
    fun `getPendingDelete filters out items not deleted locally`() {
        val expectedItem = synchronizableItemWrapper(
            entity = synchronizableTrackingEntity(isDeletedLocally = true)
        )
        val expectedItem1 = synchronizableItemWrapper(
            entity = synchronizableTrackingEntity(isDeletedLocally = true)
        )

        val nonExpectedItem = synchronizableItemWrapper(
            entity = synchronizableTrackingEntity(isDeletedLocally = false)
        )

        mockFilterPassing()

        mockReadByUploadStatus(expectedItem1, expectedItem, nonExpectedItem)

        useCase.getPendingDelete(bundle)
            .assertContainsExclusively(listOf(expectedItem, expectedItem1))
    }

    @Test
    fun `getPendingDelete filters out items that don't pass the filter`() {
        val expectedItem = synchronizableItemWrapper(
            entity = synchronizableTrackingEntity(isDeletedLocally = true)
        )
        val expectedItem1 = synchronizableItemWrapper(
            entity = synchronizableTrackingEntity(isDeletedLocally = true)
        )

        val nonExpectedItem = synchronizableItemWrapper(
            entity = synchronizableTrackingEntity(isDeletedLocally = true)
        )

        mockFilterPassing(expectedItem)
        mockFilterPassing(expectedItem1)
        mockFilterNotPassing(nonExpectedItem)

        mockReadByUploadStatus(expectedItem1, expectedItem, nonExpectedItem)

        useCase.getPendingDelete(bundle)
            .assertContainsExclusively(listOf(expectedItem, expectedItem1))
    }

    /*
    Utils
     */

    private fun mockFilterPassing() {
        whenever(filterPendingWrapperUseCase.nullUnlessPending(any()))
            .thenAnswer { it.getArgument(0) }
    }

    private fun mockFilterPassing(wrapper: SynchronizableItemWrapper) {
        whenever(filterPendingWrapperUseCase.nullUnlessPending(wrapper))
            .thenAnswer { it.getArgument(0) }
    }

    private fun mockFilterNotPassing(wrapper: SynchronizableItemWrapper) {
        whenever(filterPendingWrapperUseCase.nullUnlessPending(wrapper))
            .thenReturn(null)
    }

    private fun mockReadByUploadStatus(vararg wrappers: SynchronizableItemWrapper = arrayOf()) {
        whenever(readByUploadStatusUseCase.readByUploadStatus(bundle, PENDING, IN_PROGRESS))
            .thenReturn(wrappers.toList())
    }

    private val bundle = synchronizableItemBundle()
}
