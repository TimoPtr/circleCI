/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.operations.usecases

import com.android.synchronizator.localSynchronizableItem
import com.android.synchronizator.remoteSynchronizableItem
import com.android.synchronizator.synchronizableItemBundle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.ConflictResolution
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

internal class ProcessUpdatedIdUseCaseTest : BaseUnitTest() {
    private val conflictResolutionUseCase: ConflictResolutionUseCase = mock()
    private val readByKolibreeIdUseCase: ReadByKolibreeIdUseCase = mock()

    private val useCase =
        ProcessUpdatedIdUseCase(conflictResolutionUseCase, readByKolibreeIdUseCase)

    @Test
    fun `process passes remote and local item to ConflictStrategy`() {
        val kolibreeId = 76L

        val remoteItem = remoteSynchronizableItem(kolibreeId = kolibreeId)
        val localItem = localSynchronizableItem()
        whenever(bundle.api.get(kolibreeId)).thenReturn(remoteItem)

        whenever(readByKolibreeIdUseCase.read(kolibreeId, bundle)).thenReturn(localItem)

        whenever(bundle.conflictStrategy.resolve(localItem, remoteItem))
            .thenReturn(
                ConflictResolution(
                    localSynchronizable = localItem,
                    remoteSynchronizable = remoteItem,
                    resolvedSynchronizable = null
                )
            )

        useCase.process(kolibreeId, bundle)

        verify(bundle.conflictStrategy).resolve(localItem, remoteItem)
    }

    @Test
    fun `process passes ConflictResolution to conflicResolutionUseCase`() {
        val kolibreeId = 1L

        val item = remoteSynchronizableItem()
        whenever(bundle.api.get(kolibreeId)).thenReturn(item)

        val expectedConflictResolution = ConflictResolution(
            localSynchronizable = null,
            remoteSynchronizable = item,
            resolvedSynchronizable = null
        )
        whenever(bundle.conflictStrategy.resolve(null, item))
            .thenReturn(expectedConflictResolution)

        useCase.process(kolibreeId, bundle)

        verify(conflictResolutionUseCase).resolve(expectedConflictResolution, bundle)
    }

    private val bundle = synchronizableItemBundle()
}
