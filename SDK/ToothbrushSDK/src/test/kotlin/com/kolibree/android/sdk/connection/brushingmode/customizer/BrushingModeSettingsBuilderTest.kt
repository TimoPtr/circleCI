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
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeSettingsBuilder.Companion.MAX_SEGMENT_COUNT
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeSettingsBuilder.Companion.MAX_STRENGTH
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeSettingsBuilder.Companion.MIN_STRENGTH
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence
import io.kotlintest.shouldThrow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** [BrushingModeSettingsBuilder] unit tests */
class BrushingModeSettingsBuilderTest : BaseUnitTest() {

    /*
    strengthOption (property)
     */

    @Test
    fun `default value of strengthOption is OneLevel`() {
        assertEquals(
            BrushingModeStrengthOption.OneLevel,
            BrushingModeSettingsBuilder().strengthOption
        )
    }

    /*
    lastSegmentStrategy (property)
     */

    @Test
    fun `default value of lastSegmentStrategy is UseSystemDefault`() {
        assertEquals(
            BrushingModeLastSegmentStrategy.UseSystemDefault,
            BrushingModeSettingsBuilder().lastSegmentStrategy
        )
    }

    /*
    segments
     */

    @Test
    fun `default value of segments is empty list`() {
        assertTrue(BrushingModeSettingsBuilder().segments.isEmpty())
    }

    /*
    lastSegment
     */

    @Test
    fun `default value of lastSegment is null`() {
        assertNull(BrushingModeSettingsBuilder().lastSegment)
    }

    /*
    strengthOption (method)
     */

    @Test
    fun `strengthOption sets property to expected value`() {
        val expectedOption = BrushingModeStrengthOption.ThreeLevels
        val builder = BrushingModeSettingsBuilder()

        builder.strengthOption(expectedOption)

        assertEquals(expectedOption, builder.strengthOption)
    }

    /*
    lastSegmentStrategy (method)
     */

    @Test
    fun `lastSegmentStrategy sets property to expected value`() {
        val expectedStrategy = BrushingModeLastSegmentStrategy.KeepRunningAfterLastSegment
        val builder = BrushingModeSettingsBuilder()

        builder.lastSegmentStrategy(expectedStrategy)

        assertEquals(expectedStrategy, builder.lastSegmentStrategy)
    }

    /*
    addSegmentWithSequence
     */

    @Test
    fun `addSegmentWithSequence adds expected segment to the list`() {
        val expectedSequence = BrushingModeSequence.GumCare
        val expectedStrength = 3
        val builder = BrushingModeSettingsBuilder()

        builder.addSegmentWithSequence(expectedSequence, expectedStrength)

        val segment = builder.segments[0]
        assertEquals(expectedSequence.bleIndex, segment.sequenceId)
        assertEquals(expectedStrength, segment.strength)
    }

    @Test
    fun `addSegmentWithSequence throws StrengthOutOfBoundsException when strength is below the lower bound`() {
        shouldThrow<StrengthOutOfBoundsException> {
            BrushingModeSettingsBuilder().addSegmentWithSequence(
                sequence = BrushingModeSequence.GumCare,
                strength = MIN_STRENGTH - 1
            )
        }
    }

    @Test
    fun `addSegmentWithSequence throws StrengthOutOfBoundsException when strength is over the upper bound`() {
        shouldThrow<StrengthOutOfBoundsException> {
            BrushingModeSettingsBuilder().addSegmentWithSequence(
                sequence = BrushingModeSequence.GumCare,
                strength = MAX_STRENGTH + 1
            )
        }
    }

    @Test
    fun `addSegmentWithSequence throws TooManySegmentsException when segments has MAX_SEGMENT_COUNT items`() {
        val builder = BrushingModeSettingsBuilder()

        for (i in 1..MAX_SEGMENT_COUNT) {
            builder.segments.add(BrushingModeSegment(0, 1))
        }

        shouldThrow<TooManySegmentsException> {
            builder.addSegmentWithSequence(BrushingModeSequence.GumCare, 1)
        }
    }

    @Test
    fun `addSegmentWithSequence throws TooManySegmentsException with MAX_SEGMENT_COUNT - 1 segments and a last segment`() {
        val builder = BrushingModeSettingsBuilder()

        for (i in 1 until MAX_SEGMENT_COUNT) {
            builder.segments.add(BrushingModeSegment(0, 1))
        }
        builder.lastSegment = BrushingModeSegment(0, 1)

        shouldThrow<TooManySegmentsException> {
            builder.addSegmentWithSequence(BrushingModeSequence.GumCare, 1)
        }
    }

    /*
    lastSegment
     */

    @Test
    fun `lastSegment sets property with expected values`() {
        val expectedSequence = BrushingModeSequence.GumCare
        val expectedStrength = 3
        val builder = BrushingModeSettingsBuilder()

        builder.lastSegment(expectedSequence, expectedStrength)

        val segment = builder.lastSegment!!
        assertEquals(expectedSequence.bleIndex, segment.sequenceId)
        assertEquals(expectedStrength, segment.strength)
    }

