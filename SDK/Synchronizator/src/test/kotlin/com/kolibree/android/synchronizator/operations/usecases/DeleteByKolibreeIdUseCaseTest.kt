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
import com.android.synchronizator.synchronizableItemBundle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly.overrideDelegateWith
import com.kolibree.android.synchronizator.data.usecases.DeleteByUuidUseCase
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import java.util.UUID
import org.junit.Test

internal class DeleteByKolibreeIdUseCaseTest : BaseUnitTest() {
    private val deleteByUuidUseCase: DeleteByUuidUseCase = mock()

    private val useCase = DeleteByKolibreeIdUseCase(deleteByUuidUseCase)

    @Test
    fun `when dataStore returns null, delete does nothing`() {
        whenever(bundle.dataStore.getByKolibreeId(any())).thenReturn(null)

        useCase.delete(1L, bundle)

        verifyNoMoreInteractions(deleteByUuidUseCase)
    }

    @Test(expected = AssertionError::class)
    fun `when dataStore returns item with null id, FailEarly kicks in`() {
        overrideDelegateWith(TestDelegate)
        try {
            whenever(bundle.dataStore.getByKolibreeId(any()))
                .thenReturn(localSynchronizableItem(uuid = null))

            useCase.delete(1L, bundle)
        } finally {
            overrideDelegateWith(NoopTestDelegate)
        }
    }

    @Test
    fun `when dataStore returns item, we delete the bundle item and then tracking entity`() {
        val expectedUuid = UUID.randomUUID()
        whenever(bundle.dataStore.getByKolibreeId(any()))
            .thenReturn(localSynchronizableItem(uuid = expectedUuid))

        useCase.delete(1L, bundle)

        verify(deleteByUuidUseCase).delete(expectedUuid, bundle)
    }

    private val bundle = synchronizableItemBundle()
}
