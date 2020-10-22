/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.KLTBDriverListener
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_OVERPRESSURE_SENSOR_CONTROL
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice.FileType
import com.kolibree.android.test.extensions.assertLastValueWithPredicate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Test

/** [GlintDriver] unit tests */
class GlintDriverTest : BaseUnitTest() {

    private val bleManager: KLNordicBleManager = mock()

    private val listener: KLTBDriverListener = mock()

    private val streamer: CharacteristicNotificationStreamer = mock()

    private val notifyListenerScheduler: Scheduler = mock()

    private lateinit var driver: GlintDriver

    override fun setup() {
        super.setup()

        driver = GlintDriver(
            bleManager = bleManager,
            listener = listener,
            bluetoothScheduler = Schedulers.io(),
            streamer = streamer,
            notifyListenerScheduler = notifyListenerScheduler,
            mac = ""
        )
    }

    /*
    fileType
     */

    @Test
    fun `fileType is GLINT`() {
        assertEquals(FileType.GLINT, driver.fileType())
    }

    /*
    onDeviceParameterNotification
     */

    @Test
    fun `onDeviceParameterNotification parses and emits overpressure sensor's state`() {
        val payloadReader = PayloadReader(byteArrayOf(0x01, 0x01))

        val testObserver = driver.overpressureStateFlowable().test()
        driver.onDeviceParameterNotification(
            commandId = GattCharacteristic.DEVICE_PARAMETERS_OVERPRESSURE_SENSOR_STATE,
            payloadReader = payloadReader
        )

        testObserver.assertValue { it.detectorIsActive && it.uiNotificationIsActive }
    }

    @Test
    fun `onDeviceParameterNotification doesn't emit overpressure state when command ID is not the one`() {
        val payloadReader = PayloadReader(byteArrayOf())

        val testObserver = driver.overpressureStateFlowable().test()
        driver.onDeviceParameterNotification(
            commandId = 0,
            payloadReader = payloadReader
        )

        testObserver.assertNoValues()
    }

    /*
    enableOverpressureDetector
     */

    @Test
    fun `enableOverpressureDetector invokes setAndGetDeviceParameterOnce with expected parameters`() {
        whenever(bleManager.setAndGetDeviceParameter(any()))
            .thenReturn(PayloadReader(byteArrayOf()))

        driver.enableOverpressureDetector(true).test().assertNoErrors().assertComplete()
        verify(bleManager).setAndGetDeviceParameter(
            byteArrayOf(DEVICE_PARAMETERS_OVERPRESSURE_SENSOR_CONTROL, 1)
        )

        driver.enableOverpressureDetector(false).test().assertNoErrors().assertComplete()
        verify(bleManager).setAndGetDeviceParameter(
            byteArrayOf(DEVICE_PARAMETERS_OVERPRESSURE_SENSOR_CONTROL, 0)
        )
    }

    /*
    isOverpressureDetectorEnabled
     */

    @Test
    fun `isOverpressureDetectorEnabled invokes setAndGetDeviceParameterOnce with expected parameters and emits expected result`() {
        whenever(bleManager.setAndGetDeviceParameter(any()))
            .thenReturn(
                PayloadReader(
                    byteArrayOf(
                        DEVICE_PARAMETERS_OVERPRESSURE_SENSOR_CONTROL,
                        0x01
                    )
                )
            )

        driver.isOverpressureDetectorEnabled().test().assertLastValueWithPredicate { it }

        verify(bleManager).setAndGetDeviceParameter(
            byteArrayOf(DEVICE_PARAMETERS_OVERPRESSURE_SENSOR_CONTROL)
        )
    }

    /*
    enablePickupDetector
     */

    @Test
    fun `enablePickupDetector invokes setAndGetDeviceParameterOnce with expected parameters`() {
        whenever(bleManager.setAndGetDeviceParameter(any()))
            .thenReturn(PayloadReader(byteArrayOf()))

        driver.enablePickupDetector(true).test().assertNoErrors().assertComplete()
        verify(bleManager).setAndGetDeviceParameter(
            byteArrayOf(GattCharacteristic.DEVICE_PARAMETERS_PICKUP_DETECTION_CONTROL, 1)
        )

        driver.enablePickupDetector(false).test().assertNoErrors().assertComplete()
        verify(bleManager).setAndGetDeviceParameter(
            byteArrayOf(GattCharacteristic.DEVICE_PARAMETERS_PICKUP_DETECTION_CONTROL, 0)
        )
    }
}