    @Test
    fun `lastSegment throws StrengthOutOfBoundsException when strength is below the lower bound`() {
        shouldThrow<StrengthOutOfBoundsException> {
            BrushingModeSettingsBuilder().lastSegment(
                sequence = BrushingModeSequence.GumCare,
                strength = MIN_STRENGTH - 1
            )
        }
    }

    @Test
    fun `lastSegment throws StrengthOutOfBoundsException when strength is over the upper bound`() {
        shouldThrow<StrengthOutOfBoundsException> {
            BrushingModeSettingsBuilder().lastSegment(
                sequence = BrushingModeSequence.GumCare,
                strength = MAX_STRENGTH + 1
            )
        }
    }

    @Test
    fun `lastSegment throws TooManySegmentsException when segments has MAX_SEGMENT_COUNT items`() {
        val builder = BrushingModeSettingsBuilder()

        for (i in 1..MAX_SEGMENT_COUNT) {
            builder.segments.add(BrushingModeSegment(0, 1))
        }

        shouldThrow<TooManySegmentsException> {
            builder.lastSegment(BrushingModeSequence.GumCare, 1)
        }
    }

    /*
    build
     */

    @Test
    fun `build returns expected result when there is no last segment`() {
        val expectedSequence = BrushingModeSequence.GumCare
        val expectedStrength = 5
        val expectedStrategy = BrushingModeLastSegmentStrategy.KeepRunningAfterLastSegment
        val expectedOption = BrushingModeStrengthOption.TenLevels

        val result = BrushingModeSettingsBuilder()
            .addSegmentWithSequence(expectedSequence, expectedStrength)
            .addSegmentWithSequence(BrushingModeSequence.GumCare, 3)
            .addSegmentWithSequence(BrushingModeSequence.GumCare, 3)
            .addSegmentWithSequence(BrushingModeSequence.GumCare, 3)
            .addSegmentWithSequence(BrushingModeSequence.GumCare, 3)
            .addSegmentWithSequence(BrushingModeSequence.GumCare, 3)
            .addSegmentWithSequence(BrushingModeSequence.GumCare, 3)
            .addSegmentWithSequence(BrushingModeSequence.GumCare, 3)
            .lastSegmentStrategy(expectedStrategy)
            .strengthOption(expectedOption)
            .build()

        assertEquals(expectedOption, result.strengthOption)
        assertEquals(expectedStrategy, result.lastSegmentStrategy)
        assertEquals(8, result.segmentCount)
        assertEquals(expectedSequence.bleIndex, result.segments[0].sequenceId)
        assertEquals(expectedStrength, result.segments[0].strength)
        assertTrue(result.modifiable)
        assertEquals(BrushingMode.UserDefined.bleIndex, result.brushingModeId)
    }

    @Test
    fun `build returns expected result when there is a last segment`() {
        val expectedSequence = BrushingModeSequence.PolishingMode
        val expectedStrength = 3
        val expectedSequenceLast = BrushingModeSequence.GumCare
        val expectedStrengthLast = 9
        val expectedStrategy = BrushingModeLastSegmentStrategy.EndAfterLastSegment
        val expectedOption = BrushingModeStrengthOption.ThreeLevels

        val result = BrushingModeSettingsBuilder()
            .addSegmentWithSequence(expectedSequence, expectedStrength)
            .lastSegmentStrategy(expectedStrategy)
            .strengthOption(expectedOption)
            .lastSegment(expectedSequenceLast, expectedStrengthLast)
            .build()

        assertEquals(expectedOption, result.strengthOption)
        assertEquals(expectedStrategy, result.lastSegmentStrategy)
        assertEquals(1, result.segmentCount)
        assertEquals(2, result.segments.size)
        assertEquals(expectedSequence.bleIndex, result.segments[0].sequenceId)
        assertEquals(expectedStrength, result.segments[0].strength)
        assertEquals(expectedSequenceLast.bleIndex, result.segments[1].sequenceId)
        assertEquals(expectedStrengthLast, result.segments[1].strength)
        assertTrue(result.modifiable)
        assertEquals(BrushingMode.UserDefined.bleIndex, result.brushingModeId)
    }

    @Test
    fun `build throws NoLastSegmentException when segment limit is not reached and there is no last segment`() {
        shouldThrow<NoLastSegmentException> {
            BrushingModeSettingsBuilder().build()
        }
    }

    /*
    Constants
     */

    @Test
    fun `value of MAX_SEGMENT_COUNT is 8`() {
        assertEquals(8, MAX_SEGMENT_COUNT)
    }

    @Test
    fun `value of MIN_STRENGTH is 1`() {
        assertEquals(1, MIN_STRENGTH)
    }

    @Test
    fun `value of MAX_STRENGTH is 10`() {
        assertEquals(10, MAX_STRENGTH)
    }
}
