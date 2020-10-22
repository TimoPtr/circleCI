/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.usecases

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.extensions.assertLastValueWithPredicate
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_MAC
import com.kolibree.pairing.assistant.PairingAssistant
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.SingleSubject
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertTrue
import org.junit.Test

class BlinkUseCaseTest : BaseUnitTest() {

    private val pairingAssistant: PairingAssistant = mock()
    private val timeoutScheduler = TestScheduler()

    private val useCase = BlinkUseCase(pairingAssistant, timeoutScheduler)

    @Test
    fun `blink emits InProgress immediately after subscribing and attempts to connectAndBlinkBlue`() {
        val blinkSubject = mockSingleParameter()

        invokeBlink(blinkSubject).test()
            .assertValue(BlinkEvent.InProgress)
            .assertNotComplete()

        assertTrue(blinkSubject.hasObservers())
    }

    @Test
    fun `blink emits Success after blink emits success`() {
        val blinkSubject = mockSingleParameter()

        val observer = invokeBlink(blinkSubject).test()

        val expectedConnection = mock<KLTBConnection>()
        blinkSubject.onSuccess(expectedConnection)

        observer.assertValueCount(2)
        observer.assertValueAt(0, BlinkEvent.InProgress)
        observer.assertLastValueWithPredicate {
            (it as BlinkEvent.Success).connection == expectedConnection
        }
    }

    /*
    Completion
     */

    @Test
    fun `blink completes after emitting Success`() {
        val blinkSubject = mockSingleParameter()

        val observer = invokeBlink(blinkSubject).test()

        val expectedConnection = mock<KLTBConnection>()
        blinkSubject.onSuccess(expectedConnection)

        observer.assertComplete()
    }

    @Test
    fun `blink completes after emitting Error`() {
        val blinkSubject = mockSingleParameter()
        mockImmediateUnpair()

        val observer = invokeBlink(blinkSubject).test()

        blinkSubject.onError(TestForcedException())

        observer.assertComplete()
    }

    @Test
    fun `blink completes after emitting Timeout`() {
        val blinkSubject = mockSingleParameter()
        mockImmediateUnpair()

        val observer = invokeBlink(blinkSubject).test()

        forceBlinkTimeout()

        observer.assertComplete()
    }

    /*
    Error
     */

    @Test
    fun `blink emits Error after blink emits error`() {
        val blinkSubject = mockSingleParameter()
        mockImmediateUnpair()

        val observer = invokeBlink(blinkSubject).test()

        blinkSubject.onError(TestForcedException())

        observer.assertValueCount(2)
        observer.assertValueAt(0, BlinkEvent.InProgress)
        observer.assertLastValueWithPredicate {
            (it as BlinkEvent.Error).throwable is TestForcedException
        }
    }

    @Test
    fun `blink subscribes to unpair with result mac after blink emits error`() {
        val blinkSubject = mockSingleParameter()
        val unpairSubject = mockUnpair()

        val observer = invokeBlink(blinkSubject).test()

        blinkSubject.onError(TestForcedException())

        assertTrue(unpairSubject.hasObservers())

        observer.assertValueCount(1)

        unpairSubject.onComplete()

        observer.assertValueCount(2)
    }

    /*
    Dispose
     */

    @Test
    fun `blink invokes unpair on mac after dispose without success`() {
        val blinkSubject = mockSingleParameter()
        mockImmediateUnpair()

        val mac = "random"

        val observer = invokeBlink(blinkSubject, mac).test()

        assertTrue(blinkSubject.hasObservers())

        verify(pairingAssistant, never()).unpair(any())

        observer.dispose()

        verify(pairingAssistant).unpair(mac)
    }

    /*
    Timeout
     */

    @Test
    fun `blink attempt times out after 15 seconds and emits a BlinkEvent Timeout`() {
        val blinkSubject = mockSingleParameter()
        mockImmediateUnpair()

        val observer = invokeBlink(blinkSubject).test()

        forceBlinkTimeout()

        observer.assertValues(BlinkEvent.InProgress, BlinkEvent.Timeout)
    }

    /*
    Utils
     */

    private fun invokeBlink(
        blinkSubject: SingleSubject<KLTBConnection>,
        mac: String = DEFAULT_MAC
    ) =
        useCase.blink(blinkSubject, mac)

    private fun mockSingleParameter(): SingleSubject<KLTBConnection> {
        val blinkSubject = SingleSubject.create<KLTBConnection>()
        whenever(pairingAssistant.connectAndBlinkBlue(any()))
            .thenReturn(blinkSubject)

        return blinkSubject
    }

    private fun mockImmediateUnpair() {
        whenever(pairingAssistant.unpair(any())).thenReturn(Completable.complete())
    }

    private fun mockUnpair(): CompletableSubject {
        val unpairSubject = CompletableSubject.create()
        whenever(pairingAssistant.unpair(any()))
            .thenReturn(unpairSubject)

        return unpairSubject
    }

    private fun forceBlinkTimeout() {
        timeoutScheduler.advanceTimeBy(CONNECTION_TIMEOUT.toMillis() + 1, TimeUnit.MILLISECONDS)
    }
}
