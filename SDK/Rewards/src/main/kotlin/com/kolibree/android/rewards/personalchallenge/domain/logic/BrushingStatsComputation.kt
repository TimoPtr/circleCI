/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.domain.logic

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingStat
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingType
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.kolibree.sdkws.data.model.Brushing
import org.threeten.bp.LocalDate

internal fun lastMonthOnly(brushings: List<Brushing>): List<Brushing> {
    val endDate = TrustedClock.getNowLocalDate()
    val beginDate = endDate.minusMonths(1)
    return brushings.filter {
        isDateInPeriod(it.dateTime.toLocalDate(), beginDate, endDate)
    }
}

internal fun List<Brushing>.toBrushingsStat(checkupCalculator: CheckupCalculator): List<BrushingStat> =
    map { brushing ->
        BrushingStat(
            date = brushing.dateTime.toLocalDate(),
            coverage = calcSurfaceCoverage(checkupCalculator, brushing),
            type = BrushingType.from(brushing.game)
        )
    }

private fun calcSurfaceCoverage(checkupCalculator: CheckupCalculator, brushing: IBrushing): Int =
    checkupCalculator.calculateCheckup(
        processedData = brushing.processedData,
        timestampInSeconds = brushing.duration,
        duration = brushing.durationObject
    ).surfacePercentage

internal fun lastWeekOnly(brushings: List<BrushingStat>) =
    daysInPeriodOnly(brushings, 0, ONE_WEEK - 1)

internal fun secondWeekOnly(brushings: List<BrushingStat>) =
    daysInPeriodOnly(brushings, ONE_WEEK, ONE_WEEK - 1)

internal fun lastTwoWeeksOnly(brushings: List<BrushingStat>) =
    daysInPeriodOnly(brushings, 0, TWO_WEEKS - 1)

internal fun lastThreeDaysOnly(brushings: List<BrushingStat>) =
    daysInPeriodOnly(brushings, 0, THREE_DAYS - 1)

internal fun lastTenDaysOnly(brushings: List<BrushingStat>) =
    daysInPeriodOnly(brushings, 0, TEN_DAYS - 1)

private fun daysInPeriodOnly(
    brushings: List<BrushingStat>,
    startDay: Long,
    period: Long
): List<BrushingStat> {
    val endDate = TrustedClock.getNowLocalDate().minusDays(startDay)
    val beginDate = endDate.minusDays(period)
    return brushings.filter {
        isDateInPeriod(it.date, beginDate, endDate)
    }
}

internal fun isDateInPeriod(date: LocalDate, beginDate: LocalDate, endDate: LocalDate) =
    date.isEqual(beginDate) || date.isEqual(endDate) ||
        (date.isAfter(beginDate) && date.isBefore(endDate))

internal const val ONE_WEEK = 7L
internal const val TWO_WEEKS = 14L
internal const val THREE_DAYS = 3L
internal const val TEN_DAYS = 10L
