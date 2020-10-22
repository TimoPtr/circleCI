/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble

import com.kolibree.android.app.test.CommonBaseTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.core.driver.KLTBDriverListener
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.kolibree.android.sdk.version.SoftwareVersion
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Assert
import org.junit.Test
import org.mockito.Mock

class CB1DriverTest : CommonBaseTest() {

    @Mock
    internal lateinit var driverListener: KLTBDriverListener

    @Mock
    internal lateinit var bleManager: KLNordicBleManager

    private val notifyListenerScheduler: Scheduler = TestScheduler()

    private val caster = CharacteristicNotificationStreamer()

    private lateinit var driver: TestCB1Driver

    override fun setup() {
        super.setup()

        driver = TestCB1Driver(
            bleManager,
            driverListener,
            Schedulers.io(),
            "mac",
            caster,
            notifyListenerScheduler
        )
    }

    @Test
    fun `toothbrushModel returns CONNECT_B1`() {
        Assert.assertEquals(ToothbrushModel.CONNECT_B1, driver.toothbrushModel())
    }

    /*
    calibrationDataSize
     */

    @Test
    fun `calibrationDataSize returns AraDriver CALIBRATION_DATA_SIZE`() {
        assertEquals(AraDriver.CALIBRATION_DATA_SIZE, driver.testCalibrationDataSize())
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

            Assert.assertTrue("Expected false for $it", invokeSupportsBrushingEventsPolling())
        }
    }

    /*
    supportsReadingBootloader
     */

    @Test
    fun `testSupportsReadingBootloader returns false for any FW below 1 4 9`() {
        arrayOf(
            SoftwareVersion(1, 0, 0),
            SoftwareVersion(1, 4, 0),
            SoftwareVersion(1, 4, 8),
            SoftwareVersion(0, 5, 3)
        ).forEach {
            driver.testFirmwareVersion = it

            assertFalse("Expected false for $it", driver.testSupportsReadingBootloader())
        }
    }

    @Test
    fun `supportsReadingBootloader returns true if FW greater or equal to 1 4 9 and isRunningBootloader is false`() {
        assertFalse(driver.isRunningBootloader)

        arrayOf(
            SoftwareVersion(2, 0, 0),
            SoftwareVersion(3, 0, 0),
            SoftwareVersion(1, 4, 9),
            SoftwareVersion(1, 4, 10),
            SoftwareVersion(1, 5, 0)
        ).forEach {
            driver.testFirmwareVersion = it

            assertTrue("Expected true for $it", driver.testSupportsReadingBootloader())
        }
    }

    @Test
    fun `supportsReadingBootloader returns false if isRunningBootloader is true`() {
        driver.setIsRunningBootloader(true)

        arrayOf(
            SoftwareVersion(2, 0, 0),
            SoftwareVersion(3, 0, 0),
            SoftwareVersion(1, 4, 9),
            SoftwareVersion(1, 4, 10),
            SoftwareVersion(1, 5, 0),
            SoftwareVersion(1, 0, 0),
            SoftwareVersion(1, 4, 0),
            SoftwareVersion(1, 4, 8),
            SoftwareVersion(0, 5, 3)
        ).forEach {
            driver.testFirmwareVersion = it

            assertFalse("Expected true for $it", driver.testSupportsReadingBootloader())
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

            assertTrue("Expected true for $it", invokesupportsGRUData())
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

            assertFalse("Expected false for $it", invokesupportsGRUData())
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

    /*
    Utils
    */

    private fun invokesupportsGRUData(): Boolean {
        return driver.supportsGRUData()
    }

    private fun invokeSupportsBrushingEventsPolling(): Boolean {
        return driver.testSupportsBrushingEventsPolling()
    }
}

private class TestCB1Driver(
    bleManager: KLNordicBleManager,
    listener: KLTBDriverListener,
    bluetoothScheduler: Scheduler,
    mac: String,
    streamer: CharacteristicNotificationStreamer,
    notifyListenerScheduler: Scheduler
) : CB1Driver(bleManager, listener, bluetoothScheduler, mac, streamer, notifyListenerScheduler) {
    lateinit var testFirmwareVersion: SoftwareVersion

    override fun getFirmwareVersion(): SoftwareVersion {
        return testFirmwareVersion
    }

    fun setIsRunningBootloader(enableRunningBootloader: Boolean) {
        runningBootloader.set(enableRunningBootloader)
    }

    fun testSupportsReadingBootloader() = supportsReadingBootloader()

    fun testCalibrationDataSize() = calibrationDataSize()
    fun testSupportsBrushingEventsPolling(): Boolean = supportsBrushingEventsPolling()
}
