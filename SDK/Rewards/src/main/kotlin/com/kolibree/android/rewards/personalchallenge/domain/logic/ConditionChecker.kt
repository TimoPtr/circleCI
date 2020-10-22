/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.domain.logic

import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingStat
import javax.inject.Inject
import org.threeten.bp.LocalDate

@Suppress("TooManyFunctions")
internal class ConditionChecker @Inject constructor() {

    fun atLeastBrushingPerDay(
        perDay: Int,
        days: Int,
        brushings: List<BrushingStat>
    ): Boolean {
        val brushingPerDays = mutableMapOf<LocalDate, Int>()
        for (brushing in brushings) {
            val count = brushingPerDays[brushing.date] ?: 0
            brushingPerDays[brushing.date] = (count + 1)
        }
        val atLeastBrushingPerDay = brushingPerDays.count {
            it.value >= perDay
        }
        return atLeastBrushingPerDay >= days
    }

    fun lessThanBrushingPerDay(
        perDay: Int,
        days: Int,
        brushings: List<BrushingStat>
    ): Boolean {
        val brushingPerDays = mutableMapOf<LocalDate, Int>()
        for (brushing in brushings) {
            val count = brushingPerDays[brushing.date] ?: 0
            brushingPerDays[brushing.date] = (count + 1)
        }

        return brushingPerDays.count { it.value >= perDay } < days
    }

    fun maxBrushing(max: Int, brushings: List<BrushingStat>) = brushings.size <= max

    fun minBrushing(min: Int, brushings: List<BrushingStat>) = brushings.size >= min

    fun noBrushing(brushings: Int) = brushings == 0

    fun noBrushing(brushings: List<BrushingStat>, predicate: (BrushingStat) -> Boolean): Boolean =
        brushings.none(predicate)

    fun firstBrushing(firstBrushingDate: LocalDate?, brushings: List<BrushingStat>): Boolean {
        val brushingDates = brushings.map { it.date }.toSet()
        return firstBrushingDate in brushingDates
    }

    fun frequencyIncreasedByFiftyPercent(
        before: List<BrushingStat>,
        now: List<BrushingStat>
    ): Boolean {
        val frequencyBefore = before.size
        val frequencyNow = now.size
        return frequencyNow >= INCREASE_BY_150_PERCENT * frequencyBefore &&
            frequencyNow > 0 &&
            !frequencyIncreasedByHundredPercent(before, now)
    }

    fun frequencyIncreasedByHundredPercent(
        before: List<BrushingStat>,
        now: List<BrushingStat>
    ): Boolean {
        val frequencyBefore = before.size
        val frequencyNow = now.size
        return frequencyNow >= frequencyBefore + frequencyBefore &&
            frequencyNow > 0
    }

    fun aboveGoodCoverage(minBrushing: Int, days: List<BrushingStat>): Boolean =
        days.filter {
            it.coverage >= GOOD_COVERAGE
        }.size >= minBrushing

    fun lessThanAverageCoverage(coverage: Int, days: List<BrushingStat>): Boolean =
        days.map(BrushingStat::coverage).average() < coverage
}

private const val GOOD_COVERAGE = 80
private const val INCREASE_BY_150_PERCENT = 1.5f
