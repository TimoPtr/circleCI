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
import com.android.synchronizator.synchronizableItemWrapper
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.data.usecases.SynchronizableItemWrapperProvider
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class ReadByKolibreeIdUseCaseTest : BaseUnitTest() {
    private val wrapperProvider: SynchronizableItemWrapperProvider = mock()

    private val useCase = ReadByKolibreeIdUseCase(wrapperProvider)

    @Test
    fun `when there's no local item with kolibreeId, then return null`() {
        assertNull(bundle.dataStore.getByKolibreeId(kolibreeId))

        assertNull(useCase.read(kolibreeId, bundle))
    }

    @Test
    fun `when there's a local item with kolibreeId, then return instance from wrapperProvider`() {
        val localItem = localSynchronizableItem()
        whenever(bundle.dataStore.getByKolibreeId(kolibreeId))
            .thenReturn(localItem)

        val expectedWrapper = synchronizableItemWrapper()
        whenever(wrapperProvider.provide(localItem, bundle))
            .thenReturn(expectedWrapper)

        assertEquals(expectedWrapper.synchronizableItem, useCase.read(kolibreeId, bundle))
    }

    private val kolibreeId = 54L
    private val bundle = synchronizableItemBundle()
}
