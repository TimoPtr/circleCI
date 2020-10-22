/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.plaqless

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspFlashFileType
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspState
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.driver.ble.CommandSet
import com.kolibree.android.sdk.test.mockDvpCommand
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.test.TestForcedException
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.SingleSubject
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class WakeUpDspUseCaseTest : BaseUnitTest() {
    private val bleDriver: BleDriver = mock()
    private val dspStateUseCase: DspStateUseCase = mock()
    private val delayScheduler = TestScheduler()
    private val timeoutScheduler = TestScheduler()

    private val useCase = WakeUpDspUseCase(
        bleDriver = bleDriver,
        dspStateUseCase = dspStateUseCase,
        delayScheduler = delayScheduler,
        timeoutScheduler = timeoutScheduler
    )

    @Test
    fun `wakeUpCompletable starts with a Ping`() {
        mockPing()

        useCase.wakeUpCompletable().test()

        verify(bleDriver).setAndGetDeviceParameterOnce(CommandSet.ping())
    }

    @Test
    fun `wakeUpCompletable requests DspState 3 seconds after ping success`() {
        val pingSubject = mockPing()
        val dspStateSubject = mockDspState()

        useCase.wakeUpCompletable().test()

        assertFalse(dspStateSubject.hasObservers())

        delayScheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        assertFalse(dspStateSubject.hasObservers())

        pingSubject.signalPingSuccess()

        assertFalse(dspStateSubject.hasObservers())

        delayScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertFalse(dspStateSubject.hasObservers())

        delayScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        assertTrue(dspStateSubject.hasObservers())
    }

    @Test
    fun `when DspState emits DspState with valid firmware, wakeUpCompletable completes`() {
        val pingSubject = mockPing()

        val validFirmwareDspState = validFirmwareDspState()
        whenever(dspStateUseCase.dspStateSingle())
            .thenReturn(Single.just(validFirmwareDspState))

        val observer = useCase.wakeUpCompletable().test()

        pingSubject.signalPingSuccess()

        // 7 secs until timeout
        advanceTimeBy(3)

        observer.assertComplete().assertNoErrors()
    }

    @Test
    fun `when dspStateUseCase emits DspState with valid firmware on a retry, wakeUpCompletable completes`() {
        val pingSubject = mockPing()

        var isFirstSubscription = true
        whenever(dspStateUseCase.dspStateSingle())
            .thenReturn(
                Single.fromCallable {
                    if (isFirstSubscription) {
                        invalidFirmwareDspState().also { isFirstSubscription = false }
                    } else {
                        validFirmwareDspState()
                    }
                }
            )

        val observer = useCase.wakeUpCompletable().test()

        pingSubject.signalPingSuccess()

        advanceTimeBy(3)

        observer.assertNotComplete().assertNoErrors()

        // exoecting a success on 2nd attempt
        advanceTimeBy(1)

        observer.assertComplete().assertNoErrors()
    }

    @Test
    fun `when dspStateUseCase emits an error different than DspNotAwakeException, then we never retry command`() {
        val pingSubject = mockPing()

        var subscriptionCounter = 0
        whenever(dspStateUseCase.dspStateSingle())
            .thenReturn(Single.error<DspState>(TestForcedException())
                .doOnSubscribe { subscriptionCounter++ })

        val observer = useCase.wakeUpCompletable().test()

        pingSubject.signalPingSuccess()

        advanceTimeBy(3)

        observer.assertError(TestForcedException::class.java)

        assertEquals(1, subscriptionCounter)
    }

    @Test
    fun `when DspState has invalid firmware, wakeUpCompletable re-requests DspState every second until timeout`() {
        val pingSubject = mockPing()

        var subscriptionCounter = 0
        whenever(dspStateUseCase.dspStateSingle())
            .thenReturn(Single.just(invalidFirmwareDspState())
                .doOnSubscribe { subscriptionCounter++ })

        val observer = useCase.wakeUpCompletable().test()

        pingSubject.signalPingSuccess()

        // 7 secs until timeout
        advanceTimeBy(3)

        assertEquals(1, subscriptionCounter)

        observer.assertNotComplete().assertNoErrors()

        // 6 secs until timeout
        advanceTimeBy(1)

        assertEquals(2, subscriptionCounter)

        observer.assertNotComplete().assertNoErrors()

        // 1 secs until timeout
        advanceTimeBy(5)

        assertEquals(7, subscriptionCounter)

        observer.assertNotComplete().assertNoErrors()

        // timeout!
        advanceTimeBy(1)

        observer.assertError(TimeoutException::class.java)
    }

    /*
    Utils
     */
    private fun mockPing(): SingleSubject<PayloadReader> {
        return bleDriver.mockDvpCommand(CommandSet.ping())
    }

    private fun mockDspState(): SingleSubject<DspState> {
        return SingleSubject.create<DspState>().apply {
            whenever(dspStateUseCase.dspStateSingle())
                .thenReturn(this)
        }
    }

    private fun SingleSubject<PayloadReader>.signalPingSuccess() {
        onSuccess(PayloadReader(byteArrayOf()))
    }

    private fun advanceTimeBy(seconds: Long) {
        delayScheduler.advanceTimeBy(seconds, TimeUnit.SECONDS)
        timeoutScheduler.advanceTimeBy(seconds, TimeUnit.SECONDS)
    }

    private fun invalidFirmwareDspState(): DspState {
        return DspState(
            hasValidFirmware = false,
            usesDeprecatedFirmwareFormat = false,
            firmwareVersion = DspVersion.NULL,
            flashFileType = DspFlashFileType.NO_FLASH_FILE,
            flashFileVersion = DspVersion.NULL,
            bootloaderVersion = 0
        )
    }

    private fun validFirmwareDspState(): DspState {
        return DspState(
            hasValidFirmware = true,
            usesDeprecatedFirmwareFormat = false,
            firmwareVersion = DspVersion.NULL,
            flashFileType = DspFlashFileType.NO_FLASH_FILE,
            flashFileVersion = DspVersion.NULL,
            bootloaderVersion = 0
        )
    }
}
