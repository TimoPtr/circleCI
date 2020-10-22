/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush.dsp

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspStatePayloadParser.Companion.firstVersionWithModernDspPayload
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspStatePayloadParserImpl.Companion.BOOTLOADER_FLASH_FILE_INDEX
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspStatePayloadParserImpl.Companion.CONFIGURATION_FLASH_FILE_INDEX
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspStatePayloadParserImpl.Companion.FIRMWARE_FLASH_FILE_INDEX
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspStatePayloadParserImpl.Companion.INVALID_DSP_FIRMWARE
import com.kolibree.android.sdk.connection.toothbrush.dsp.DspStatePayloadParserImpl.Companion.NO_FLASH_FILE_INDEX
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import io.kotlintest.shouldThrow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** [DspStatePayloadParserImpl] unit tests */
class DspStatePayloadParserImplTest : BaseUnitTest() {

    private lateinit var parser: DspStatePayloadParserImpl

    /*
    parseFlashFileType
     */

    @Test
    fun `parseFlashFileType returns NO_FLASH_FILE when index is NO_FLASH_FILE_INDEX`() {
        initParser()
        assertEquals(
            DspFlashFileType.NO_FLASH_FILE,
            parser.parseFlashFileType(NO_FLASH_FILE_INDEX)
        )
    }

    @Test
    fun `parseFlashFileType returns FIRMWARE_FLASH_FILE when index is FIRMWARE_FLASH_FILE_INDEX`() {
        initParser()
        assertEquals(
            DspFlashFileType.FIRMWARE_FLASH_FILE,
            parser.parseFlashFileType(FIRMWARE_FLASH_FILE_INDEX)
        )
    }

    @Test
    fun `parseFlashFileType returns BOOTLOADER_FLASH_FILE when index is BOOTLOADER_FLASH_FILE_INDEX`() {
        initParser()
        assertEquals(
            DspFlashFileType.BOOTLOADER_FLASH_FILE,
            parser.parseFlashFileType(BOOTLOADER_FLASH_FILE_INDEX)
        )
    }

    @Test
    fun `parseFlashFileType returns CONFIGURATION_FLASH_FILE when index is CONFIGURATION_FLASH_FILE_INDEX`() {
        initParser()
        assertEquals(
            DspFlashFileType.CONFIGURATION_FLASH_FILE,
            parser.parseFlashFileType(CONFIGURATION_FLASH_FILE_INDEX)
        )
    }

    @Test
    fun `parseFlashFileType throws IllegalArgumentException when index is unknown`() {
        initParser()
        shouldThrow<IllegalArgumentException> {
            parser.parseFlashFileType(100)
        }
    }

    /*
    parseDspStatePayload
     */

    @Test
    fun `parseDspStatePayload correctly parses deprecated payload with valid firmware and no flash file`() {
        initParser(false)
        val payload = PayloadReader(
            byteArrayOf(
                GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_DSP_VERSIONS,
                0x01,
                0x04, 0x00, 0x01, 0x00, 0x00, 0x02, 0x00, 0x00,
                0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            )
        )

        val state = parser.parseDspStatePayload(payload)
        assertTrue(state.hasValidFirmware)
        assertTrue(state.usesDeprecatedFirmwareFormat)
        assertEquals(DspVersion(4, 1, 512), state.firmwareVersion)
        assertEquals(DspFlashFileType.NO_FLASH_FILE, state.flashFileType)
        assertEquals(DspVersion.NULL, state.flashFileVersion)
        assertEquals(0, state.bootloaderVersion)
    }

    @Test
    fun `parseDspStatePayload correctly parses deprecated payload with invalid firmware and firmware flash file`() {
        initParser(false)
        val payload = PayloadReader(
            byteArrayOf(
                GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_DSP_VERSIONS,
                0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x01,
                0x04, 0x00, 0x01, 0x00, 0x00, 0x02, 0x00, 0x00
            )
        )

        val state = parser.parseDspStatePayload(payload)
        assertFalse(state.hasValidFirmware)
        assertTrue(state.usesDeprecatedFirmwareFormat)
        assertEquals(DspVersion.NULL, state.firmwareVersion)
        assertEquals(DspFlashFileType.FIRMWARE_FLASH_FILE, state.flashFileType)
        assertEquals(DspVersion(4, 1, 512), state.flashFileVersion)
        assertEquals(0, state.bootloaderVersion)
    }

    @Test
    fun `parseDspStatePayload correctly parses payload with valid firmware and firmware flash file`() {
        initParser()
        val payload = PayloadReader(
            byteArrayOf(
                GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_DSP_VERSIONS,
                0xFF.toByte(),
                0x04, 0x00, 0x01, 0x00, 0x00, 0x02,
                0x12, 0x00,
                0x01,
                0x04, 0x00, 0x02, 0x00, 0x00, 0x02, 0x00, 0x00
            )
        )

        val state = parser.parseDspStatePayload(payload)
        assertTrue(state.hasValidFirmware)
        assertFalse(state.usesDeprecatedFirmwareFormat)
        assertEquals(DspVersion(4, 1, 512), state.firmwareVersion)
        assertEquals(DspFlashFileType.FIRMWARE_FLASH_FILE, state.flashFileType)
        assertEquals(DspVersion(4, 2, 512), state.flashFileVersion)
        assertEquals(18, state.bootloaderVersion)
    }

    @Test
    fun `parseDspStatePayload correctly parses payload with invalid firmware and firmware flash file`() {
        initParser()
        val payload = PayloadReader(
            byteArrayOf(
                GattCharacteristic.DEVICE_PARAMETERS_PLAQLESS_DSP_VERSIONS,
                0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x16, 0x00,
                0x01,
                0x04, 0x00, 0x02, 0x00, 0x00, 0x02, 0x00, 0x00
            )
        )

        val state = parser.parseDspStatePayload(payload)
        assertFalse(state.hasValidFirmware)
        assertFalse(state.usesDeprecatedFirmwareFormat)
        assertEquals(DspVersion.NULL, state.firmwareVersion)
        assertEquals(DspFlashFileType.FIRMWARE_FLASH_FILE, state.flashFileType)
        assertEquals(DspVersion(4, 2, 512), state.flashFileVersion)
        assertEquals(22, state.bootloaderVersion)
    }

    /*
    Constants
     */

    @Test
    fun `value of INVALID_DSP_FIRMWARE is 0x00`() {
        assertEquals(0x00.toByte(), INVALID_DSP_FIRMWARE)
    }

    @Test
    fun `value of NO_FLASH_FILE_INDEX is 0`() {
        assertEquals(0, NO_FLASH_FILE_INDEX)
    }

    @Test
    fun `value of FIRMWARE_FLASH_FILE_INDEX is 1`() {
        assertEquals(1, FIRMWARE_FLASH_FILE_INDEX)
    }

    @Test
    fun `value of BOOTLOADER_FLASH_FILE_INDEX is 2`() {
        assertEquals(2, BOOTLOADER_FLASH_FILE_INDEX)
    }

    @Test
    fun `value of CONFIGURATION_FLASH_FILE_INDEX is 3`() {
        assertEquals(3, CONFIGURATION_FLASH_FILE_INDEX)
    }

    /*
    Utils
     */

    private fun initParser(modernPayload: Boolean = true) {
        parser = DspStatePayloadParser.create(
            if (modernPayload)
                firstVersionWithModernDspPayload
            else
                SoftwareVersion("2.0.11")
        )
    }
}
