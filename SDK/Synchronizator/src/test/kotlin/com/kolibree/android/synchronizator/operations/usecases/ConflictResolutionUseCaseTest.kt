/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.operations.usecases

import com.android.synchronizator.remoteSynchronizableItem
import com.android.synchronizator.synchronizableItem
import com.android.synchronizator.synchronizableItemBundle
import com.android.synchronizator.synchronizableItemWrapper
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.ConflictResolution
import com.kolibree.android.synchronizator.data.usecases.UpdateUploadStatusUseCase
import com.kolibree.android.synchronizator.insert
import com.kolibree.android.synchronizator.models.UploadStatus
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertNull
import org.junit.Test

internal class ConflictResolutionUseCaseTest : BaseUnitTest() {
    private val updateUploadStatusUseCase: UpdateUploadStatusUseCase = mock()

    private val useCase = ConflictResolutionUseCase(updateUploadStatusUseCase)

    @Test
    fun `process doesn't insert when synchronizableForDatastore returns null`() {
        val item = remoteSynchronizableItem()
        val conflictResolution = ConflictResolution(
            localSynchronizable = null,
            remoteSynchronizable = item,
            resolvedSynchronizable = null
        )

        assertNull(conflictResolution.synchronizableForDatastore())

        useCase.resolve(conflictResolution, bundle)

        verify(bundle.dataStore, never()).insert(any())
    }

    @Test
    fun `process inserts to bundle the item returned by updateUploadStatusUseCase`() {
        val kolibreeId = 1L

        val item = remoteSynchronizableItem()

        val conflictResolvedItem = synchronizableItem()
        val conflictResolution = ConflictResolution(
            localSynchronizable = null,
            remoteSynchronizable = item,
            resolvedSynchronizable = conflictResolvedItem
        )

        val wrapper = synchronizableItemWrapper()
        whenever(
            updateUploadStatusUseCase.update(
                conflictResolvedItem,
                bundle,
                UploadStatus.COMPLETED
            )
        )
            .thenReturn(wrapper)

        useCase.resolve(conflictResolution, bundle)

        verify(bundle.dataStore).insert(wrapper)
    }

    private val bundle = synchronizableItemBundle()
}
