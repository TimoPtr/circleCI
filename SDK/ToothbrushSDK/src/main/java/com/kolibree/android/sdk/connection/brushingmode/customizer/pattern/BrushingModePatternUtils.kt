/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer.pattern

import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurve
import com.kolibree.android.sdk.core.binary.Bitmask
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import kotlin.experimental.and

@Suppress("MagicNumber")
internal fun parseBrushingPatternSettings(payloadReader: PayloadReader): BrushingModePatternSettings {
    val header = payloadReader.skip(1).readInt8()
    return BrushingModePatternSettings(
        modifiable = Bitmask(header).getBit(7),
        patternId = (header and 0b00011111).toInt(),
        oscillatingMode = BrushingModePatternOscillatingMode
            .fromBleIndex(payloadReader.readInt8().toInt()),
        patternFrequency = payloadReader.readUnsignedInt16(),
        minimalDutyCycleHalfPercent = payloadReader.readInt8().toInt(),
        strength1DutyCycleHalfPercent = payloadReader.readInt8().toInt(),
        strength10DutyCycleHalfPercent = payloadReader.readInt8().toInt(),
        oscillatingPeriodTenthSecond = payloadReader.readInt8().toInt(),
        oscillationParam1 = payloadReader.readInt8().toInt(),
        oscillationParam2 = payloadReader.readInt8().toInt(),
        oscillationParam3 = payloadReader.readInt8().toInt(),
        curve = BrushingModeCurve.fromBleIndex(payloadReader.readInt8().toInt())
    )
}

@Suppress("MagicNumber")
internal fun setBrushingModePatternSettingsPayload(settings: BrushingModePatternSettings) =
    PayloadWriter(13)
        .writeByte(GattCharacteristic.DEVICE_PARAMETERS_BRUSHING_MODE_PATTERN)
        .writeByte(0b10000101.toByte())
        .writeByte(settings.oscillatingMode.bleIndex.toByte())
        .writeUnsignedInt16(settings.patternFrequency)
        .writeByte(settings.minimalDutyCycleHalfPercent.toByte())
        .writeByte(settings.strength1DutyCycleHalfPercent.toByte())
        .writeByte(settings.strength10DutyCycleHalfPercent.toByte())
        .writeByte(settings.oscillatingPeriodTenthSecond.toByte())
        .writeByte(settings.oscillationParam1.toByte())
        .writeByte(settings.oscillationParam2.toByte())
        .writeByte(settings.oscillationParam3.toByte())
        .writeByte(settings.curve.bleIndex.toByte())
        .bytes
