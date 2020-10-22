/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.databinding.playground.lottie

import androidx.annotation.RawRes
import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class LottiePlaygroundViewState(
    @RawRes val lottieAnimation: Int? = null,
    val isDelayOver: Boolean = false
) : BaseViewState {
    companion object {
        fun initial() = LottiePlaygroundViewState()
    }
}
