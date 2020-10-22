/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog

import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.SHORT_MONTH_FORMATTER
import org.threeten.bp.Month
import org.threeten.bp.format.DateTimeFormatter

internal fun buildYears(
    earliestYear: Int,
    @VisibleForTesting latestYear: Int
): Array<String> = (earliestYear..latestYear).map(Int::toString).toTypedArray()

internal fun buildMonths(
    @VisibleForTesting formatter: DateTimeFormatter = SHORT_MONTH_FORMATTER
): Array<String> {
    return Month.values().map { month ->
        formatter.format(month)
    }.toTypedArray()
}
