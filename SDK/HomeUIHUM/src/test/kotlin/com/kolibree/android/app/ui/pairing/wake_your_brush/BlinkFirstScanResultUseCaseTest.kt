/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.wake_your_brush

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.pairing.usecases.BlinkEvent
import com.kolibree.android.app.ui.pairing.usecases.BlinkScanResultUseCase
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.test.mocks.fakeScanResult
import com.kolibree.pairing.assistant.PairingAssistant
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.PublishSubject
import junit.framework.TestCase.assertTrue
import org.junit.Test

class BlinkFirstScanResultUseCaseTest : BaseUnitTest() {
    private val pairingAssistant: PairingAssistant = mock()
    private val blinkScanResultUseCase: BlinkScanResultUseCase = mock()

    private val useCase = BlinkFirstScanResultUseCase(pairingAssistant, blinkScanResultUseCase)

    @Test
    fun `blinkFirstScanResult attempts to blink first scan result`() {
        val scanSubject = pairingAssistant.mockScannerObservable()
        val blinkResultSubject = mockBlinkResultUseCase()

        useCase.blinkFirstScanResult().test()

        val expectedScanResult = fakeScanResult()
        scanSubject.onNext(expectedScanResult)

        verify(blinkScanResultUseCase).blink(expectedScanResult)
        assertTrue(blinkResultSubject.hasObservers())
    }

    @Test
    fun `blinkFirstScanResult ignores every scan result after the first one`() {
        val scanSubject = pairingAssistant.mockScannerObservable()
        mockBlinkResultUseCase()

        useCase.blinkFirstScanResult().test()

        scanSubject.onNext(fakeScanResult())
        scanSubject.onNext(fakeScanResult())
        scanSubject.onNext(fakeScanResult())
        scanSubject.onNext(fakeScanResult())

        verify(blinkScanResultUseCase, times(1)).blink(any())
    }

    /*
    Utils
     */

    private fun mockBlinkResultUseCase(): PublishSubject<BlinkEvent> {
        val blinkSubject = PublishSubject.create<BlinkEvent>()
        whenever(blinkScanResultUseCase.blink(any())).thenReturn(blinkSubject)

        return blinkSubject
    }
}

internal fun PairingAssistant.mockScannerObservable(): PublishSubject<ToothbrushScanResult> {
    val subject = PublishSubject.create<ToothbrushScanResult>()
    whenever(scannerObservable()).thenReturn(subject)

    return subject
}
