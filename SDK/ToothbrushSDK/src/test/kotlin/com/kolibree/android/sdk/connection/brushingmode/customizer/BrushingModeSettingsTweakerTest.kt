/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence.Companion.CUSTOM_MODE_SEQUENCE_BLE_INDEX
import io.kotlintest.shouldThrow
import org.junit.Assert.assertEquals
import org.junit.Test

/** [BrushingModeSettingsTweaker] unit tests */
class BrushingModeSettingsTweakerTest : BaseUnitTest() {

    /*
    addSegmentWithCustomSequence
     */

    @Test
    fun `addSegmentWithCustomSequence adds expected segment to the list`() {
        val expectedStrength = 3
        val builder = BrushingModeSettingsTweaker()

        builder.addSegmentWithCustomSequence(expectedStrength)

        val segment = builder.segments[0]
        assertEquals(CUSTOM_MODE_SEQUENCE_BLE_INDEX, segment.sequenceId)
        assertEquals(expectedStrength, segment.strength)
    }

    @Test
    fun `addSegmentWithCustomSequence throws StrengthOutOfBoundsException when strength is below the lower bound`() {
        shouldThrow<StrengthOutOfBoundsException> {
            BrushingModeSettingsTweaker().addSegmentWithCustomSequence(
                strength = BrushingModeSettingsBuilder.MIN_STRENGTH - 1
            )
        }
    }

    @Test
    fun `addSegmentWithCustomSequence throws StrengthOutOfBoundsException when strength is over the upper bound`() {
        shouldThrow<StrengthOutOfBoundsException> {
            BrushingModeSettingsTweaker().addSegmentWithCustomSequence(
                strength = BrushingModeSettingsBuilder.MAX_STRENGTH + 1
            )
        }
    }

    @Test
    fun `addSegmentWithCustomSequence throws TooManySegmentsException when segments has MAX_SEGMENT_COUNT items`() {
        val builder = BrushingModeSettingsTweaker()

        for (i in 1..BrushingModeSettingsBuilder.MAX_SEGMENT_COUNT) {
            builder.segments.add(BrushingModeSegment(0, 1))
        }

        shouldThrow<TooManySegmentsException> {
            builder.addSegmentWithCustomSequence(1)
        }
    }

    @Test
    fun `addSegmentWithCustomSequence throws TooManySegmentsException with MAX_SEGMENT_COUNT - 1 segments and a last segment`() {
        val builder = BrushingModeSettingsTweaker()

        for (i in 1 until BrushingModeSettingsBuilder.MAX_SEGMENT_COUNT) {
            builder.segments.add(BrushingModeSegment(0, 1))
        }
        builder.lastSegment = BrushingModeSegment(0, 1)

        shouldThrow<TooManySegmentsException> {
            builder.addSegmentWithCustomSequence(1)
        }
    }
}
