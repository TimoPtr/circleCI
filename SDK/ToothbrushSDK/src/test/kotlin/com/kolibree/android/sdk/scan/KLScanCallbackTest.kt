/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.scan

import android.bluetooth.BluetoothDevice
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.core.notification.ListenerNotifier
import com.kolibree.android.sdk.core.notification.ListenerPool
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import no.nordicsemi.android.support.v18.scanner.ScanResult
import org.junit.Test

internal class KLScanCallbackTest : BaseUnitTest() {
    private val scanResultFactory: ToothbrushScanResultFactory = mock()
    private val bluetoothDevice: BluetoothDevice = mock()
    private val listeners: ListenerPool<ToothbrushScanCallback> = mock()
    private val connectionScannedTracker: ConnectionScannedTracker = mock()

    private val scanCallback =
        KLScanCallback(scanResultFactory, bluetoothDevice, listeners, connectionScannedTracker)

    /*
    onScanResult
     */
    @Test
    fun `onScanResult invokes notifyListeners onToothbrushFound with result from scanResultFactory`() {
        val scanResult = mock<ScanResult>()

        val expectedToothbrushScanResult = mock<ToothbrushScanResult>()
        whenever(scanResultFactory.parseScanResult(scanResult))
            .thenReturn(expectedToothbrushScanResult)

        scanCallback.onScanResult(0, scanResult)

        argumentCaptor<ListenerNotifier<ToothbrushScanCallback>> {
            verify(listeners).notifyListeners(capture())

            val toothbrushScanCallback = mock<AnyToothbrushScanCallback>()
            firstValue.notifyListener(toothbrushScanCallback)

            verify(toothbrushScanCallback).onToothbrushFound(expectedToothbrushScanResult)
        }
    }

    @Test
    fun `onScanResult notifies connectionScannedTracker`() {
        val scanResult = mock<ScanResult>()

        val toothbrushScanResult = mock<ToothbrushScanResult>()
        val expectedMac = "my mac"
        whenever(toothbrushScanResult.mac).thenReturn(expectedMac)
        whenever(scanResultFactory.parseScanResult(scanResult))
            .thenReturn(toothbrushScanResult)

        scanCallback.onScanResult(0, scanResult)

        verify(connectionScannedTracker).onConnectionScanned(expectedMac)
    }

    /*
    addListener
     */
    @Test
    fun `addListener invokes addListener on listeners`() {
        val toothbrushScanCallback = mock<AnyToothbrushScanCallback>()
        scanCallback.addListener(toothbrushScanCallback)

        verify(listeners).add(toothbrushScanCallback)
    }

    /*
    removeListener
     */
    @Test
    fun `removeListener invokes removeListener on listeners`() {
        val toothbrushScanCallback = mock<AnyToothbrushScanCallback>()
        scanCallback.removeListener(toothbrushScanCallback)

        verify(listeners).remove(toothbrushScanCallback)
    }

    /*
    hasListeners
     */
    @Test
    fun `hasListeners returns true if listeners size is greater than 0`() {
        whenever(listeners.size()).thenReturn(1)

        assertTrue(scanCallback.hasListeners())
    }

    @Test
    fun `hasListeners returns false if listeners size is 0`() {
        whenever(listeners.size()).thenReturn(0)

        assertFalse(scanCallback.hasListeners())
    }
}
