/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.data.usecases

import com.android.synchronizator.synchronizableItem
import com.android.synchronizator.synchronizableItemBundle
import com.android.synchronizator.synchronizableItemWrapper
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.data.SynchronizableTrackingEntityDataStore
import com.kolibree.android.synchronizator.data.database.updateWrapper
import com.kolibree.android.synchronizator.models.UploadStatus
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class UpdateUploadStatusUseCaseTest : BaseUnitTest() {
    private val entityDataStore: SynchronizableTrackingEntityDataStore = mock()
    private val wrapperProvider: SynchronizableItemWrapperProvider = mock()

    private val useCase = UpdateUploadStatusUseCase(entityDataStore, wrapperProvider)

    /*
    update
     */
    @Test
    fun `update inserts an updated version of TrackingEntity with expected UploadStatus`() {
        val item = synchronizableItem(uuid = null)

        val wrapper = synchronizableItemWrapper()
        whenever(wrapperProvider.provide(item, bundle))
            .thenReturn(wrapper)

        val expectedUploadStatus = UploadStatus.IN_PROGRESS
        val returnedWrapper = useCase.update(item, bundle, expectedUploadStatus)

        val expectedWrapper = wrapper.withUploadStatus(expectedUploadStatus)

        assertEquals(
            expectedWrapper,
            returnedWrapper
        )

        verify(entityDataStore).updateWrapper(expectedWrapper)
    }

    /*
    update wrapper
     */
    @Test
    fun `when update wrapper is invoked, we return a new instance with new UploadStatus and we update the database`() {
        val wrapper = synchronizableItemWrapper()

        val expectedUploadStatus = UploadStatus.COMPLETED
        val returnedWrapper = useCase.update(wrapper, bundle, expectedUploadStatus)

        val expectedWrapper = wrapper.withUploadStatus(expectedUploadStatus)

        assertEquals(
            expectedWrapper,
            returnedWrapper
        )

        verify(entityDataStore).updateWrapper(expectedWrapper)
    }

    private val bundle = synchronizableItemBundle()
}
