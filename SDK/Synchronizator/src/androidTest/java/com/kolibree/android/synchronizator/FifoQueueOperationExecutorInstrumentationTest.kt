/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator

import android.content.Context
import android.os.Handler
import android.os.Message
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.utils.randomPositiveInt
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.util.concurrent.ConcurrentLinkedQueue
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.Duration

internal class FifoQueueOperationExecutorInstrumentationTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val networkChecker: NetworkChecker = mock()
    private val synchronizeOnNetworkAvailableUseCase: SynchronizeOnNetworkAvailableUseCase = mock()

    private val testHandler = TestHandler(context())

    private val queue =
        FifoQueueOperationExecutor(
            networkChecker,
            synchronizeOnNetworkAvailableUseCase,
            testHandler
        )

    /*
    enqueue
     */
    @Test
    fun whenInternetIsAvailable_thenEnqueuePostsToHandlerOperationRun() {
        val operation = FakeOperation()

        whenever(networkChecker.hasConnectivity()).thenReturn(true)

        queue.enqueue(operation)

        assertEquals(1, testHandler.messages.size)

        assertTrue(operation.runInvoked)
        assertFalse(operation.onOperationNotRunInvoked)
    }

    @Test
    fun whenInternetIsNotAvailable_thenEnqueueSchedulesSynchronizeOnNetworkAvailable() {
        val operation = FakeOperation()

        whenever(networkChecker.hasConnectivity()).thenReturn(false)

        queue.enqueue(operation)

        assertFalse(operation.runInvoked)

        assertEquals(1, testHandler.messages.size)

        verify(synchronizeOnNetworkAvailableUseCase).schedule()
    }

    @Test
    fun whenInternetIsNotAvailable_thenEnqueueInvokesOnOperationNotRun() {
        val operation = FakeOperation()

        whenever(networkChecker.hasConnectivity()).thenReturn(false)

        queue.enqueue(operation)

        assertTrue(operation.onOperationNotRunInvoked)
    }

    /*
    cancelOperations

    I didn't find a way to test that operations were removed from the Handler
     */
    @Test
    fun whenCancelOperationsIsInvoked_weDontCrash() {
        queue.cancelOperations()
    }

    @Test
    fun whenCancelOperationsIsInvoked_afterAnOperationIsRunning_invokeOnOperationCanceledOnOperation() {
        val operation = spy(FakeOperation())

        doAnswer {
            assertFalse(operation.testIsCanceled())

            queue.cancelOperations()
        }.whenever(operation).run()

        whenever(networkChecker.hasConnectivity()).thenReturn(true)

        queue.enqueue(operation)

        assertTrue(operation.testIsCanceled())
    }

    /*
    This test failed very frequently on real multicore devices. On Emulator, it's harder to make it
    fail
     */
    @Test
    fun cancelOperations_doesNotThrowConcurrentModificationException() {
        testHandler.storeMessages = false

        whenever(networkChecker.hasConnectivity()).thenReturn(true)

        val zeroDelay = Duration.ofSeconds(0)

        val nbOfThreads = 5000
        val producerThreads = (1..nbOfThreads).map {
            Thread {
                queue.enqueue(
                    queueOperation = FakeOperation(
                        duration = Duration.ofMillis(
                            randomPositiveInt(minValue = 1, maxValue = 10).toLong()
                        )
                    ),
                    initialDelay = zeroDelay
                )
            }
        }

        val consumerThreads = (1..nbOfThreads / 2).map {
            Thread {
                queue.cancelOperations()
            }
        }

        val threads = producerThreads + consumerThreads
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        // if no exception was thrown here, test succeeded
    }
}

private class TestHandler(context: Context) : Handler(context.mainLooper) {
    val messages = ConcurrentLinkedQueue<Message>()

    var storeMessages = true

    override fun sendMessageAtTime(msg: Message, uptimeMillis: Long): Boolean {
        if (storeMessages) messages.add(msg)
        msg.callback.run()

        return false
    }
}
