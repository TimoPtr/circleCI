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
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.core.ScanBeforeReconnectStrategy
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class ScanBeforeConnectFilterTest : BaseUnitTest() {
    private val connectionScannedTracker: ConnectionScannedTracker = mock()
    private val scanBeforeReconnectStrategy: ScanBeforeReconnectStrategy = mock()

    private val scanBeforeConnectFilter =
        ScanBeforeConnectFilter(connectionScannedTracker, scanBeforeReconnectStrategy)

    @Test
    fun `scanBeforeConnect returns false if connectionScannedTracker returns true`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()

        whenever(connectionScannedTracker.isConnectionAlreadyScanned(connection.toothbrush().mac))
            .thenReturn(true)

        assertFalse(scanBeforeConnectFilter.scanBeforeConnect(connection))
    }

    @Test
    fun `scanBeforeConnect returns false if connectionScannedTracker returns false and shouldScanBeforeReconnect returns false`() {
        whenever(connectionScannedTracker.isConnectionAlreadyScanned(any()))
            .thenReturn(false)

        whenever(scanBeforeReconnectStrategy.shouldScanBeforeReconnect(any()))
            .thenReturn(false)

        ToothbrushModel.values()
            .forEach { model ->
                val connection = KLTBConnectionBuilder.createAndroidLess()
                    .withModel(model)
                    .build()

                assertFalse(scanBeforeConnectFilter.scanBeforeConnect(connection))
            }
    }

    @Test
    fun `scanBeforeConnect returns true if connectionScannedTracker returns false and shouldScanBeforeReconnect returns true`() {
        whenever(connectionScannedTracker.isConnectionAlreadyScanned(any()))
            .thenReturn(false)

        whenever(scanBeforeReconnectStrategy.shouldScanBeforeReconnect(any()))
            .thenReturn(true)

        ToothbrushModel.values()
            .forEach { model ->
                val connection = KLTBConnectionBuilder.createAndroidLess()
                    .withModel(model)
                    .build()

                assertTrue(scanBeforeConnectFilter.scanBeforeConnect(connection))
            }
    }
}
