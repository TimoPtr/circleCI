/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.led.mode

import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class ModeLedViewState(
    val led1pwm: Int,
    val led2pwm: Int,
    val led3pwm: Int,
    val led4pwm: Int,
    val led5pwm: Int,
    val durationMillis: Int
) : BaseViewState {

    companion object {
        fun initial() = ModeLedViewState(
            led1pwm = 100,
            led2pwm = 0,
            led3pwm = 0,
            led4pwm = 0,
            led5pwm = 0,
            durationMillis = 1000
        )
    }
}
