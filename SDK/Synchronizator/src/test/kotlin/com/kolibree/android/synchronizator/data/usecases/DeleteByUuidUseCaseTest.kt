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
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.data.SynchronizableTrackingEntityDataStore
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import java.util.UUID
import org.junit.Test

internal class DeleteByUuidUseCaseTest : BaseUnitTest() {
    private val entityDataStore: SynchronizableTrackingEntityDataStore = mock()

    private val useCase = DeleteByUuidUseCase(entityDataStore)

    @Test
    fun `when delete is invoked, then delete the bundle item and then tracking entity in order`() {
        val uuid = UUID.randomUUID()

        useCase.delete(uuid, bundle)

        inOrder(entityDataStore, bundle.dataStore) {
            verify(bundle.dataStore).delete(uuid)
            verify(entityDataStore).delete(uuid)
        }
    }

    private val bundle = synchronizableItemBundle()
}
