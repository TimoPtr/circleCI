/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.completeprofile

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class CompleteProfileBubbleViewState(
    val profileBubbleVisible: Boolean = false,
    @IntRange(from = 0, to = ONE_HUNDRED_PERCENT) val profileCompletionPercentage: Int = 0
) : BaseViewState {

    @IgnoredOnParcel
    @FloatRange(from = 0.0, to = 1.0)
    val profileBubbleProgress: Float = profileCompletionPercentage.toFloat() / ONE_HUNDRED_PERCENT

    companion object {

        fun initial() = CompleteProfileBubbleViewState()

        const val ONE_HUNDRED_PERCENT = 100L
    }
}
