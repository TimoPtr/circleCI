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
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.wake_your_brush.mockBluetoothObservable
import com.kolibree.android.app.ui.pairing.wake_your_brush.mockImmediateUnpairblinking
import com.kolibree.android.app.ui.pairing.wake_your_brush.mockScannerObservable
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.android.test.extensions.advanceTimeBy
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.extensions.assertLastValueWithPredicate
import com.kolibree.android.test.mocks.fakeScanResult
import com.kolibree.pairing.assistant.PairingAssistant
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.temporal.ChronoUnit

internal class AutoExpireScannerTest : BaseUnitTest() {
    private val pairingAssistant: PairingAssistant = mock()
    private val bluetoothUtils: IBluetoothUtils = mock()
    private val sharedFacade: PairingFlowSharedFacade = mock()
    private val timeoutScheduler = TestScheduler()

    private val scanner =
        AutoExpireScanner(pairingAssistant, sharedFacade, bluetoothUtils, timeoutScheduler)

    override fun setup() {
        super.setup()

        sharedFacade.mockImmediateUnpairblinking()
    }

    @Test
    fun `when bluetooth is off, emits BluetoothOff`() {
        mockBluetooth(initialValue = false)

        val scanSubject = pairingAssistant.mockScannerObservable()

        scanner.scan().test().assertValue(AutoExpireScanResult.BluetoothOff).assertNotComplete()

        assertFalse(scanSubject.hasObservers())
    }

    @Test
    fun `when bluetooth goes off and on, automatically resubscribe to scan`() {
        val bluetoothSubject = mockBluetooth(initialValue = true)

        val scanObservable = pairingAssistant.mockScannerObservable()

        val observer = scanner.scan().test().assertValue(emptyScanBatch)

        assertTrue(bluetoothSubject.hasObservers())

        bluetoothSubject.onNext(false)

        observer.assertLastValue(AutoExpireScanResult.BluetoothOff).assertNotComplete()

        assertFalse(scanObservable.hasObservers())

        bluetoothSubject.onNext(true)

        observer.assertLastValue(emptyScanBatch).assertNotComplete()

        assertTrue(scanObservable.hasObservers())

        observer.assertValueCount(3)
    }

    @Test
    fun `when bluetooth is on, emits list with result right after it's received`() {
        mockBluetooth(initialValue = true)

        val scanSubject = pairingAssistant.mockScannerObservable()

        val observer = scanner.scan().test().assertValue(emptyScanBatch)

        val expectedResult1 = fakeScanResult(mac = "1")
        scanSubject.onNext(expectedResult1)

        observer.assertValueCount(2)
        observer.assertLastValueWithPredicate { it == scanBatch(listOf(expectedResult1)) }

        val expectedResult2 = fakeScanResult(mac = "2")
        scanSubject.onNext(expectedResult2)

        observer.assertValueCount(3)
        observer.assertLastValueWithPredicate {
            it == scanBatch(listOf(expectedResult1, expectedResult2))
        }
    }

    @Test
    fun `when bluetooth is on, emits clears scan result after 12 seconds without seeing it`() {
        mockBluetooth(initialValue = true)

        val scanSubject = pairingAssistant.mockScannerObservable()

        val observer = scanner.scan().test().assertValue(emptyScanBatch)

        val expectedResult = fakeScanResult(mac = "1")
        scanSubject.onNext(expectedResult)

        observer.assertValueCount(2)

        advanceTime(seconds = 5)

        observer.assertValueCount(2)

        advanceTime()

        observer.assertLastValueWithPredicate { it is AutoExpireScanResult.Batch && it.results.isEmpty() }

        observer.assertValueCount(3)
    }

    @Test
    fun `when bluetooth is on, emits doesn't clear scan result after 12 seconds if it received it in the meantime`() {
        mockBluetooth(initialValue = true)

        val scanSubject = pairingAssistant.mockScannerObservable()

        val observer = scanner.scan().test().assertValue(emptyScanBatch)

        val expectedResult = fakeScanResult()
        scanSubject.onNext(expectedResult)

        val resultToBeCleared = fakeScanResult(mac = "2")
        scanSubject.onNext(resultToBeCleared)

        advanceTime(seconds = 5)

        scanSubject.onNext(expectedResult)

        advanceTime(cleanupIntervalSeconds() - 1)

        observer.assertLastValueWithPredicate { it == scanBatch(listOf(expectedResult)) }
    }

    /*
    Utils
     */
    private val emptyScanBatch: AutoExpireScanResult = AutoExpireScanResult.Batch(listOf())

    private fun scanBatch(results: List<ToothbrushScanResult>): AutoExpireScanResult.Batch {
        return AutoExpireScanResult.Batch(results)
    }

    private fun advanceTime(seconds: Long = cleanupIntervalSeconds()) {
        TrustedClock.advanceTimeBy(seconds, ChronoUnit.SECONDS)
        timeoutScheduler.advanceTimeBy(seconds, TimeUnit.SECONDS)
    }

    private fun cleanupIntervalSeconds() = CLEANUP_INTERVAL.seconds

    private fun mockBluetooth(initialValue: Boolean): BehaviorSubject<Boolean> {
        return bluetoothUtils.mockBluetoothObservable(initialValue = initialValue)
    }
}
