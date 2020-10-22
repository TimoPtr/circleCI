/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer.sequence

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence.Companion.CUSTOM_MODE_SEQUENCE_BLE_INDEX
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic.DEVICE_PARAMETERS_BRUSHING_MODE_SEQUENCE
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** BrushingSequenceUtils unit tests */
class BrushingSequenceUtilsTest : BaseUnitTest() {

    /*
    parseSequenceSettings
     */

    @Test
    fun `parseSequenceSettings returns expected data`() {
        val payloadReader = PayloadReader(byteArrayOf(
            DEVICE_PARAMETERS_BRUSHING_MODE_SEQUENCE,
            0b10000100.toByte(),
            0x02,
            0x00, 0x0A,
            0x01, 0x0F
        ))

        val settings = parseSequenceSettings(payloadReader)

        assertTrue(settings.modifiable)
        assertEquals(CUSTOM_MODE_SEQUENCE_BLE_INDEX, settings.sequenceId)
        assertEquals(2, settings.patternCount)
        assertEquals(2, settings.patterns.size)
        assertEquals(BrushingModePattern.CleanBrushing, settings.patterns[0].pattern)
        assertEquals(10, settings.patterns[0].durationSeconds)
        assertEquals(BrushingModePattern.WhiteningBrushing, settings.patterns[1].pattern)
        assertEquals(15, settings.patterns[1].durationSeconds)
    }

    /*
    setBrushingModeSequenceSettingsPayload
     */

    @Test
    fun `setBrushingModeSequenceSettingsPayload returns expected data`() {
        val expectedPattern1 = BrushingModePattern.PhaseChange
        val expectedPatternDuration1 = 12
        val expectedPattern2 = BrushingModePattern.OverPressure
        val expectedPatternDuration2 = 14

        val payload = setBrushingModeSequenceSettingsPayload(listOf(
            BrushingModeSequencePattern(expectedPattern1, expectedPatternDuration1),
            BrushingModeSequencePattern(expectedPattern2, expectedPatternDuration2)
        ))

        assertArrayEquals(
            byteArrayOf(
                DEVICE_PARAMETERS_BRUSHING_MODE_SEQUENCE,
                0b10000100.toByte(),
                0x02,
                expectedPattern1.bleIndex.toByte(),
                expectedPatternDuration1.toByte(),
                expectedPattern2.bleIndex.toByte(),
                expectedPatternDuration2.toByte()
            ),
            payload
        )
    }
}
