/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.frequency

import android.content.Context

internal object DayRendererFactory {

    fun create(context: Context, type: DayType?, dayIndex: Int): DayRenderer = when (type) {
        is DayType.NotAvailableDay -> NotAvailableDayRenderer(context)
        is DayType.NoBrushingDay -> NoBrushingDayRenderer(context)
        is DayType.FutureDay -> FutureDayRenderer(context)
        is DayType.SingleBrushingDay -> SingleBrushingDayRenderer(context)
        is DayType.PerfectDay -> PerfectDayRenderer(
            context = context,
            type = type,
            dayOfWeekIndex = dayIndex
        )
        else -> EmptyDayRenderer
    }
}
