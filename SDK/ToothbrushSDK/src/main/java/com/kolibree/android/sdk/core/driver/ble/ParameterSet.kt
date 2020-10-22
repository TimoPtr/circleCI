package com.kolibree.android.sdk.core.driver.ble

import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_MULTI_USER_MODE
import com.kolibree.android.sdk.util.MouthZoneIndexMapper
import com.kolibree.kml.MouthZone16

/** BLE toothbrushes parameter definitions  */
internal object ParameterSet {

    /**
     * Get the payload to get the toothbrush name parameter value
     *
     * @return non null payload
     */
    @JvmStatic
    val toothbrushNameParameterPayload = byteArrayOf(TOOTHBRUSH_NAME)

    /**
     * Get the payload to get the auto shutdown timeout parameter value
     *
     * @return non null payload
     */
    @JvmStatic
    val autoShutdownTimeoutParameterPayload = byteArrayOf(AUTO_SHUTDOWN_TIMEOUT)

    /**
     * Get the payload to set accelerometer and gyroscope calibration parameter
     *
     * @return non null payload
     */
    @JvmStatic
    fun calibrateAccelerometerAndGyrometerParameterPayload(): ByteArray = byteArrayOf(
        ACCELEROMETER_GYROMETER_CALIBRATION, 0x0, // axis X
        0xFF.toByte() // sign -1
    )

    /**
     * Get the payload to set the toothbrush name parameter
     *
     * @param name non null name String
     * @return non null payload
     */
    @JvmStatic
    fun setToothbrushNameParameterPayload(name: String): ByteArray = name.toByteArray().let { nameBytes ->
        PayloadWriter(nameBytes.size + 1)
            .writeByte(TOOTHBRUSH_NAME)
            .writeByteArray(nameBytes)
            .bytes
    }

    /**
     * Get the payload to set the auto shutdown timeout parameter value
     *
     * @return non null payload
     */
    @JvmStatic
    fun setAutoShutdownTimeoutParameterPayload(timeoutSeconds: Int): ByteArray = PayloadWriter(3)
        .writeByte(AUTO_SHUTDOWN_TIMEOUT)
        .writeUnsignedInt16(timeoutSeconds)
        .bytes

    /**
     * Get the payload to set the supervised zone parameter value
     *
     * @return non null payload
     */
    @JvmStatic
    fun setSupervisedZonePayload(zone: MouthZone16, sequenceId: Byte): ByteArray =
        byteArrayOf(SUPERVISED_ZONE, sequenceId, MouthZoneIndexMapper.mapMouthZone16ToId(zone))

    /**
     * Create a payload that sets the BLE advertising intervals parameter (0x3D)
     */
    @JvmStatic
    fun setAdvertisingIntervalsPayload(fastModeIntervalMs: Long, slowModeIntervalMs: Long) =
        PayloadWriter(SET_ADVERTISING_INTERVALS_PAYLOAD_LENGTH)
            .writeByte(ADVERTISING_INTERVALS)
            .writeInt16(fastModeIntervalMs.toShort())
            .writeInt16(slowModeIntervalMs.toShort())
            .bytes

    /**
     * Create a payload to disable MultiUserMode (0x31)
     */
    @JvmStatic
    fun disableMultiUserModePayload() =
        PayloadWriter(2)
            .writeByte(DEVICE_PARAMETERS_MULTI_USER_MODE)
            .writeByte(0) // 0: disable, 1: enable
            .bytes
}

/** Auto shutdown timeout  */
private const val AUTO_SHUTDOWN_TIMEOUT: Byte = 0x34

/** Toothbrush name  */
private const val TOOTHBRUSH_NAME: Byte = 0x38

/** Calibrate Accelerometer and Gyrometer  */
private const val ACCELEROMETER_GYROMETER_CALIBRATION: Byte = 0x46

/** Set the supervised zone */
@VisibleForTesting
const val SUPERVISED_ZONE: Byte = 0x18

/** Set / get BLE advertising intervals */
@VisibleForTesting
const val ADVERTISING_INTERVALS: Byte = 0x3D
@VisibleForTesting
const val SET_ADVERTISING_INTERVALS_PAYLOAD_LENGTH = 5
