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
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.sdk.core.driver.KLTBDriverListener
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.SENSORS_DETECTIONS
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.SENSOR_RAW_DATA
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test

internal class KolibreBleDriverKotlinTest : BaseUnitTest() {

    private val driverListener: KLTBDriverListener = mock()

    private val bleManager: KLNordicBleManager = mock()

    private val bluetoothScheduler: Scheduler = Schedulers.io()

    private val notifyListenerScheduler: Scheduler = TestScheduler()

    private val notificationCaster: CharacteristicNotificationStreamer = mock()

    private lateinit var bleDriver: KolibreeBleDriver

    @Before
    @Throws(Exception::class)
    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)

        bleDriver = spy(
            KolibreeBleDriverTest.StubKolibreeBleDriver(
                bleManager,
                driverListener,
                bluetoothScheduler,
                "mac",
                notificationCaster,
                notifyListenerScheduler
            )
        )
    }

    /*
    onNofify
     */
    @Test
    fun `onNotify does nothing if value is null`() {
        val characteristic = SENSOR_RAW_DATA

        bleDriver.onNotify(characteristic.UUID, null)

        verify(notificationCaster, never()).onNewData(any())
    }

    @Test
    fun `onNotify invokes processDeviceParameterNotification if characteristic is DEVICE_PARAMETERS`() {
        val expectedPayload = byteArrayOf(0)
        bleDriver.onNotify(DEVICE_PARAMETERS.UUID, expectedPayload)

        verify(bleDriver).processDeviceParameterNotification(expectedPayload)
        verify(bleDriver, never()).processSensorDetectionNotification(expectedPayload)
        verify(bleDriver, never()).processRawDataNotification(expectedPayload)
    }

    @Test
    fun `onNotify invokes processSensorDetectionNotification if characteristic is SENSORS_DETECTIONS`() {
        val expectedPayload = byteArrayOf(0)
        bleDriver.onNotify(SENSORS_DETECTIONS.UUID, expectedPayload)

        verify(bleDriver).processSensorDetectionNotification(expectedPayload)
        verify(bleDriver, never()).processDeviceParameterNotification(expectedPayload)
        verify(bleDriver, never()).processRawDataNotification(expectedPayload)
    }

    @Test
    fun `onNotify invokes processRawDataNotification if characteristic is SENSOR_RAW_DATA`() {
        val expectedPayload = byteArrayOf(0)
        bleDriver.onNotify(SENSOR_RAW_DATA.UUID, expectedPayload)

        verify(bleDriver).processRawDataNotification(expectedPayload)
        verify(bleDriver, never()).processSensorDetectionNotification(expectedPayload)
        verify(bleDriver, never()).processDeviceParameterNotification(expectedPayload)
    }

    @Test
    fun `onNotify sends payload to notificationCaster for any non-null payload`() {
        arrayOf(SENSOR_RAW_DATA, SENSORS_DETECTIONS, DEVICE_PARAMETERS).forEach { characteristic ->
            val expectedPayload = byteArrayOf(0)
            bleDriver.onNotify(characteristic.UUID, expectedPayload)

            verify(notificationCaster).onNewData(
                BleNotificationData(characteristic.UUID, expectedPayload)
            )
        }
    }
}
