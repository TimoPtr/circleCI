/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.core.driver.KLTBDriverListener
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_MAC
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import junit.framework.TestCase
import junit.framework.TestCase.assertFalse
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test

class CE2DriverTest : BaseUnitTest() {
    private val driverListener: KLTBDriverListener = mock()

    private val bleManager: KLNordicBleManager = mock()
    private val streamer: CharacteristicNotificationStreamer = mock()

    private lateinit var driver: TestCE2Driver

    override fun setup() {
        super.setup()

        driver = TestCE2Driver(
            bleManager = bleManager,
            listener = driverListener,
            bluetoothScheduler = Schedulers.io(),
            mac = DEFAULT_MAC,
            streamer = streamer,
            notifyListenerScheduler = TestScheduler()
        )
    }

    /*
    supportsBrushingEventsPolling
     */
    @Test
    fun `supportsBrushingEventsPolling returns false for any FW below 1 6 3`() {
        arrayOf(
            SoftwareVersion(1, 0, 0),
            SoftwareVersion(1, 6, 2),
            SoftwareVersion(1, 5, 3)
        ).forEach {
            driver.testFirmwareVersion = it

            assertFalse("Expected true for $it", invokeSupportsBrushingEventsPolling())
        }
    }

    @Test
    fun `supportsBrushingEventsPolling returns true for any FW equal or over 1 6 3`() {
        arrayOf(
            SoftwareVersion(2, 0, 0),
            SoftwareVersion(3, 0, 0),
            SoftwareVersion(1, 6, 3),
            SoftwareVersion(2, 5, 3)
        ).forEach {
            driver.testFirmwareVersion = it

            assertTrue("Expected false for $it", invokeSupportsBrushingEventsPolling())
        }
    }

    /*
    supportsGRUData
     */
    @Test
    fun `supportsGRUData returns true for any FW below 2 0 0`() {
        arrayOf(
            SoftwareVersion(1, 0, 0),
            SoftwareVersion(1, 6, 2),
            SoftwareVersion(1, 5, 3)
        ).forEach {
            driver.testFirmwareVersion = it

            assertTrue("Expected true for $it", invokeSupportsGRUData())
        }
    }

    @Test
    fun `supportsGRUData returns false for any FW equal or over 2 0 0`() {
        arrayOf(
            SoftwareVersion(2, 0, 0),
            SoftwareVersion(3, 0, 0),
            SoftwareVersion(2, 6, 2),
            SoftwareVersion(2, 5, 3)
        ).forEach {
            driver.testFirmwareVersion = it

            assertFalse("Expected false for $it", invokeSupportsGRUData())
        }
    }

    @Test
    fun `toothbrushModel returns CONNECT_E2`() {
        Assert.assertEquals(ToothbrushModel.CONNECT_E2, driver.testGetToothbrushModel())
    }

    /*
    Utils
     */

    private fun invokeSupportsGRUData(): Boolean {
        return driver.supportsGRUData()
    }

    private fun invokeSupportsBrushingEventsPolling(): Boolean {
        return driver.testSupportsBrushingEventsPolling()
    }

    /*
    supportsReadingBootloader
     */

    @Test
    fun `testSupportsReadingBootloader returns false for any FW below 1 4 1`() {
        assertFalse(driver.isRunningBootloader)

        arrayOf(
            SoftwareVersion(1, 0, 0),
            SoftwareVersion(1, 4, 0),
            SoftwareVersion(0, 5, 3)
        ).forEach {
            driver.testFirmwareVersion = it

            assertFalse("Expected false for $it", driver.testSupportsReadingBootloader())
        }
    }

    @Test
    fun `supportsReadingBootloader returns true if FW greater or equal to 1 4 1 and isRunningBootloader is false`() {
        assertFalse(driver.isRunningBootloader)

        arrayOf(
            SoftwareVersion(2, 0, 0),
            SoftwareVersion(3, 0, 0),
            SoftwareVersion(1, 4, 1),
            SoftwareVersion(1, 5, 0)
        ).forEach {
            driver.testFirmwareVersion = it

            TestCase.assertTrue("Expected true for $it", driver.testSupportsReadingBootloader())
        }
    }

    @Test
    fun `supportsReadingBootloader returns false if isRunningBootloader is true`() {
        driver.setIsRunningBootloader(true)

        arrayOf(
            SoftwareVersion(2, 0, 0),
            SoftwareVersion(3, 0, 0),
            SoftwareVersion(1, 4, 1),
            SoftwareVersion(1, 5, 0),
            SoftwareVersion(1, 0, 0),
            SoftwareVersion(1, 4, 0),
            SoftwareVersion(0, 5, 3)
        ).forEach {
            driver.testFirmwareVersion = it

            assertFalse("Expected true for $it", driver.testSupportsReadingBootloader())
        }
    }

    /*
    overpressureStateFlowable
     */

    @Test
    fun `overpressureStateFlowable emits CommandNotSupportedException`() {
        driver.overpressureStateFlowable()
            .test()
            .assertError(CommandNotSupportedException::class.java)
    }
}

private class TestCE2Driver(
    bleManager: KLNordicBleManager,
    listener: KLTBDriverListener,
    bluetoothScheduler: Scheduler,
    mac: String,
    streamer: CharacteristicNotificationStreamer,
    notifyListenerScheduler: Scheduler
) : CE2Driver(bleManager, listener, bluetoothScheduler, mac, streamer, notifyListenerScheduler) {
    lateinit var testFirmwareVersion: SoftwareVersion

    override fun getFirmwareVersion(): SoftwareVersion {
        return testFirmwareVersion
    }

    fun setIsRunningBootloader(enableRunningBootloader: Boolean) {
        runningBootloader.set(enableRunningBootloader)
    }

    fun testGetToothbrushModel() = toothbrushModel()

    fun testSupportsReadingBootloader() = supportsReadingBootloader()

    fun testSupportsBrushingEventsPolling() = supportsBrushingEventsPolling()
}
