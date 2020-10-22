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
import com.kolibree.android.sdk.core.binary.Bitmask
import com.kolibree.android.sdk.core.driver.KLTBDriverListener
import com.kolibree.android.sdk.core.driver.VibratorMode.START
import com.kolibree.android.sdk.core.driver.VibratorMode.STOP
import com.kolibree.android.sdk.core.driver.VibratorMode.STOP_AND_HALT_RECORDING
import com.kolibree.android.sdk.core.driver.ble.AraDriver.SLOW_MODE_ADVERTISING_DELAY
import com.kolibree.android.sdk.core.driver.ble.CommandSet.setVibrationPayload
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_MONITOR_CURRENT_BRUSHING
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.schedulers.Schedulers
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AraDriverTest : BaseUnitTest() {

    private val driverListener: KLTBDriverListener = mock()

    private val bleManager: KLNordicBleManager = mock()

    private val notificationCaster = CharacteristicNotificationStreamer()

    private lateinit var driver: AraDriver

    @Before
    @Throws(Exception::class)
    override fun setup() {
        super.setup()

        driver = spy(
            AraDriver(
                bleManager,
                driverListener,
                Schedulers.io(),
                "mac",
                notificationCaster,
                Schedulers.io()
            )
        )
    }

    @Test
    fun `toothbrushModel returns ARA`() {
        assertEquals(ToothbrushModel.ARA, driver.toothbrushModel())
    }

    @Test
    fun `supportsBrushingEventsPolling returns true`() {
        assertTrue(driver.supportsBrushingEventsPolling())
    }

    /*
  UTILS
   */

    private fun sensorPayload(
        svmOn: Boolean,
        rnnOn: Boolean,
        rawDataOn: Boolean,
        handedness: Boolean
    ): ByteArray {
        val streamingBitmask = Bitmask()
        val detectionBitmask = Bitmask()

        streamingBitmask.set(0, rawDataOn).set(1, false).set(2, svmOn || rnnOn)
        detectionBitmask.set(1, false).set(2, rnnOn || svmOn).set(3, true) // Ara uses RNN as SVM

        return byteArrayOf(
            streamingBitmask.get(), detectionBitmask.get(), 50, // Default value
            (if (handedness) 0x01 else 0x00).toByte(), 1
        )
    }

    /*
  ON SENSOR CONTROL

  We don't really test the payload here, I copy&pasted from the diver implementation

  It does serve tho as a test of what to expect from the payload, if we decide to change it one day

  We do test the enable/disable notifications.
   */

    @Test
    fun onSensorControl_svmOn_invokesEnableNotificationsForSensorCharacteristicsAndWritePayloadWithEnableSensorDetectionOn() {
        val svm = true
        val rnn = false
        val raw = false
        val handedness = false

        driver.onSensorControl(svm, rnn, raw, handedness)

        val expectedPayload = sensorPayload(svm, rnn, raw, handedness)
        verify(bleManager).writeSensorStreamingControl(expectedPayload)
    }

    @Test
    fun onSensorControl_rnnOn_invokesEnableNotificationsForSensorCharacteristicsAndWritePayload() {
        val svm = false
        val rnn = true
        val raw = false
        val handedness = false

        driver.onSensorControl(svm, rnn, raw, handedness)

        val expectedPayload = sensorPayload(svm, rnn, raw, handedness)
        verify(bleManager).writeSensorStreamingControl(expectedPayload)
    }

    @Test
    fun onSensorControl_rawDataOn_invokesEnableNotificationsForSensorCharacteristicsAndWritePayload() {
        val svm = false
        val rnn = false
        val raw = true
        val handedness = false

        driver.onSensorControl(svm, rnn, raw, handedness)

        val expectedPayload = sensorPayload(svm, rnn, raw, handedness)
        verify(bleManager).writeSensorStreamingControl(expectedPayload)
    }

    /*
    setVibratorMode
     */
    @Test
    fun `setVibratorMode sends monitor current command before invoking super if parameter is STOP_AND_HALT_RECORDING`() {
        driver.setVibratorMode(STOP_AND_HALT_RECORDING).test()

        inOrder(bleManager) {
            verify(bleManager).setDeviceParameter(
                byteArrayOf(
                    DEVICE_PARAMETERS_MONITOR_CURRENT_BRUSHING
                )
            )
            verify(bleManager).setDeviceParameter(
                setVibrationPayload(
                    STOP_AND_HALT_RECORDING
                )
            )
        }

        verify(bleManager, never()).setDeviceParameter(setVibrationPayload(START))
    }

    @Test
    fun `setVibratorMode doesn't send any extra vibration command before if parameter is STOP`() {
        driver.setVibratorMode(STOP).test()

        verify(bleManager, never()).setDeviceParameter(
            byteArrayOf(
                DEVICE_PARAMETERS_MONITOR_CURRENT_BRUSHING
            )
        )
        verify(bleManager).setDeviceParameter(setVibrationPayload(STOP))
    }

    @Test
    fun `setVibratorMode doesn't send any extra vibration command before if parameter is START`() {
        driver.setVibratorMode(START).test()

        verify(bleManager, never()).setDeviceParameter(
            byteArrayOf(
                DEVICE_PARAMETERS_MONITOR_CURRENT_BRUSHING
            )
        )

        verify(bleManager).setDeviceParameter(setVibrationPayload(START))
    }

    /*
    supportsReadingBootloader
     */

    @Test
    fun `supportsReadingBootloader returns false`() {
        assertFalse(driver.supportsReadingBootloader())
    }

    /*
    forceSlowModeAdvertisingInterval
     */

    @Test
    fun `forceSlowModeAdvertisingInterval calls setDeviceParameter with expected payload`() {
        val expectedPayload = ParameterSet.setAdvertisingIntervalsPayload(
            fastModeIntervalMs = 0L,
            slowModeIntervalMs = SLOW_MODE_ADVERTISING_DELAY
        )

        driver.forceSlowModeAdvertisingInterval()

        verify(bleManager).setDeviceParameter(expectedPayload)
    }

    @Test
    fun `forceSlowModeAdvertisingInterval does nothing in bootloader`() {
        whenever(driver.isRunningBootloader).doReturn(true)

        driver.forceSlowModeAdvertisingInterval()

        verify(bleManager, never()).setDeviceParameter(any())
    }

    /*
    Constants
     */

    @Test
    fun `value of SLOW_MODE_ADVERTISING_DELAY is 1285L`() {
        assertEquals(1285L, SLOW_MODE_ADVERTISING_DELAY)
    }

    /*
    disableMultiUserMode
     */

    @Test
    fun `disableMultiUserMode calls setDeviceParameters with expected payload`() {
        driver.disableMultiUserMode()

        verify(bleManager).setDeviceParameter(ParameterSet.disableMultiUserModePayload())
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
