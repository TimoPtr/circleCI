/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.operations

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.QueueOperationExecutor
import com.kolibree.android.synchronizator.operations.utils.OperationProvider
import com.kolibree.android.synchronizator.operations.utils.provider
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

internal class SynchronizeOperationTest : BaseUnitTest() {

    private val synchronizeQueueOperationProvider: OperationProvider<SynchronizeQueueOperation> =
        provider()

    private val queueOperationExecutor: QueueOperationExecutor = mock()

    val operation =
        SynchronizeOperation(synchronizeQueueOperationProvider.provider(), queueOperationExecutor)

    @Test
    fun `run enqueues a new SynchronizeQueueOperation`() {
        operation.run().test().assertComplete()

        verify(queueOperationExecutor)
            .enqueue(synchronizeQueueOperationProvider.operations.single())
    }
}
