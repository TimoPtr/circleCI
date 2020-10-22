/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.led.signal

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.sdk.connection.toothbrush.led.LedPattern
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class LedSignalViewState(
    val red: Int,
    val green: Int,
    val blue: Int,
    val pattern: LedPattern,
    val periodMillis: Int,
    val durationMillis: Int
) : BaseViewState {

    val isPeriodModifiable: Boolean
        get() = pattern != LedPattern.FIXED

    companion object {
        fun initial() = LedSignalViewState(
            red = 0,
            green = 0,
            blue = 0,
            pattern = LedPattern.FIXED,
            periodMillis = 250,
            durationMillis = 1000
        )
    }
}
