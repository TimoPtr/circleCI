/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline

import androidx.annotation.Keep
import com.kolibree.android.commons.extensions.zeroIfNan
import com.kolibree.statsoffline.models.SanitizedDateRange
import com.kolibree.statsoffline.models.YearWeek
import java.math.RoundingMode
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.YearMonth
import org.threeten.bp.ZonedDateTime

/**
 * Returns a [List] [LocalDate] between startDate and endDate, both included
 *
 * @throws [IllegalArgumentException] if startDate isn't before endDate
 */
internal fun dateRangeBetween(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
    return SanitizedDateRange(startDate, endDate).datesInRange()
}

@Keep
fun LocalDate.toYearWeek(): YearWeek = YearWeek.from(this)

internal fun LocalDateTime.toYearMonth() = YearMonth.from(this)
internal fun LocalDate.toYearMonth() = YearMonth.from(this)
internal fun ZonedDateTime.toYearMonth() = YearMonth.from(this)

/*
If we need to convert to Int, use

ceil(sum * 100F / size).toInt()

to match old checkup implementation (which matched iOS'). I don't know about KML.
 */
/**
 * Round to a single decimal [Float] using [RoundingMode.HALF_UP] rounding mode
 *
 * Examples
 *
 * 3.36 -> 3.4
 * 3.34 -> 3.3
 */
internal fun Double.roundOneDecimalToFloat(): Float =
    toBigDecimal().setScale(1, RoundingMode.HALF_UP).toFloat()

/**
 * Round to a single decimal [Double] using [RoundingMode.HALF_UP] rounding mode
 *
 * Examples
 *
 * 3.36 -> 3.4
 * 3.34 -> 3.3
 */
internal fun Double.roundOneDecimal(): Double =
    toBigDecimal().setScale(1, RoundingMode.HALF_UP).toDouble()

/**
 * Round to a single decimal [Double] using [RoundingMode.HALF_UP] rounding mode
 *
 * Examples
 *
 * 3.36 -> 3.4
 * 3.34 -> 3.3
 */
internal fun Long.roundOneDecimal(): Long =
    toBigDecimal().setScale(1, RoundingMode.HALF_UP).toLong()

internal fun Double.isNonZero(): Boolean = this != 0.0

internal fun List<Double>.calculateStatsAverage(excludeZero: Boolean = true): Double =
    asSequence()
        .filter { if (excludeZero) it.isNonZero() else true }
        .average()
        .zeroIfNan()
        .roundOneDecimal()
