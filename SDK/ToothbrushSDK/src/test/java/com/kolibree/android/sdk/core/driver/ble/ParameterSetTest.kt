package com.kolibree.android.sdk.core.driver.ble

import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_MULTI_USER_MODE
import com.kolibree.android.sdk.util.MouthZoneIndexMapper
import com.kolibree.kml.MouthZone16
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

/** [ParameterSet] test unit  */
class ParameterSetTest {

    @Test
    fun `test calibrateAccelerometerAndGyrometer`() {
        assertArrayEquals(
            byteArrayOf(0x46, 0x00, 0xFF.toByte()),
            ParameterSet.calibrateAccelerometerAndGyrometerParameterPayload()
        )
    }

    @Test
    fun `test getToothbrushName`() {
        assertArrayEquals(byteArrayOf(0x38), ParameterSet.toothbrushNameParameterPayload)
    }

    @Test
    fun `test setToothbrushName`() {
        assertArrayEquals(
            byteArrayOf(0x38, 0x4a, 0x65, 0x61, 0x6e, 0x20, 0x41, 0x72, 0x61),
            ParameterSet.setToothbrushNameParameterPayload("Jean Ara")
        )
    }

    @Test
    fun `test getAutoShutdownTimeoutParameterPayload`() {
        assertArrayEquals(byteArrayOf(0x34), ParameterSet.autoShutdownTimeoutParameterPayload)
    }

    @Test
    fun `test setAutoShutdownTimeoutParameterPayload`() {
        assertArrayEquals(
            byteArrayOf(0x34, 0xCD.toByte(), 0xAB.toByte()),
            ParameterSet.setAutoShutdownTimeoutParameterPayload(0xABCD)
        )
    }

    @Test
    fun `test setSupervisedZonePayload`() {
        MouthZone16.values().forEach { zone ->
            assertArrayEquals("for zone $zone",
                byteArrayOf(SUPERVISED_ZONE, 0xff.toByte(), MouthZoneIndexMapper.mapMouthZone16ToId(zone)),
                ParameterSet.setSupervisedZonePayload(zone, 0xff.toByte())
            )
        }
    }

    /*
    setAdvertisingIntervalsPayload
     */

    @Test
    fun `value of ADVERTISING_INTERVALS is 0x3D`() {
        assertEquals(0x3D.toByte(), ADVERTISING_INTERVALS)
    }

    @Test
    fun `value of SET_ADVERTISING_INTERVALS_PAYLOAD_LENGTH is 5`() {
        assertEquals(5, SET_ADVERTISING_INTERVALS_PAYLOAD_LENGTH)
    }

    @Test
    fun `setAdvertisingIntervalsPayload correctly creates the payload`() {
        assertArrayEquals(
            byteArrayOf(ADVERTISING_INTERVALS, 0x00, 0x00, 0x05, 0x05),
            ParameterSet.setAdvertisingIntervalsPayload(0L, 1285L)
        )
    }

    /*
    disableMultiUserModePayload
     */

    @Test
    fun `disableMultiUserModePayload correctly creates the payload`() {
        assertArrayEquals(
            byteArrayOf(DEVICE_PARAMETERS_MULTI_USER_MODE, 0x00),
            ParameterSet.disableMultiUserModePayload()
        )
    }
}
