/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer.sequence

import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern
import com.kolibree.android.sdk.core.binary.Bitmask
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import kotlin.experimental.and

// https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0/edit?pli=1#gid=506526620
@Suppress("MagicNumber")
internal fun parseSequenceSettings(payloadReader: PayloadReader): BrushingModeSequenceSettings {
    val sequenceInfoMask: Byte = payloadReader.skip(1).readInt8()
    val patternCount = payloadReader.readInt8().toInt()

    return BrushingModeSequenceSettings(
        modifiable = Bitmask(sequenceInfoMask).getBit(7),
        sequenceId = (sequenceInfoMask and 0b00011111).toInt(),
        patternCount = patternCount,
        patterns = parseBrushingModeSequencePatterns(patternCount, payloadReader)
    )
}

private fun parseBrushingModeSequencePatterns(
    count: Int,
    payloadReader: PayloadReader
) = mutableListOf<BrushingModeSequencePattern>().apply {
    for (i in 0 until count) {
        add(BrushingModeSequencePattern(
            pattern = BrushingModePattern.fromBleIndex(payloadReader.readInt8().toInt()),
            durationSeconds = payloadReader.readInt8().toInt()
        ))
    }
}

// https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0/edit?pli=1#gid=506526620
@Suppress("MagicNumber")
internal fun setBrushingModeSequenceSettingsPayload(patterns: List<BrushingModeSequencePattern>) =
    PayloadWriter(brushingModeSequenceSettingsPayloadSize(patterns.size))
        .writeByte(GattCharacteristic.DEVICE_PARAMETERS_BRUSHING_MODE_SEQUENCE)
        .writeByte(0b10000100.toByte())
        .writeByte(patterns.size.toByte())
        .apply {
            for (sequencePattern in patterns) {
                writeByte(sequencePattern.pattern.bleIndex.toByte())
                writeByte(sequencePattern.durationSeconds.toByte())
            }
        }.bytes

@Suppress("MagicNumber")
private fun brushingModeSequenceSettingsPayloadSize(patternCount: Int) =
    3 + patternCount * 2 // 1 command ID byte + 2 info bytes + 2 bytes per pattern
