/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer

import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode.UserDefined
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurve
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurveSettings
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.parseCustomBrushingModeCurveSettingsPayload
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.setCustomBrushingModeCurveSettingsPayload
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePatternSettings
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.parseBrushingPatternSettings
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.setBrushingModePatternSettingsPayload
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequencePattern
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequenceSettings
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.parseSequenceSettings
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.setBrushingModeSequenceSettingsPayload
import com.kolibree.android.sdk.core.binary.Bitmask
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_BRUSHING_MODE_CURVE
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_BRUSHING_MODE_PATTERN
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_BRUSHING_MODE_SEQUENCE
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_CUSTOM_BRUSHING_MODE_SETTINGS
import io.reactivex.Completable
import io.reactivex.Single
import kotlin.experimental.and
import kotlin.math.min

/** [BrushingModeCustomizer] implementation */
// It's also BrushingModeTweaker's implementation but that's a secret
@Suppress("MagicNumber")
internal class BrushingModeCustomizerImpl(
    private val driver: BleDriver
) : BrushingModeCustomizer, BrushingModeTweaker {

    @Suppress("LongMethod")
    override fun getBrushingModeSettings(mode: BrushingMode): Single<BrushingModeSettings> =
        driver.setAndGetDeviceParameterOnce(
            byteArrayOf(
                DEVICE_PARAMETERS_CUSTOM_BRUSHING_MODE_SETTINGS,
                mode.bleIndex.toByte()
            )
        ).map { reader ->
            val modeInfoMask: Byte = reader.skip(1).readInt8()
            val strengthManagementMode = BrushingModeStrengthOption
                .fromBleValue(reader.readInt8().toInt())
            val segmentDefinitionBitmask = Bitmask(reader.readInt8())
            val lastSegmentStrategy = BrushingModeLastSegmentStrategy
                .fromBleValue(segmentDefinitionBitmask.highNibble.toInt())
            val segmentCount = segmentDefinitionBitmask.lowNibble.toInt()

            return@map BrushingModeSettings(
                modifiable = Bitmask(modeInfoMask).getBit(7),
                brushingModeId = (modeInfoMask and 0b00011111).toInt(),
                strengthOption = strengthManagementMode,
                lastSegmentStrategy = lastSegmentStrategy,
                segmentCount = segmentCount,
                segments = parseBrushingModeSegments(segmentCount, reader)
            )
        }

    private fun parseBrushingModeSegments(count: Int, reader: PayloadReader) =
        mutableListOf<BrushingModeSegment>().apply {
            for (i in 0 until min(count + 1, 8)) {
                add(
                    BrushingModeSegment(
                        reader.readInt8().toInt(),
                        reader.readInt8().toInt()
                    )
                )
            }
        }

    override fun setCustomBrushingModeSettings(brushingModeSettings: BrushingModeSettings): Completable {
        val segmentDefinition = Bitmask()
            .setHighNibble(brushingModeSettings.lastSegmentStrategy.bleValue.toByte())
            .setLowNibble(brushingModeSettings.segmentCount.toByte())
            .get()

        val writer = PayloadWriter(customModePayloadLength(brushingModeSettings.segments.size))
            .writeByte(DEVICE_PARAMETERS_CUSTOM_BRUSHING_MODE_SETTINGS)
            .writeByte(BrushingMode.UserDefined.bleIndex.toByte() and 0b00011111)
            .writeByte(brushingModeSettings.strengthOption.bleValue.toByte())
            .writeByte(segmentDefinition)

        for (segment in brushingModeSettings.segments) {
            writer.writeByte(segment.sequenceId.toByte())
            writer.writeByte(segment.strength.toByte())
        }

        return driver.setAndGetDeviceParameterOnce(writer.bytes).ignoreElement()
    }

    override fun getCustomBrushingModeSettings() = getBrushingModeSettings(UserDefined)

    @Suppress("MagicNumber")
    // https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0/edit?pli=1#gid=506526620&range=H64
    private fun customModePayloadLength(fragmentCount: Int) =
        4 + fragmentCount * 2 // 4 bytes for mode configuration, 2 bytes per segment

    override fun getSequenceSettings(
        sequence: BrushingModeSequence
    ): Single<BrushingModeSequenceSettings> = driver.setAndGetDeviceParameterOnce(
        byteArrayOf(DEVICE_PARAMETERS_BRUSHING_MODE_SEQUENCE, sequence.bleIndex.toByte())
    ).map(::parseSequenceSettings)

    override fun setSequenceSettings(
        patterns: List<BrushingModeSequencePattern>
    ): Completable = driver.setAndGetDeviceParameterOnce(
        setBrushingModeSequenceSettingsPayload(patterns)
    ).ignoreElement()

    override fun getPatternSettings(
        pattern: BrushingModePattern
    ): Single<BrushingModePatternSettings> = driver.setAndGetDeviceParameterOnce(
        byteArrayOf(DEVICE_PARAMETERS_BRUSHING_MODE_PATTERN, pattern.bleIndex.toByte())
    ).map(::parseBrushingPatternSettings)

    override fun setPatternSettings(settings: BrushingModePatternSettings): Completable =
        driver.setAndGetDeviceParameterOnce(
            setBrushingModePatternSettingsPayload(settings)
        ).ignoreElement()

    override fun getCurveSettings(curve: BrushingModeCurve): Single<BrushingModeCurveSettings> =
        driver.setAndGetDeviceParameterOnce(
            byteArrayOf(DEVICE_PARAMETERS_BRUSHING_MODE_CURVE, curve.bleIndex.toByte())
        ).map(::parseCustomBrushingModeCurveSettingsPayload)

    override fun setCurveSettings(settings: BrushingModeCurveSettings): Completable =
        driver.setAndGetDeviceParameterOnce(
            setCustomBrushingModeCurveSettingsPayload(settings)
        ).ignoreElement()
}
