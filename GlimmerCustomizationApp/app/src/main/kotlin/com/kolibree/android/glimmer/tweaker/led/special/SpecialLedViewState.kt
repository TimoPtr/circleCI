/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.led.special

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.sdk.connection.toothbrush.led.SpecialLed
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class SpecialLedViewState(
    val led: SpecialLed,
    val pwm: Int
) : BaseViewState {

    companion object {
        fun initial() = SpecialLedViewState(
            led = SpecialLed.WarningLed,
            pwm = 0
        )
    }
}
