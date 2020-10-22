/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.toothbrush

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.GLINT
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.connection.toothbrush.SwitchOffMode
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspState
import com.kolibree.android.sdk.connection.toothbrush.led.LedPattern
import com.kolibree.android.sdk.connection.toothbrush.led.SpecialLed
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.BaseDriver
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.driver.ble.CommandSet
import com.kolibree.android.sdk.core.driver.ble.ParameterSet
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.android.test.TestForcedException
import com.kolibree.kml.MouthZone16
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.Duration

/** Created by miguelaragues on 10/4/18.  */
internal class ToothbrushBaseBleImplTest : BaseUnitTest() {

    private lateinit var toothbrushBase: StubToothbrushBaseBleImpl

    private val bleDriver: BleDriver = mock()

    override fun setup() {
        super.setup()

        bleDriver.mockDriverVersions()
        toothbrushBase = spy(
            StubToothbrushBaseBleImpl(DEFAULT_MAC, CONNECT_E1, bleDriver, DEFAULT_NAME)
        )
    }

    /*
  ping
   */

    @Test
    fun `ping does not send command if it's running bootloader`() {
        whenever(bleDriver.isRunningBootloader).thenReturn(true)

        toothbrushBase.ping().test().assertComplete()

        verify(bleDriver, never()).setDeviceParameter(any())
    }

    @Test
    fun `ping sends command if we are not running bootloader`() {
        whenever(bleDriver.isRunningBootloader).thenReturn(false)

        toothbrushBase.ping().test().assertComplete()

        verify(bleDriver).setDeviceParameter(CommandSet.ping())
    }

    @Test
    fun `ping emits error from driver`() {
        whenever(bleDriver.isRunningBootloader).thenReturn(false)

        whenever(bleDriver.setDeviceParameter(CommandSet.ping())).thenThrow(TestForcedException())

        toothbrushBase.ping().test().assertError(TestForcedException::class.java)
    }

    /*
  PLAY LED SIGNAL
   */

    @Test
    fun `playLedSignal supportsLedPlaySignal false doesNothing`() {
        whenever(toothbrushBase.supportsLedPlaySignal()).thenReturn(false)

        toothbrushBase
            .playLedSignal(0x01.toByte(), 0x01.toByte(), 0x01.toByte(), LedPattern.FIXED, 0, 0)
            .test()
            .assertComplete()
        verify(bleDriver, never()).sendCommand(anyOrNull())
    }

    @Test
    fun `playLedSignal supportsLedPlaySignal true invokes driver sendCommand with ExpectedPayload`() {
        val red: Byte = 1
        val green: Byte = 2
        val blue: Byte = 2
        val period = 7
        val duration = 8
        val pattern = mock<LedPattern>()
        val expectedPayload = CommandSet.playLedSignal(red, green, blue, pattern, period, duration)

        whenever(toothbrushBase.supportsLedPlaySignal()).thenReturn(true)

        toothbrushBase
            .playLedSignal(red, green, blue, pattern, period, duration)
            .test()
            .assertComplete()

        verify(bleDriver).sendCommand(expectedPayload)
    }

    @Test
    fun `playLedSignal driver sendCommand throws exception`() {
        val red: Byte = 1
        val green: Byte = 2
        val blue: Byte = 2
        val period = 7
        val duration = 8
        val pattern = mock<LedPattern>()

        whenever(toothbrushBase.supportsLedPlaySignal()).thenReturn(true)

        whenever(bleDriver.sendCommand(anyOrNull())).thenThrow(Exception("Test forced exception"))

        toothbrushBase
            .playLedSignal(red, green, blue, pattern, period, duration)
            .test()
            .assertError(Exception::class.java)
    }

    /*
  CALIBRATE ACCELEROMETER AND GYROMETER
   */
    @Test
    fun `calibrateAccelerometerAndGyrometer returns value from driver SetDeviceParameter`() {
        val expectedPayload = ParameterSet.calibrateAccelerometerAndGyrometerParameterPayload()

        val expectedReponse = true
        doReturn(expectedReponse).whenever(bleDriver).setDeviceParameter(expectedPayload)

        toothbrushBase.calibrateAccelerometerAndGyrometer().test().assertValue(expectedReponse)

        verify(bleDriver).setDeviceParameter(expectedPayload)
    }

    @Test
    fun `calibrateAccelerometerAndGyrometer driver setDeviceParameter throwsError_rethrowsError`() {
        whenever(bleDriver.setDeviceParameter(anyOrNull()))
            .thenThrow(Exception("Test forced exception"))

        toothbrushBase.calibrateAccelerometerAndGyrometer().test()
            .assertError(Exception::class.java)
    }

