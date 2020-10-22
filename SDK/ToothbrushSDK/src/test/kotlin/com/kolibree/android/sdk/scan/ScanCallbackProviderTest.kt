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
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.scan.DeviceScanCallbackTest.Companion.scanCallback
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNotSame
import org.junit.Test

class ScanCallbackProviderTest : BaseUnitTest() {
    private lateinit var scanCallbackProvider: ScanCallbackProvider

    override fun setup() {
        super.setup()

        scanCallbackProvider = spy(ScanCallbackProvider(toothbrushScanResultFactory()))

        doAnswer {
            scanCallback((it.getArgument(0) as ToothbrushScanCallback).bluetoothDevice())
        }.whenever(scanCallbackProvider).scanCallback(any())
    }

    @Test
    fun `callbackForDevice returns callback for AnyToothbrushScanCallback`() {
        assertNotNull(scanCallbackProvider.getOrCreate(NordicBleScannerWrapperTest.createAnyToothbrushCallback()))
    }

    @Test
    fun `callbackForDevice returns same instance for multiple invocations with AnyToothbrushCallback`() {
        assertEquals(
            scanCallbackProvider.getOrCreate(NordicBleScannerWrapperTest.createAnyToothbrushCallback()),
            scanCallbackProvider.getOrCreate(NordicBleScannerWrapperTest.createAnyToothbrushCallback())
        )
    }

    @Test
    fun `callbackForDevice returns same instance for multiple invocations with same callback`() {
        val callback = NordicBleScannerWrapperTest.createAnyToothbrushCallback()
        assertEquals(
            scanCallbackProvider.getOrCreate(callback),
            scanCallbackProvider.getOrCreate(callback)
        )
    }

    @Test
    fun `callbackForDevice returns same instance for multiple invocations with same device`() {
        val device = mock<BluetoothDevice>()

        assertEquals(
            scanCallbackProvider.getOrCreate(
                NordicBleScannerWrapperTest.createSpecificCallback(
                    device
                )
            ),
            scanCallbackProvider.getOrCreate(
                NordicBleScannerWrapperTest.createSpecificCallback(
                    device
                )
            )
        )
    }

    @Test
    fun `callbackForDevice adds listeners to instance returned with same device`() {
        val device = mock<BluetoothDevice>()

        val firstSpecificCallback = NordicBleScannerWrapperTest.createSpecificCallback(device)
        val callback = scanCallbackProvider.getOrCreate(firstSpecificCallback)
        verify(callback.listeners).add(firstSpecificCallback)

        val secondSpecificCallback = NordicBleScannerWrapperTest.createSpecificCallback(device)
        val secondCallback = scanCallbackProvider.getOrCreate(secondSpecificCallback)

        assertEquals(secondCallback, callback)
        verify(secondCallback.listeners).add(secondSpecificCallback)
    }

    @Test
    fun `callbackForDevice returns different instance for multiple invocations with different device`() {
        val device1 = mock<BluetoothDevice>()
        val device2 = mock<BluetoothDevice>()

        assertNotSame(
            scanCallbackProvider.getOrCreate(
                NordicBleScannerWrapperTest.createSpecificCallback(
                    device1
                )
            ),
            scanCallbackProvider.getOrCreate(
                NordicBleScannerWrapperTest.createSpecificCallback(
                    device2
                )
            )
        )
    }

    @Test
    fun `callbackForDevice returns different instance for multiple invocations with different callbacks`() {
        assertNotSame(
            scanCallbackProvider.getOrCreate(NordicBleScannerWrapperTest.createSpecificCallback(mock())),
            scanCallbackProvider.getOrCreate(NordicBleScannerWrapperTest.createAnyToothbrushCallback())
        )
    }
}
