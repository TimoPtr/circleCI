/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.mode

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeLastSegmentStrategy
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeLastSegmentStrategy.UseSystemDefault
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeSegment
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeSettings
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeStrengthOption
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeStrengthOption.OneLevel
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class ModeViewState(
    val selectedMode: BrushingMode,
    val modifiable: Boolean,
    val strengthOption: BrushingModeStrengthOption,
    val lastSegmentStrategy: BrushingModeLastSegmentStrategy,
    val sequenceSegment1: BrushingModeSegment,
    val sequenceSegment2: BrushingModeSegment,
    val sequenceSegment3: BrushingModeSegment,
    val sequenceSegment4: BrushingModeSegment,
    val sequenceSegment5: BrushingModeSegment,
    val sequenceSegment6: BrushingModeSegment,
    val sequenceSegment7: BrushingModeSegment,
    val sequenceSegment8: BrushingModeSegment,
    val lastSegment: BrushingModeSegment,
    val enabledSegmentCount: Int
) : BaseViewState {

    @IgnoredOnParcel
    val removeButtonEnabled: Boolean = enabledSegmentCount > 0

    @IgnoredOnParcel
    val addButtonEnabled: Boolean = enabledSegmentCount < MAX_SEGMENTS

    companion object {

        fun initial() = ModeViewState(
            selectedMode = BrushingMode.defaultMode(),
            modifiable = false,
            strengthOption = OneLevel,
            lastSegmentStrategy = UseSystemDefault,
            sequenceSegment1 = BrushingModeSegment.default(),
            sequenceSegment2 = BrushingModeSegment.default(),
            sequenceSegment3 = BrushingModeSegment.default(),
            sequenceSegment4 = BrushingModeSegment.default(),
            sequenceSegment5 = BrushingModeSegment.default(),
            sequenceSegment6 = BrushingModeSegment.default(),
            sequenceSegment7 = BrushingModeSegment.default(),
            sequenceSegment8 = BrushingModeSegment.default(),
            lastSegment = BrushingModeSegment.default(),
            enabledSegmentCount = 0
        )

        @Suppress("MagicNumber")
        fun withSettings(settings: BrushingModeSettings) =
            ModeViewState(
                selectedMode = settings.mode(),
                modifiable = settings.modifiable,
                strengthOption = settings.strengthOption,
                lastSegmentStrategy = settings.lastSegmentStrategy,
                sequenceSegment1 = settings.itemDataAtPosition(0),
                sequenceSegment2 = settings.itemDataAtPosition(1),
                sequenceSegment3 = settings.itemDataAtPosition(2),
                sequenceSegment4 = settings.itemDataAtPosition(3),
                sequenceSegment5 = settings.itemDataAtPosition(4),
                sequenceSegment6 = settings.itemDataAtPosition(5),
                sequenceSegment7 = settings.itemDataAtPosition(6),
                sequenceSegment8 = settings.itemDataAtPosition(7),
                lastSegment = settings.lastSegment(),
                enabledSegmentCount = settings.segmentCount
            )

        const val MAX_SEGMENTS = 8
    }
}

private fun BrushingModeSettings.itemDataAtPosition(position: Int) =
    segments.getOrNull(position)
        ?: BrushingModeSegment.default()

private fun BrushingModeSettings.lastSegment() =
    when {
        segmentCount != segments.size -> segments[segmentCount]
        segmentCount == ModeViewState.MAX_SEGMENTS -> segments[ModeViewState.MAX_SEGMENTS - 1]
        else -> BrushingModeSegment.default()
    }
