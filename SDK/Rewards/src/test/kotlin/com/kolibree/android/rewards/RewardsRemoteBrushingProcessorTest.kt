/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.Synchronizator
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class RewardsRemoteBrushingProcessorTest : BaseUnitTest() {
    private val synchronizator: Synchronizator = mock()

    private val processor = RewardsRemoteBrushingProcessor(synchronizator)

    @Test
    fun `onBrushingsRemoved invokes synchronize after 2 seconds`() = runBlockingTest {
        val virtualStart = currentTime

        processor.onBrushingsRemoved()

        assertSynchronizeInvokedAfterDelay(virtualStart)
    }

    @Test
    fun `onBrushingsCreated invokes synchronize after 2 seconds`() = runBlockingTest {
        val virtualStart = currentTime

        processor.onBrushingsCreated()

        assertSynchronizeInvokedAfterDelay(virtualStart)
    }

    /*
    Utils
     */

    private fun TestCoroutineScope.assertSynchronizeInvokedAfterDelay(
        virtualStart: Long
    ) {
        verify(synchronizator).synchronize()

        val virtualDuration = currentTime - virtualStart

        assertTrue(virtualDuration >= 2000L)
    }
}
