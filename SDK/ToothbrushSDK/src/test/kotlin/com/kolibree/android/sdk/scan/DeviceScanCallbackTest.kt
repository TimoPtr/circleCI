/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.sdk.scan

import android.bluetooth.BluetoothDevice
import android.os.ParcelUuid
import android.provider.Settings
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.core.notification.ListenerNotifier
import com.kolibree.android.sdk.util.KolibreeUtils
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.util.UUID
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import no.nordicsemi.android.support.v18.scanner.ScanRecord
import no.nordicsemi.android.support.v18.scanner.ScanResult
import org.junit.Test

internal class DeviceScanCallbackTest : BaseUnitTest() {
    companion object {
        private const val KOLIBREE_V2_START_ADDRESS = "C0:4B:AA"

        fun scanCallback(
            bluetoothDevice: BluetoothDevice? = null,
            connectionScannedTracker: ConnectionScannedTracker = mock()
        ) =
            KLScanCallback(
                toothbrushScanResultFactory(), bluetoothDevice, mock(),
                connectionScannedTracker = connectionScannedTracker
            )
    }

    @Test
    fun scanResult_notKolibreeV2_neverInvokesOnToothbrushFound() {
        val scanResult = mock<ScanResult>()
        val bluetoothDevice = mock<BluetoothDevice>()
        whenever(bluetoothDevice.address).thenReturn("")
        whenever(scanResult.device).thenReturn(bluetoothDevice)

        assertFalse(KolibreeUtils.isKolibreeV2(bluetoothDevice))

        val callback = scanCallback()
        callback.onScanResult(0, scanResult)

        verify(callback.listeners, never()).notifyListeners(any())
    }

    @Test
    fun scanResult_kolibreeV2_nullScanRecord_neverInvokesOnToothbrushFound() {
        val scanResult = mock<ScanResult>()

        attachKolibreeV2Device(scanResult)

        val callback = scanCallback()
        callback.onScanResult(0, scanResult)

        verify(callback.listeners, never()).notifyListeners(any())
    }

    @Test
    fun scanResult_kolibreeV2_nonNullScanRecord_withoutServices_neverInvokesOnToothbrushFound() {
        val scanResult = mock<ScanResult>()

        attachKolibreeV2Device(scanResult)

        attachAraScanRecord(scanResult)

        val callback = scanCallback()
        callback.onScanResult(0, scanResult)

        verify(callback.listeners, never()).notifyListeners(any())
    }

    @Test
    fun scanResult_kolibreeV2_nonNullScanRecord_withServices_invokesOnToothbrushFound() {
        val scanResult = mock<ScanResult>()

        attachKolibreeV2Device(scanResult)

        val scanRecord = attachAraScanRecord(scanResult)

        val servicesList = ArrayList<ParcelUuid>()
        val uuid = UUID.fromString(ToothbrushScanResultFactory.KLTB002_UUID)
        val parcelUuid = mock<ParcelUuid>()
        whenever(parcelUuid.uuid).thenReturn(uuid)
        servicesList.add(parcelUuid)
        whenever(scanRecord.serviceUuids).thenReturn(servicesList)

        val callback = scanCallback()
        callback.onScanResult(0, scanResult)

        argumentCaptor<ListenerNotifier<ToothbrushScanCallback>> {
            verify(callback.listeners).notifyListeners(capture())

            val toothbrushScanCallback = mock<AnyToothbrushScanCallback>()
            firstValue.notifyListener(toothbrushScanCallback)

            argumentCaptor<ToothbrushScanResult> {
                verify(toothbrushScanCallback).onToothbrushFound(capture())

                val toothbrushScanResult = firstValue
                assertNotNull(toothbrushScanResult)

                assertEquals(Settings.Global.DEVICE_NAME, toothbrushScanResult.name)
                assertEquals(KOLIBREE_V2_START_ADDRESS, toothbrushScanResult.mac)
            }
        }
    }

    /*
    UTILS
     */

    private fun attachKolibreeV2Device(scanResult: ScanResult): BluetoothDevice {
        val bluetoothDevice = mock<BluetoothDevice>()
        whenever(bluetoothDevice.name).thenReturn(Settings.Global.DEVICE_NAME)
        whenever(bluetoothDevice.address).thenReturn(KOLIBREE_V2_START_ADDRESS)
        whenever(scanResult.device).thenReturn(bluetoothDevice)

        assertTrue(KolibreeUtils.isKolibreeV2(bluetoothDevice))

        return bluetoothDevice
    }

    private fun attachAraScanRecord(scanResult: ScanResult): ScanRecord {
        return attachRecord(scanResult, ToothbrushModel.ARA)
    }

    private fun attachRecord(scanResult: ScanResult, toothbrushModel: ToothbrushModel): ScanRecord {
        val scanRecord = mock<ScanRecord>()

        val modelIdentifier: Byte = when (toothbrushModel) {
            ToothbrushModel.ARA -> 0x00
            ToothbrushModel.CONNECT_M1 -> 0x01
            ToothbrushModel.CONNECT_E1 -> 0x02
            ToothbrushModel.CONNECT_E2 -> 0x03
            ToothbrushModel.CONNECT_B1 -> 0x04
            ToothbrushModel.PLAQLESS -> 0x05
            else -> 0x00
        }

        val scanRecordBytes =
            byteArrayOf(0xFF.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, modelIdentifier)
        whenever(scanRecord.bytes).thenReturn(scanRecordBytes)
        whenever(scanResult.scanRecord).thenReturn(scanRecord)

        return scanRecord
    }
}
