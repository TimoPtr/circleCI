/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.calendar.logic.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate

@Keep
@Parcelize
data class BrushingStreak(
    val start: LocalDate,
    val end: LocalDate
) : Parcelable