    /*
  SET TOOTHBRUSH NAME
   */
    @Test
    fun `setToothbrushName invokes driver setDeviceParameter with expectedPayload`() {
        val name = "dasdsadsa"
        val expectedPayload = ParameterSet.setToothbrushNameParameterPayload(name)

        toothbrushBase.setToothbrushName(name)

        verify(bleDriver).setDeviceParameter(expectedPayload)
    }

    /*
  GET HARDWARE VERSION
   */
    @Test
    fun `getHardwareVersion invokes driver getHardwareVersion`() {
        toothbrushBase.hardwareVersion

        verify(bleDriver).getHardwareVersion()
    }

    /*
  GET FIRMWARE VERSION
   */
    @Test
    fun `getFirmwareVersion invokes driver getFirmwareVersion`() {
        toothbrushBase.firmwareVersion

        verify(bleDriver).getFirmwareVersion()
    }

    /*
  GET DSP VERSION
   */
    @Test
    fun `getDspVersion invokes driver getDspVersion`() {
        toothbrushBase.dspVersion

        verify(bleDriver).getDspVersion()
    }

    /*
  SUPPORTS LED PLAY SIGNAL
   */
    @Test
    fun `supportsLedPlaySignal isRunningBootloader returns true returns false`() {
        whenever(bleDriver.isRunningBootloader).thenReturn(true)

        assertFalse(toothbrushBase.supportsLedPlaySignal())
    }

    @Test
    fun `supportsLedPlaySignal isRunningBootloader returns false softwareVersion is before minSupported returns false`() {
        whenever(bleDriver.isRunningBootloader).thenReturn(false)

        val fwVersion =
            SoftwareVersion(StubToothbrushBaseBleImpl.MIN_SOFTWARE_VERSION_MAJOR - 1, 0, 0)
        whenever(bleDriver.getFirmwareVersion()).thenReturn(fwVersion)

        assertFalse(toothbrushBase.supportsLedPlaySignal())
    }

    @Test
    fun `supportsLedPlaySignal isRunningBootloader returns false softwareVersion is after minSupported returns true`() {
        whenever(bleDriver.isRunningBootloader).thenReturn(false)

        val fwVersion =
            SoftwareVersion(StubToothbrushBaseBleImpl.MIN_SOFTWARE_VERSION_MAJOR + 1, 0, 0)
        whenever(bleDriver.getFirmwareVersion()).thenReturn(fwVersion)

        assertTrue(toothbrushBase.supportsLedPlaySignal())
    }

    /*
  IS RUNNING DFU_BOOTLOADER
   */
    @Test
    fun `isRunningBootloader invokes driver isRunningBootloader`() {
        toothbrushBase.isRunningBootloader

        verify(bleDriver).isRunningBootloader
    }

    /*
    playModeLedPattern
     */

    @Test
    fun `playModeLedPattern emits CommandNotSupportedException when model has no Mode LEDs`() {
        toothbrushBase.playModeLedPattern(patternDuration = Duration.ZERO)
            .test()
            .assertNotComplete()
            .assertError(CommandNotSupportedException::class.java)
    }

    @Test
    fun `playModeLedPattern emits IllegalArgumentException when PWMs sum is over 100`() {
        toothbrushBase = spy(StubToothbrushBaseBleImpl("", GLINT, bleDriver, ""))

        toothbrushBase.playModeLedPattern(
            pwmLed0 = 100,
            pwmLed1 = 50,
            patternDuration = Duration.ZERO
        )
            .test()
            .assertNotComplete()
            .assertError(IllegalArgumentException::class.java)
    }

    @Test
    fun `playModeLedPattern sends expected data on the parameters char`() {
        toothbrushBase = spy(StubToothbrushBaseBleImpl("", GLINT, bleDriver, ""))

        whenever(bleDriver.setAndGetDeviceParameterOnce(any()))
            .thenReturn(Single.just(PayloadReader(byteArrayOf())))

        toothbrushBase.playModeLedPattern(
            pwmLed0 = 50,
            pwmLed4 = 50,
            patternDuration = Duration.ofMillis(10L)
        )
            .test()
            .assertComplete()
            .assertNoErrors()

        verify(bleDriver).setAndGetDeviceParameterOnce(
            byteArrayOf(
                GattCharacteristic.DEVICE_PARAMETERS_MODE_LED_PATTERN,
                0x32,
                0x00,
                0x00,
                0x00,
                0x32,
                0x0A,
                0x00
            )
        )
    }

