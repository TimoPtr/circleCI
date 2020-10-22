/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer.curve

import com.kolibree.android.sdk.core.binary.Bitmask
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import kotlin.experimental.and

@Suppress("MagicNumber")
internal fun setCustomBrushingModeCurveSettingsPayload(settings: BrushingModeCurveSettings) =
    PayloadWriter(15)
        .writeByte(GattCharacteristic.DEVICE_PARAMETERS_BRUSHING_MODE_CURVE)
        .writeByte(0b10000011.toByte())
        .writeUnsignedInt16(settings.referenceVoltageMv)
        .writeUnsignedInt16(settings.divider)
        .writeByte(settings.slope10PercentsDutyCycle.toByte())
        .writeByte(settings.slope20PercentsDutyCycle.toByte())
        .writeByte(settings.slope30PercentsDutyCycle.toByte())
        .writeByte(settings.slope40PercentsDutyCycle.toByte())
        .writeByte(settings.slope50PercentsDutyCycle.toByte())
        .writeByte(settings.slope60PercentsDutyCycle.toByte())
        .writeByte(settings.slope70PercentsDutyCycle.toByte())
        .writeByte(settings.slope80PercentsDutyCycle.toByte())
        .writeByte(settings.slope90PercentsDutyCycle.toByte())
        .bytes

@Suppress("MagicNumber", "LongMethod")
internal fun parseCustomBrushingModeCurveSettingsPayload(
    payloadReader: PayloadReader
): BrushingModeCurveSettings {
    val header = payloadReader.skip(1).readInt8()
    return BrushingModeCurveSettings(
        modifiable = Bitmask(header).getBit(7),
        curveId = (header and 0b00011111).toInt(),
        referenceVoltageMv = payloadReader.readUnsignedInt16(),
        divider = payloadReader.readUnsignedInt16(),
        slope10PercentsDutyCycle = payloadReader.readInt8().toInt(),
        slope20PercentsDutyCycle = payloadReader.readInt8().toInt(),
        slope30PercentsDutyCycle = payloadReader.readInt8().toInt(),
        slope40PercentsDutyCycle = payloadReader.readInt8().toInt(),
        slope50PercentsDutyCycle = payloadReader.readInt8().toInt(),
        slope60PercentsDutyCycle = payloadReader.readInt8().toInt(),
        slope70PercentsDutyCycle = payloadReader.readInt8().toInt(),
        slope80PercentsDutyCycle = payloadReader.readInt8().toInt(),
        slope90PercentsDutyCycle = payloadReader.readInt8().toInt()
    )
}
