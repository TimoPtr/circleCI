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
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.sdk.connection.detectors.data.plaqlessRawSensorNotificationData
import com.kolibree.android.sdk.connection.detectors.data.plaqlessSensorNotificationData
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.sdk.core.driver.KLTBDriverListener
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.PLAQLESS_DETECTOR_CHAR
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.PLAQLESS_IMU_CHAR
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.kolibree.android.sdk.plaqless.PlaqlessRingLedState
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotSame
import junit.framework.TestCase.assertTrue
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

internal class PlaqlessDriverTest : BaseUnitTest() {

    @Mock
    internal lateinit var driverListener: KLTBDriverListener

    @Mock
    internal lateinit var bleManager: KLNordicBleManager

    private val notificationCaster = CharacteristicNotificationStreamer()

    private lateinit var driver: TestPlaqlessDriver

    @Before
    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)

        driver =
            TestPlaqlessDriver(
                bleManager,
                driverListener,
                Schedulers.io(),
                "mac",
                notificationCaster,
                notifyListenerScheduler = TestScheduler()
            )
    }

    @Test
    fun `toothbrushModel returns PLAQLESS`() {
        Assert.assertEquals(ToothbrushModel.PLAQLESS, driver.testGetToothbrushModel())
    }

    @Test
    fun `calibrationDataSize returns AraDriver CALIBRATION_DATA_SIZE`() {
        assertEquals(
            AraDriver.CALIBRATION_DATA_SIZE,
            (driver as KolibreeBleDriver).calibrationDataSize()
        )
    }

    @Test
    fun `onDeviceParameterNotification is call when DEVICE_PARAMETERS and PLAQLESS_RING_LED_STATE`() {
        driver = spy(driver)
        val payloadWriter = PayloadWriter(5)
        payloadWriter.writeByte(GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_RING_LED_STATE)
        payloadWriter.writeUnsignedInt8(0)
        payloadWriter.writeUnsignedInt8(0)
        payloadWriter.writeUnsignedInt8(0)
        payloadWriter.writeUnsignedInt8(0)
        driver.onNotify(GattCharacteristic.DEVICE_PARAMETERS.UUID, payloadWriter.bytes)

        verify((driver as KolibreeBleDriver))
            .onDeviceParameterNotification(
                eq(GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_RING_LED_STATE),
                any()
            )
    }

    @Test
    fun `onNotify with DEVICE_PARAMETERS and PLAQLESS_RING_LED_STATE accept in plaqlessRingLedStateRelay`() {
        driver = spy(driver)
        val payloadWriter = PayloadWriter(5)
        payloadWriter.writeByte(GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_RING_LED_STATE)
        payloadWriter.writeUnsignedInt8(0)
        payloadWriter.writeUnsignedInt8(0)
        payloadWriter.writeUnsignedInt8(0)
        payloadWriter.writeUnsignedInt8(0)
        val testObserver = driver.plaqlessRingLedStateRelay.test()
        driver.onNotify(GattCharacteristic.DEVICE_PARAMETERS.UUID, payloadWriter.bytes)

        testObserver.assertValues(PlaqlessRingLedState(0, 0, 0, 0))
    }

    /*
    plaqlessRawDataNotifications
     */
    @Test
    fun `plaqlessRawDataNotifications does not enable PlaqlessRawDataNotifications if no one subscribes`() {
        driver.plaqlessRawDataNotifications()

        verify(bleManager, never()).enablePlaqlessRawDataNotifications()
    }

    @Test
    fun `plaqlessRawDataNotifications enables PlaqlessRawDataNotifications on subscribe`() {
        driver.plaqlessRawDataNotifications().test()

        verify(bleManager).enablePlaqlessRawDataNotifications()
    }

    @Test
    fun `plaqlessRawDataNotifications only enables PlaqlessRawDataNotifications once for multiple subscribers`() {
        driver.plaqlessRawDataNotifications().test()
        driver.plaqlessRawDataNotifications().test()

        verify(bleManager, times(1)).enablePlaqlessRawDataNotifications()
    }

    @Test
    fun `plaqlessRawDataNotifications disables PlaqlessRawDataNotifications on dispose`() {
        val observer = driver.plaqlessRawDataNotifications().test()

        verify(bleManager, never()).disablePlaqlessRawDataNotifications()

        observer.dispose()

        verify(bleManager).disablePlaqlessRawDataNotifications()
    }

    @Test
    fun `plaqlessRawDataNotifications disables PlaqlessRawDataNotifications only after all observers have unsubscribed`() {
        val firstObserver = driver.plaqlessRawDataNotifications().test()
        val secondObserver = driver.plaqlessRawDataNotifications().test()

        verify(bleManager, never()).disablePlaqlessRawDataNotifications()

        firstObserver.dispose()

        verify(bleManager, never()).disablePlaqlessRawDataNotifications()

        secondObserver.dispose()

        verify(bleManager, times(1)).disablePlaqlessRawDataNotifications()
    }

    @Test
    fun `plaqlessRawDataNotifications returns same Flowable if the first subscriber didn't unsubscribe`() {
        val firstFlowable = driver.plaqlessRawDataNotifications()
        firstFlowable.test()

        assertEquals(firstFlowable, driver.plaqlessRawDataNotifications())
    }

    @Test
    fun `plaqlessRawDataNotifications returns different Flowable after the first subscriber unsubscribed`() {
        val firstFlowable = driver.plaqlessRawDataNotifications()
        val observer = firstFlowable.test()

        assertEquals(firstFlowable, driver.plaqlessRawDataNotifications())

        observer.dispose()

        assertNotSame(firstFlowable, driver.plaqlessRawDataNotifications())
    }

    @Test
    fun `plaqlessRawDataNotifications emits data for PLAQLESS_IMU_CHAR`() {
        val observer = driver.plaqlessRawDataNotifications().test().assertEmpty()

        val expectedArray = plaqlessRawSensorNotificationData()
        notificationCaster.onNewData(BleNotificationData(PLAQLESS_IMU_CHAR.UUID, expectedArray))

        observer.assertValueCount(1)
    }

    @Test
    fun `plaqlessRawDataNotifications does not emit data for other characteristics`() {
        val observer = driver.plaqlessRawDataNotifications().test().assertEmpty()

        GattCharacteristic.values().filterNot { it == PLAQLESS_IMU_CHAR }.forEach {
            notificationCaster.onNewData(BleNotificationData(it.UUID, byteArrayOf()))
        }

        observer.assertEmpty()
    }

    /*
    plaqlessNotifications
    */

    @Test
    fun `plaqlessNotifications does not enable PlaqlessNotifications if no one subscribes`() {
        driver.plaqlessNotifications()

        verify(bleManager, never()).enablePlaqlessNotifications()
    }

    @Test
    fun `plaqlessNotifications enables PlaqlessNotifications on subscribe`() {
        driver.plaqlessNotifications().test()

        verify(bleManager).enablePlaqlessNotifications()
    }

    @Test
    fun `plaqlessNotifications only enables PlaqlessNotifications once for multiple subscribers`() {
        driver.plaqlessNotifications().test()
        driver.plaqlessNotifications().test()

        verify(bleManager, times(1)).enablePlaqlessNotifications()
    }

    @Test
    fun `plaqlessNotifications disables PlaqlessNotifications on dispose`() {
        val observer = driver.plaqlessNotifications().test()

        verify(bleManager, never()).disablePlaqlessNotifications()

        observer.dispose()

        verify(bleManager).disablePlaqlessNotifications()
    }

    @Test
    fun `plaqlessNotifications disables PlaqlessNotifications only after all observers have unsubscribed`() {
        val firstObserver = driver.plaqlessNotifications().test()
        val secondObserver = driver.plaqlessNotifications().test()

        verify(bleManager, never()).disablePlaqlessNotifications()

        firstObserver.dispose()

        verify(bleManager, never()).disablePlaqlessNotifications()

        secondObserver.dispose()

        verify(bleManager, times(1)).disablePlaqlessNotifications()
    }

    @Test
    fun `plaqlessNotifications returns same Flowable if the first subscriber didn't unsubscribe`() {
        val firstFlowable = driver.plaqlessNotifications()
        firstFlowable.test()

        assertEquals(firstFlowable, driver.plaqlessNotifications())
    }

    @Test
    fun `plaqlessNotifications returns different Flowable after the first subscriber unsubscribed`() {
        val firstFlowable = driver.plaqlessNotifications()
        val observer = firstFlowable.test()

        assertEquals(firstFlowable, driver.plaqlessNotifications())

        observer.dispose()

        assertNotSame(firstFlowable, driver.plaqlessNotifications())
    }

    @Test
    fun `plaqlessNotifications emits data for PLAQLESS_DETECTOR_CHAR`() {
        val observer = driver.plaqlessNotifications().test().assertEmpty()

        notificationCaster.onNewData(
            BleNotificationData(PLAQLESS_DETECTOR_CHAR.UUID, plaqlessSensorNotificationData())
        )

        observer.assertValueCount(1)
    }

    @Test
    fun `plaqlessNotifications does not emit data for other characteristics`() {
        val observer = driver.plaqlessNotifications().test().assertEmpty()

        GattCharacteristic.values().filterNot { it == PLAQLESS_DETECTOR_CHAR }.forEach {
            notificationCaster.onNewData(BleNotificationData(it.UUID, byteArrayOf()))
        }

        observer.assertEmpty()
    }

    /*
    supportsBrushingEventsPolling
     */
    @Test
    fun `supportsBrushingEventsPolling returns false for any FW below 2 0 10`() {
        arrayOf(
            SoftwareVersion(2, 0, 9),
            SoftwareVersion(1, 6, 2),
            SoftwareVersion(1, 5, 3)
        ).forEach {
            driver.testFirmwareVersion = it

            assertFalse("Expected true for $it", invokeSupportsBrushingEventsPolling())
        }
    }

    @Test
    fun `supportsBrushingEventsPolling returns true for any FW equal or over 2 0 10`() {
        arrayOf(
            SoftwareVersion(2, 0, 10),
            SoftwareVersion(3, 0, 0),
            SoftwareVersion(2, 6, 11),
            SoftwareVersion(2, 5, 3)
        ).forEach {
            driver.testFirmwareVersion = it

            assertTrue("Expected false for $it", invokeSupportsBrushingEventsPolling())
        }
    }

    /*
    plaqlessRingLedState
    */

    @Test
    fun `plaqlessRingLedState does not setAndGetDeviceParameter if no one subscribes`() {
        driver.plaqlessRingLedState()

        verify(bleManager, never()).setAndGetDeviceParameter(any())
    }

    @Test
    fun `plaqlessRingLedState setAndGetDeviceParameter on subscribe`() {
        mockSetAndGetDeviceParameter()
        driver.plaqlessRingLedState().test()

        verify(bleManager).setAndGetDeviceParameter(any())
    }

    @Test
    fun `plaqlessRingLedState returns new Floawable each times`() {
        mockSetAndGetDeviceParameter()
        val flowable1 = driver.plaqlessRingLedState()
        val flowable2 = driver.plaqlessRingLedState()

        assertNotSame(flowable1, flowable2)
    }

    @Test
    fun `plaqlessRingLedState subscribe to plaqlessRingLedStateRelay`() {
        mockSetAndGetDeviceParameter()
        driver.plaqlessRingLedState().test()

        assertTrue(driver.plaqlessRingLedStateRelay.hasObservers())
    }

    @Test
    fun `plaqlessRingLedState merge plaqlessRingLedStateRelay and getPlaqlessRingState`() {
        val expectedColor1: Short = 0
        val expectedColor2: Short = 1
        mockSetAndGetDeviceParameter(expectedColor1)
        val testObserver = driver.plaqlessRingLedState().test()
        driver.plaqlessRingLedStateRelay.accept(
            PlaqlessRingLedState(
                expectedColor2,
                expectedColor2,
                expectedColor2,
                expectedColor2
            )
        )
        testObserver.assertValueCount(2)
        testObserver.assertValues(
            PlaqlessRingLedState(expectedColor1, expectedColor1, expectedColor1, expectedColor1),
            PlaqlessRingLedState(expectedColor2, expectedColor2, expectedColor2, expectedColor2)
        )
    }

    /*
    getPlaqlessRingState
    */

    @Test
    fun `getPlaqlessRingState call setAndGetDeviceParameter with DEVICE_PARAMETERS_PLAQLESS_RING_LED_STATE`() {
        val expectedColor: Short = 0
        mockSetAndGetDeviceParameter(expectedColor)
        val result = driver.getPlaqlessRingState()

        assertEquals(
            PlaqlessRingLedState(
                expectedColor,
                expectedColor,
                expectedColor,
                expectedColor
            ), result
        )
        verify(bleManager)
            .setAndGetDeviceParameter(
                eq(byteArrayOf(GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_RING_LED_STATE))
            )
    }

    private fun mockSetAndGetDeviceParameter(expectedColor: Short = 0) {
        val payloadReader = mock<PayloadReader>()
        whenever(payloadReader.skip(1)).thenReturn(payloadReader)
        whenever(payloadReader.readUnsignedInt8()).thenReturn(expectedColor)
        whenever(bleManager.setAndGetDeviceParameter(any())).thenReturn(payloadReader)
    }

    /*
    supportsGRUData
     */
    @Test
    fun `supportsGRUData returns false`() {
        assertFalse(driver.supportsGRUData())
    }

    /*
    supportsReadingBootloader
     */

    @Test
    fun `testSupportsReadingBootloader returns false for any FW below 2 0 0 and isRunningBootloader is false`() {
        assertFalse(driver.isRunningBootloader)

        arrayOf(
            SoftwareVersion(1, 0, 0),
            SoftwareVersion(1, 4, 0),
            SoftwareVersion(1, 9, 9)
        ).forEach {
            driver.testFirmwareVersion = it

            assertFalse("Expected false for $it", driver.testSupportsReadingBootloader())
        }
    }

    @Test
    fun `supportsReadingBootloader returns true if FW greater or equal to 2 0 0 and isRunningBootloader is false`() {
        driver.setIsRunningBootloader(false)

        arrayOf(
            SoftwareVersion(2, 0, 0),
            SoftwareVersion(3, 0, 0),
            SoftwareVersion(2, 0, 1)
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
            SoftwareVersion(2, 0, 1),
            SoftwareVersion(1, 0, 0),
            SoftwareVersion(1, 4, 0),
            SoftwareVersion(1, 9, 9)
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

    /*
    Utils
     */

    private fun invokeSupportsBrushingEventsPolling(): Boolean {
        return driver.testSupportsBrushingEventsPolling()
    }
}

private class TestPlaqlessDriver(
    bleManager: KLNordicBleManager,
    listener: KLTBDriverListener,
    bluetoothScheduler: Scheduler,
    mac: String,
    streamer: CharacteristicNotificationStreamer,
    notifyListenerScheduler: Scheduler
) : PlaqlessDriver(
    bleManager,
    listener,
    bluetoothScheduler,
    mac,
    streamer,
    notifyListenerScheduler
) {
    lateinit var testFirmwareVersion: SoftwareVersion

    override fun getFirmwareVersion(): SoftwareVersion {
        return testFirmwareVersion
    }

    fun testGetToothbrushModel() = toothbrushModel()

    fun setIsRunningBootloader(enableRunningBootloader: Boolean) {
        runningBootloader.set(enableRunningBootloader)
    }

    fun testSupportsReadingBootloader() = supportsReadingBootloader()
    fun testSupportsBrushingEventsPolling(): Boolean = supportsBrushingEventsPolling()
}
