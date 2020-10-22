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
import com.kolibree.android.sdk.core.driver.VibratorMode
import org.junit.Assert.assertArrayEquals
import org.junit.Test

/** [CommandSet] test unit  */
class CommandSetTest {

    @Test
    fun `test restoreToFactoryDefaults`() {
        assertArrayEquals(
            byteArrayOf(0x10, 0x04, 0x03, 0x02, 0x01), CommandSet.restoreToFactoryDefaults(0x01020304)
        )
    }

    @Test
    fun `setVibrationPayload START`() {
        assertArrayEquals(byteArrayOf(0x11, 0x01), CommandSet.setVibrationPayload(VibratorMode.START))
    }

    @Test
    fun `setVibrationPayload STOP`() {
        assertArrayEquals(byteArrayOf(0x11, 0x00), CommandSet.setVibrationPayload(VibratorMode.STOP))
    }

    @Test
    fun `setVibrationPayload STOP_AND_HALT_RECORDING`() {
        assertArrayEquals(
            byteArrayOf(0x11, 0x02),
            CommandSet.setVibrationPayload(VibratorMode.STOP_AND_HALT_RECORDING)
        )
    }

    @Test
    fun `switchOffDevice HardOff`() {
        assertArrayEquals(byteArrayOf(0x12, 0x00), CommandSet.switchOffDevice(SwitchOffMode.HARD_OFF))
    }

    @Test
    fun `switchOffDevice Reboot`() {
        assertArrayEquals(byteArrayOf(0x12, 0x01), CommandSet.switchOffDevice(SwitchOffMode.REBOOT))
    }

    @Test
    fun `switchOffDevice SoftOff`() {
        assertArrayEquals(byteArrayOf(0x12, 0x02), CommandSet.switchOffDevice(SwitchOffMode.SOFT_OFF))
    }

    @Test
    fun `switchOffDevice FactoryHardOff`() {
        assertArrayEquals(
            byteArrayOf(0x12, 0x03), CommandSet.switchOffDevice(SwitchOffMode.FACTORY_HARD_OFF)
        )
    }

    @Test
    fun `switchOffDevice ResetBackupDomain`() {
        assertArrayEquals(
            byteArrayOf(0x12, 0x04), CommandSet.switchOffDevice(SwitchOffMode.RESET_BACKUP_DOMAIN)
        )
    }

    @Test
    fun `switchOffDevice travelModel`() {
        assertArrayEquals(
            byteArrayOf(0x12, 0x05), CommandSet.switchOffDevice(SwitchOffMode.TRAVEL_MODE)
        )
    }

    @Test
    fun `test monitorCurrentBrushingSession`() {
        assertArrayEquals(byteArrayOf(0x13, 0x00), CommandSet.monitorCurrentBrushingSession())
    }

    @Test
    fun `testPlayLedSignal Fixed`() {
        val r: Byte = 0x30
        val g: Byte = 0x20
        val b: Byte = 0x10
        val period = 0x01F4 // 500ms (must be ignored here)
        val duration = 0x0FA0 // 4 seconds duration

        assertArrayEquals(
            byteArrayOf(0x15, r, g, b, 0xFF.toByte(), 0x00, 0x00, 0xA0.toByte(), 0x0F),
            CommandSet.playLedSignal(r, g, b, LedPattern.FIXED, period, duration)
        )
    }

    @Test
    fun `playLedSignal NotFixed`() {
        val r: Byte = 0x30
        val g: Byte = 0x20
        val b: Byte = 0x10
        val period = 0x01F4 // 500ms (must be ignored here)
        val duration = 0x0FA0 // 4 seconds duration

        assertArrayEquals(
            byteArrayOf(0x15, r, g, b, 0x00, 0xF4.toByte(), 0x01, 0xA0.toByte(), 0x0F),
            CommandSet.playLedSignal(r, g, b, LedPattern.SINUS, period, duration)
        )
    }

    @Test
    fun `setVibrationSignal with intensity`() {
        val intensity: Byte = 50 // 50%
        assertArrayEquals(
            byteArrayOf(0x16, 0x00, intensity, 0xFF.toByte(), 0x00, 0x00, 0x00, 0x00),
            CommandSet.setVibrationSignal(intensity)
        )
    }

    @Test
    fun `testPing`() {
        assertArrayEquals(byteArrayOf(0x17), CommandSet.ping())
    }
}
