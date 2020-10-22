/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.frequency

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class WeekDaysType(
    val days: List<DayType> = emptyList()
) : Parcelable {

    fun dayAt(position: Int): DayType {
        return days.getOrNull(position) ?: DayType.EmptyDay
    }
}
