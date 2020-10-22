/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.operations

import com.android.synchronizator.synchronizableItem
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.SingleSubject
import java.util.UUID
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class UpdateOperationTest : BaseUnitTest() {
    private val createOrEditOperation: CreateOrEditOperation = mock()
    private val updateOperation: UpdateOperation = UpdateOperation(createOrEditOperation)

    @Test
    fun `when synchronizableItem has null uuid, then update emits IllegalStateException`() {
        val item = synchronizableItem(uuid = null)

        updateOperation.run(item).test().assertError(IllegalArgumentException::class.java)
    }

    @Test
    fun `when synchronizableItem has null uuid, then we don't run createOrEditOperation`() {
        val item = synchronizableItem(uuid = null)

        updateOperation.run(item).test()

        verifyNoMoreInteractions(createOrEditOperation)
    }

    @Test
    fun `when synchronizableItem uuid is not null, then we run createOrEditOperation`() {
        val item = synchronizableItem(uuid = UUID.randomUUID())

        val subject = SingleSubject.create<SynchronizableItem>()
        whenever(createOrEditOperation.run(item)).thenReturn(subject)

        val observer = updateOperation.run(item).test().assertNotComplete()

        assertTrue(subject.hasObservers())
        subject.onSuccess(item)

        observer.assertValue(item)
    }
}
