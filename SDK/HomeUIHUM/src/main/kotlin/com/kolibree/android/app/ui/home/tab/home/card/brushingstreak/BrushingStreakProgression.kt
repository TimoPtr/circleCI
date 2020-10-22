/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushingstreak

import android.os.Parcelable
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.failearly.FailEarly
import kotlinx.android.parcel.Parcelize

@Parcelize
@VisibleForApp
data class BrushingStreakProgression(val currentProgression: Int) : Parcelable {
    init {
        FailEarly.failInConditionMet(
            currentProgression < 0,
            "Current Progression should not be negative"
        )
    }

    fun isStepFinished(step: Int): Boolean = step <= currentProgression
}
