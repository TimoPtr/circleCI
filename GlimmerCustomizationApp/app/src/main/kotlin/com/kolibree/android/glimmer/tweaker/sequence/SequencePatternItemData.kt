/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.sequence

import android.os.Parcelable
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern.CleanBrushing
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SequencePatternItemData(
    val pattern: BrushingModePattern = CleanBrushing,
    val durationSeconds: Int = 0
) : Parcelable