    /*
    getSpecialLedBleIndex
     */

    @Test
    fun `getSpecialLedBleIndex returns 0x00 for WarningLed`() {
        assertEquals(0x00.toByte(), toothbrushBase.getSpecialLedBleIndex(SpecialLed.WarningLed))
    }

    @Test
    fun `getSpecialLedBleIndex returns 0x01 for StrengthLedNominal`() {
        assertEquals(
            0x01.toByte(),
            toothbrushBase.getSpecialLedBleIndex(SpecialLed.StrengthLedNominal)
        )
    }

    @Test
    fun `getSpecialLedBleIndex returns 0x02 for StrengthLedLow`() {
        assertEquals(0x02.toByte(), toothbrushBase.getSpecialLedBleIndex(SpecialLed.StrengthLedLow))
    }

    /*
    setSpecialLedPwm
     */

    @Test
    fun `setSpecialLedPwm emits CommandNotSupportedException when not a Glint`() {
        ToothbrushModel.values()
            .filterNot { it.isGlint }
            .forEach {
                StubToothbrushBaseBleImpl(DEFAULT_MAC, it, bleDriver, DEFAULT_NAME)
                    .setSpecialLedPwm(SpecialLed.WarningLed, 0xF0)
                    .test()
                    .assertError(CommandNotSupportedException::class.java)
            }
    }

    @Test
    fun `setSpecialLedPwm emits IllegalArgumentException when PWM is out of range`() {
        whenever(bleDriver.setAndGetDeviceParameterOnce(any()))
            .thenReturn(Single.just(PayloadReader(byteArrayOf())))

        StubToothbrushBaseBleImpl(DEFAULT_MAC, GLINT, bleDriver, DEFAULT_NAME)
            .setSpecialLedPwm(SpecialLed.WarningLed, -1)
            .test()
            .assertError(IllegalArgumentException::class.java)

        StubToothbrushBaseBleImpl(DEFAULT_MAC, GLINT, bleDriver, DEFAULT_NAME)
            .setSpecialLedPwm(SpecialLed.WarningLed, 256)
            .test()
            .assertError(IllegalArgumentException::class.java)
    }

    @Test
    fun `setSpecialLedPwm calls parameters char with expected payload`() {
        whenever(bleDriver.setAndGetDeviceParameterOnce(any()))
            .thenReturn(Single.just(PayloadReader(byteArrayOf())))

        val expectedLedIndex: Byte = 0x00
        val expectedPwm = 0x1D

        StubToothbrushBaseBleImpl(DEFAULT_MAC, GLINT, bleDriver, DEFAULT_NAME)
            .setSpecialLedPwm(SpecialLed.WarningLed, expectedPwm)
            .test()
            .assertNoErrors()

        verify(bleDriver).setAndGetDeviceParameterOnce(byteArrayOf(
            GattCharacteristic.DEVICE_PARAMETERS_SPECIAL_LED_CONTROL,
            expectedLedIndex,
            expectedPwm.toByte()
        ))
    }

    /*
    getSpecialLedPwm
     */

    @Test
    fun `getSpecialLedPwm emits CommandNotSupportedException when not a Glint`() {
        ToothbrushModel.values()
            .filterNot { it.isGlint }
            .forEach {
                StubToothbrushBaseBleImpl(DEFAULT_MAC, it, bleDriver, DEFAULT_NAME)
                    .getSpecialLedPwm(SpecialLed.WarningLed)
                    .test()
                    .assertError(CommandNotSupportedException::class.java)
            }
    }

    @Test
    fun `getSpecialLedPwm calls parameters char with expected payload`() {
        val expectedPwm = 0x1D.toByte()
        val payloadReader = PayloadReader(byteArrayOf(0x00, 0x00, 0x00, expectedPwm))
        val expectedLedIndex: Byte = 0x00
        whenever(bleDriver.getDeviceParameter(any()))
            .thenReturn(payloadReader)

        StubToothbrushBaseBleImpl(DEFAULT_MAC, GLINT, bleDriver, DEFAULT_NAME)
            .getSpecialLedPwm(SpecialLed.WarningLed)
            .test()
            .assertValue(expectedPwm.toInt())

        verify(bleDriver).getDeviceParameter(byteArrayOf(
            GattCharacteristic.DEVICE_PARAMETERS_SPECIAL_LED_CONTROL,
            expectedLedIndex
        ))
    }

