/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.mode

import androidx.annotation.StringRes
import com.kolibree.android.glimmer.R
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeLastSegmentStrategy
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeStrengthOption
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence.CleanMode
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence.Custom
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence.GumCare
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence.PolishingMode
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence.WhiteningMode

@StringRes
internal fun BrushingMode.getResourceId(): Int {
    return when (this) {
        BrushingMode.Regular -> R.string.brushing_mode_regular
        BrushingMode.Slow -> R.string.brushing_mode_slow
        BrushingMode.Strong -> R.string.brushing_mode_strong
        BrushingMode.Polishing -> R.string.brushing_mode_polishing
        BrushingMode.UserDefined -> R.string.brushing_mode_user_defined
    }
}

@StringRes
internal fun BrushingModeStrengthOption.getResourceId(): Int {
    return when (this) {
        BrushingModeStrengthOption.OneLevel -> R.string.brushing_mode_strength_option_one_level
        BrushingModeStrengthOption.ThreeLevels -> R.string.brushing_mode_strength_option_three_levels
        BrushingModeStrengthOption.TenLevels -> R.string.brushing_mode_strength_option_ten_levels
    }
}

@StringRes
internal fun BrushingModeLastSegmentStrategy.getResourceId(): Int {
    return when (this) {
        BrushingModeLastSegmentStrategy.UseSystemDefault ->
            R.string.brushing_mode_last_segment_strategy_use_system_default
        BrushingModeLastSegmentStrategy.EndAfterLastSegment ->
            R.string.brushing_mode_last_segment_strategy_end_after_last_segment
        BrushingModeLastSegmentStrategy.KeepRunningAfterLastSegment ->
            R.string.brushing_mode_last_segment_strategy_keep_running_after_last_segment
    }
}

@StringRes
internal fun BrushingModeSequence.getResourceId(): Int {
    return when (this) {
        CleanMode -> R.string.brushing_mode_sequence_clean_mode
        WhiteningMode -> R.string.brushing_mode_sequence_whitening_mode
        GumCare -> R.string.brushing_mode_sequence_gum_care
        PolishingMode -> R.string.brushing_mode_sequence_polishing_mode
        Custom -> R.string.brushing_mode_sequence_custom
    }
}
