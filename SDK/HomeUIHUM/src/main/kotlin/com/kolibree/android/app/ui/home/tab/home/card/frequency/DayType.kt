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
import org.threeten.bp.LocalDate

internal sealed class DayType(val date: LocalDate? = null) : Parcelable {

    @Parcelize
    object EmptyDay : DayType()

    @Parcelize
    object NotAvailableDay : DayType()

    @Parcelize
    object NoBrushingDay : DayType()

    @Parcelize
    data class SingleBrushingDay(val day: LocalDate) : DayType(day)

    @Parcelize
    object FutureDay : DayType()

    @Parcelize
    internal data class PerfectDay(
        val day: LocalDate,
        val brushings: Int,
        val isPerfectDayBefore: Boolean = false,
        val isPerfectDayAfter: Boolean = false
    ) : DayType(day)
}
