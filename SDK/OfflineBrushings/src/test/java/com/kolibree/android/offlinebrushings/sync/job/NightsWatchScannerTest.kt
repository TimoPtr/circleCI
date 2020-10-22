/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import android.app.PendingIntent
import android.content.Context
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.scan.ToothbrushScanner
import com.kolibree.android.sdk.scan.ToothbrushScannerFactory
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_MAC
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class NightsWatchScannerTest : BaseUnitTest() {
    private val context: Context = mock<Context>().apply {
        whenever(applicationContext).thenReturn(this)
    }
    private val toothbrushScannerFactory: ToothbrushScannerFactory = mock()
    private val scanPendingIntentProvider: ScanPendingIntentProvider = mock()
    private val macsToScanProvider: NightsWatchMacsToScanProvider = mock()

    private val nightsWatchScanner = NightsWatchScannerImpl(
        context,
        toothbrushScannerFactory,
        scanPendingIntentProvider,
        macsToScanProvider
    )

    /*
    startScan
     */

    @Test
    fun `startScan never invokes startScan if macsToScan is empty`() {
        mockMacsToScan()

        val toothbrushScanner = mockToothbrushScanner()

        nightsWatchScanner.startScan()

        verify(macsToScanProvider).provide()
        verify(toothbrushScanner, never()).startScan(any(), any(), any())
    }

    @Test
    fun `startScan returns SCAN_NOT_NEEDED if macsToScan is empty`() {
        mockMacsToScan()

        mockToothbrushScanner()

        assertEquals(StartScanResult.SCAN_NOT_NEEDED, nightsWatchScanner.startScan())
    }

    @Test
    fun `startScan does nothing if getCompatibleBleScanner returns null`() {
        mockOneMacToScan()

        whenever(toothbrushScannerFactory.getCompatibleBleScanner()).thenReturn(null)

        nightsWatchScanner.startScan()

        verifyNoMoreInteractions(scanPendingIntentProvider)
    }

    @Test
    fun `startScan returns SCAN_NOT_NEEDED if getCompatibleBleScanner returns null`() {
        mockOneMacToScan()

        whenever(toothbrushScannerFactory.getCompatibleBleScanner()).thenReturn(null)

        assertEquals(StartScanResult.SCAN_NOT_NEEDED, nightsWatchScanner.startScan())
    }

    @Test
    fun `startScan invokes startScan on getCompatibleBleScanner`() {
        val expectedMacs = mockOneMacToScan()

        val toothbrushScanner = mockToothbrushScanner()

        val expectedPendingIntent = mock<PendingIntent>()
        whenever(scanPendingIntentProvider.provide()).thenReturn(expectedPendingIntent)

        nightsWatchScanner.startScan()

        verify(toothbrushScanner).startScan(context, expectedMacs, expectedPendingIntent)
    }

    @Test
    fun `startScan returns SUCCESS if startScan returns true`() {
        mockOneMacToScan()

        val toothbrushScanner = mockToothbrushScanner()

        whenever(toothbrushScanner.startScan(any(), any(), any())).thenReturn(true)

        val expectedPendingIntent = mock<PendingIntent>()
        whenever(scanPendingIntentProvider.provide()).thenReturn(expectedPendingIntent)

        assertEquals(StartScanResult.SUCCESS, nightsWatchScanner.startScan())
    }

    @Test
    fun `startScan returns FAILURE if startScan returns false`() {
        mockOneMacToScan()

        val toothbrushScanner = mockToothbrushScanner()

        whenever(toothbrushScanner.startScan(any(), any(), any())).thenReturn(false)

        val expectedPendingIntent = mock<PendingIntent>()
        whenever(scanPendingIntentProvider.provide()).thenReturn(expectedPendingIntent)

        assertEquals(StartScanResult.FAILURE, nightsWatchScanner.startScan())
    }

    /*
    stopScan
     */

    @Test
    fun `stopScan does nothing if getCompatibleBleScanner returns null`() {
        whenever(toothbrushScannerFactory.getCompatibleBleScanner()).thenReturn(null)

        nightsWatchScanner.stopScan()
    }

    @Test
    fun `stopScan invokes stopScan on getCompatibleBleScanner`() {
        val toothbrushScanner = mockToothbrushScanner()

        val expectedPendingIntent = mock<PendingIntent>()
        whenever(scanPendingIntentProvider.provide()).thenReturn(expectedPendingIntent)

        nightsWatchScanner.stopScan()

        verify(toothbrushScanner).stopScan(context, expectedPendingIntent)
    }

    /*
    Utils
     */
    private fun mockOneMacToScan() = mockMacsToScan(DEFAULT_MAC)

    private fun mockMacsToScan(vararg macsToScan: String): List<String> {
        val macList = macsToScan.toList()
        whenever(macsToScanProvider.provide()).thenReturn(macList)

        return macList
    }

    private fun mockToothbrushScanner(): ToothbrushScanner {
        val toothbrushScanner = mock<ToothbrushScanner>()
        whenever(toothbrushScannerFactory.getCompatibleBleScanner()).thenReturn(toothbrushScanner)
        return toothbrushScanner
    }
}
