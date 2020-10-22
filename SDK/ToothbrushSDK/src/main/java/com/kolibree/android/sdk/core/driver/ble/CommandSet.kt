/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble

import com.kolibree.android.sdk.connection.toothbrush.SwitchOffMode
import com.kolibree.android.sdk.connection.toothbrush.led.LedPattern
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.sdk.core.driver.VibratorMode
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_PUSH_DSP

/** Command set for Ara and CM1 toothbrushes  */
internal object CommandSet {

    /**
     * Build a restore to factory defaults command payload
     *
     *
     * Command ID 0x10
     *
     * @param passkey long passkey
     * @return non null command payload byte array
     */
    @JvmStatic
    fun restoreToFactoryDefaults(passkey: Int): ByteArray =
        PayloadWriter(5).writeByte(0x10.toByte()).writeInt32(passkey).bytes

    /**
     * Build a start/stop vibration command payload
     *
     *
     * Command ID 0x11
     *
     *
     * payload 0x02 asks the FW to force brushing stop. See
     * https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0/edit#gid=506526620&range=G4
     *
     *
     * Always send 0x02 when stopping. Ticket related https://jira.kolibree.com/browse/KLTB002-5725
     *
     * @param vibratorMode vibrator mode to set
     * @return non null command payload byte array
     */
    @JvmStatic
    fun setVibrationPayload(vibratorMode: VibratorMode): ByteArray =
        byteArrayOf(
            GattCharacteristic.DEVICE_PARAMETERS_VIBRATION, when (vibratorMode) {
                VibratorMode.START -> 0x01
                VibratorMode.STOP_AND_HALT_RECORDING -> 0x02
                VibratorMode.STOP -> 0x00
            }
        )

    /**
     * Build switch off the toothbrush command payload
     *
     *
     * Command ID 0x12
     *
     * @param mode non null SwitchOffMode
     * @return non null command payload byte array
     */
    @JvmStatic
    fun switchOffDevice(mode: SwitchOffMode): ByteArray = byteArrayOf(0x12, mode.ordinal.toByte())

    /**
     * Build monitor current brushing session command payload
     *
     *
     * Command ID 0x13
     *
     * @return non null command payload byte array
     */
    @JvmStatic
    fun monitorCurrentBrushingSession(): ByteArray = byteArrayOf(0x13, 0x00)

    /**
     * Build unlock sensitive data write protection command payload
     *
     *
     * Command ID 0x14
     *
     * @param passkey long passkey
     * @return non null command payload byte array
     */
    @JvmStatic
    fun unlockSensitiveDataWriteProtection(passkey: Int): ByteArray =
        PayloadWriter(5).writeByte(0x14.toByte()).writeInt32(passkey).bytes

    /**
     * Build play LED signal command payload
     *
     *
     * Command ID 0x15
     *
     * @param pattern non null LedPattern
     * @return non null command payload byte array
     */
    @JvmStatic
    @Suppress("LongParameterList")
    fun playLedSignal(
        red: Byte,
        green: Byte,
        blue: Byte,
        pattern: LedPattern,
        period: Int,
        duration: Int
    ): ByteArray = PayloadWriter(9)
        .writeByte(0x15.toByte())
        .writeByte(red)
        .writeByte(green)
        .writeByte(blue)
        .writeByte(if (pattern === LedPattern.FIXED) 0xFF.toByte() else (pattern.ordinal - 1).toByte())
        .writeUnsignedInt16(if (pattern === LedPattern.FIXED) 0 else period)
        .writeUnsignedInt16(duration)
        .bytes

    /**
     * Build set vibration signal command payload
     *
     *
     * Command ID 0x16
     *
     *
     * Note that only a part of the possibilities offered by this command are implemented here
     *
     * @param intensityPercent 0 - 100 % (0 will disable vibration)
     * @return non null command payload byte array
     */
    @JvmStatic
    fun setVibrationSignal(intensityPercent: Byte): ByteArray = byteArrayOf(
        0x16, 0x00, // Brushing signal
        intensityPercent, // Intensity
        0xFF.toByte(), // Fixed pattern (see fw doc to add more)
        0x00, 0x00, 0x00, 0x00 // Unused (see fw doc to add more)
    )

    /**
     * Build ping command payload
     *
     *
     * Command ID 0x17
     *
     * @return non null command payload byte array
     */
    @JvmStatic
    fun ping(): ByteArray = byteArrayOf(0x17)

    /**
     * https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0/edit#gid=506526620&range=G59
     *
     * @return [0x51,0x01], to push DSP firmware from Flash to DSP
     */
    fun pushDspFirmware(): ByteArray {
        val payloadWriter = PayloadWriter(2)

        payloadWriter.writeByte(DEVICE_PARAMETERS_PUSH_DSP)
        payloadWriter.writeByte(1)

        return payloadWriter.bytes
    }

    /**
     * https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0/edit#gid=506526620&range=G59
     *
     * @return [0x51,0x00], to get the state of push DSP firmware from Flash to DSP
     */
    fun getPushDspFirmwareState(): ByteArray = byteArrayOf(DEVICE_PARAMETERS_PUSH_DSP)
}
