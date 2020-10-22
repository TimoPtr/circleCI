/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.scan

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class EstablishConnectionFilterTest : BaseUnitTest() {
    private val connectionScannedTracker: ConnectionScannedTracker = mock()

    private val establishConnectFilter = EstablishConnectionFilter(connectionScannedTracker)

    @Test
    fun `canAttemptConnection returns true if isConnectionAlreadyScanned returns true`() {
        val mac = KLTBConnectionBuilder.DEFAULT_MAC

        whenever(connectionScannedTracker.isConnectionAlreadyScanned(mac))
            .thenReturn(true)

        assertTrue(establishConnectFilter.canAttemptConnection(mac))
    }

    @Test
    fun `canAttemptConnection returns true if isConnectionAlreadyScanned returns false and no one invoked disableScanBeforeConnect`() {
        val mac = KLTBConnectionBuilder.DEFAULT_MAC

        whenever(connectionScannedTracker.isConnectionAlreadyScanned(mac))
            .thenReturn(false)

        assertTrue(establishConnectFilter.canAttemptConnection(mac))
    }

    @Test
    fun `canAttemptConnection returns true if isConnectionAlreadyScanned returns false and enableScanBeforeConnect was invoked`() {
        val mac = KLTBConnectionBuilder.DEFAULT_MAC

        whenever(connectionScannedTracker.isConnectionAlreadyScanned(mac))
            .thenReturn(false)

        establishConnectFilter.enableScanBeforeConnect()

        assertTrue(establishConnectFilter.canAttemptConnection(mac))
    }

    @Test
    fun `canAttemptConnection returns false if isConnectionAlreadyScanned returns false and disableScanBeforeConnect was invoked`() {
        val mac = KLTBConnectionBuilder.DEFAULT_MAC

        whenever(connectionScannedTracker.isConnectionAlreadyScanned(mac))
            .thenReturn(false)

        establishConnectFilter.disableScanBeforeConnect()

        assertFalse(establishConnectFilter.canAttemptConnection(mac))
    }

    @Test
    fun `canAttemptConnection returns false if isConnectionAlreadyScanned returns false the last invocation was disableScanBeforeConnect`() {
        val mac = KLTBConnectionBuilder.DEFAULT_MAC

        whenever(connectionScannedTracker.isConnectionAlreadyScanned(mac))
            .thenReturn(false)

        establishConnectFilter.enableScanBeforeConnect()
        establishConnectFilter.enableScanBeforeConnect()
        establishConnectFilter.disableScanBeforeConnect()
        establishConnectFilter.enableScanBeforeConnect()
        establishConnectFilter.disableScanBeforeConnect()

        assertFalse(establishConnectFilter.canAttemptConnection(mac))
    }

    @Test
    fun `canAttemptConnection returns true if isConnectionAlreadyScanned returns false the last invocation was enableScanBeforeConnect`() {
        val mac = KLTBConnectionBuilder.DEFAULT_MAC

        whenever(connectionScannedTracker.isConnectionAlreadyScanned(mac))
            .thenReturn(false)

        establishConnectFilter.enableScanBeforeConnect()
        establishConnectFilter.enableScanBeforeConnect()
        establishConnectFilter.disableScanBeforeConnect()
        establishConnectFilter.enableScanBeforeConnect()
        establishConnectFilter.disableScanBeforeConnect()
        establishConnectFilter.enableScanBeforeConnect()

        assertTrue(establishConnectFilter.canAttemptConnection(mac))
    }
}
