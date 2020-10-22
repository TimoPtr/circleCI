/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.wake_your_brush

import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class WakeYourBrushViewState(
    val showNothingHappening: Boolean = false
) : BaseViewState {

    companion object {
        fun initial() = WakeYourBrushViewState()
    }
}
