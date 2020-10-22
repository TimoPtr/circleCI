/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.sequence

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence.CleanMode
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequencePattern
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequenceSettings
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class SequenceViewState(
    val selectedSequence: BrushingModeSequence,
    val modifiable: Boolean,
    val sequencePattern1: SequencePatternItemData,
    val sequencePattern2: SequencePatternItemData,
    val sequencePattern3: SequencePatternItemData,
    val sequencePattern4: SequencePatternItemData,
    val sequencePattern5: SequencePatternItemData,
    val sequencePattern6: SequencePatternItemData,
    val sequencePattern7: SequencePatternItemData,
    val sequencePattern8: SequencePatternItemData,
    val enabledPatternCount: Int
) : BaseViewState {

    @IgnoredOnParcel
    val removeButtonEnabled: Boolean = enabledPatternCount > 1

    @IgnoredOnParcel
    val addButtonEnabled: Boolean = enabledPatternCount < MAX_SEQUENCE_PATTERN

    companion object {
        fun initial() = SequenceViewState(
            selectedSequence = CleanMode,
            modifiable = false,
            sequencePattern1 = SequencePatternItemData(),
            sequencePattern2 = SequencePatternItemData(),
            sequencePattern3 = SequencePatternItemData(),
            sequencePattern4 = SequencePatternItemData(),
            sequencePattern5 = SequencePatternItemData(),
            sequencePattern6 = SequencePatternItemData(),
            sequencePattern7 = SequencePatternItemData(),
            sequencePattern8 = SequencePatternItemData(),
            enabledPatternCount = 0
        )

        @Suppress("MagicNumber")
        fun withSettings(settings: BrushingModeSequenceSettings) =
            SequenceViewState(
                selectedSequence = settings.sequence(),
                modifiable = settings.modifiable,
                sequencePattern1 = settings.itemDataAtPosition(0),
                sequencePattern2 = settings.itemDataAtPosition(1),
                sequencePattern3 = settings.itemDataAtPosition(2),
                sequencePattern4 = settings.itemDataAtPosition(3),
                sequencePattern5 = settings.itemDataAtPosition(4),
                sequencePattern6 = settings.itemDataAtPosition(5),
                sequencePattern7 = settings.itemDataAtPosition(6),
                sequencePattern8 = settings.itemDataAtPosition(7),
                enabledPatternCount = settings.patternCount
            )

        private const val MAX_SEQUENCE_PATTERN = 8
    }
}

private fun BrushingModeSequenceSettings.itemDataAtPosition(position: Int) =
    patterns.getOrNull(position)
        ?.toItemData()
        ?: SequencePatternItemData()

private fun BrushingModeSequencePattern.toItemData() = SequencePatternItemData(
    pattern = pattern,
    durationSeconds = durationSeconds
)
