/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.logic.models

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class QuizAnswer(
    @StringRes val message: Int,
    @StringRes val hint: Int,
    val selected: Boolean = false,
    val isFirst: Boolean = false,
    val isLast: Boolean = false
) : Parcelable {

    @IgnoredOnParcel
    val isInMiddle = !isFirst && !isLast
}
