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
import android.os.Handler
import android.os.Message
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.scan.BluetoothWindowedScanner.Companion.STOP_DELAY_MILLIS
import com.kolibree.android.sdk.scan.DeviceScanCallbackTest.Companion.scanCallback
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.android.test.utils.failearly.executeRunnablesImmediately
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import junit.framework.TestCase.assertTrue
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import org.junit.Test
import org.mockito.Mock

class BluetoothWindowedScannerTest : BaseUnitTest() {

    @Mock
    internal lateinit var scanner: BluetoothLeScannerCompat

    @Mock
    internal lateinit var bluetoothUtils: IBluetoothUtils

    @Mock
    internal lateinit var handler: Handler

    private lateinit var windowedScanner: BluetoothWindowedScanner

    override fun setup() {
        super.setup()

        windowedScanner = spy(BluetoothWindowedScanner(scanner, bluetoothUtils, handler))
    }

    /*
    TOKEN FOR CALLBACK
     */
    @Test
    fun `tokenForCallback returns ANY_TOOTHBRUSH_CALLBACK_TOKEN if bluetoothDevice is null`() {
        assertEquals(
            BluetoothWindowedScanner.ANY_TOOTHBRUSH_CALLBACK_TOKEN,
            windowedScanner.tokenForCallback(createCallback())
        )
    }

    @Test
    fun `tokenForCallback returns ANY_TOOTHBRUSH_CALLBACK_TOKEN if bluetoothDevice address is null`() {
        assertEquals(
            BluetoothWindowedScanner.ANY_TOOTHBRUSH_CALLBACK_TOKEN,
            windowedScanner.tokenForCallback(createCallback(mock()))
        )
    }

    @Test
    fun `tokenForCallback returns ANY_TOOTHBRUSH_CALLBACK_TOKEN if bluetoothDevice address is not valid`() {
        val validMac = "ZZ:4B:24:4B:11:A7"
        val device = mock<BluetoothDevice>()
        whenever(device.address).thenReturn(validMac)

        assertEquals(
            BluetoothWindowedScanner.ANY_TOOTHBRUSH_CALLBACK_TOKEN,
            windowedScanner.tokenForCallback(createCallback(device))
        )
    }

    @Test
    fun `tokenForCallback returns value if bluetoothDevice address is valid`() {
        val validMac = "C0:4B:24:4B:11:A7"
        val device = mock<BluetoothDevice>()
        whenever(device.address).thenReturn(validMac)

        assertNotSame(
            BluetoothWindowedScanner.ANY_TOOTHBRUSH_CALLBACK_TOKEN,
            windowedScanner.tokenForCallback(createCallback(device))
        )
    }

    /*
    STOP SCAN
     */
    @Test
    fun `stopScanWithDelay invokes sendMessageDelayed with expected Message, token and delay`() {
        handler.executeRunnablesImmediately()

        val callback = createCallback()

        val expectedToken = 65
        doReturn(expectedToken).whenever(windowedScanner).tokenForCallback(callback)

        val expectedMessage = mock<Message>()
        doReturn(expectedMessage).whenever(windowedScanner).getPostMessage(any(), eq(expectedToken))

        windowedScanner.stopScanWithDelay(callback)

        verify(handler).sendMessageDelayed(expectedMessage, STOP_DELAY_MILLIS)
    }

    @Test
    fun `stopScanWithDelay stores callback token in stopScanTokens`() {
        handler.executeRunnablesImmediately()

        val callback = createCallback()

        val expectedToken = 65
        doReturn(expectedToken).whenever(windowedScanner).tokenForCallback(callback)

        doReturn(mock<Message>()).whenever(windowedScanner).getPostMessage(any(), eq(expectedToken))

        assertTrue(windowedScanner.stopScanTokens.isEmpty())

        windowedScanner.stopScanWithDelay(callback)

        assertEquals(expectedToken, windowedScanner.stopScanTokens.single())
    }

    @Test
    fun `stopScanWithDelay runnable invokes bleScanner stopScan with expected parameters`() {
        handler.executeRunnablesImmediately()

        val callback = createCallback()

        val expectedToken = 65
        doReturn(expectedToken).whenever(windowedScanner).tokenForCallback(callback)

        val expectedMessage = mock<Message>()
        doReturn(expectedMessage).whenever(windowedScanner).getPostMessage(any(), eq(expectedToken))

        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)

        windowedScanner.stopScanWithDelay(callback)

