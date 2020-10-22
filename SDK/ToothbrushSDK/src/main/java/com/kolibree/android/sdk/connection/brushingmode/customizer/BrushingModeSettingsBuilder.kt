/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence

/** [BrushingModeSettings] builder */
@Keep
open class BrushingModeSettingsBuilder {

    @VisibleForTesting
    internal var strengthOption: BrushingModeStrengthOption = BrushingModeStrengthOption.OneLevel

    @VisibleForTesting
    internal var lastSegmentStrategy: BrushingModeLastSegmentStrategy =
        BrushingModeLastSegmentStrategy.UseSystemDefault

    @VisibleForTesting
    internal val segments: MutableList<BrushingModeSegment> = mutableListOf()

    @VisibleForTesting
    internal var lastSegment: BrushingModeSegment? = null

    fun strengthOption(option: BrushingModeStrengthOption) = apply {
        strengthOption = option
    }

    fun lastSegmentStrategy(strategy: BrushingModeLastSegmentStrategy) = apply {
        lastSegmentStrategy = strategy
    }

    fun addSegmentWithSequence(sequence: BrushingModeSequence, strength: Int) = apply {
        addSegment(sequenceId = sequence.bleIndex, strength = strength)
    }

    fun lastSegment(sequence: BrushingModeSequence, strength: Int) = apply {
        assertStrengthWithinBounds(strength)
        assertSegmentsCountLimitNotReached()
        lastSegment = BrushingModeSegment(sequenceId = sequence.bleIndex, strength = strength)
    }

    fun build(): BrushingModeSettings {
        if (segments.size < MAX_SEGMENT_COUNT && lastSegment == null) {
            throw NoLastSegmentException()
        }

        return BrushingModeSettings(
            strengthOption = strengthOption,
            lastSegmentStrategy = lastSegmentStrategy,
            segmentCount = segments.size, // Last segment is not taken in account here
            segments = lastSegment?.let { segments.plus(it) } ?: segments,
            modifiable = true,
            brushingModeId = BrushingMode.UserDefined.bleIndex
        )
    }

    protected fun addSegment(sequenceId: Int, strength: Int) {
        assertStrengthWithinBounds(strength)
        assertSegmentsCountLimitNotReached()
        segments.add(
            BrushingModeSegment(
                sequenceId = sequenceId,
                strength = strength
            )
        )
    }

    private fun assertSegmentsCountLimitNotReached() {
        val effectiveLimit = if (lastSegment == null) MAX_SEGMENT_COUNT else MAX_SEGMENT_COUNT - 1

        if (segments.size == effectiveLimit) {
            throw TooManySegmentsException()
        }
    }

    private fun assertStrengthWithinBounds(strength: Int) {
        if (strength !in MIN_STRENGTH..MAX_STRENGTH) {
            throw StrengthOutOfBoundsException()
        }
    }

    internal companion object {

        @VisibleForTesting
        const val MAX_SEGMENT_COUNT = 8

        @VisibleForTesting
        const val MIN_STRENGTH = 1

        @VisibleForTesting
        const val MAX_STRENGTH = 10
    }
}
