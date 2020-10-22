/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush.dsp

import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.version.SoftwareVersion

/** Helper class for DSP state payload parsing */
internal interface DspStatePayloadParser {

    /**
     * Parse a [PayloadReader] into a [DspState]
     *
     * @param payloadReader [PayloadReader]
     * @return [DspState]
     */
    fun parseDspStatePayload(payloadReader: PayloadReader): DspState

    companion object {

        @JvmStatic
        fun create(firmwareVersion: SoftwareVersion) =
            DspStatePayloadParserImpl(
                usesModernPayload = firmwareVersion.isNewerOrSame(firstVersionWithModernDspPayload)
            )

        @VisibleForTesting
        val firstVersionWithModernDspPayload = SoftwareVersion("2.0.12")
    }
}

// https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0
internal class DspStatePayloadParserImpl(
    private val usesModernPayload: Boolean
) : DspStatePayloadParser {

    override fun parseDspStatePayload(payloadReader: PayloadReader) =
        if (usesModernPayload) {
            readDspState(payloadReader)
        } else {
            readDeprecatedDspState(payloadReader)
        }

    private fun readDspState(payloadReader: PayloadReader) = DspState(
        hasValidFirmware = payloadReader.skip(1).readInt8() != INVALID_DSP_FIRMWARE,
        usesDeprecatedFirmwareFormat = false,
        firmwareVersion = payloadReader.readDspVersion(),
        bootloaderVersion = payloadReader.readUnsignedInt16(),
        flashFileType = parseFlashFileType(payloadReader.readInt8().toInt()),
        flashFileVersion = payloadReader.readDspVersion()
    )

    private fun readDeprecatedDspState(payloadReader: PayloadReader) = DspState(
        hasValidFirmware = payloadReader.skip(1).readBoolean(),
        usesDeprecatedFirmwareFormat = true,
        firmwareVersion = payloadReader.readDspVersionCompat(),
        flashFileType = parseFlashFileType(payloadReader.readInt8().toInt()),
        flashFileVersion = payloadReader.readDspVersionCompat(),
        bootloaderVersion = 0
    )

    @VisibleForTesting
    fun parseFlashFileType(flashFileTypeIndex: Int) =
        when (flashFileTypeIndex) {
            NO_FLASH_FILE_INDEX -> DspFlashFileType.NO_FLASH_FILE
            FIRMWARE_FLASH_FILE_INDEX -> DspFlashFileType.FIRMWARE_FLASH_FILE
            BOOTLOADER_FLASH_FILE_INDEX -> DspFlashFileType.BOOTLOADER_FLASH_FILE
            CONFIGURATION_FLASH_FILE_INDEX -> DspFlashFileType.CONFIGURATION_FLASH_FILE
            else -> throw IllegalArgumentException("Unknown file type index: $flashFileTypeIndex")
        }

    internal companion object {

        @VisibleForTesting
        const val INVALID_DSP_FIRMWARE: Byte = 0x00

        @VisibleForTesting
        const val NO_FLASH_FILE_INDEX = 0

        @VisibleForTesting
        const val FIRMWARE_FLASH_FILE_INDEX = 1

        @VisibleForTesting
        const val BOOTLOADER_FLASH_FILE_INDEX = 2

        @VisibleForTesting
        const val CONFIGURATION_FLASH_FILE_INDEX = 3
    }
}
