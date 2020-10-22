/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.timer

import com.kolibree.android.app.base.BaseViewState
import java.util.concurrent.TimeUnit
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class GuidedBrushingTimerViewState(val elapsedMillis: Long = 0, val lastPassedMillis: Long = 0) :
    BaseViewState {

    fun secondsElapsed() = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis)

    fun withTimerStarted(millis: Long): GuidedBrushingTimerViewState {
        return copy(lastPassedMillis = millis)
    }

    fun withTimeUpdate(millis: Long): GuidedBrushingTimerViewState {
        return copy(
            elapsedMillis = elapsedMillis + millis - lastPassedMillis,
            lastPassedMillis = millis
        )
    }

    fun withRestart(): GuidedBrushingTimerViewState {
        return copy(elapsedMillis = 0)
    }

    internal companion object {
        fun initial() = GuidedBrushingTimerViewState()
    }
}
