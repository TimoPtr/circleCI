/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer.sequence

import android.os.Parcelable
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern
import kotlinx.android.parcel.Parcelize

/** [BrushingModeSequenceSettings] pattern with duration */
@VisibleForApp
@Parcelize
data class BrushingModeSequencePattern(
    val pattern: BrushingModePattern,
    val durationSeconds: Int
) : Parcelable
