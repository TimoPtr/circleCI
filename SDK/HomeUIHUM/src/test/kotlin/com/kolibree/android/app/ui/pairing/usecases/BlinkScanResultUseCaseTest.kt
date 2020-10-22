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
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.test.mocks.fakeScanResult
import com.kolibree.pairing.assistant.PairingAssistant
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.SingleSubject
import junit.framework.TestCase.assertTrue
import org.junit.Test

class BlinkScanResultUseCaseTest : BaseUnitTest() {

    private val blinkUseCase: BlinkUseCase = mock()
    private val pairingAssistant: PairingAssistant = mock()

    private val useCase = BlinkScanResultUseCase(pairingAssistant, blinkUseCase)

    @Test
    fun `blink invokes blinkUseCase with single from connectAndBlinkBlue`() {
        val scanResult = fakeScanResult()

        val connectAndBlinkSubject = mockConnectAndBlink(scanResult)

        val blinkSubject = PublishSubject.create<BlinkEvent>()
        whenever(blinkUseCase.blink(connectAndBlinkSubject, scanResult.mac))
            .thenReturn(blinkSubject)

        useCase.blink(scanResult).test()

        assertTrue(blinkSubject.hasObservers())
    }

    /*
    Utils
     */

    private fun mockConnectAndBlink(scanResult: ToothbrushScanResult): SingleSubject<KLTBConnection> {
        val blinkSubject = SingleSubject.create<KLTBConnection>()
        whenever(pairingAssistant.connectAndBlinkBlue(scanResult))
            .thenReturn(blinkSubject)

        return blinkSubject
    }
}