    @Test
    fun `switchOffDevice completes when everything is fine`() {
        val switchOffMode = SwitchOffMode.TRAVEL_MODE
        val payloadReader = PayloadReader(byteArrayOf(0x12))

        whenever(bleDriver.setAndGetDeviceParameterOnce(CommandSet.switchOffDevice(switchOffMode)))
            .thenReturn(Single.just(payloadReader))

        StubToothbrushBaseBleImpl(DEFAULT_MAC, GLINT, bleDriver, DEFAULT_NAME)
            .switchOffDevice(switchOffMode).test().assertComplete()

        verify(bleDriver).setAndGetDeviceParameterOnce(CommandSet.switchOffDevice(switchOffMode))
    }

    @Test
    fun `switchOffDevice error when returns UNSUPPORTED_SWITCH_OFF errors`() {
        val switchOffMode = SwitchOffMode.TRAVEL_MODE
        val payloadReader = PayloadReader(byteArrayOf(0x12, UNSUPPORTED_SWITCH_OFF))

        whenever(bleDriver.setAndGetDeviceParameterOnce(CommandSet.switchOffDevice(switchOffMode)))
            .thenReturn(Single.just(payloadReader))

        StubToothbrushBaseBleImpl(DEFAULT_MAC, GLINT, bleDriver, DEFAULT_NAME)
            .switchOffDevice(switchOffMode).test().assertErrorMessage("unsupported switch-off mode")

        verify(bleDriver).setAndGetDeviceParameterOnce(CommandSet.switchOffDevice(switchOffMode))
    }

    @Test
    fun `switchOffDevice error when returns MALFORMED_SWITCH_OFF_COMMAND errors`() {
        val switchOffMode = SwitchOffMode.TRAVEL_MODE
        val payloadReader = PayloadReader(byteArrayOf(0x12, MALFORMED_SWITCH_OFF_COMMAND))

        whenever(bleDriver.setAndGetDeviceParameterOnce(CommandSet.switchOffDevice(switchOffMode)))
            .thenReturn(Single.just(payloadReader))

        StubToothbrushBaseBleImpl(DEFAULT_MAC, GLINT, bleDriver, DEFAULT_NAME)
            .switchOffDevice(switchOffMode).test().assertErrorMessage("malformed command")

        verify(bleDriver).setAndGetDeviceParameterOnce(CommandSet.switchOffDevice(switchOffMode))
    }

    @Test
    fun `switchOffDevice error when returns UNKNOWN_ERROR_SWITCH_OFF_COMMAND errors`() {
        val switchOffMode = SwitchOffMode.TRAVEL_MODE
        val payloadReader = PayloadReader(byteArrayOf(0x12, UNKNOWN_ERROR_SWITCH_OFF_COMMAND))

        whenever(bleDriver.setAndGetDeviceParameterOnce(CommandSet.switchOffDevice(switchOffMode)))
            .thenReturn(Single.just(payloadReader))

        StubToothbrushBaseBleImpl(DEFAULT_MAC, GLINT, bleDriver, DEFAULT_NAME)
            .switchOffDevice(switchOffMode).test().assertErrorMessage("unknown error")

        verify(bleDriver).setAndGetDeviceParameterOnce(CommandSet.switchOffDevice(switchOffMode))
    }

    private class StubToothbrushBaseBleImpl(
        mac: String,
        model: ToothbrushModel,
        driver: BleDriver,
        toothbrushName: String
    ) : ToothbrushBaseBleImpl(mac, model, driver, toothbrushName) {

        override fun setSupervisedMouthZone(zone: MouthZone16, sequenceId: Byte): Single<Boolean> =
            Single.never()

        override fun minFwSupportingPlayLed(): SoftwareVersion = MIN_SOFTWARE_VERSION

        override fun update(update: AvailableUpdate): Observable<OtaUpdateEvent> =
            Observable.empty()

        override fun dspState(): Single<DspState> = Single.never()

        companion object {
            internal const val MIN_SOFTWARE_VERSION_MAJOR = 2
            internal val MIN_SOFTWARE_VERSION = SoftwareVersion(MIN_SOFTWARE_VERSION_MAJOR, 4, 0)
        }
    }

    private fun BaseDriver.mockDriverVersions(): BaseDriver {
        whenever(getFirmwareVersion()).thenReturn(mock())
        whenever(getHardwareVersion()).thenReturn(mock())
        return this
    }

    companion object {

        private const val DEFAULT_MAC = ToothbrushImplementationFactoryTest.MAC
        private const val DEFAULT_NAME = ToothbrushImplementationFactoryTest.NAME
    }
}
