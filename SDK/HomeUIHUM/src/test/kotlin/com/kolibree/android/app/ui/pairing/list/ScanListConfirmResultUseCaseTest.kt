/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.list

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.pairing.brush_found.BrushFoundConfirmConnectionUseCase
import com.kolibree.android.app.ui.pairing.usecases.BlinkEvent
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.test.mocks.fakeScanResult
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanListConfirmResultUseCaseTest : BaseUnitTest() {

    private val scanListBlinkConnectionUseCase: ScanListBlinkConnectionUseCase = mock()
    private val brushFoundConfirmConnectionUseCase: BrushFoundConfirmConnectionUseCase = mock()

    private val useCase =
        ScanListConfirmResultUseCase(
            scanListBlinkConnectionUseCase,
            brushFoundConfirmConnectionUseCase
        )

    @Test
    fun `doOnSubscribeBlock is invoked after subscribing to blink`() {
        val scanResult = fakeScanResult()
        val blinkSubject = mockBlinkScanResult(scanResult)

        var blockInvoked = false
        val block = {
            blockInvoked = true
        }

        useCase.confirm(scanResult, doOnSubscribeBlock = block).test()

        assertTrue(blinkSubject.hasObservers())
        assertTrue(blockInvoked)
    }

    @Test
    fun `confirm invokes maybeConfirmConnection only after BlinkEvent Success`() {
        val scanResult = fakeScanResult()
        val blinkSubject = mockBlinkScanResult(scanResult)

        useCase.confirm(scanResult).test()

        blinkSubject.onNext(BlinkEvent.InProgress)

        verify(brushFoundConfirmConnectionUseCase, never()).maybeConfirmConnection()

        blinkSubject.onNext(BlinkEvent.Timeout)

        verify(brushFoundConfirmConnectionUseCase, never()).maybeConfirmConnection()

        blinkSubject.onNext(BlinkEvent.Success(connection = mock()))

        verify(brushFoundConfirmConnectionUseCase).maybeConfirmConnection()
    }

    /*
    Utils
     */

    private fun mockBlinkScanResult(scanResult: ToothbrushScanResult): PublishSubject<BlinkEvent> {
        return scanListBlinkConnectionUseCase.mockBlinkScanResult(scanResult)
    }
}
