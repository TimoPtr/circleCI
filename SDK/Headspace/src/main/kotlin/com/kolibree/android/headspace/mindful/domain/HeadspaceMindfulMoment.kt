/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.domain

import android.os.Parcelable
import com.kolibree.android.annotation.VisibleForApp
import kotlinx.android.parcel.Parcelize

@Parcelize
@VisibleForApp
data class HeadspaceMindfulMoment(
    val quote: String,
    val animationJson: String,
    val backgroundColorHexString: String,
    val textColorHexString: String
) : Parcelable