        argumentCaptor<Runnable> {
            verify(windowedScanner).getPostMessage(capture(), eq(expectedToken))

            firstValue.run()

            verify(scanner).stopScan(callback)
        }
    }

    @Test
    fun `stopScanWithDelay runnable invokes stop scan even if bluetooth is not enabled`() {
        handler.executeRunnablesImmediately()

        val callback = createCallback()

        val expectedToken = 65
        doReturn(expectedToken).whenever(windowedScanner).tokenForCallback(callback)

        val expectedMessage = mock<Message>()
        doReturn(expectedMessage).whenever(windowedScanner).getPostMessage(any(), eq(expectedToken))

        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(false)

        windowedScanner.stopScanWithDelay(callback)

        argumentCaptor<Runnable> {
            verify(windowedScanner).getPostMessage(capture(), eq(expectedToken))

            firstValue.run()

            verify(scanner).stopScan(callback)
        }
    }

    /*
    START SCAN
     */
    @Test
    fun `startScan cancels previous stopScans when handler has pending stops for given callback`() {
        handler.executeRunnablesImmediately()

        val callback = createCallback()

        val whatAndToken = 65
        doReturn(whatAndToken).whenever(windowedScanner).tokenForCallback(callback)

        whenever(handler.hasMessages(whatAndToken)).thenReturn(true)

        windowedScanner.startScan(listOf(), mock(), callback)

        verify(handler).removeCallbacksAndMessages(whatAndToken)
    }

    @Test
    fun `startScan removes token from stopScanTokens when handler has pending stops for given callback`() {
        handler.executeRunnablesImmediately()

        val callback = createCallback()

        val whatAndToken = 65
        doReturn(whatAndToken).whenever(windowedScanner).tokenForCallback(callback)

        windowedScanner.stopScanTokens.add(whatAndToken)

        whenever(handler.hasMessages(whatAndToken)).thenReturn(true)

        windowedScanner.startScan(listOf(), mock(), callback)

        assertTrue(windowedScanner.stopScanTokens.isEmpty())
    }

    @Test
    fun `startScan never cancels previous stopScans when handler doesn't have pending stops for given callback`() {
        handler.executeRunnablesImmediately()

        val callback = createCallback()

        val whatAndToken = 65
        doReturn(whatAndToken).whenever(windowedScanner).tokenForCallback(callback)

        whenever(handler.hasMessages(whatAndToken)).thenReturn(false)

        doNothing().whenever(windowedScanner).detectTooManyScans(callback)

        windowedScanner.startScan(listOf(), mock(), callback)

        verify(handler, never()).removeCallbacksAndMessages(whatAndToken)
    }

    @Test
    fun `startScan doesn't invoke startScan if bluetooth is not enabled`() {
        handler.executeRunnablesImmediately()

        val callback = createCallback()

        val whatAndToken = 65
        doReturn(whatAndToken).whenever(windowedScanner).tokenForCallback(callback)

        whenever(handler.hasMessages(whatAndToken)).thenReturn(false)

        doNothing().whenever(windowedScanner).detectTooManyScans(callback)

        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(false)

        val expectedFilters = listOf<ScanFilter>()
        val expectedSettings = mock<ScanSettings>()
        windowedScanner.startScan(expectedFilters, expectedSettings, callback)

        verify(scanner, never()).startScan(expectedFilters, expectedSettings, callback)
    }

    @Test
    fun `startScan invokes startScan when handler doesn't have pending stops for given callback`() {
        handler.executeRunnablesImmediately()

        val callback = createCallback()

        val whatAndToken = 65
        doReturn(whatAndToken).whenever(windowedScanner).tokenForCallback(callback)

        whenever(handler.hasMessages(whatAndToken)).thenReturn(false)

        doNothing().whenever(windowedScanner).detectTooManyScans(callback)

        whenever(bluetoothUtils.isBluetoothEnabled).thenReturn(true)

        val expectedFilters = listOf<ScanFilter>()
        val expectedSettings = mock<ScanSettings>()
        windowedScanner.startScan(expectedFilters, expectedSettings, callback)

        verify(scanner).startScan(expectedFilters, expectedSettings, callback)
    }

    /*
    onBluetoothOff
     */
    @Test
    fun `onBluetoothOff cancels messages and callbacks for tokens stored`() {
        val token1 = 1
        val token2 = 2
        windowedScanner.stopScanTokens.add(token1)
        windowedScanner.stopScanTokens.add(token2)

        windowedScanner.onBluetoothOff()

        verify(handler).removeCallbacksAndMessages(token1)
        verify(handler).removeCallbacksAndMessages(token2)
    }

    @Test
    fun `onBluetoothOff clears stored tokens`() {
        val token1 = 1
        val token2 = 2
        windowedScanner.stopScanTokens.add(token1)
        windowedScanner.stopScanTokens.add(token2)

        windowedScanner.onBluetoothOff()

        assertTrue(windowedScanner.stopScanTokens.isEmpty())
    }

    @Test
    fun `onBluetoothOff cancels messages for decrement counter task`() {
        windowedScanner.onBluetoothOff()

        // ideally we should verify that this happens before setting the value to 0
        verify(handler).removeCallbacksAndMessages(BluetoothWindowedScanner.DECREMENT_COUNTER_TOKEN)
    }

    @Test
    fun `onBluetoothOff sets startScan counter to 0`() {
        windowedScanner.startsDuringRestrictionWindow.set(30)

        windowedScanner.onBluetoothOff()

        assertEquals(0, windowedScanner.startsDuringRestrictionWindow.get())
    }

    /*
    Utils
     */

    private fun createCallback(device: BluetoothDevice? = null): KLScanCallback {
        return scanCallback(bluetoothDevice = device)
    }
}
