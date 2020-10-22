/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.models

import com.android.synchronizator.synchronizableItemWrapper
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class SynchronizableItemWrapperTest : BaseUnitTest() {
    @Test
    fun `withUploadStatus returns instance that includes entity with updated UploadStatus`() {
        val wrapper = synchronizableItemWrapper()

        val expectedUploadStatus = UploadStatus.COMPLETED
        val newWrapper = wrapper.withUploadStatus(expectedUploadStatus)

        val expectedWrapper = SynchronizableItemWrapper(
            trackingEntity = wrapper.trackingEntity.copy(uploadStatus = expectedUploadStatus),
            synchronizableItem = wrapper.synchronizableItem
        )

        assertEquals(
            expectedWrapper,
            newWrapper
        )
    }

    @Test
    fun `withIsDeletedLocally returns instance that includes entity with updated isDeletedLocally`() {
        val wrapper = synchronizableItemWrapper()

        val expectedIsDeleted = true
        val newWrapper = wrapper.withIsDeletedLocally(expectedIsDeleted)

        val expectedWrapper = SynchronizableItemWrapper(
            trackingEntity = wrapper.trackingEntity.copy(isDeletedLocally = expectedIsDeleted),
            synchronizableItem = wrapper.synchronizableItem
        )

        assertEquals(
            expectedWrapper,
            newWrapper
        )
    }

    @Test
    fun `withKolibreeId returns instance that includes item with updated kolibreeId`() {
        val wrapper = synchronizableItemWrapper()

        val expectedKolibreeId = 656L
        val newWrapper = wrapper.withKolibreeId(expectedKolibreeId)

        val expectedWrapper = SynchronizableItemWrapper(
            trackingEntity = wrapper.trackingEntity,
            synchronizableItem = wrapper.synchronizableItem.withKolibreeId(expectedKolibreeId)
        )

        assertEquals(
            expectedWrapper,
            newWrapper
        )
    }

    @Test
    fun `withUpdatedAt returns instance that includes item with updated kolibreeId`() {
        val wrapper = synchronizableItemWrapper()

        val expectedUpdatedAt = TrustedClock.getNowZonedDateTime().minusMinutes(1)
        val newWrapper = wrapper.withUpdatedAt(expectedUpdatedAt)

        val expectedWrapper = SynchronizableItemWrapper(
            trackingEntity = wrapper.trackingEntity,
            synchronizableItem = wrapper.synchronizableItem.withUpdatedAt(expectedUpdatedAt)
        )

        assertEquals(
            expectedWrapper,
            newWrapper
        )
    }
}
