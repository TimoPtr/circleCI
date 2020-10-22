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
import android.content.Intent
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanResultProcessorTest : BaseUnitTest() {
    private val connectionScannedTracker: ConnectionScannedTracker = mock()

    private val scanResultExtractor = IntentScanResultProcessor(connectionScannedTracker)

    @Test
    fun readMacs_withErrorIntent_returnsEmptyLIst() {
        val list = scanResultExtractor.process(
            mock<Intent>().apply {
                whenever(hasExtra(BluetoothLeScannerCompat.EXTRA_ERROR_CODE))
                    .thenReturn(true)
            }
        )

        assertTrue(list.isEmpty())
    }

    @Test
    fun readMacs_withErrorIntent_neverInvokesConnectionScannedTracker() {
        scanResultExtractor.process(
            mock<Intent>().apply {
                whenever(hasExtra(BluetoothLeScannerCompat.EXTRA_ERROR_CODE))
                    .thenReturn(true)
            }
        )

        verify(connectionScannedTracker, never()).onConnectionScanned(any())
    }

    @Test
    fun readMacs_withEmptyResults_returnsEmptyList() {
        assertTrue(scanResultExtractor.process(intentWithResults()).isEmpty())
    }

    @Test
    fun readMacs_withResults_returnsListWithMacAddresses() {
        val expectedMac1 = "mac1"
        val expectedMac2 = "mac2"

        val expectedList = listOf(expectedMac1, expectedMac2)

        assertEquals(
            expectedList,
            scanResultExtractor.process(
                intentWithResults(createScanResult(expectedMac1), createScanResult(expectedMac2))
            )
        )
    }

    @Test
    fun readMacs_withResults_invokesConnectionScannedTrackerOnEachResult() {
        val expectedMac1 = "mac1"
        val expectedMac2 = "mac2"

        scanResultExtractor.process(
            intentWithResults(createScanResult(expectedMac1), createScanResult(expectedMac2))
        )

        verify(connectionScannedTracker).onConnectionScanned(expectedMac1)
        verify(connectionScannedTracker).onConnectionScanned(expectedMac2)
    }

    /*
    Utils
     */

    private fun createScanResult(mac: String): ScanResult {
        val scanResult = mock<ScanResult>()
        val bleDevice = mock<BluetoothDevice>()

        whenever(bleDevice.address).thenReturn(mac)

        whenever(scanResult.device).thenReturn(bleDevice)

        return scanResult
    }

    private fun intentWithResults(vararg scanResults: ScanResult): Intent {
        return mock<Intent>().apply {
            whenever(hasExtra(BluetoothLeScannerCompat.EXTRA_ERROR_CODE))
                .thenReturn(false)

            whenever(getParcelableArrayListExtra<ScanResult>(BluetoothLeScannerCompat.EXTRA_LIST_SCAN_RESULT))
                .thenReturn(ArrayList(scanResults.toList()))
        }
    }
}
